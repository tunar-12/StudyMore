package StudyMore.controllers;

import StudyMore.Main;
import StudyMore.models.SessionState;
import StudyMore.models.StudySession;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class StudyController {

    @FXML private Label timerLabel;
    @FXML private Label streakLabel;
    @FXML private Button timerControlButton;
    @FXML private Button longBreakButton;
    @FXML private Button shortBreakButton;
    @FXML private Label multiplierLabel;

    private static final int LONG_BREAK_SECONDS  = 1200; // 20 min
    private static final int SHORT_BREAK_SECONDS = 600;  // 10 min

    private StudySession session;
    private Timeline studyTimeline;
    private Timeline breakTimeline;

    public void initialize() {
        session = new StudySession(Main.user);
        streakLabel.setText(Main.user.getStudyStreak() + " Days");
        updateTimerLabel(session.getDuration());
        studyTimeline = buildStudyTimeline(); 
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