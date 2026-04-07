package StudyMore.controllers;

import StudyMore.Main;
import StudyMore.models.CosmeticType;
import StudyMore.models.SessionState;
import StudyMore.models.StudySession;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import StudyMore.ApiClient;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import org.json.JSONArray;
import org.json.JSONObject;

public class StudyController {

    @FXML private Label timerLabel;
    @FXML private Label streakLabel;
    @FXML private Label studyTime;
    @FXML private Label goal;
    @FXML private Button timerControlButton;
    @FXML private Button longBreakButton;
    @FXML private Button shortBreakButton;
    @FXML private Label multiplierLabel;
    @FXML private ImageView catSkin;
    @FXML private ImageView catHouse;
    @FXML private VBox leaderboardContainer;

    private int LONG_BREAK_SECONDS  = 1200; // 20 min
    private int SHORT_BREAK_SECONDS = 600;  // 10 min
    private int STUDY_SECONDS = 1500; 

    private StudySession session;
    private Timeline studyTimeline;
    private Timeline breakTimeline;

    public void initialize() {
        StudyMore.models.Settings settings = Main.settings;
        if (settings == null) {
            settings = Main.mngr.getSettings(Main.user.getUserId());
        }
        if (settings == null) {
            settings = new StudyMore.models.Settings();
            Main.mngr.saveSettings(Main.user.getUserId(), settings);
            Main.settings = settings;
        }

        STUDY_SECONDS       = settings.getStudyTime() * 60;
        SHORT_BREAK_SECONDS = settings.getShortBreak() * 60;
        LONG_BREAK_SECONDS  = settings.getLongBreak() * 60;

        catSkin.setImage(new Image("/StudyMore/" + Main.user.getInventory().getEquipped(CosmeticType.MASCOT_SKIN).getImagePath()));
        catHouse.setImage(new Image("/StudyMore/" + Main.user.getInventory().getEquipped(CosmeticType.MASCOT_HOUSE).getImagePath()));
        studyTime.setText(ProfileController.calculateStudyTimes()[1] + "H");
        session = new StudySession(Main.user);
        streakLabel.setText(Main.user.getStudyStreak() + " Days");
        updateTimerLabel(session.getDuration());
        studyTimeline = buildStudyTimeline();

        loadGroupLeaderboard();
    }

    // FXML handlers

