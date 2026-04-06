package com.codemaxxers.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.codemaxxers.model.enums.Rank;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id")
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Rank rank;

    private int rating;
    private int coinBalance;
    private int studyStreak;
    private long totalStudyTime;
    private long dailyStudyTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_friends",
        joinColumns        = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();

    private LocalDateTime createdAt;

    // Default constructor required by JPA
    public User() {}

    // registration
    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rank = Rank.BRONZE;
        this.rating = 0;
        this.coinBalance = 0;
        this.studyStreak = 0;
        this.totalStudyTime = 0L;
        this.dailyStudyTime = 0L;
        this.createdAt = LocalDateTime.now();
    }

    public void updateProfile(String newUsername, String newEmail) {
        this.username = newUsername;
        this.email = newEmail;
    }

    public Set<User> getFriends() {
        return friends;
    }

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
    public int getStudyStreak() { 
        return studyStreak; 
    }
    public long getTotalStudyTime() { 
        return totalStudyTime; 
    }
    public long getDailyStudyTime() { 
        return dailyStudyTime; 
    }
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }


    public void addFriend(User user) {
        this.friends.add(user);
        user.friends.add(this);
    }

    public void removeFriend(User user) {
        this.friends.remove(user);
        user.friends.remove(this);
    }

    public boolean isFriendWith(User user) {
        return this.friends.contains(user);
    }

    public void setUserId(Long userId) { 
        this.userId = userId; 
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
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
}