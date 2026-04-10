package StudyMore.controllers;

import StudyMore.ApiClient;
import StudyMore.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AchievementsController {

    @FXML private VBox achievementsContainer;
    @FXML private Label completedCountLabel; // "COMPLETED: X/Y" — add fx:id to FXML header label

    @FXML
    public void initialize() {
        loadFromDatabase();
    }
    private void loadFromDatabase() {
        achievementsContainer.getChildren().clear();

        String query = """
                SELECT a.id, a.title, a.description, a.type, a.target_value, a.reward,
                       ua.progress, ua.is_completed
                FROM user_achievements ua
                JOIN achievements a ON ua.achievement_id = a.id
                WHERE ua.user_id = ?
                ORDER BY ua.is_completed DESC, a.type ASC
                """;

        List<StackPane> cards = new ArrayList<>();
        int completed = 0;
        int total     = 0;

        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(query)) {
            stmt.setLong(1, Main.user.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String title       = rs.getString("title");
                    String description = rs.getString("description");
                    String type        = rs.getString("type");
                    int    target      = rs.getInt("target_value");
                    int    reward      = rs.getInt("reward");
                    int    progress    = rs.getInt("progress");
                    boolean done       = rs.getInt("is_completed") == 1;
                    String progressText = formatProgress(progress, target, type);
                    String rewardText = reward + " COINS";

                    double fraction = target == 0 ? 1.0 : Math.min(1.0, (double) progress / target);

                    cards.add(buildCard(title, description, progressText, rewardText, fraction, done));
                    total++;
                    if (done) completed++;
                }
            }
        } catch (SQLException e) {
            System.err.println("AchievementsController error: " + e.getMessage());
        }
        if (completedCountLabel != null) {
            completedCountLabel.setText("COMPLETED: " + completed + "/" + total);
        }
        for (int i = 0; i < cards.size(); i += 2) {
            HBox row = new HBox(24);
            StackPane left = cards.get(i);
            HBox.setHgrow(left, Priority.ALWAYS);
            left.setMaxWidth(Double.MAX_VALUE);
            row.getChildren().add(left);

            if (i + 1 < cards.size()) {
                StackPane right = cards.get(i + 1);
                HBox.setHgrow(right, Priority.ALWAYS);
                right.setMaxWidth(Double.MAX_VALUE);
                row.getChildren().add(right);
            } else {
                Region filler = new Region();
                HBox.setHgrow(filler, Priority.ALWAYS);
                row.getChildren().add(filler);
            }

            achievementsContainer.getChildren().add(row);
        }
        if (cards.isEmpty()) {
            Label empty = new Label("NO ACHIEVEMENTS FOUND. START STUDYING TO UNLOCK THEM!");
            empty.setTextFill(Color.web("#444"));
            empty.setFont(Font.font("System", FontWeight.BOLD, 11));
            achievementsContainer.getChildren().add(empty);
        }
    }
    private String formatProgress(int progress, int target, String type) {
        return switch (type) {
            case "TIME_BASED"   -> progress + " / " + target + " H";   // hours
            case "STREAK_BASED" -> progress + " / " + target + " D";   // days
            case "SOCIAL"       -> progress + " / " + target + " G";   // groups/friends
            default             -> progress + " / " + target;
        };
    }
    private StackPane buildCard(String title, String description, String progressText,
                                String rewardText, double progressFraction, boolean isCompleted) {
        StackPane card = new StackPane();
        String borderColor = isCompleted ? "white" : "#262626";
        card.setStyle("-fx-background-color: black; -fx-border-color: " + borderColor + ";");

        HBox mainHBox = new HBox(24.0);
        mainHBox.setStyle("-fx-padding: 24;");
        VBox infoVBox = new VBox(16.0);
        HBox.setHgrow(infoVBox, Priority.ALWAYS);

        VBox titleDescBox = new VBox(8.0);
        HBox titleHBox = new HBox();
        titleHBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title.toUpperCase());
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18.0));

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Label statusLabel = new Label(isCompleted ? "COMPLETED" : "IN PROGRESS");
        if (isCompleted) {
            statusLabel.setStyle("-fx-background-color: white; -fx-padding: 4 8; -fx-border-color: white; -fx-text-fill: black;");
        } else {
            statusLabel.setStyle("-fx-background-color: black; -fx-padding: 4 8; -fx-border-color: #262626; -fx-text-fill: #a3a3a3;");
        }
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 10.0));
        statusLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        titleHBox.getChildren().addAll(titleLabel, spacer1, statusLabel);

        Label descLabel = new Label(description.toUpperCase());
        descLabel.setTextFill(Color.web("#737373"));
        descLabel.setFont(Font.font("System", FontWeight.BOLD, 12.0));

        titleDescBox.getChildren().addAll(titleHBox, descLabel);

        Region vSpacer = new Region();
        VBox.setVgrow(vSpacer, Priority.ALWAYS);
        VBox progressBox = new VBox(12.0);
        HBox progressHBox = new HBox();
        progressHBox.setAlignment(Pos.BOTTOM_LEFT);

        Label progressLabel = new Label("PROGRESS: " + progressText.toUpperCase());
        progressLabel.setTextFill(Color.web("#a3a3a3"));
        progressLabel.setFont(Font.font("System", FontWeight.BOLD, 10.0));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox rewardBox = new HBox(8.0);
        rewardBox.setAlignment(Pos.CENTER);
        rewardBox.setStyle("-fx-background-color: black; -fx-border-color: #262626; -fx-padding: 6 12;");

        Label rewardLabel = new Label("REWARD: " + rewardText.toUpperCase());
        rewardLabel.setTextFill(Color.web("#a3a3a3"));
        rewardLabel.setFont(Font.font("System", FontWeight.BOLD, 10.0));

        rewardBox.getChildren().add(rewardLabel);
        progressHBox.getChildren().addAll(progressLabel, spacer2, rewardBox);

        StackPane barContainer = new StackPane();
        barContainer.setAlignment(Pos.CENTER_LEFT);
        barContainer.setPrefHeight(8.0);
        barContainer.setStyle("-fx-background-color: black; -fx-border-color: #262626;");

        Region fillRegion = new Region();
        String fillColor = isCompleted ? "white" : "#737373";
        fillRegion.setStyle("-fx-background-color: " + fillColor + ";");
        double clamped = Math.max(0.0, Math.min(1.0, progressFraction));
        fillRegion.maxWidthProperty().bind(barContainer.widthProperty().multiply(clamped));

        barContainer.getChildren().add(fillRegion);
        progressBox.getChildren().addAll(progressHBox, barContainer);

        infoVBox.getChildren().addAll(titleDescBox, vSpacer, progressBox);
        mainHBox.getChildren().add(infoVBox);
        card.getChildren().add(mainHBox);

        return card;
    }
    public static void updateProgress(long userId, String type, int amount) {
        String update = """
                UPDATE user_achievements
                SET progress = MIN(progress + ?,
                    (SELECT target_value FROM achievements WHERE id = user_achievements.achievement_id))
                WHERE user_id = ?
                AND achievement_id IN (SELECT id FROM achievements WHERE type = ?)
                AND is_completed = 0
                """;

        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(update)) {
            stmt.setInt(1, amount);
            stmt.setLong(2, userId);
            stmt.setString(3, type);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("updateProgress error: " + e.getMessage());
            return;
        }
        String complete = """
            SELECT ua.id, a.reward, a.title FROM user_achievements ua
            JOIN achievements a ON ua.achievement_id = a.id
            WHERE ua.user_id = ? AND a.type = ? AND ua.is_completed = 0
            AND ua.progress >= a.target_value
            """;

        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(complete)) {
            stmt.setLong(1, userId);
            stmt.setString(2, type);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long uaId  = rs.getLong("id");
                    int reward = rs.getInt("reward");
                    String achTitle = rs.getString("title");

                    try (PreparedStatement upd = Main.mngr.getConnection().prepareStatement(
                            "UPDATE user_achievements SET is_completed = 1, completed_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                        upd.setLong(1, uaId);
                        upd.executeUpdate();
                    }
                    try (PreparedStatement coins = Main.mngr.getConnection().prepareStatement(
                            "UPDATE user_stats SET coin_balance = coin_balance + ? WHERE user_id = ?")) {
                        coins.setInt(1, reward);
                        coins.setLong(2, userId);
                        coins.executeUpdate();
                    }
                    Main.user.setCoinBalance(Main.user.getCoinBalance() + reward);
                    showUnlockNotification(achTitle, reward);
                    try {
                        String heartbeatBody = "{\"userId\":" + userId
                                            + ",\"coinBalance\":" + Main.user.getCoinBalance() + "}";
                        ApiClient.postAuth("/auth/users/heartbeat", heartbeatBody);
                    } catch (Exception ignored) {}
                    System.out.println("Achievement unlocked! +" + reward + " coins.");
                }
            }
        } catch (SQLException e) {
            System.err.println("completion check error: " + e.getMessage());
        }
    }
    private static void showUnlockNotification(String title, int reward) {
        Platform.runLater(() -> {
            Label trophy = new Label("🏆");
            trophy.setFont(Font.font(28));

            Label titleLbl = new Label("ACHIEVEMENT UNLOCKED");
            titleLbl.setFont(Font.font("System", FontWeight.BOLD, 10));
            titleLbl.setTextFill(Color.web("#a3a3a3"));

            Label nameLbl = new Label(title.toUpperCase());
            nameLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            nameLbl.setTextFill(Color.WHITE);

            Label rewardLbl = new Label("🪙 +" + reward + " COINS");
            rewardLbl.setFont(Font.font("System", FontWeight.BOLD, 11));
            rewardLbl.setTextFill(Color.GOLD);

            VBox textBox = new VBox(3, titleLbl, nameLbl, rewardLbl);
            textBox.setAlignment(Pos.CENTER_LEFT);

            HBox card = new HBox(12, trophy, textBox);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(16, 20, 16, 16));
            card.setStyle("-fx-background-color: #111111; " +
                        "-fx-border-color: white; " +
                        "-fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0, 0, 4);");

            StackPane root = new StackPane(card);
            root.setStyle("-fx-background-color: transparent;");

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setAlwaysOnTop(true);
            stage.setScene(new Scene(root, 320, 80));
            stage.getScene().setFill(null);

            javafx.geometry.Rectangle2D screen =
                    javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(screen.getMaxX() - 340);
            stage.setY(screen.getMaxY() - 100);
            stage.show();

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), card);
            slideIn.setFromX(340);
            slideIn.setToX(0);
            slideIn.play();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(600), root);
            fadeOut.setDelay(Duration.seconds(3));
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> stage.close());
            fadeOut.play();
        });
    }
}