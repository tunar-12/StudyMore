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
        this.inventory = new Inventory(this.userId, this);
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
    public int getStudyStreak() {  // It gets the total study sessions to caclulate how many days have it been.
        String query = "SELECT * FROM sessions";

        try (Statement stmt = Main.mngr.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            int count = 0;

            while (rs.next()) {
                count++;
                userId = rs.getLong("id");
            }

            if (count != studyStreak) {
                studyStreak = count;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return studyStreak; 
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