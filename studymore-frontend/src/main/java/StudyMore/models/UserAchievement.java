package StudyMore.models;

import java.time.LocalDateTime;

public class UserAchievement {

    private long id;
    private User user;
    private Achievement achievement;
    private int progress;
    private boolean isCompleted;
    private LocalDateTime completedAt;

    public UserAchievement(long id, User user, Achievement achievement,
                           int progress, boolean isCompleted, LocalDateTime completedAt) {
        this.id          = id;
        this.user        = user;
        this.achievement = achievement;
        this.progress    = progress;
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
    }
    public boolean updateProgress(int value) {
        if (isCompleted) return false;
        this.progress += value;
        if (achievement.checkCompletion(this.progress)) {
            complete();
            return true;
        }
        return false;
    }
    public void complete() {
        if (isCompleted) return;
        this.isCompleted  = true;
        this.completedAt  = LocalDateTime.now();
        user.setCoinBalance(user.getCoinBalance() + achievement.getReward());
    }

    public long getId()                    { return id; }
    public User getUser()                  { return user; }
    public Achievement getAchievement()    { return achievement; }
    public int getProgress()               { return progress; }
    public boolean isCompleted()           { return isCompleted; }
    public LocalDateTime getCompletedAt()  { return completedAt; }
}