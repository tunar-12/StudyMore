package com.codemaxxers.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Comparator;

@Entity
@Table(name = "study_groups")
public class StudyGroup {

    
    public static final int DEFAULT_MAX_MEMBERS = 10;
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long groupId;
 
    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String studyGoal;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "study_group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();
 
    @Column(nullable = false)
    private int maxMembers = DEFAULT_MAX_MEMBERS;
    @Column(nullable = false)
    private long studyCoins = 0;
 
    @Column(nullable = false)
    private boolean isActive = true;
 
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public StudyGroup() {}
 
    public StudyGroup(String title, String studyGoal, User host, int maxMembers) {
        this.title = title;
        this.studyGoal = studyGoal;
        this.host = host;
        this.maxMembers = maxMembers;
        this.members.add(host);
    }

    public void addMember(User user) {
        if (!isActive) throw new IllegalStateException("Cannot join an inactive group.");
        if (isFull())  throw new IllegalStateException("Group is already full.");
        members.add(user);
    }

    public void removeMember(User user) {
        members.remove(user);
        if (members.isEmpty()) {
            isActive = false;
            return;
        }
        if (user.equals(host)) {
            host = members.iterator().next();
        }
    }

    public boolean isFull() {
        return members.size() >= maxMembers;
    }

    public long calculateGroupCoins() {
        studyCoins = members.stream()
                .mapToLong(User::getCoinBalance)
                .sum();
        return studyCoins;
    }

    public List<User> getLeaderboard() {
        return members.stream()
                .sorted(Comparator.comparingLong(User::getCoinBalance).reversed())
                .collect(Collectors.toList());
    }

    public Long getGroupId()                        { return groupId; }
    public String getTitle()                        { return title; }
    public void setTitle(String title)              { this.title = title; }
    public String getStudyGoal()                    { return studyGoal; }
    public void setStudyGoal(String studyGoal)      { this.studyGoal = studyGoal; }
    public User getHost()                           { return host; }
    public void setHost(User host)                  { this.host = host; }
    public Set<User> getMembers()                   { return members; }
    public int getMaxMembers()                      { return maxMembers; }
    public void setMaxMembers(int maxMembers)       { this.maxMembers = maxMembers; }
    public long getStudyCoins()                     { return studyCoins; }
    public boolean isActive()                       { return isActive; }
    public void setActive(boolean active)           { isActive = active; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public int getMemberCount()                     { return members.size(); }
}