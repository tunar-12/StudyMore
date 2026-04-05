package StudyMore.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import StudyMore.Main;
import StudyMore.models.CosmeticType;
import StudyMore.models.StudySession;
import StudyMore.models.User;

public class ProfileController {
    @FXML
    private HBox heatmapGrid;
    @FXML
    private Label username;
    @FXML
    private Label title;
    @FXML
    private Label multiplier;
    @FXML
    private Label coin;
    @FXML
    private Label rating;
    @FXML
    private Label rank;
    @FXML
    private Label totalStudyTime;
    @FXML
    private Label weeklyStudyTime;
    @FXML
    private Label studyStreak;
    
    //TODO: profile, banner, medal

    private static final String[] HEATMAP_COLORS = {
        "-fx-background-color: black; -fx-border-color: #262626; -fx-border-width: 1;", // Level 0: Empty
        "-fx-background-color: #262626;", // Level 1: Low
        "-fx-background-color: #525252;", // Level 2: Medium
        "-fx-background-color: #a3a3a3;", // Level 3: High
        "-fx-background-color: white;"    // Level 4: Max
    };

    public void initialize() {
        username.setText(Main.user.getUsername());
        multiplier.setText(Main.mngr.getTodaysStudySession(Main.user).getMultiplier().getValue() + "X MULT");
        coin.setText("" + Main.user.getCoinBalance());
        rating.setText("" + Main.user.getRating());
        rank.setText(Main.user.getRank().toString());
        studyStreak.setText(Main.user.getStudyStreak() + "");
        title.setText(Main.user.getInventory().getEquipped(CosmeticType.TITLE).getName().toUpperCase());
        calculateStudyTimes();
  
        loadAndDisplayHeatmap(Main.user);
    }

    public void calculateStudyTimes() {
        int totalTime = 0;
        int weeklyTime = 0;

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        for (StudySession s : Main.mngr.getStudySessions(Main.user)) {
            totalTime += s.getDuration();
            
            LocalDate sessionDate = s.getStartTime().toLocalDate();

            // Check if the session falls within this week
            if (!sessionDate.isBefore(startOfWeek) && !sessionDate.isAfter(endOfWeek)) {
                weeklyTime += s.getDuration();
            }
        }

        totalStudyTime.setText((totalTime / 3600) + "");
        weeklyStudyTime.setText((weeklyTime / 3600) + "");

    }

    public void loadAndDisplayHeatmap(User user) {
        ArrayList<StudySession> sessions = Main.mngr.getStudySessions(user);
        Map<LocalDate, Integer> dailyDurations = new HashMap<>();

        for (StudySession session : sessions) {
            if (session.getStartTime() != null) {
                LocalDate date = session.getStartTime().toLocalDate();
                dailyDurations.put(date, dailyDurations.getOrDefault(date, 0) + session.getDuration());
            }
        }

        int maxVal = dailyDurations.values().stream().max(Integer::compareTo).orElse(0);

        heatmapGrid.getChildren().clear();
        heatmapGrid.setSpacing(6.0); 

        LocalDate today = LocalDate.now();

        int todayRow = today.getDayOfWeek().getValue() % 7; 

        LocalDate gridStartDate = today.minusDays(todayRow).minusWeeks(52);

        for (int col = 0; col < 53; col++) {
            VBox weekColumn = new VBox(6.0);
            
            for (int row = 0; row < 7; row++) {
  
                LocalDate cellDate = gridStartDate.plusDays((col * 7) + row);
                
                Region cell = new Region();
                cell.setPrefSize(12.0, 12.0);
                cell.setMinSize(12.0, 12.0);
                cell.setMaxSize(12.0, 12.0);

                if (cellDate.isAfter(today) || cellDate.isBefore(today.minusDays(364))) {
                    cell.setStyle("-fx-background-color: transparent;");
                } else {
                    int duration = dailyDurations.getOrDefault(cellDate, 0);
                    int intensity = calculateIntensity(duration, maxVal);
                    cell.setStyle(HEATMAP_COLORS[intensity]);
                }
                
                weekColumn.getChildren().add(cell);
            }
            heatmapGrid.getChildren().add(weekColumn);
        }
    }

    private int calculateIntensity(int value, int maxVal) {
        // If the value is 0 or the whole array is 0, return the base color
        if (value == 0 || maxVal == 0) return 0;
        
        double ratio = (double) value / maxVal;
        int level = (int) Math.ceil(ratio * 4.0);
        
        // safety bounds 
        return Math.min(Math.max(level, 1), 4);
    }
}
