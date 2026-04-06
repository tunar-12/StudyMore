package com.codemaxxers.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.codemaxxers.model.enums.RequestStatus;

@Entity
@Table(name = "friend_requests",
       uniqueConstraints = @UniqueConstraint(columnNames = {"sender_id", "receiver_id"}))
public class FriendRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long requestId;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
 
    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    public FriendRequest() {}
 
    public FriendRequest(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

     public void accept() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be accepted."); // look again
        }
        this.status = RequestStatus.ACCEPTED;
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);
    }

    public void deny() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be denied.");
        }
        this.status = RequestStatus.DENIED;
    }
 
    public boolean isPending()  { return status == RequestStatus.PENDING; }
    public boolean isAccepted() { return status == RequestStatus.ACCEPTED; }


    public Long getRequestId()                        { return requestId; }
    public User getSender()                           { return sender; }
    public void setSender(User sender)                { this.sender = sender; }
    public User getReceiver()                         { return receiver; }
    public void setReceiver(User receiver)            { this.receiver = receiver; }
    public RequestStatus getStatus()                  { return status; }
    public void setStatus(RequestStatus status)       { this.status = status; }
    public LocalDateTime getSentAt()                  { return sentAt; }
}
