package com.codemaxxers.repository;

import com.codemaxxers.model.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    List<StudyGroup> findByIsActiveTrue();

    List<StudyGroup> findByHost_UserId(Long hostId);

    @Query("SELECT g FROM StudyGroup g JOIN g.members m WHERE m.userId = :userId AND g.isActive = true")
    List<StudyGroup> findActiveGroupsByMemberId(@Param("userId") Long userId);

    @Query("SELECT g FROM StudyGroup g WHERE g.isActive = true AND g.title LIKE %:keyword%")
    List<StudyGroup> searchActiveByTitle(@Param("keyword") String keyword);
}