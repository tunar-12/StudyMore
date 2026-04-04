package StudyMore.controllers;

import StudyMore.Main;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AchievementsController {

    @FXML
    private VBox achievementsContainer;

    public void initialize() {
        //Dummy data to test 
        addAchievement("title", "description", "50", "BackGround", 1, true);
        addAchievement("title", "description", "30", "Banner", 0.7, false);
    }


    public void addAchievement(String title, String description, String progressText, String rewardText, double progressFraction, boolean isCompleted) {
        // Main Card Container
        StackPane card = new StackPane();
        String borderColor = isCompleted ? "white" : "#262626";
        card.setStyle("-fx-background-color: black; -fx-border-color: " + borderColor + ";");
        HBox mainHBox = new HBox(24.0);
        mainHBox.setStyle("-fx-padding: 24;");

        // Right Information Container
        VBox infoVBox = new VBox(16.0);
        HBox.setHgrow(infoVBox, Priority.ALWAYS);

        // Title Spacer and Status 
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

        // Progress and Reward Box
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

        // Progress Bar
        StackPane barContainer = new StackPane();
        barContainer.setAlignment(Pos.CENTER_LEFT);
        barContainer.setPrefHeight(8.0);
        barContainer.setStyle("-fx-background-color: black; -fx-border-color: #262626;");

        Region fillRegion = new Region();
        String fillColor = isCompleted ? "white" : "#737373";
        fillRegion.setStyle("-fx-background-color: " + fillColor + ";");
        
        double clampedFraction = Math.max(0.0, Math.min(1.0, progressFraction));
        fillRegion.maxWidthProperty().bind(barContainer.widthProperty().multiply(clampedFraction));

        barContainer.getChildren().add(fillRegion);

        progressBox.getChildren().addAll(progressHBox, barContainer);

        infoVBox.getChildren().addAll(titleDescBox, vSpacer, progressBox);
        mainHBox.getChildren().add(infoVBox);
        card.getChildren().add(mainHBox);

        // ğut it into ui
        achievementsContainer.getChildren().add(card);
    }
}