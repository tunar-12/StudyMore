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
import java.util.function.Consumer;

import StudyMore.Main;
import StudyMore.models.ReviewIntensity;
import StudyMore.models.SRSHistoryEntry;
import StudyMore.models.SRSScheduler;
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
            tempDatabase.clear(); // TODO: remove the placeholders after testing
            if (Main.user != null && Main.user.getTasks() != null) {
                tempDatabase.addAll(Main.user.getTasks());
            }
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
            currentlyEditingTask.setTitle(title);
            currentlyEditingTask.setContent(content);
            Main.mngr.updateTask(currentlyEditingTask);
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

            if (Main.user != null) {
                Main.user.getTasks().add(taskToAdd);
                Main.mngr.addTask(Main.user.getUserId(), taskToAdd);
            }            
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
        // NORMAL TASK LOGIC
        if (!task.isSrsEnabled()) {
            promptConfirmation(() -> {
                task.complete();
                Main.mngr.updateTask(task);
                AchievementsController.updateProgress(Main.user.getUserId(), "TASK_BASED", 1);
                refreshTaskDisplay();
            });
            return;
        }

        // SRS TASK LOGIC
        promptQualityScore(task, (qualityScore) -> {
            
            SRSScheduler.processReview(task, qualityScore); 
            
            // Completed recently, will be INACTIVE soon
            task.setCompleted(true);
        
            Main.mngr.updateTask(task);
            
            SRSHistoryEntry latestLog = task.getSrsData().getLatestHistoryEntry();
            if (latestLog != null) {
                Main.mngr.insertTaskHistory(task.getID(), latestLog);
            }

            AchievementsController.updateProgress(Main.user.getUserId(), "TASK_BASED", 1);
            refreshTaskDisplay();
        });        


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

        // 0-ACTIVE comes first, 1-INACTIVE comes second, 2-COMPLETED comes third
        tempDatabase.sort((t1, t2) -> Integer.compare(t1.getCurrentState(), t2.getCurrentState()));

        for (Task task : tempDatabase) {
            String recallTime = task.getDaysUntilRecall();
            addTaskToGrid(task, recallTime);
        }
    }

    @FXML
    private void deleteTask(){
        if (currentlyEditingTask != null) {
            promptConfirmation(() -> {
                tempDatabase.remove(currentlyEditingTask);
                Main.mngr.deleteTask(currentlyEditingTask);
                if (Main.user != null) {
                    Main.user.getTasks().remove(currentlyEditingTask);
                }
                // TODO: call delete task (remove the pointers)
                // TODO: implement task deletion in task class               
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

    private void promptQualityScore(Task task, Consumer<Integer> onScoreSelected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/QualityScoreOverlay.fxml"));
            loader.setController(this);
            Parent overlay = loader.load();

            Button blackoutBtn = (Button) loader.getNamespace().get("blackoutBtn");
            Button hardBtn = (Button) loader.getNamespace().get("hardBtn");
            Button mediumBtn = (Button) loader.getNamespace().get("mediumBtn");
            Button goodBtn = (Button) loader.getNamespace().get("goodBtn");
            Button easyBtn = (Button) loader.getNamespace().get("easyBtn");

            // QUALITYSCORE LOGIC
            if (blackoutBtn != null) blackoutBtn.setOnAction(e -> confirmAndSubmit(1, onScoreSelected));
            if (hardBtn != null)    hardBtn.setOnAction(e -> confirmAndSubmit(2, onScoreSelected));
            if (mediumBtn != null)  mediumBtn.setOnAction(e -> confirmAndSubmit(3, onScoreSelected));
            if (goodBtn != null)    goodBtn.setOnAction(e -> confirmAndSubmit(4, onScoreSelected));
            if (easyBtn != null)    easyBtn.setOnAction(e -> confirmAndSubmit(5, onScoreSelected));

            pageRoot.getChildren().add(overlay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // A helper method to handle the nested confirmation logic for qualityScore
    private void confirmAndSubmit(int score, Consumer<Integer> onScoreSelected) {
        // Runs if user decides to proceed with the selected qualityScore
        promptConfirmation(() -> {
            closeOverlay(); 
            onScoreSelected.accept(score);
        });
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

            int state = task.getCurrentState();
            switch (state) {
                case 2: // COMPLETED (Grayed out + vibrant completed button)
                    if (titleLabel != null) titleLabel.setOpacity(0.4);
                    if (contentLabel != null) contentLabel.setOpacity(0.4);
                    if (srsBadge != null) srsBadge.setOpacity(0.4);
                    card.getChildren().get(2).setOpacity(0.1);

                    if (configBtn != null) {
                        configBtn.setOpacity(0.4);
                        configBtn.setDisable(true);
                    }
                    if (completeBtn != null) {
                        completeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #4CAF50; -fx-text-fill: #4CAF50; -fx-border-radius: 5;");
                        completeBtn.setDisable(true);
                    }
                    break;

                case 1: // INACTIVE (SRS waiting for future date) - (Grayed out + locked out)
                    if (titleLabel != null) titleLabel.setOpacity(0.3);
                    if (contentLabel != null) contentLabel.setOpacity(0.3);
                    if (srsBadge != null) srsBadge.setOpacity(0.3);
                    card.getChildren().get(2).setOpacity(0.1);

                    if (configBtn != null) {
                        configBtn.setOpacity(1.0);
                        configBtn.setOnAction(event -> openTaskConfig(task));
                    }
                    if (completeBtn != null) {
                        completeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #262626; -fx-text-fill: #404040; -fx-border-radius: 5;");
                        completeBtn.setDisable(true); 
                    }
                    break;

                case 0: // ACTIVE (Due now) - (Bright task card + normal completed button)
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
                        completeBtn.setDisable(false);
                    }
                    break;
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