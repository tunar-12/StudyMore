package StudyMore.controllers;

import StudyMore.Main;
import StudyMore.models.Cosmetic;
import StudyMore.models.CosmeticType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.Parent;

public class Controller {
    public static Controller instance;

    @FXML 
    private StackPane contentArea;

    @FXML 
    private BorderPane mainLayout;

    @FXML 
    private Button btnStudy;
    @FXML 
    private Button btnProfile;
    @FXML 
    private Button btnFriends;
    @FXML 
    private Button btnInventory;
    @FXML 
    private Button btnAchievements;
    @FXML 
    private Button btnSettings;
    @FXML
    private Button btnTasks;
    @FXML
    private Button btnShop;

    private final String ACTIVE_STYLE = "-fx-background-color: white; -fx-text-fill: black; -fx-padding: 12 24;";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #a3a3a3; -fx-padding: 12 24;";

    public void initialize() {
        instance = this; 
        refreshBackground(); 
        handleStudy(); 
    }

    public void refreshBackground() {
        Cosmetic equippedBg = Main.user.getInventory().getEquipped(CosmeticType.BACKGROUND);
        mainLayout.setStyle("-fx-background-image: url('/StudyMore/" + equippedBg.getImagePath() + "'); -fx-background-size: cover; -fx-background-position: center; -fx-background-repeat: no-repeat;");  
    }

    // helper method to reset all buttons and highlight the active one
    private void setActiveTab(Button activeButton) {
        // Reset all buttons to inactive
        btnStudy.setStyle(INACTIVE_STYLE);
        btnProfile.setStyle(INACTIVE_STYLE);
        btnFriends.setStyle(INACTIVE_STYLE);
        btnInventory.setStyle(INACTIVE_STYLE);
        btnAchievements.setStyle(INACTIVE_STYLE);
        btnSettings.setStyle(INACTIVE_STYLE);
        btnTasks.setStyle(INACTIVE_STYLE);
        btnShop.setStyle(INACTIVE_STYLE);

        // Highlight the clicked button
        if (activeButton != null) {
            activeButton.setStyle(ACTIVE_STYLE);
        }
    }

    @FXML
    private void handleStudy() {
        loadFXML("../fxml/Study.fxml");
        setActiveTab(btnStudy);
    }

    @FXML
    private void handleProfile() {
        loadFXML("../fxml/Profile.fxml");
        setActiveTab(btnProfile);
    }

    @FXML
    private void handleFriends() {
        loadFXML("../fxml/Friends.fxml");
        setActiveTab(btnFriends);
    }

    @FXML
    private void handleInventory() {
        loadFXML("../fxml/Inventory.fxml");
        setActiveTab(btnInventory);
    }

    @FXML
    private void handleAchievements() {
        loadFXML("../fxml/Achievements.fxml");
        setActiveTab(btnAchievements);
    }

    @FXML
    private void handleTasks() {
        loadFXML("../fxml/Tasks.fxml");
        setActiveTab(btnTasks);
    }

    @FXML
    private void handleShop() {
        loadFXML("../fxml/ItemShop.fxml");
        setActiveTab(btnShop);
    }

    @FXML
    private void handleSettings() {
        loadFXML("../fxml/Settings.fxml");
        setActiveTab(btnSettings);
    }

    private void loadFXML(String fileName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fileName));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}