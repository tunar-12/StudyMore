package com.codemaxxers.repository;

import com.codemaxxers.model.Achievement;
import com.codemaxxers.model.enums.AchievementType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByType(AchievementType type);
}