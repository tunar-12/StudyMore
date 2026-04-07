package StudyMore.controllers;

import StudyMore.Main;
import StudyMore.models.CosmeticType;
import StudyMore.models.SessionState;
import StudyMore.models.StudySession;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    @FXML private VBox studyTasksContainer;
    @FXML private Label userRankLabel;
    @FXML private Label catQuoteLabel;

    private int LONG_BREAK_SECONDS  = 1200; // 20 min
    private int SHORT_BREAK_SECONDS = 600;  // 10 min
    private int STUDY_SECONDS = 1500; 

    private static StudySession session;
    private static Timeline studyTimeline;
    private static Timeline breakTimeline;
    private static StudyController currentInstance;

public void initialize() {
        currentInstance = this;

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
        streakLabel.setText(Main.user.getStudyStreak() + " Days");
        StudyMore.models.MascotCat mascot = new StudyMore.models.MascotCat(Main.user.getUserId());
        catQuoteLabel.setText(mascot.getRandomQuote());

        // Session is created only if it doesnt already exist
        if (session == null) {
            session = new StudySession(Main.user);
            studyTimeline = buildStudyTimeline();
            breakTimeline = buildBreakTimeline();
        }

        // Sync the ui with the background state
        if (session.getState() == SessionState.STUDYING) {
            timerControlButton.setText("STOP");
            updateTimerLabel(session.getDuration());
        } else if (session.getState() == SessionState.ON_BREAK) {
            timerControlButton.setText("START");
            updateTimerLabel(session.getBreakTimeRemaining());
        } else {
            timerControlButton.setText("START");
            updateTimerLabel(session.getDuration());
        }

        updateMultiplier(session.getMultiplier().getValue());
        loadGroupLeaderboard();
        loadDueTasks();
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

            if (isMe) {
                userRankLabel.setText((i + 1) + ". You");
            }
            
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

    private static void stopBreakTimeline() {
        if (breakTimeline != null) {
            breakTimeline.stop();
        }
    }

    // Timeline builders
    private static Timeline buildStudyTimeline() {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), e -> {
            if (session != null) {
                session.incrementDuration();
                
                // Only update UI if the Study page is currently open
                if (currentInstance != null) {
                    currentInstance.updateTimerLabel(session.getDuration());
                    
                    if(session.getMultiplier().isUpdated()) {
                        currentInstance.updateMultiplier(session.getMultiplier().getValue());
                    }
                }
            }
        });

        Timeline tl = new Timeline(keyFrame);
        tl.setCycleCount(Timeline.INDEFINITE);
        return tl;
    }

    private static Timeline buildBreakTimeline() {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), e -> {
            if (session != null) {
                session.tickBreak();

                if (session.isBreakOver()) {
                    if (breakTimeline != null) breakTimeline.stop();
                    
                    // Auto-start study in the background
                    session.start();
                    if (studyTimeline != null) studyTimeline.play();
                    
                    // Update UI button if they are looking at it
                    if (currentInstance != null) {
                        currentInstance.timerControlButton.setText("STOP");
                    }
                } else {
                    if (currentInstance != null) {
                        currentInstance.updateTimerLabel(session.getBreakTimeRemaining());
                        if(session.getMultiplier().isUpdated()) {
                            currentInstance.updateMultiplier(session.getMultiplier().getValue());
                        }
                    }
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

    private void loadDueTasks() {
        studyTasksContainer.getChildren().clear();

        if (Main.user == null || Main.user.getTasks() == null) return;

        for (StudyMore.models.Task task : Main.user.getTasks()) {
            // State 0 = active/due today for both normal and SRS tasks
            if (task.getCurrentState() == 0) {
                addTaskToGrid(task);
            }
        }
    }

    private void addTaskToGrid(StudyMore.models.Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/TaskCard.fxml"));
            VBox card = loader.load();

            Label titleLabel = (Label) loader.getNamespace().get("taskTitle");
            Label contentLabel = (Label) loader.getNamespace().get("taskContent");
            VBox srsBadge = (VBox) loader.getNamespace().get("srsBadgeContainer");
            Label recallLabel = (Label) loader.getNamespace().get("recallLabel");

            // STRICT READ-ONLY MODE (user is not supposed to interact with the task from study page)
            Button configBtn = (Button) loader.getNamespace().get("configBtn");
            Button completeBtn = (Button) loader.getNamespace().get("completeBtn");
            
            if (configBtn != null) {
                configBtn.setVisible(false);
                configBtn.setManaged(false); // Collapses the space completely
            }
            if (completeBtn != null) {
                completeBtn.setVisible(false);
                completeBtn.setManaged(false); // Collapses the space completely
            }

            // Disables the entire card to kill any hover effects or cursor changes
            card.setFocusTraversable(false);
            card.setStyle(card.getStyle() + " -fx-cursor: default;");

            // Populate Text
            if (titleLabel != null) titleLabel.setText(task.getTitle().toUpperCase());
            if (contentLabel != null) contentLabel.setText(task.getContent());

            // Handle SRS Badge
            if (task.isSrsEnabled()) {
                if (srsBadge != null) {
                    srsBadge.setManaged(true);
                    srsBadge.setVisible(true);
                }
                if (recallLabel != null) recallLabel.setText("DUE NOW"); 
            } else {
                if (srsBadge != null) {
                    srsBadge.setManaged(false);
                    srsBadge.setVisible(false);
                }
            }

            studyTasksContainer.getChildren().add(card);
        } catch (java.io.IOException e) {
            System.err.println("Error loading TaskCard in Study page: " + e.getMessage());
        }
    }
}