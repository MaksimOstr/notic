package com.notic.repository;

import com.notic.entity.FriendshipRequest;
import com.notic.projection.FriendshipRequestProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long>, JpaSpecificationExecutor<FriendshipRequest> {


    @Modifying
    @Query("DELETE FriendshipRequest fr WHERE fr.id = :requestId AND fr.receiver.id = :receiverId")
    int rejectFriendshipRequest(@Param("requestId") long requestId, @Param("receiverId") long receiverId);

    @Query("""
    SELECT new com.notic.projection.FriendshipRequestProjection(
        fr.id,
        fr.sender.profile.username,
        fr.sender.profile.avatar,
        fr.createdAt
        ) FROM FriendshipRequest fr WHERE fr.receiver.id = :receiverId
    """)
    Page<FriendshipRequestProjection> getAllFriendshipRequests(@Param("receiverId") long receiverId, Pageable pageable);

    @Query("""
    SELECT 
        fr.id as id,
        fr.sender.id as senderId,
        fr.receiver.id as receiverId,
        fr.receiver.profile.avatar as receiverAvatar,
        fr.receiver.profile.username as receiverUsername
    FROM FriendshipRequest fr
    WHERE fr.id = :id
""")
    <T> Optional<T> findById(long id, Class<T> projection);
}
