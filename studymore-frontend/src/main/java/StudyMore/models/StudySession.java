package StudyMore.models;

import java.time.LocalDateTime;

public class StudySession {
    private long sessionID;
    private User user;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Multiplier multiplier;
    private int duration;
    private int coinsEarned;

    public StudySession(
        User user
    ) {
        this.sessionID = SnowflakeIDGenerator.generate();
        this.user = user;
        this.startTime = LocalDateTime.now();
        this.multiplier = new Multiplier();
        this.duration = 0;
        this.coinsEarned = 0;
    }

    public void start() {

    }

    public void stop() {

    }

    public void end() {
        endTime = LocalDateTime.now();
    }

    public void calculateMultiplierIncrease() {

    }

    public void calculateCoins() {

    }

    public void calculateCooldown() {

    }

    public void getDuration() {

    }
}
