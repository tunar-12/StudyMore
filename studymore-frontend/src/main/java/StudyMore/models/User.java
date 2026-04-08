package StudyMore.models;

import java.util.List;

import StudyMore.Main;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class User {

    private Long userId;
    private String username;
    private String email;
    private String passwordHash;
    private Rank rank;
    private int rating;
    private int coinBalance;
    private int studyStreak;
    private long totalStudyTime;
    private long dailyStudyTime;
    private MascotCat mascotCat;
    private Inventory inventory;
    private List<User> friends;
    private List<Task> tasks;

    public User(Long userId) {
        //Friend
        this.userId = userId;
    }

    // Constructing a new user
    public User(String username, String email, String passwordHash) {
        this.userId = SnowflakeIDGenerator.generate();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rank = Rank.BRONZE;
        this.rating = 0;
        this.coinBalance = 0;
        this.studyStreak = 0;
        this.totalStudyTime = 0L;
        this.dailyStudyTime = 0L;
        this.mascotCat = new MascotCat(this.userId);
        this.inventory = new Inventory(SnowflakeIDGenerator.generate(), this);
        this.friends = new java.util.ArrayList<>();
        this.tasks = new java.util.ArrayList<>();
    }

    // Constructor for database query
    public User(long id, String username, String email, String passwordHash, Rank rank, int rating,
        int coinBalance, int studyStreak, long totalStudyTime, long dailyStudyTime, 
        MascotCat cat, Inventory inventory, ArrayList<User> friends, ArrayList<Task> tasks) {
            this.userId = id;
            this.username = username;
            this.email = email;
            this.passwordHash = passwordHash;
            this.rank = rank;
            this.rating = rating;
            this.coinBalance = coinBalance;
            this.studyStreak = studyStreak;
            this.totalStudyTime = totalStudyTime;
            this.dailyStudyTime = dailyStudyTime;
            this.mascotCat = cat;
            this.inventory = inventory;
            this.friends = friends;
            this.tasks = tasks;
    }
    

    public void login() {
        System.out.println("User " + username + " logged in.");
    }

    public void register() {
        System.out.println("User " + username + " registered.");
    }

    public void updateProfile(String newUsername, String newEmail) {
        this.username = newUsername;
        this.email = newEmail;
        System.out.println("Profile updated for " + username);
    }

    //public Stats getStats() {
    //    // TODO
    //    return null;
    //}

    public Long getUserId() { 
        return userId; 
    }
    
    public String getUsername() { 
        return username; 
    }
    public Inventory getInventory() {
        return inventory;
    }
    public String getEmail() { 
        return email; 
    }
    public String getPasswordHash() { 
        return passwordHash; 
    }
    public Rank getRank() { 
        return rank; 
    }
    public int getRating() { 
        return rating; 
    }
    public int getCoinBalance() { 
        return coinBalance; 
    }
    public MascotCat getMascotCat() {
        return mascotCat;
    }

    public int getStudyStreak() {
        String query = """
            SELECT DISTINCT DATE(start_time, 'localtime') as study_date
            FROM sessions
            WHERE user_id = ?
            AND start_time IS NOT NULL
            ORDER BY study_date DESC
            """;

        try (java.sql.PreparedStatement stmt = Main.mngr.getConnection().prepareStatement(query)) {
            stmt.setLong(1, this.userId);

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                int streak = 0;
                java.time.LocalDate expected = java.time.LocalDate.now();

                while (rs.next()) {
                    java.time.LocalDate studyDate = java.time.LocalDate.parse(rs.getString("study_date"));

                    if (studyDate.equals(expected) || studyDate.equals(expected.minusDays(1))) {
                        streak++;
                        expected = studyDate.minusDays(1);
                    } else {
                        break;
                    }
                }

                this.studyStreak = streak;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.studyStreak;
    }
    
    public long getTotalStudyTime() { 
        return totalStudyTime; 
    }
    public long getDailyStudyTime() { 
        return dailyStudyTime; 
    }
    public List<User> getFriends() { 
        return friends; 
    }

    public List<Task> getTasks(){
        return tasks;
    }

    public void setUsername(String username) { 
        this.username = username; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash; 
    }
    public void setRank(Rank rank) { 
        this.rank = rank; 
    }
    public void setRating(int rating) { 
        this.rating = rating; 
    }
    public void setCoinBalance(int coinBalance) { 
        this.coinBalance = coinBalance; 
    }
    public void setStudyStreak(int studyStreak) { 
        this.studyStreak = studyStreak; 
    }
    public void setTotalStudyTime(long totalStudyTime) { 
        this.totalStudyTime = totalStudyTime; 
    }
    public void setDailyStudyTime(long dailyStudyTime) { 
        this.dailyStudyTime = dailyStudyTime; 
    }

}