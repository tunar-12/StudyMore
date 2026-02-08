package StudyMore.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;

public class Controller {

    @FXML // Main Area to put our content
    private StackPane contentArea;

    public void initialize() {
        handleStudy();
    }

    @FXML
    private void handleStudy() {
        loadFXML("../fxml/Study.fxml");
    }

    @FXML
    private void handleProfile() {
        loadFXML("../fxml/Profile.fxml");
    }

    @FXML
    private void handleFriends() {
        loadFXML("../fxml/Friends.fxml");
    }

    @FXML
    private void handleInventory() {
        loadFXML("../fxml/Inventory.fxml");
    }
    @FXML
    private void handleAchievements() {
        loadFXML("../fxml/Achievements.fxml");
    }

    @FXML
    private void handleSettings() {
        loadFXML("../fxml/Settings.fxml");
    }

    // Helper method to switch the content of the center stage
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