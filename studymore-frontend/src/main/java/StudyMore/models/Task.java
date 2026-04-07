package StudyMore.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import StudyMore.Main;
import StudyMore.controllers.AchievementsController;

public class Task {
    // Standard SQL format: "2026-04-05 22:49:53"
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private long taskId;
    private String title;
    private String content;
    private boolean completed;
    private LocalDateTime createdAt;

    private boolean srsEnabled;
    private SRSMetadata srsData;
    private LocalDate nextRecallDate;
    
    public Task(String title, String content, boolean srsEnabled){
        this(title, content, srsEnabled, null);
    }

    public Task(String title, String content, boolean srsEnabled, ReviewIntensity intensity){
        this.taskId = SnowflakeIDGenerator.generate();
        this.title = title;
        this.content = content;
        this.srsEnabled = srsEnabled;
        if (srsEnabled){
            if (intensity != null){
                this.srsData = new SRSMetadata(intensity);
            } else {
                this.srsData = new SRSMetadata(ReviewIntensity.STANDARD);
            }
        }
        this.completed = false;
        this.createdAt = LocalDateTime.now();
    }

    // sets the isComplete variable to be true, also cant edit a completed task
    public void complete(){ // basically setter
        if (!srsEnabled){
            this.completed = true;
            return;
        }

        // ask for the difficulty for the srsenabled tasks
        // invoke the methods about srs scheduling
        this.completed = true;
        AchievementsController.updateProgress(Main.user.getUserId(), "TASK_BASED", 1);
    }

    // updates the task according to the parameters (only incomplete tasks can be modified)
    public boolean updateContent(String title, String content, boolean srsEnabled){
        if (completed) return false;
        this.title = title;
        this.content = content;
        this.srsEnabled = srsEnabled;
        return true;
    }

    /**
     * Calculates exactly what state the task should be in right now.
     * Returns 0 = ACTIVE (Due), 1 = INACTIVE (Waiting), 2 = COMPLETED (Done today or permanently)
     */
    public int getCurrentState() {
        // Scenario: Normal task
        if (!isSrsEnabled()) {
            return isCompleted() ? 2 : 0;
        }

        LocalDate today = LocalDate.now();

        // Scenario: Due today or due date was in the past
        if (this.nextRecallDate == null || !this.nextRecallDate.isAfter(today)) {
            this.completed = false; // Reset the flag so it's "Incomplete" again
            return 0; // ACTIVE (Bright & Clickable)
        }

        // Scenario: Recall date is in the future
        if (this.nextRecallDate.isAfter(today)) {            
            if (isCompleted()) { // Finished the task recently, will display it as completed for a while
                return 2; // COMPLETED (Vibrant Green)
            } else { // Finished the task earlier, waiting for the next recall date
                return 1; // INACTIVE (Gray/Locked)
            }
        
        } else {
            // Fail-safe, should not trigger unless an error occurs
            return 0;
        }
    }

    public String getDaysUntilRecall() {
        if (nextRecallDate == null || nextRecallDate.isBefore(LocalDate.now()) || nextRecallDate.isEqual(LocalDate.now())) {
            return "DUE NOW";
        }
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), nextRecallDate);
        return daysBetween + (daysBetween == 1 ? " DAY" : " DAYS");
    }

    public String getTitle(){
        return this.title;
    }

    public String getContent(){
        return this.content;
    }

    public long getID(){
        return this.taskId;
    }

    public String getCreatedAtAsString(){
        if (this.createdAt == null) {
            return LocalDateTime.now().format(TIMESTAMP_FORMAT);
        }
        return this.createdAt.format(TIMESTAMP_FORMAT);
    }

    public boolean isCompleted(){
        return this.completed;
    }

    public LocalDateTime getCreationTime(){
        return this.createdAt;
    }

    public boolean isSrsEnabled(){
        return this.srsEnabled;
    }

    public SRSMetadata getSrsData(){
        return this.srsData;
    }

    public LocalDate getNextRecallDate() {
        return this.nextRecallDate;
    }

    public String getNextRecallDateAsString() {
        if (this.nextRecallDate == null) return null;
        return this.nextRecallDate.format(DATE_FORMAT);
    }   

    public void setTitle(String title){
        this.title = title;
    }

    public void setContent(String content){
        this.content = content;
    }    

    public void setCreatedAtFromString(String timestamp) {
        this.createdAt = LocalDateTime.parse(timestamp, TIMESTAMP_FORMAT);
    }

    public void setTaskId(long id) {
        this.taskId = id;
    }

    public void setCompleted(boolean isCompleted) {
        this.completed = isCompleted;
    }

    public void setNextRecallDate(LocalDate date) {
        this.nextRecallDate = date;
    }

    public void setNextRecallDateFromString(String dateStr) {
        if (dateStr != null && !dateStr.isEmpty() && !dateStr.equals("null")) {
            // Handles both formats (e.g. turns "2026-04-08 03:00:00" into "2026-04-08")
            String justTheDate = dateStr.split(" ")[0]; 
            
            this.nextRecallDate = LocalDate.parse(justTheDate, DATE_FORMAT);
        }
    }


}
