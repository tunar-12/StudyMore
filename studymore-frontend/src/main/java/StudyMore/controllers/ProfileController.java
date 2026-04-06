package StudyMore.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    @FXML 
    private ImageView profileImageView;
    @FXML
    private ImageView bannerImageView;
    @FXML
    private HBox medalsContainer;

    private static final String[] HEATMAP_COLORS = {
        "-fx-background-color: black; -fx-border-color: #262626; -fx-border-width: 1;", // Level 0: Empty
        "-fx-background-color: #262626;", // Level 1: Low
        "-fx-background-color: #525252;", // Level 2: Medium
        "-fx-background-color: #a3a3a3;", // Level 3: High
        "-fx-background-color: white;"    // Level 4: Max
    };

    public void initialize() {
        username.setText(Main.user.getUsername());
        multiplier.setText(String.format("%.1fX MULT", Main.mngr.getTodaysStudySession(Main.user).getMultiplier().getValue()));
        coin.setText("" + Main.user.getCoinBalance());
        rating.setText("" + Main.user.getRating());
        rank.setText(Main.user.getRank().toString());
        studyStreak.setText(Main.user.getStudyStreak() + "");
        title.setText(Main.user.getInventory().getEquipped(CosmeticType.TITLE).getName().toUpperCase());
        int [] times = calculateStudyTimes();
        totalStudyTime.setText(times[0] + "");
        weeklyStudyTime.setText(times[1] + "");
        bannerImageView.setImage(new Image("/StudyMore/" + Main.user.getInventory().getEquipped(CosmeticType.BANNER).getImagePath()));
        profileImageView.setImage(new Image("/StudyMore/" + Main.user.getInventory().getEquipped(CosmeticType.MASCOT_SKIN).getImagePath()));
        addMedalImage("/StudyMore/" + Main.user.getInventory().getEquipped(CosmeticType.MEDAL).getImagePath());
        loadAndDisplayHeatmap(Main.user);
    }

    public static int [] calculateStudyTimes() {
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

        return new int[]{(totalTime / 3600), (weeklyTime / 3600)};
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

    private void addMedalImage(String path) {
        // Create the image container
        ImageView medalImageView = new ImageView();
        
        medalImageView.setFitWidth(24);
        medalImageView.setFitHeight(24);
        medalImageView.setPreserveRatio(true);

        try {
            Image medalIcon = new Image(path);
            medalImageView.setImage(medalIcon);
        } catch (NullPointerException e) {
            System.out.println("Medal image not found! The container will be empty.");
        }

        medalsContainer.getChildren().add(medalImageView);
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
