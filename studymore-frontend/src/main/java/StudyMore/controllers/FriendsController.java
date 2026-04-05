package StudyMore.controllers;

import StudyMore.ApiClient;
import StudyMore.Main;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendsController {
    @FXML private TextField searchField;
    @FXML private Label onlineCountLabel;
    @FXML private VBox friendListContainer;
    @FXML private VBox leaderboardContainer;
    @FXML private Label goalLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        loadFriends(null);
        loadGroupLeaderboard();
    }

    @FXML
    private void onSearch() {
        loadFriends(searchField.getText().trim());
    }

    private void loadFriends(String keyword) {
        friendListContainer.getChildren().clear();

        String url = keyword == null || keyword.isEmpty()
                ? "/friends/" + Main.user.getUserId()
                : "/friends/search?keyword=" + keyword + "&requestingUserId=" + Main.user.getUserId();
        String response = ApiClient.get(url);
        JSONArray arr;
        try {
            arr = new JSONArray(response);
        } catch (Exception e) {
            System.err.println("Friends parse error: " + response);
            arr = new JSONArray();
        }

        for (int i = 0; i < arr.length(); i++) {
            JSONObject u      = arr.getJSONObject(i);
            String username   = u.optString("username", "?");
            friendListContainer.getChildren().add(buildFriendRow(username, "OFFLINE", false));
        }

        if (arr.isEmpty()) {
            Label l = new Label("NO FRIENDS YET");
            l.setStyle("-fx-text-fill: #333; -fx-font-size: 11px; -fx-font-family: monospace;");
            l.setPadding(new Insets(20, 16, 0, 16));
            friendListContainer.getChildren().add(l);
        }

        onlineCountLabel.setText("0 ONLINE");
    }

    private String getFriendStatus(long friendId) {
        
        try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(
                "SELECT sg.title FROM study_group_members sgm JOIN study_groups sg ON sgm.group_id = sg.id " +
                "WHERE sgm.user_id = ? AND sg.is_active = 1 LIMIT 1")) {
            s.setLong(1, friendId);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) return "IN A GROUP";
            }
        } catch (SQLException ignored) {}

        if (isRecentlyActive(friendId)) return "STUDYING";

        try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(
                "SELECT start_time FROM sessions WHERE user_id = ? ORDER BY start_time DESC LIMIT 1")) {
            s.setLong(1, friendId);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next() && rs.getString("start_time") != null) return "LAST SEEN RECENTLY";
            }
        } catch (SQLException ignored) {}

        return "OFFLINE";
    }

    private boolean isRecentlyActive(long userId) {
        try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(
                "SELECT 1 FROM sessions WHERE user_id = ? AND DATE(start_time) = DATE('now','localtime') LIMIT 1")) {
            s.setLong(1, userId);
            try (ResultSet rs = s.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { return false; }
    }
    private void loadGroupLeaderboard() {
        leaderboardContainer.getChildren().clear();

        JSONArray groups;
        try {
            groups = new JSONArray(ApiClient.get("/groups/user/" + Main.user.getUserId()));
        } catch (Exception e) {
            System.err.println("Groups parse error: " + e.getMessage());
            groups = new JSONArray();
        }

        if (groups.isEmpty()) {
            goalLabel.setText("GOAL: 50H");
            Label l = new Label("JOIN OR CREATE A GROUP TO SEE THE LEADERBOARD");
            l.setStyle("-fx-text-fill: #333; -fx-font-size: 11px; -fx-font-family: monospace;");
            l.setPadding(new Insets(32, 24, 0, 24));
            leaderboardContainer.getChildren().add(l);
            return;
        }

        JSONObject group = groups.getJSONObject(0);
        long groupId   = group.getLong("groupId");
        int  studyGoal = group.optInt("studyGoal", 50);
        goalLabel.setText("GOAL: " + studyGoal + "H");

        JSONArray members;
        try {
            members = new JSONArray(ApiClient.get("/groups/" + groupId + "/leaderboard"));
        } catch (Exception e) {
            System.err.println("Leaderboard parse error: " + e.getMessage());
            members = new JSONArray();
        }

        if (members.isEmpty()) {
            leaderboardContainer.getChildren().add(new Label("NO MEMBERS YET"));
            return;
        }

        long maxSec = 1;
        for (int i = 0; i < members.length(); i++) {
            long t = members.getJSONObject(i).optLong("totalStudyTime", 0);
            if (t > maxSec) maxSec = t;
        }

        for (int i = 0; i < members.length(); i++) {
            JSONObject u = members.getJSONObject(i);
            long   uid   = u.optLong("userId", -1);
            String uname = u.optString("username", "?");
            long   sec   = u.optLong("totalStudyTime", 0);
            int    hours = (int)(sec / 3600);
            double prog  = (double) sec / maxSec;
            boolean isMe = uid == Main.user.getUserId();
            leaderboardContainer.getChildren().add(
                buildLeaderboardRow(i + 1, isMe ? "YOU" : uname.toUpperCase(), hours, prog, isMe));
        }
    }
    @FXML
    private void onCreateGroup() {
        TextInputDialog d1 = new TextInputDialog();
        d1.setTitle("Create Group"); d1.setHeaderText(null); d1.setContentText("Group name:");
        d1.showAndWait().ifPresent(title -> {
            if (title.trim().isEmpty()) return;
            TextInputDialog d2 = new TextInputDialog("50");
            d2.setTitle("Study Goal"); d2.setHeaderText(null); d2.setContentText("Goal (hours):");
            d2.showAndWait().ifPresent(goalStr -> {
                int goal = 50;
                try { goal = Integer.parseInt(goalStr.trim()); } catch (NumberFormatException ignored) {}
                String body = "{\"title\":\"" + title.trim() + "\","
                            + "\"studyGoal\":\"" + goalStr.trim() + "\","
                            + "\"hostId\":" + Main.user.getUserId() + ","
                            + "\"maxMembers\":10}";
                ApiClient.post("/groups", body);
                setStatus("Group created!");
                loadGroupLeaderboard();
            });
        });
    }

    @FXML
    private void onViewRequests() {
        JSONArray arr = new JSONArray(
            ApiClient.get("/friends/requests/pending?userId=" + Main.user.getUserId()));

        if (arr.isEmpty()) { setStatus("No pending requests."); return; }

        List<String> names = new ArrayList<>();
        List<Long>   reqIds = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject req    = arr.getJSONObject(i);
            JSONObject sender = req.optJSONObject("sender");
            names.add(sender != null ? sender.optString("username", "?") : "?");
            reqIds.add(req.getLong("requestId"));
        }

        ChoiceDialog<String> d = new ChoiceDialog<>(names.get(0), names);
        d.setTitle("Accept Request"); d.setHeaderText(null); d.setContentText("Accept from:");
        d.showAndWait().ifPresent(sel -> {
            int idx = names.indexOf(sel);
            String res = ApiClient.put("/friends/requests/" + reqIds.get(idx) 
                                    + "/accept?receiverId=" + Main.user.getUserId());
            setStatus("Now friends with " + sel + "!");
            loadFriends(null);
        });
    }

    private void acceptRequest(long requestId, String senderName) {
        try {
            long senderId;
            try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(
                    "SELECT sender_id FROM friend_requests WHERE id = ?")) {
                s.setLong(1, requestId);
                try (ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) return;
                    senderId = rs.getLong("sender_id");
                }
            }
            try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(
                    "UPDATE friend_requests SET status='ACCEPTED' WHERE id=?")) {
                s.setLong(1, requestId); s.executeUpdate();
            }
            try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(
                    "INSERT INTO friends (user_id, friend_id) VALUES (?,?) ON CONFLICT DO NOTHING")) {
                s.setLong(1, Main.user.getUserId()); s.setLong(2, senderId); s.executeUpdate();
                s.setLong(1, senderId); s.setLong(2, Main.user.getUserId()); s.executeUpdate();
            }
            setStatus("Now friends with " + senderName + "!");
            AchievementsController.updateProgress(Main.user.getUserId(), "SOCIAL", 1);
            loadFriends(null);
        } catch (SQLException e) { setStatus("Error: " + e.getMessage()); }
    }
    @FXML
    private void onAddFriend() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Add Friend"); d.setHeaderText(null); d.setContentText("Search username:");
        d.showAndWait().ifPresent(kw -> {
            if (kw.trim().isEmpty()) return;
            JSONArray results = new JSONArray(
                ApiClient.get("/friends/search?keyword=" + kw.trim() 
                            + "&requestingUserId=" + Main.user.getUserId()));

            List<String> choices = new ArrayList<>();
            List<Long>   ids     = new ArrayList<>();
            for (int i = 0; i < results.length(); i++) {
                JSONObject u = results.getJSONObject(i);
                choices.add(u.getString("username"));
                ids.add(u.getLong("userId"));
            }
            if (choices.isEmpty()) { setStatus("No users found."); return; }

            ChoiceDialog<String> cd = new ChoiceDialog<>(choices.get(0), choices);
            cd.setTitle("Send Request"); cd.setHeaderText(null); cd.setContentText("Select:");
            cd.showAndWait().ifPresent(sel -> sendRequest(ids.get(choices.indexOf(sel)), sel));
        });
    }

    private void sendRequest(long receiverId, String name) {
        String body = "{\"senderId\":" + Main.user.getUserId() + ",\"receiverId\":" + receiverId + "}";
        String response = ApiClient.post("/friends/requests", body);
        JSONObject res = new JSONObject(response);
        if (res.has("error")) setStatus("Error: " + res.getString("error"));
        else setStatus("Request sent to " + name + "!");
    }
    private HBox buildFriendRow(String username, String statusText, boolean online) {
        Circle dot = new Circle(4);
        dot.setFill(online ? Color.web("#4caf50") : Color.web("#2a2a2a"));
        Label avatar = new Label("⬤");
        avatar.setPrefSize(40, 40); avatar.setMinSize(40, 40);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("-fx-background-color: #161616; -fx-border-color: #222; -fx-border-width: 1; " +
                        "-fx-background-radius: 20; -fx-border-radius: 20; -fx-text-fill: #2a2a2a; -fx-font-size: 20;");

        StackPane avatarStack = new StackPane(avatar, dot);
        StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(dot, new Insets(0, 1, 1, 0));

        Label nameLbl = new Label(username.toUpperCase());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-font-family: monospace;");

        Label statusLbl = new Label(statusText);
        statusLbl.setStyle("-fx-text-fill: #444; -fx-font-size: 10px; -fx-font-family: monospace;");

        VBox textBox = new VBox(3, nameLbl, statusLbl);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button optBtn = new Button("···");
        optBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-cursor: hand; -fx-font-size: 13px;");

        HBox row = new HBox(12, avatarStack, textBox, optBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-background-color: #0a0a0a; -fx-border-color: #141414; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #111; -fx-border-color: #141414; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color: #0a0a0a; -fx-border-color: #141414; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        return row;
    }

    private VBox buildLeaderboardRow(int rank, String username, int hours, double progress, boolean isMe) {
        Label rankLbl = new Label(String.valueOf(rank));
        rankLbl.setStyle("-fx-text-fill: #333; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: monospace;");
        rankLbl.setMinWidth(40); rankLbl.setAlignment(Pos.CENTER);

        Label avatar = new Label();
        avatar.setPrefSize(40, 40); avatar.setMinSize(40, 40);
        String avStyle = isMe
            ? "-fx-background-color: #1a1a1a; -fx-border-color: #fbbf24; -fx-border-width: 1; -fx-background-radius: 20; -fx-border-radius: 20;"
            : "-fx-background-color: #111; -fx-border-color: #222; -fx-border-width: 1; -fx-background-radius: 20; -fx-border-radius: 20;";
        avatar.setStyle(avStyle);

        Label nameLbl = new Label(username);
        nameLbl.setStyle("-fx-text-fill: " + (isMe ? "white" : "#888") + "; " +
                         "-fx-font-weight: bold; -fx-font-size: 12px; -fx-font-family: monospace;");
        ProgressBar bar = new ProgressBar(progress);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(3);
        bar.setStyle("-fx-accent: " + (isMe ? "#fbbf24" : "#2a2a2a") + "; -fx-background-color: #161616;");
        HBox.setHgrow(bar, Priority.ALWAYS);
        Label hoursLbl = new Label(String.valueOf(hours));
        hoursLbl.setStyle("-fx-text-fill: " + (isMe ? "white" : "#555") + "; " +
                          "-fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: monospace;");
        Label hLbl = new Label("H");
        hLbl.setStyle("-fx-text-fill: #333; -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: monospace; -fx-padding: 8 0 0 0;");

        HBox hoursBox = new HBox(1, hoursLbl, hLbl);
        hoursBox.setAlignment(Pos.BASELINE_LEFT);
        hoursBox.setMinWidth(56);

        HBox barAndHours = new HBox(12, bar, hoursBox);
        barAndHours.setAlignment(Pos.CENTER);
        HBox.setHgrow(bar, Priority.ALWAYS);

        VBox center = new VBox(6, nameLbl, barAndHours);
        HBox.setHgrow(center, Priority.ALWAYS);

        HBox row = new HBox(16, rankLbl, avatar, center);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 24, 16, 24));
        row.setStyle("-fx-background-color: " + (isMe ? "#0e0e00" : "#0a0a0a") + "; " +
                     "-fx-border-color: #141414; -fx-border-width: 0 0 1 0;");

        return new VBox(row);
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
}