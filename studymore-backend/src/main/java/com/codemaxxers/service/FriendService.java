package com.codemaxxers.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.codemaxxers.model.FriendRequest;
import com.codemaxxers.model.User;
import com.codemaxxers.model.enums.RequestStatus;
import com.codemaxxers.repository.FriendRequestRepository;
import com.codemaxxers.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FriendService {
 
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
 
    public FriendService(FriendRequestRepository friendRequestRepository,
                         UserRepository userRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
    }
    
    public List<User> searchUsers(String keyword, Long requestingUserId) {
        return userRepository.findByUsernameContainingIgnoreCase(keyword)
                .stream()
                .filter(u -> !u.getUserId().equals(requestingUserId))
                .collect(Collectors.toList());
    }
 
    // sending a requst
    public FriendRequest sendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send a friend request to yourself.");
        }
 
        friendRequestRepository.findBetweenUsers(senderId, receiverId).ifPresent(existing -> {
            throw new IllegalStateException(
                "A friend request already exists between these users (status: " + existing.getStatus() + ").");
        });
 
        User sender   = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + senderId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found: " + receiverId));
 
        return friendRequestRepository.save(new FriendRequest(sender, receiver));
    }

    // responding to requests
    public FriendRequest acceptRequest(Long requestId, Long receiverId) {
        FriendRequest req = getRequestForReceiver(requestId, receiverId);
        req.accept();
        return friendRequestRepository.save(req);
    }
    public FriendRequest denyRequest(Long requestId, Long receiverId) {
        FriendRequest req = getRequestForReceiver(requestId, receiverId);
        req.deny();
        return friendRequestRepository.save(req);
    }
    public List<User> getFriends(Long userId) {
        return friendRequestRepository.findAcceptedByUserId(userId)
                .stream()
                .map(req -> req.getSender().getUserId().equals(userId)  // ← getUserId()
                        ? req.getReceiver()
                        : req.getSender())
                .collect(Collectors.toList());
    }
    public List<FriendRequest> getPendingRequests(Long userId) {
        return friendRequestRepository.findByReceiver_UserIdAndStatus(userId, RequestStatus.PENDING); // ← UserId
    }
 
    // helper
    private FriendRequest getRequestForReceiver(Long requestId, Long receiverId) {
        FriendRequest req = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));
        if (!req.getReceiver().getUserId().equals(receiverId)) {
            throw new SecurityException("You are not the receiver of this request.");
        }
        return req;
    }
}