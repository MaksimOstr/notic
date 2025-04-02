package com.notic.repository;

import com.notic.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    boolean existsByReceiver_IdAndSender_Id(long receiver, long sender);
}
