package StudyMore.models;

import java.time.LocalDateTime;

public class StudySession {

    private final long sessionID;
    private final User user;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private Multiplier multiplier;

    private int duration;
    private int coinsEarned;
    private SessionState state;

    private int breakDuration;
    private int breakTimeRemaining;

    public StudySession(User user) {
        this.sessionID = SnowflakeIDGenerator.generate();
        this.user = user;
        this.startTime = LocalDateTime.now();
        this.multiplier = new Multiplier();
        this.duration = 0;
        this.coinsEarned = 0;
        this.state = SessionState.IDLE;

        System.out.println("LOG: Created ID: " + sessionID);
    }

    // Session lifecycle

    public void start() {
        state = SessionState.STUDYING;
    }

    public void stop() {
        state = SessionState.IDLE;
    }

    public void end() {
        endTime = LocalDateTime.now();
    }

    // Study timer

    public void incrementDuration() {
        duration++; // handles internal duration 
        multiplier.increment(); // handles multiplier
    }

    public int getDuration() {
        return duration;
    }

    // Break timer

    public void startBreak(int breakSeconds) {
        state = SessionState.ON_BREAK;
        breakDuration = breakSeconds;
        breakTimeRemaining = breakSeconds;
    }

    public void tickBreak() {
        if (state == SessionState.ON_BREAK) {
            breakTimeRemaining--; // handle internal duration
            multiplier.applyCooldown(); // handle cooldown
        }
    }

    public boolean isBreakOver() {
        return state == SessionState.ON_BREAK && breakTimeRemaining <= 0;
    }

    public void resetBreak() {
        breakDuration = 0;
        breakTimeRemaining = 0;
    }

    public boolean isOnBreak() {
        return state == SessionState.ON_BREAK;
    }

    public int getBreakTimeRemaining() {
        return breakTimeRemaining;
    }

    // State

    public SessionState getState() {
        return state;
    }

    public Multiplier getMultiplier() {
        return multiplier;
    }

    // Calculation (TODO)

    public void calculateCoins() {
        // TODO
    }

}