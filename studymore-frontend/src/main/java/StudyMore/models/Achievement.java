package StudyMore.models;

public class Achievement {

    private long achievementId;
    private String title;
    private String description;
    private String type;
    private int targetValue;
    private int reward;
    private String iconPath;

    public Achievement(long achievementId, String title, String description,
                       String type, int targetValue, int reward, String iconPath) {
        this.achievementId = achievementId;
        this.title         = title;
        this.description   = description;
        this.type          = type;
        this.targetValue   = targetValue;
        this.reward        = reward;
        this.iconPath      = iconPath;
    }
    public boolean checkCompletion(int currentProgress) {
        return currentProgress >= targetValue;
    }
    public int getProgress(int currentProgress) {
        if (targetValue == 0) return 100;
        return Math.min(100, (int)((currentProgress / (double) targetValue) * 100));
    }

    public long getAchievementId() { return achievementId; }
    public String getTitle()       { return title; }
    public String getDescription() { return description; }
    public String getType()        { return type; }
    public int getTargetValue()    { return targetValue; }
    public int getReward()         { return reward; }
    public String getIconPath()    { return iconPath; }
}