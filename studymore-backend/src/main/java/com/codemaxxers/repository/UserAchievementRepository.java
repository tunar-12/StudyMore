package com.codemaxxers.repository;

import com.codemaxxers.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
 
    List<UserAchievement> findByUser_Id(Long userId);
 
    List<UserAchievement> findByUser_IdAndCompletedTrue(Long userId);
 
    List<UserAchievement> findByUser_IdAndCompletedFalse(Long userId);
 
    Optional<UserAchievement> findByUser_IdAndAchievement_AchievementId(Long userId, Long achievementId);
 
    boolean existsByUser_IdAndAchievement_AchievementId(Long userId, Long achievementId);
 
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user.id = :userId " +
           "AND ua.achievement.type = :type")
    List<UserAchievement> findByUserIdAndType(@Param("userId") Long userId,
                                              @Param("type") com.codemaxxers.model.enums.AchievementType type);
}