    @FXML
    private void timerController() {
        if (session.getState() == SessionState.STUDYING) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    @FXML
    private void longBreak() {
        startBreak(LONG_BREAK_SECONDS);
    }

    @FXML
    private void shortBreak() {
        startBreak(SHORT_BREAK_SECONDS);
    }

    private void loadGroupLeaderboard() {
        leaderboardContainer.getChildren().clear();

        JSONArray groups;
        try {
            groups = new JSONArray(ApiClient.get("/groups/user/" + Main.user.getUserId()));
        } catch (Exception e) {
            groups = new JSONArray();
        }

        if (groups.isEmpty()) {
            goal.setText("Goal: No Study Group");
            return;
        }

        JSONObject bestGroup = groups.getJSONObject(0);
        for (int i = 1; i < groups.length(); i++) {
            JSONObject g = groups.getJSONObject(i);
            if (g.optInt("memberCount", 0) > bestGroup.optInt("memberCount", 0)) {
                bestGroup = g;
            }
        }

        long groupId   = bestGroup.getLong("groupId");
        int  studyGoal = 50;
        try { studyGoal = Integer.parseInt(bestGroup.optString("studyGoal", "50")); } 
        catch (NumberFormatException ignored) {}
        goal.setText("Goal: " + studyGoal + "H");

        JSONArray members;
        try {
            members = new JSONArray(ApiClient.get("/groups/" + groupId + "/leaderboard"));
        } catch (Exception e) {
            return;
        }

        java.util.List<JSONObject> memberList = new java.util.ArrayList<>();
        for (int i = 0; i < members.length(); i++) memberList.add(members.getJSONObject(i));
        memberList.sort((a, b) -> Long.compare(
            b.optLong("totalStudyTime", 0),
            a.optLong("totalStudyTime", 0)
        ));

        for (int i = 0; i < memberList.size(); i++) {
            JSONObject u  = memberList.get(i);
            long   uid    = u.optLong("userId", -1);
            String uname  = u.optString("username", "?");
            long   sec    = u.optLong("totalStudyTime", 0);
            int    hours  = (int)(sec / 3600);
            boolean isMe  = (uid == Main.user.getUserId());
            leaderboardContainer.getChildren().add(
                buildLeaderboardRow(i + 1, isMe ? "You" : uname, hours, isMe));
        }
    }

    private HBox buildLeaderboardRow(int rank, String username, int hours, boolean isMe) {
        Label nameLbl = new Label(rank + ". " + username);
        nameLbl.setStyle("-fx-font-size: 14px;");
        
        if (isMe) {
            nameLbl.setStyle(nameLbl.getStyle() + " -fx-font-weight: bold; -fx-text-fill: black;");
        } else {
            nameLbl.setStyle(nameLbl.getStyle() + " -fx-text-fill: #a3a3a3;");
        }

        AnchorPane spacer = new AnchorPane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hoursLbl = new Label(hours + "H");
        hoursLbl.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 14px;");
        if (isMe) {
            hoursLbl.setStyle(hoursLbl.getStyle() + " -fx-font-weight: bold; -fx-text-fill: black;");
        } else {
            hoursLbl.setStyle(hoursLbl.getStyle() + " -fx-text-fill: #737373;");
        }

        HBox row = new HBox(nameLbl, spacer, hoursLbl);
        row.setStyle("-fx-padding: 12;");

        if (isMe) {
            row.setStyle(row.getStyle() + " -fx-background-color: white;");
        } else {
            row.setStyle(row.getStyle() + " -fx-background-color: black; -fx-border-color: #262626;");
        }
        
        return row;
    }

    // Timer control

    private void startTimer() {
        if (session.isOnBreak()) {
            session.resetBreak();
            stopBreakTimeline();
        }

        session.start();
        studyTimeline.play();
        timerControlButton.setText("STOP");
    }

    private void stopTimer() {
        studyTimeline.stop();
        session.stop();

        session.calculateCoins();
        session.end();

        
        timerControlButton.setText("START");
    }

    private void startBreak(int seconds) {
        stopTimer();
        stopBreakTimeline();

        session.startBreak(seconds);
        breakTimeline = buildBreakTimeline();
        breakTimeline.play();
    }

    private void stopBreakTimeline() {
        if (breakTimeline != null) {
            breakTimeline.stop();
        }
    }

    // Timeline builders

    private Timeline buildStudyTimeline() {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), e -> {
            session.incrementDuration();
            updateTimerLabel(session.getDuration());

            if(session.getMultiplier().isUpdated()) {
                updateMultiplier(session.getMultiplier().getValue());
            }
        });

        Timeline tl = new Timeline(keyFrame);
        tl.setCycleCount(Timeline.INDEFINITE);
        return tl;
    }

    private Timeline buildBreakTimeline() {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), e -> {
            session.tickBreak();

            if (session.isBreakOver()) {
                stopBreakTimeline();
                startTimer();
            } else {
                updateTimerLabel(session.getBreakTimeRemaining());

                if(session.getMultiplier().isUpdated()) {
                    updateMultiplier(session.getMultiplier().getValue());
                }
            }
        });

        Timeline tl = new Timeline(keyFrame);
        tl.setCycleCount(Timeline.INDEFINITE);
        return tl;
    }

    // UI helper

    private void updateTimerLabel(int totalSeconds) {
        int hours   = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String text = hours > 0
            ? String.format("%02d:%02d", hours, minutes)
            : String.format("%02d:%02d", minutes, seconds);

        timerLabel.setText(text);
    }

    private void updateMultiplier(double val) {
        multiplierLabel.setText(String.format("%.1fx", val));
    }
}