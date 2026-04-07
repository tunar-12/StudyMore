package StudyMore.controllers;

import StudyMore.Main;
import StudyMore.db.DatabaseManager;
import StudyMore.models.Settings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML private CheckBox lockInModeCheck;
    @FXML private CheckBox mascotCheck;

    @FXML private TextField shortBreakField;
    @FXML private TextField longBreakField;

    @FXML private Label unsavedLabel;

    private Settings settings;
    private DatabaseManager db;
    private long currentUserId = 1L; // replace with actual logged in user id later

    @FXML
    public void initialize() {
        db = new DatabaseManager();
        settings = Main.settings;
        loadSettings();
    }

    private void loadSettings() {
        lockInModeCheck.setSelected(settings.isLockInMode());
        mascotCheck.setSelected(settings.isMascotVisible());
        shortBreakField.setText(String.valueOf(settings.getShortBreak()));
        longBreakField.setText(String.valueOf(settings.getLongBreak()));
        unsavedLabel.setVisible(false);
    }

    @FXML
    private void onSettingChanged() {
        unsavedLabel.setVisible(true);
    }

    @FXML
    private void saveSettings() {
        settings.setDarkMode(true);
        settings.setLockInMode(lockInModeCheck.isSelected());
        settings.setMascotVisible(mascotCheck.isSelected());
        settings.setStartSound(true);
        settings.setBreakAlert(true);
        settings.setPopups(true);

        try {
            settings.setStudyTime(0);
            settings.setShortBreak(Integer.parseInt(shortBreakField.getText()));
            settings.setLongBreak(Integer.parseInt(longBreakField.getText()));
            settings.setLongBreakAfter(0);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number input in settings.");
            return;
        }

        db.saveSettings(currentUserId, settings);
        applySettingsGlobally(); 
        unsavedLabel.setVisible(false);
        System.out.println("Settings saved successfully.");
    }

    @FXML
    private void resetDefaults() {
        settings.resetDefaults();
        db.saveSettings(currentUserId, settings);
        applySettingsGlobally(); 
        loadSettings();
    }

    private void applySettingsGlobally() {
        javafx.scene.Scene scene = Main.primarStageStatic.getScene();
        if (settings.isDarkMode()) {
            scene.getRoot().getStyleClass().add("light-mode");
        } else {
            scene.getRoot().getStyleClass().remove("light-mode");
        }
    }
    
    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        System.out.println("Initiating logout...");

        Main.stopSyncLoop();

        if (Main.mngr != null) {
            Main.mngr.wipeAndRebuildDatabase();
        }

        Main.user = null;

        System.out.println("Exiting application...");
        javafx.application.Platform.exit();
        System.exit(0);
    }
}