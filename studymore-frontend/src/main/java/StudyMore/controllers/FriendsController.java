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

        String response = ApiClient.get("/friends/" + Main.user.getUserId());
        JSONArray arr;
        try {
            arr = new JSONArray(response);
        } catch (Exception e) {
            System.err.println("Friends parse error: " + response);
            arr = new JSONArray();
        }

        JSONArray filtered = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject u = arr.getJSONObject(i);
            String username = u.optString("username", "");
            if (keyword == null || keyword.isEmpty() || 
                username.toLowerCase().contains(keyword.toLowerCase())) {
                filtered.put(u);
            }
        }

        int onlineCount = 0;

        for (int i = 0; i < filtered.length(); i++) {
            JSONObject u       = filtered.getJSONObject(i);
            String username    = u.optString("username", "?");
            String lastSeenStr = u.optString("lastSeen", null);

            boolean online    = false;
            String statusText = "OFFLINE";

            if (lastSeenStr != null) {
                try {
                    java.time.LocalDateTime lastSeen = java.time.LocalDateTime.parse(lastSeenStr);
                    java.time.LocalDateTime utcNow   = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);
                    long minutesAgo = java.time.Duration.between(lastSeen, utcNow).toMinutes();
                    if (minutesAgo <= 6) {
                        online     = true;
                        statusText = "ONLINE";
                    } else if (minutesAgo <= 30) {
                        statusText = "LAST SEEN " + minutesAgo + "M AGO";
                    }
                } catch (Exception ignored) {}
            }

            if (online) onlineCount++;
            friendListContainer.getChildren().add(buildFriendRow(username, statusText, online));
        }

        if (filtered.isEmpty()) {
            Label l = new Label(keyword != null && !keyword.isEmpty() 
                ? "NO FRIENDS MATCHING \"" + keyword.toUpperCase() + "\""
                : "NO FRIENDS YET — ADD SOMEONE TO GET STARTED");
            l.setStyle("-fx-text-fill: #404040; -fx-font-size: 11px; -fx-font-weight: bold;");
            l.setPadding(new Insets(32, 16, 0, 16));
            friendListContainer.getChildren().add(l);
        }

        onlineCountLabel.setText(onlineCount + " ONLINE");
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
            goalLabel.setText("GOAL: —");
            Label l = new Label("JOIN OR CREATE A GROUP TO SEE THE LEADERBOARD");
            l.setStyle("-fx-text-fill: #404040; -fx-font-size: 11px; -fx-font-weight: bold;");
            l.setPadding(new Insets(40, 24, 0, 24));
            leaderboardContainer.getChildren().add(l);
            return;
        }

        List<String>  groupTitles = new ArrayList<>();
        List<Long>    groupIds    = new ArrayList<>();
        List<Integer> studyGoals  = new ArrayList<>();

        for (int i = 0; i < groups.length(); i++) {
            JSONObject g = groups.getJSONObject(i);
            groupTitles.add(g.optString("title", "Group " + (i + 1)));
            groupIds.add(g.getLong("groupId"));
            int sg = 50;
            try { sg = Integer.parseInt(g.optString("studyGoal", "50")); } catch (NumberFormatException ignored) {}
            studyGoals.add(sg);
        }

        if (groups.length() > 1) {
            ComboBox<String> groupPicker = new ComboBox<>();
            groupPicker.getItems().addAll(groupTitles);
            groupPicker.setValue(groupTitles.get(0));
            groupPicker.setStyle(
                "-fx-background-color: #111111; -fx-text-fill: white; " +
                "-fx-border-color: #262626; -fx-border-width: 1; " +
                "-fx-font-size: 12px; -fx-font-weight: bold;");
            groupPicker.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(groupPicker, new Insets(12, 24, 4, 24));

            final List<Long>    fIds   = groupIds;
            final List<Integer> fGoals = studyGoals;
            groupPicker.setOnAction(e -> {
                int idx = groupPicker.getSelectionModel().getSelectedIndex();
                if (idx >= 0) loadGroupById(fIds.get(idx), fGoals.get(idx));
            });
            leaderboardContainer.getChildren().add(groupPicker);
        }

        loadGroupById(groupIds.get(0), studyGoals.get(0));
    }

    private void loadGroupById(long groupId, int studyGoal) {
        javafx.scene.Node comboBox = null;
        if (!leaderboardContainer.getChildren().isEmpty() &&
                leaderboardContainer.getChildren().get(0) instanceof ComboBox) {
            comboBox = leaderboardContainer.getChildren().get(0);
        }
        leaderboardContainer.getChildren().clear();
        if (comboBox != null) leaderboardContainer.getChildren().add(comboBox);

        goalLabel.setText("GOAL: " + studyGoal + "H");

        Button inviteBtn = new Button("+ INVITE FRIEND");
        inviteBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: white; " +
            "-fx-border-color: #262626; -fx-border-width: 1; " +
            "-fx-font-size: 12px; -fx-font-weight: bold; " +
            "-fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 0;");
        inviteBtn.setOnMouseEntered(e -> inviteBtn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: white; " +
            "-fx-border-color: #404040; -fx-border-width: 1; " +
            "-fx-font-size: 12px; -fx-font-weight: bold; " +
            "-fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 0;"));
        inviteBtn.setOnMouseExited(e -> inviteBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: white; " +
            "-fx-border-color: #262626; -fx-border-width: 1; " +
            "-fx-font-size: 12px; -fx-font-weight: bold; " +
            "-fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 0;"));
        inviteBtn.setOnAction(e -> inviteFriendToGroup(groupId));
        VBox.setMargin(inviteBtn, new Insets(16, 24, 0, 24));
        leaderboardContainer.getChildren().add(inviteBtn);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #1a1a1a;");
        VBox.setMargin(sep, new Insets(16, 0, 0, 0));
        leaderboardContainer.getChildren().add(sep);

        JSONArray members;
        try {
            members = new JSONArray(ApiClient.get("/groups/" + groupId + "/leaderboard"));
        } catch (Exception e) {
            leaderboardContainer.getChildren().add(emptyLabel("NO MEMBERS YET"));
            return;
        }

        if (members.isEmpty()) {
            leaderboardContainer.getChildren().add(emptyLabel("NO MEMBERS YET"));
            return;
        }

        List<JSONObject> memberList = new ArrayList<>();
        for (int i = 0; i < members.length(); i++) {
            memberList.add(members.getJSONObject(i));
        }
        memberList.sort((a, b) -> Long.compare(
            b.optLong("totalStudyTime", 0),
            a.optLong("totalStudyTime", 0)
        ));

        long maxSec = 1;
        for (JSONObject u : memberList) {
            long t = u.optLong("totalStudyTime", 0);
            if (t > maxSec) maxSec = t;
        }

        for (int i = 0; i < memberList.size(); i++) {
            JSONObject u  = memberList.get(i);
            long   uid    = u.optLong("userId", -1);
            String uname  = u.optString("username", "?");
            long   sec    = u.optLong("totalStudyTime", 0);
            int    hours  = (int)(sec / 3600);
            double prog   = (double) sec / maxSec;
            boolean isMe  = uid == Main.user.getUserId();
            leaderboardContainer.getChildren().add(
                buildLeaderboardRow(i + 1, isMe ? "YOU" : uname.toUpperCase(), hours, prog, isMe));
        }
    }


    private void inviteFriendToGroup(long groupId) {
        JSONArray arr;
        try {
            arr = new JSONArray(ApiClient.get("/friends/" + Main.user.getUserId()));
        } catch (Exception e) { setStatus("Error loading friends."); return; }

        if (arr.isEmpty()) { setStatus("No friends to invite."); return; }

        List<String> names = new ArrayList<>();
        List<Long>   ids   = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject u = arr.getJSONObject(i);
            names.add(u.optString("username", "?"));
            ids.add(u.optLong("userId", -1));
        }

        ChoiceDialog<String> d = new ChoiceDialog<>(names.get(0), names);
        d.setTitle("Invite to Group"); d.setHeaderText(null); d.setContentText("Select friend:");
        d.showAndWait().ifPresent(sel -> {
            int  idx      = names.indexOf(sel);
            long friendId = ids.get(idx);
            String response = ApiClient.post("/groups/" + groupId + "/join?userId=" + friendId, "");
            try {
                JSONObject res = new JSONObject(response);
                if (res.has("error")) setStatus("Could not invite: " + res.getString("error"));
                else { setStatus(sel + " added to group!"); loadGroupLeaderboard(); }
            } catch (Exception e) { setStatus("Error: " + e.getMessage()); }
        });
    }


    @FXML
    private void onViewRequests() {
        String raw = ApiClient.get("/friends/requests/pending?userId=" + Main.user.getUserId());
        System.out.println("REQUESTS RAW: " + raw);

        JSONArray arr;
        try {
            arr = new JSONArray(raw);
        } catch (Exception e) { 
            setStatus("Error loading requests."); 
            return; 
        }

        if (arr.isEmpty()) { setStatus("No pending requests."); return; }

        List<String> names  = new ArrayList<>();
        List<Long>   reqIds = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject req    = arr.getJSONObject(i);
            names.add(req.optString("senderUsername", "?"));
            reqIds.add(req.getLong("requestId"));   
        }

        ChoiceDialog<String> d = new ChoiceDialog<>(names.get(0), names);
        d.setTitle("Accept Request"); d.setHeaderText(null); d.setContentText("Accept from:");
        d.showAndWait().ifPresent(sel -> {
            int idx = names.indexOf(sel);
            ApiClient.put("/friends/requests/" + reqIds.get(idx) + "/accept?receiverId=" + Main.user.getUserId());
            setStatus("Now friends with " + sel + "!");
            AchievementsController.updateProgress(Main.user.getUserId(), "SOCIAL", 1);
            loadFriends(null);
        });
    }

    @FXML
    private void onAddFriend() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Add Friend"); d.setHeaderText(null); d.setContentText("Search username:");
        d.showAndWait().ifPresent(kw -> {
            if (kw.trim().isEmpty()) return;
            JSONArray results;
            try {
                results = new JSONArray(ApiClient.get("/friends/search?keyword=" + kw.trim()
                        + "&requestingUserId=" + Main.user.getUserId()));
            } catch (Exception e) { setStatus("Search error."); return; }

            List<String> choices = new ArrayList<>();
            List<Long>   ids     = new ArrayList<>();
            for (int i = 0; i < results.length(); i++) {
                JSONObject u = results.getJSONObject(i);
                choices.add(u.optString("username", "?"));
                ids.add(u.optLong("userId", -1));
            }
            if (choices.isEmpty()) { setStatus("No users found."); return; }

            ChoiceDialog<String> cd = new ChoiceDialog<>(choices.get(0), choices);
            cd.setTitle("Send Request"); cd.setHeaderText(null); cd.setContentText("Select:");
            cd.showAndWait().ifPresent(sel -> sendRequest(ids.get(choices.indexOf(sel)), sel));
        });
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
                String body = "{\"title\":\"" + title.trim() + "\","
                            + "\"studyGoal\":\"" + goalStr.trim() + "\","
                            + "\"hostId\":" + Main.user.getUserId() + ","
                            + "\"maxMembers\":10}";
                ApiClient.post("/groups", body);
                setStatus("Group \"" + title.trim() + "\" created!");
                AchievementsController.updateProgress(Main.user.getUserId(), "SOCIAL", 1);
                loadGroupLeaderboard();
            });
        });
    }

    private void sendRequest(long receiverId, String name) {
        String body     = "{\"senderId\":" + Main.user.getUserId() + ",\"receiverId\":" + receiverId + "}";
        String response = ApiClient.post("/friends/requests", body);
        try {
            JSONObject res = new JSONObject(response);
            if (res.has("error")) setStatus("Error: " + res.getString("error"));
            else setStatus("Request sent to " + name + "!");
        } catch (Exception e) { setStatus("Request sent!"); }
    }


    private HBox buildFriendRow(String username, String statusText, boolean online) {
        Circle dot = new Circle(5);
        dot.setFill(online ? Color.web("#4caf50") : Color.web("#262626"));

        Label avatar = new Label(username.substring(0, 1).toUpperCase());
        avatar.setPrefSize(44, 44); avatar.setMinSize(44, 44);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle(
            "-fx-background-color: #1a1a1a; -fx-border-color: #262626; -fx-border-width: 1; " +
            "-fx-background-radius: 22; -fx-border-radius: 22; " +
            "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        StackPane avatarStack = new StackPane(avatar, dot);
        StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(dot, new Insets(0, 2, 2, 0));

        Label nameLbl = new Label(username.toUpperCase());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        String statusColor = online ? "#4caf50" : "#404040";
        Label statusLbl = new Label(statusText);
        statusLbl.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 11px;");

        VBox textBox = new VBox(4, nameLbl, statusLbl);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button optBtn = new Button("···");
        optBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #404040; " +
            "-fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 4 8;");
        optBtn.setOnMouseEntered(e -> optBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: white; " +
            "-fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 4 8;"));
        optBtn.setOnMouseExited(e -> optBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #404040; " +
            "-fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 4 8;"));

        HBox row = new HBox(14, avatarStack, textBox, optBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: #0a0a0a; -fx-border-color: #1a1a1a; -fx-border-width: 0 0 1 0;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #111111; -fx-border-color: #1a1a1a; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color: #0a0a0a; -fx-border-color: #1a1a1a; -fx-border-width: 0 0 1 0;"));
        return row;
    }

    private VBox buildLeaderboardRow(int rank, String username, int hours, double progress, boolean isMe) {
        Label rankLbl = new Label("#" + rank);
        rankLbl.setStyle("-fx-text-fill: " + (isMe ? "white" : "#404040") + "; " +
                         "-fx-font-size: 18px; -fx-font-weight: bold;");
        rankLbl.setMinWidth(44); rankLbl.setAlignment(Pos.CENTER);

        Label avatar = new Label(username.substring(0, 1));
        avatar.setPrefSize(44, 44); avatar.setMinSize(44, 44);
        avatar.setAlignment(Pos.CENTER);
        String avStyle = isMe
            ? "-fx-background-color: #1a1a1a; -fx-border-color: #fbbf24; -fx-border-width: 1; " +
              "-fx-background-radius: 22; -fx-border-radius: 22; -fx-text-fill: #fbbf24; " +
              "-fx-font-size: 14px; -fx-font-weight: bold;"
            : "-fx-background-color: #111111; -fx-border-color: #262626; -fx-border-width: 1; " +
              "-fx-background-radius: 22; -fx-border-radius: 22; -fx-text-fill: #a3a3a3; " +
              "-fx-font-size: 14px; -fx-font-weight: bold;";
        avatar.setStyle(avStyle);

        Label nameLbl = new Label(username);
        nameLbl.setStyle("-fx-text-fill: " + (isMe ? "white" : "#a3a3a3") + "; " +
                         "-fx-font-weight: bold; -fx-font-size: 13px;");

        ProgressBar bar = new ProgressBar(progress);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(4);
        bar.setStyle("-fx-accent: " + (isMe ? "#fbbf24" : "#2a2a2a") + "; -fx-background-color: #1a1a1a;");
        HBox.setHgrow(bar, Priority.ALWAYS);

        Label hoursLbl = new Label(String.valueOf(hours));
        hoursLbl.setStyle("-fx-text-fill: " + (isMe ? "white" : "#737373") + "; " +
                          "-fx-font-size: 24px; -fx-font-weight: bold;");
        Label hLbl = new Label("H");
        hLbl.setStyle("-fx-text-fill: " + (isMe ? "#a3a3a3" : "#404040") + "; " +
                      "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");

        HBox hoursBox = new HBox(2, hoursLbl, hLbl);
        hoursBox.setAlignment(Pos.BASELINE_LEFT);
        hoursBox.setMinWidth(60);

        HBox barAndHours = new HBox(12, bar, hoursBox);
        barAndHours.setAlignment(Pos.CENTER);
        HBox.setHgrow(bar, Priority.ALWAYS);

        VBox center = new VBox(6, nameLbl, barAndHours);
        HBox.setHgrow(center, Priority.ALWAYS);

        HBox row = new HBox(16, rankLbl, avatar, center);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(18, 24, 18, 24));
        row.setStyle(
            "-fx-background-color: " + (isMe ? "#111100" : "#0a0a0a") + "; " +
            "-fx-border-color: #1a1a1a; -fx-border-width: 0 0 1 0;");

        return new VBox(row);
    }


    private boolean isRecentlyActive(long userId) {
        try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(
                "SELECT 1 FROM sessions WHERE user_id = ? AND DATE(start_time) = DATE('now','localtime') LIMIT 1")) {
            s.setLong(1, userId);
            try (ResultSet rs = s.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { return false; }
    }

    private Label emptyLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #404040; -fx-font-size: 11px; -fx-font-weight: bold;");
        l.setPadding(new Insets(24, 24, 0, 24));
        return l;
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
}