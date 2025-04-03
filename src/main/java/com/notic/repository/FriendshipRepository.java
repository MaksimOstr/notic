package com.notic.repository;

import com.notic.entity.Friendship;
import com.notic.projection.FriendshipDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface FriendshipRepository extends JpaRepository<Friendship, Long>, JpaSpecificationExecutor<Friendship> {
    @Query("""
        SELECT new com.notic.projection.FriendshipDto(
            CASE WHEN f.user1.id = :userId THEN f.user2.id ELSE f.user1.id END,
            CASE WHEN f.user1.id = :userId THEN f.user2.username ELSE f.user1.username END,
            CASE WHEN f.user1.id = :userId THEN f.user2.avatar ELSE f.user1.avatar END,
            f.friendshipDate
        )
        FROM Friendship f
        WHERE (f.user1.id = :userId OR f.user2.id = :userId)
        ORDER BY f.friendshipDate DESC
    """)
    Page<FriendshipDto> findFriendshipsByUser(
            @Param("userId") long userId,
            Pageable pageable);
}
