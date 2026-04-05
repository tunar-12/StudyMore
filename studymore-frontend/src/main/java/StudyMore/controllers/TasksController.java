/**
 * add a time check to make sure the tasks are valid
 */

package StudyMore.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import StudyMore.Main;
import StudyMore.models.ReviewIntensity;
import StudyMore.models.Task;

public class TasksController {

    private Task currentlyEditingTask = null;
    private List<Task> tempDatabase = new ArrayList<>();
    @FXML private StackPane pageRoot;
    @FXML private FlowPane normalTasksContainer;
    @FXML private FlowPane srsTasksContainer;

    // This flag is necessary since we are calling initialize more than once, both at start and after closing task overlay.
    private boolean isInitialized = false;

    public void initialize() {
        if (!isInitialized) {
            tempDatabase.add(new Task("Buy Groceries", "Milk, Eggs, Bread", false));
            tempDatabase.add(new Task("Learn JavaFX", "Review lambda capture and UI rendering", true, ReviewIntensity.INTENSE));
            refreshTaskDisplay();
            isInitialized = true;
        }
    }

    @FXML
    private void handleCreateTask() {
        this.currentlyEditingTask = null;
        showOverlay("../fxml/CreateTaskOverlay.fxml");
    }

    private void showOverlay(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setController(this); 
            Parent overlay = loader.load();
            pageRoot.getChildren().add(overlay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveTask() {
        Node overlay = pageRoot.getChildren().get(pageRoot.getChildren().size() - 1);

        TextField titleField = (TextField) overlay.lookup("#titleInput");
        TextArea contentArea = (TextArea) overlay.lookup("#contentInput");
        CheckBox srsBox = (CheckBox) overlay.lookup("#srsToggle");
        ComboBox<String> intensityDropdown = (ComboBox<String>) overlay.lookup("#intensityDropdown");

        if (titleField == null) return;

        String title = titleField.getText();
        String content = (contentArea != null) ? contentArea.getText() : "";
        boolean isSrs = (srsBox != null) && srsBox.isSelected();
        String intensityString = intensityDropdown.getValue();

        if (title == null || title.isEmpty()) return;

        if (currentlyEditingTask != null){
            // TODO: update the existing task in database
            currentlyEditingTask.setTitle(title);
            currentlyEditingTask.setContent(content);
        } else {
            Task taskToAdd;
            if (isSrs){
                ReviewIntensity intensity;
                switch(intensityString){
                    case "Intense": intensity = ReviewIntensity.INTENSE; break;
                    case "Standard": intensity = ReviewIntensity.STANDARD; break;
                    case "Relaxed": intensity = ReviewIntensity.RELAXED; break;
                    default: intensity = ReviewIntensity.STANDARD; break;
                }            
                taskToAdd = new Task(title, content, isSrs, intensity);
            } else {
                taskToAdd = new Task(title, content, isSrs);
            }
            tempDatabase.add(taskToAdd);
        }

        refreshTaskDisplay();
        closeOverlay();
    }

    @FXML
    private void openTaskConfig(Task taskToEdit) {
        this.currentlyEditingTask = taskToEdit; 

        try {
            // We are using the same overlay for the task creation (via overriding)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/CreateTaskOverlay.fxml"));
            loader.setController(this); 
            Parent overlay = loader.load();

            // Existing input fields to modify
            TextField titleField = (TextField) loader.getNamespace().get("titleInput");
            TextArea contentArea = (TextArea) loader.getNamespace().get("contentInput");
            CheckBox srsBox = (CheckBox) loader.getNamespace().get("srsToggle");
            ComboBox<String> intensityDropdown = (ComboBox<String>) loader.getNamespace().get("intensityDropdown");
            
            // Elements needed to change into edit mode
            Label overlayTitle = (Label) loader.getNamespace().get("overlayTitle");
            Button deleteTaskBtn = (Button) loader.getNamespace().get("deleteTaskBtn");
            Button saveTaskBtn = (Button) loader.getNamespace().get("saveTaskBtn");

            if (overlayTitle != null) overlayTitle.setText("EDIT TASK");
            if (saveTaskBtn != null) saveTaskBtn.setText("UPDATE");
            if (deleteTaskBtn != null) {
                deleteTaskBtn.setVisible(true);
                deleteTaskBtn.setManaged(true);
            }

            // Pre-fill existing data
            if (titleField != null) titleField.setText(taskToEdit.getTitle());
            if (contentArea != null) contentArea.setText(taskToEdit.getContent());
            if (srsBox != null){ 
                srsBox.setSelected(taskToEdit.isSrsEnabled());
                srsBox.setDisable(true);
            }
            if (intensityDropdown != null) {
                if (taskToEdit.isSrsEnabled() && taskToEdit.getSrsData() != null) {
                    intensityDropdown.setValue(taskToEdit.getSrsData().getIntensity().toString());
                }
                
                // Unbinding is necessary or the program would crash in the case of:
                // srsToggle is on, the program wants to make the dropdown available
                intensityDropdown.disableProperty().unbind();
                intensityDropdown.setDisable(true);
            }
            
            pageRoot.getChildren().add(overlay);
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

    @FXML
    private void markTaskAsCompleted(Task task) {
        if (!task.isCompleted()) {
            promptConfirmation(() -> {
                task.complete();
                // TODO: Update db
                AchievementsController.updateProgress(Main.user.getUserId(), "TASK_BASED", 1);
                refreshTaskDisplay();
            });
        }
    }

    @FXML
    private void closeOverlay() {
        if (pageRoot.getChildren().size() > 1) {
            pageRoot.getChildren().remove(pageRoot.getChildren().size() - 1);
        }
    }

    private void refreshTaskDisplay() {
        normalTasksContainer.getChildren().clear();
        srsTasksContainer.getChildren().clear();

        for (Task task : tempDatabase) {
            addTaskToGrid(task, "1 DAY"); // TODO: update the second parameter after testing
        }
    }

    @FXML
    private void deleteTask(){
        if (currentlyEditingTask != null) {
            promptConfirmation(() -> {
                tempDatabase.remove(currentlyEditingTask);
                // TODO: call delete task (remove task from actual db and remove the pointers)               
                currentlyEditingTask = null;        
                closeOverlay();
                refreshTaskDisplay();
            });
        }
    }

    private void promptConfirmation(Runnable onConfirm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/ConfirmationOverlay.fxml"));
            loader.setController(this);
            Parent overlay = loader.load();

            Button cancelBtn = (Button) loader.getNamespace().get("confirmCancelButton");
            Button proceedBtn = (Button) loader.getNamespace().get("confirmProceedButton");

            if (cancelBtn != null) {
                cancelBtn.setOnAction(e -> closeOverlay());
            }
            if (proceedBtn != null) {
                proceedBtn.setOnAction(e -> {
                    closeOverlay(); 
                    onConfirm.run(); 
                });
            }

            pageRoot.getChildren().add(overlay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTaskToGrid(Task task, String recallTime) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/TaskCard.fxml"));
            VBox card = loader.load();

            Label titleLabel = (Label) loader.getNamespace().get("taskTitle");
            Label contentLabel = (Label) loader.getNamespace().get("taskContent");
            VBox srsBadge = (VBox) loader.getNamespace().get("srsBadgeContainer");
            Label recallLabel = (Label) loader.getNamespace().get("recallLabel");
            Label intensityLabel = (Label) loader.getNamespace().get("intensityLabel");

            Button configBtn = (Button) loader.getNamespace().get("configBtn");
            Button completeBtn = (Button) loader.getNamespace().get("completeBtn");

            if (titleLabel != null) titleLabel.setText(task.getTitle().toUpperCase());
            if (contentLabel != null) contentLabel.setText(task.getContent());

            if (task.isCompleted()) {
                // COMPLETED TASK: grayed out task card + vibrant complete button
                if (titleLabel != null) titleLabel.setOpacity(0.4);
                if (contentLabel != null) contentLabel.setOpacity(0.4);
                if (srsBadge != null) srsBadge.setOpacity(0.4);
                card.getChildren().get(2).setOpacity(0.1);

                if (configBtn != null) {
                    configBtn.setOpacity(0.4);
                    configBtn.setDisable(true);
                }

                if (completeBtn != null) {
                    completeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #00ff08; -fx-text-fill: #00ff08; -fx-border-radius: 5;");
                    completeBtn.setDisable(true); 
                }
            } else {
                // ACTIVE TASK: brighter task card + slightly grayed out complete button
                if (titleLabel != null) titleLabel.setOpacity(1.0);
                if (contentLabel != null) contentLabel.setOpacity(1.0);
                if (srsBadge != null) srsBadge.setOpacity(1.0);
                card.getChildren().get(2).setOpacity(0.5);

                if (configBtn != null) {
                    configBtn.setOpacity(1.0);
                    configBtn.setOnAction(event -> openTaskConfig(task));
                }

                if (completeBtn != null) {
                    completeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #404040; -fx-text-fill: #737373; -fx-border-radius: 5; -fx-cursor: hand;");
                    completeBtn.setOnAction(event -> markTaskAsCompleted(task));
                }
            }

            if (task.isSrsEnabled()) {
                if (srsBadge != null) {
                    srsBadge.setManaged(true);
                    srsBadge.setVisible(true);
                }
                if (recallLabel != null) recallLabel.setText(recallTime);
                if (intensityLabel != null) intensityLabel.setText(task.getSrsData().getIntensity().name());
                srsTasksContainer.getChildren().add(card);
            } else {
                if (srsBadge != null) {
                    srsBadge.setManaged(false);
                    srsBadge.setVisible(false);
                }
                normalTasksContainer.getChildren().add(card);
            }
        } catch (IOException e) {
            System.err.println("Error loading TaskCard.fxml: " + e.getMessage());
        }
    }
}