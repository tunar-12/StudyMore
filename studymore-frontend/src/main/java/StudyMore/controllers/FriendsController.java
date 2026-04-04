package StudyMore.controllers;

import StudyMore.Main;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

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

        String query = (keyword == null || keyword.isEmpty()) ? """
                SELECT u.id, u.username
                FROM friends f
                JOIN users u ON f.friend_id = u.id
                WHERE f.user_id = ?
                ORDER BY u.username ASC
                """ : """
                SELECT u.id, u.username
                FROM friends f
                JOIN users u ON f.friend_id = u.id
                WHERE f.user_id = ? AND LOWER(u.username) LIKE LOWER(?)
                ORDER BY u.username ASC
                """;

        int onlineCount = 0;

        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(query)) {
            stmt.setLong(1, Main.user.getUserId());
            if (keyword != null && !keyword.isEmpty()) stmt.setString(2, "%" + keyword + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    long   id       = rs.getLong("id");
                    String username = rs.getString("username");
                    String status   = getFriendStatus(id);
                    boolean online  = isRecentlyActive(id);
                    if (online) onlineCount++;
                    friendListContainer.getChildren().add(buildFriendRow(username, status, online));
                }
                if (!found) {
                    Label l = new Label("NO FRIENDS YET");
                    l.setStyle("-fx-text-fill: #333; -fx-font-size: 11px; -fx-font-family: monospace;");
                    l.setPadding(new Insets(20, 16, 0, 16));
                    friendListContainer.getChildren().add(l);
                }
            }
        } catch (SQLException e) {
            setStatus("Error: " + e.getMessage());
        }

        onlineCountLabel.setText(onlineCount + " ONLINE");
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
        long groupId   = -1;
        int  studyGoal = 50;

        try (PreparedStatement s = Main.mngr.getConnection().prepareStatement("""
                SELECT sg.id, sg.study_goal FROM study_groups sg
                JOIN study_group_members sgm ON sg.id = sgm.group_id
                WHERE sgm.user_id = ? AND sg.is_active = 1
                ORDER BY sg.created_at DESC LIMIT 1
                """)) {
            s.setLong(1, Main.user.getUserId());
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    groupId   = rs.getLong("id");
                    studyGoal = rs.getInt("study_goal");
                }
            }
        } catch (SQLException e) { setStatus("Error: " + e.getMessage()); }

        goalLabel.setText("GOAL: " + studyGoal + "H");

        if (groupId == -1) {
            Label l = new Label("JOIN OR CREATE A GROUP TO SEE THE LEADERBOARD");
            l.setStyle("-fx-text-fill: #333; -fx-font-size: 11px; -fx-font-family: monospace;");
            l.setPadding(new Insets(32, 24, 0, 24));
            leaderboardContainer.getChildren().add(l);
            return;
        }

        String q = """
                SELECT u.id, u.username, COALESCE(us.total_study_time, 0) AS stime
                FROM study_group_members sgm
                JOIN users u ON sgm.user_id = u.id
                LEFT JOIN user_stats us ON u.id = us.user_id
                WHERE sgm.group_id = ?
                ORDER BY stime DESC
                """;

        try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(q)) {
            s.setLong(1, groupId);
            try (ResultSet rs = s.executeQuery()) {
                List<long[]> rows = new ArrayList<>();
                List<String> names = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new long[]{ rs.getLong("id"), rs.getLong("stime") });
                    names.add(rs.getString("username"));
                }
                long maxSec = rows.stream().mapToLong(r -> r[1]).max().orElse(1);
                if (maxSec == 0) maxSec = 1;

                for (int i = 0; i < rows.size(); i++) {
                    long uid     = rows.get(i)[0];
                    long sec     = rows.get(i)[1];
                    int  hours   = (int)(sec / 3600);
                    double prog  = (double) sec / maxSec;
                    boolean isMe = uid == Main.user.getUserId();
                    String display = isMe ? "YOU" : names.get(i).toUpperCase();
                    leaderboardContainer.getChildren().add(
                        buildLeaderboardRow(i + 1, display, hours, prog, isMe)
                    );
                }
            }
        } catch (SQLException e) { setStatus("Error: " + e.getMessage()); }
    }
    @FXML
    private void onCreateGroup() {
        TextInputDialog d1 = new TextInputDialog();
        d1.setTitle("Create Study Group");
        d1.setHeaderText(null);
        d1.setContentText("Group name:");
        d1.showAndWait().ifPresent(title -> {
            if (title.trim().isEmpty()) return;
            TextInputDialog d2 = new TextInputDialog("50");
            d2.setTitle("Study Goal");
            d2.setHeaderText(null);
            d2.setContentText("Goal (hours):");
            d2.showAndWait().ifPresent(goalStr -> {
                int goal = 50;
                try { goal = Integer.parseInt(goalStr.trim()); } catch (NumberFormatException ignored) {}
                try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(
                        "INSERT INTO study_groups (title, host_id, study_goal, max_members, is_active, created_at) " +
                        "VALUES (?, ?, ?, 10, 1, CURRENT_TIMESTAMP)", Statement.RETURN_GENERATED_KEYS)) {
                    s.setString(1, title.trim());
                    s.setLong(2, Main.user.getUserId());
                    s.setInt(3, goal);
                    s.executeUpdate();
                    try (ResultSet k = s.getGeneratedKeys()) {
                        if (k.next()) {
                            long gid = k.getLong(1);
                            try (PreparedStatement join = Main.mngr.getConnection().prepareStatement(
                                    "INSERT INTO study_group_members (group_id, user_id) VALUES (?,?) ON CONFLICT DO NOTHING")) {
                                join.setLong(1, gid); join.setLong(2, Main.user.getUserId());
                                join.executeUpdate();
                            }
                        }
                    }
                    setStatus("Group created!");
                    loadGroupLeaderboard();
                } catch (SQLException e) { setStatus("Error: " + e.getMessage()); }
            });
        });
    }

    @FXML
    private void onViewRequests() {
        String query = """
            SELECT fr.id, u.username FROM friend_requests fr
            JOIN users u ON fr.sender_id = u.id
            WHERE fr.receiver_id = ? AND fr.status = 'PENDING'
            """;
        try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(query)) {
            s.setLong(1, Main.user.getUserId());
            List<String> names = new ArrayList<>();
            List<Long>   ids   = new ArrayList<>();
            try (ResultSet rs = s.executeQuery()) {
                while (rs.next()) { names.add(rs.getString("username")); ids.add(rs.getLong("id")); }
            }
            if (names.isEmpty()) { setStatus("No pending requests."); return; }

            ChoiceDialog<String> d = new ChoiceDialog<>(names.get(0), names);
            d.setTitle("Accept Request"); d.setHeaderText(null); d.setContentText("Accept from:");
            d.showAndWait().ifPresent(sel -> {
                int idx = names.indexOf(sel);
                acceptRequest(ids.get(idx), sel);
            });
        } catch (SQLException e) { setStatus("Error: " + e.getMessage()); }
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
            try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(
                    "SELECT id, username FROM users WHERE LOWER(username) LIKE LOWER(?) AND id != ? LIMIT 5")) {
                s.setString(1, "%" + kw.trim() + "%"); s.setLong(2, Main.user.getUserId());
                List<String> choices = new ArrayList<>(); List<Long> ids = new ArrayList<>();
                try (ResultSet rs = s.executeQuery()) {
                    while (rs.next()) { choices.add(rs.getString("username")); ids.add(rs.getLong("id")); }
                }
                if (choices.isEmpty()) { setStatus("No users found."); return; }
                ChoiceDialog<String> cd = new ChoiceDialog<>(choices.get(0), choices);
                cd.setTitle("Send Request"); cd.setHeaderText(null); cd.setContentText("Select user:");
                cd.showAndWait().ifPresent(sel -> {
                    int idx = choices.indexOf(sel);
                    sendRequest(ids.get(idx), sel);
                });
            } catch (SQLException e) { setStatus("Error: " + e.getMessage()); }
        });
    }

    private void sendRequest(long receiverId, String name) {
        try (PreparedStatement c = Main.mngr.getConnection().prepareStatement(
                "SELECT id FROM friend_requests WHERE (sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)")) {
            c.setLong(1, Main.user.getUserId()); c.setLong(2, receiverId);
            c.setLong(3, receiverId);            c.setLong(4, Main.user.getUserId());
            try (ResultSet rs = c.executeQuery()) { if (rs.next()) { setStatus("Already sent."); return; } }
        } catch (SQLException e) { setStatus("Error: " + e.getMessage()); return; }

        try (PreparedStatement s = Main.mngr.getConnection().prepareStatement(
                "INSERT INTO friend_requests (sender_id, receiver_id, status, sent_at) VALUES (?,?,'PENDING',CURRENT_TIMESTAMP)")) {
            s.setLong(1, Main.user.getUserId()); s.setLong(2, receiverId);
            s.executeUpdate(); setStatus("Request sent to " + name + "!");
        } catch (SQLException e) { setStatus("Error: " + e.getMessage()); }
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