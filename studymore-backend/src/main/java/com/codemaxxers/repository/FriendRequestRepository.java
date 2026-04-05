package com.codemaxxers.repository;

import com.codemaxxers.model.FriendRequest;
import com.codemaxxers.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByReceiver_UserIdAndStatus(Long receiverId, RequestStatus status);

    List<FriendRequest> findBySender_UserId(Long senderId);

    @Query("SELECT fr FROM FriendRequest fr WHERE " +
           "(fr.sender.userId = :a AND fr.receiver.userId = :b) OR " +
           "(fr.sender.userId = :b AND fr.receiver.userId = :a)")
    Optional<FriendRequest> findBetweenUsers(@Param("a") Long userAId, @Param("b") Long userBId);

    @Query("SELECT fr FROM FriendRequest fr WHERE " +
           "fr.status = 'ACCEPTED' AND " +
           "(fr.sender.userId = :userId OR fr.receiver.userId = :userId)")
    List<FriendRequest> findAcceptedByUserId(@Param("userId") Long userId);
}