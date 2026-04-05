package StudyMore.controllers;

import StudyMore.db.DatabaseManager;
import StudyMore.models.Settings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML private CheckBox darkModeCheck;
    @FXML private CheckBox lockInModeCheck;
    @FXML private CheckBox mascotCheck;
    @FXML private CheckBox startSoundCheck;
    @FXML private CheckBox breakAlertCheck;
    @FXML private CheckBox popupsCheck;

    @FXML private TextField studyTimeField;
    @FXML private TextField shortBreakField;
    @FXML private TextField longBreakField;
    @FXML private TextField longBreakAfterField;

    @FXML private Label unsavedLabel;

    private Settings settings;
    private DatabaseManager db;
    private long currentUserId = 1L; // replace with actual logged in user id later

    @FXML
    public void initialize() {
        db = new DatabaseManager();
        settings = db.getSettings(currentUserId);
        loadSettings();
    }

    private void loadSettings() {
        darkModeCheck.setSelected(settings.isDarkMode());
        lockInModeCheck.setSelected(settings.isLockInMode());
        mascotCheck.setSelected(settings.isMascotVisible());
        startSoundCheck.setSelected(settings.isStartSound());
        breakAlertCheck.setSelected(settings.isBreakAlert());
        popupsCheck.setSelected(settings.isPopups());
        studyTimeField.setText(String.valueOf(settings.getStudyTime()));
        shortBreakField.setText(String.valueOf(settings.getShortBreak()));
        longBreakField.setText(String.valueOf(settings.getLongBreak()));
        longBreakAfterField.setText(String.valueOf(settings.getLongBreakAfter()));
        unsavedLabel.setVisible(false);
    }

    @FXML
    private void onSettingChanged() {
        unsavedLabel.setVisible(true);
    }

    @FXML
    private void saveSettings() {
        settings.setDarkMode(darkModeCheck.isSelected());
        settings.setLockInMode(lockInModeCheck.isSelected());
        settings.setMascotVisible(mascotCheck.isSelected());
        settings.setStartSound(startSoundCheck.isSelected());
        settings.setBreakAlert(breakAlertCheck.isSelected());
        settings.setPopups(popupsCheck.isSelected());

        try {
            settings.setStudyTime(Integer.parseInt(studyTimeField.getText()));
            settings.setShortBreak(Integer.parseInt(shortBreakField.getText()));
            settings.setLongBreak(Integer.parseInt(longBreakField.getText()));
            settings.setLongBreakAfter(Integer.parseInt(longBreakAfterField.getText()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid number input in settings.");
            return;
        }

        db.saveSettings(currentUserId, settings);
        unsavedLabel.setVisible(false);
        System.out.println("Settings saved successfully.");
    }

    @FXML
    private void resetDefaults() {
        settings.resetDefaults();
        db.saveSettings(currentUserId, settings);
        loadSettings();
    }
}