package com.notic.projection;

public interface FriendshipAcceptRequestProjection {
    long getId();
    long getSenderId();
    long getReceiverId();
    String getReceiverUsername();
    String getReceiverAvatar();
}
