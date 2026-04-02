package StudyMore.models;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import StudyMore.Main;

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
        this.user = user;
        this.multiplier = new Multiplier();
        this.duration = 0;
        this.coinsEarned = 0;
        this.state = SessionState.IDLE;

        StudySession existing = Main.mngr.getTodaysStudySession(user);

        if (existing != null) {
            this.sessionID = existing.sessionID;
            this.startTime = existing.startTime;
            this.endTime = existing.endTime;
            this.duration = existing.duration;
            this.coinsEarned = existing.coinsEarned;
            this.multiplier = existing.multiplier;
            System.out.println("LOG: Resumed session ID: " + this.sessionID);
        } else {
            this.sessionID = SnowflakeIDGenerator.generate();
            this.startTime = LocalDateTime.now();
            saveSession();
            System.out.println("LOG: Created new session ID: " + this.sessionID);
        }
    }

    // Constructor for restoring a session from the database
    public StudySession(User user, long sessionID, LocalDateTime startTime, 
        LocalDateTime endTime, Multiplier multiplier, 
        int duration, int coinsEarned) {
        this.user = user;
        this.sessionID = sessionID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.multiplier = multiplier;
        this.duration = duration;
        this.coinsEarned = coinsEarned;
        this.state = (endTime != null) ? SessionState.IDLE : SessionState.STUDYING;
        System.out.println("LOG: Restored session ID: " + this.sessionID);
    }

    public void start() {
        state = SessionState.STUDYING;
    }

    public void stop() {
        state = SessionState.IDLE;
    }

    public void end() {
        endTime = LocalDateTime.now();
    }

    public void incrementDuration() {
        duration++; // handles internal duration 
        multiplier.increment(); // handles multiplier
        updateSession();
    }

    public int getDuration() {
        return duration;
    }

    public void startBreak(int breakSeconds) {
        state = SessionState.ON_BREAK;
        breakDuration = breakSeconds;
        breakTimeRemaining = breakSeconds;
    }

    public void tickBreak() {
        if (state == SessionState.ON_BREAK) {
            breakTimeRemaining--; // handle internal duration
            multiplier.applyCooldown(); // handle cooldown
            updateSession();
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

    public SessionState getState() {
        return state;
    }

    public Multiplier getMultiplier() {
        return multiplier;
    }

    public void calculateCoins() {
        coinsEarned = (int)((duration / 60) * multiplier.getValue());
    }

    public void updateSession() {
        if (sessionID == 0) return; // no session in DB yet to update

        String query = """
            UPDATE sessions
            SET duration = ?, coins_earned = ?, multiplier_value = ?, end_time = ?
            WHERE id = ?
            """;

        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(query)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            stmt.setInt(1, duration);
            stmt.setInt(2, coinsEarned);
            stmt.setDouble(3, multiplier.getValue());
            stmt.setString(4, endTime != null ? endTime.format(formatter) : null); 
            stmt.setLong(5, sessionID);
            stmt.executeUpdate();
            System.out.println("LOG: Updated session ID: " + sessionID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveSession() {
        String query = """
            INSERT INTO sessions (id, user_id, start_time, end_time, multiplier_value, coins_earned, duration)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(query)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            stmt.setLong(1, sessionID);
            stmt.setLong(2, user.getUserId());
            stmt.setString(3, startTime.format(formatter));
            stmt.setString(4, endTime != null ? endTime.format(formatter) : null);
            stmt.setDouble(5, multiplier.getValue());
            stmt.setInt(6, coinsEarned);
            stmt.setInt(7, duration);
            stmt.executeUpdate();
            System.out.println("LOG: Saved new session ID: " + sessionID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}