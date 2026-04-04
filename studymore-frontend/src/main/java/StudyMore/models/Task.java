package StudyMore.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import StudyMore.Main;
import StudyMore.controllers.AchievementsController;

public class Task {
    private long taskId;
    private String title;
    private String content;
    private boolean completed;
    private LocalDateTime createdAt;

    private boolean srsEnabled;
    private SRSMetadata srsData;
    private LocalDate nextRecallDate;

    public Task(String title, String content, boolean srsEnabled, ReviewIntensity intensity){
        this.taskId = SnowflakeIDGenerator.generate(); // TODO: temporary id generator
        this.title = title;
        this.content = content;
        this.srsEnabled = srsEnabled;
        if (srsEnabled){
            this.srsData = new SRSMetadata(intensity);
        }
        this.completed = false;
        this.createdAt = LocalDateTime.now();
    }

    // sets the isComplete variable to be true, also cant edit a completed task
    /** 
     *  NOTE-TO-SELF: maybe I could add a confirmation 
     *  when attempting to set isComplete = true in srsEnabled tasks
     *  as it will just kill the processes
     * 
     *  OR maybe this method would act like a way to initialize the next
     *  task recall schedule. So if the user fails to complete the task
     *  on the specified day according to their local time, it goes all the
     *  way back to day1 ???
    */
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

    public void scheduleNextRecall(){
        // SRSScheduler.calc(this);
    }

    // updates the task according to the parameters (only incomplete tasks can be modified)
    public boolean updateContent(String title, String content, boolean srsEnabled){
        if (completed) return false;
        this.title = title;
        this.content = content;
        this.srsEnabled = srsEnabled;
        return true;
    }

    public boolean delete(){
        /**
         * TODO: to actually give an object to garbagecollector, I need to make sure
         * that there is no other way to access the object, so it is unreachable and
         * eligible to be collected as garbage.
         * This means that I would have to manually cut the link between the object
         * and every single variable or list that has access to it.
         * 
         * WHERE TASKS WILL BE KEPT:
         * The instance variables in Task class
         * The tasks List in the User class
         * Some methods will take the Task as parameter in SRSScheduler class
         * 
         * MAYBE implement a trash mechanism to hold the objects that are waiting
         * to be deleted and delete them after a certain period of time
        */
        return true;
    }

    /**
    private long taskId;
    private String title;
    private String content;
    private boolean isComplete;
    private LocalDateTime createdAt;

    private boolean srsEnabled;
    private SRSMetadata srsData;
    private LocalDate nextRecallDate;
     */

    public String getTitle(){
        return this.title;
    }

    public String getContent(){
        return this.content;
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


}
