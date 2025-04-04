package com.notic.repository;

import com.notic.entity.Friendship;
import com.notic.projection.FriendshipProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface FriendshipRepository extends JpaRepository<Friendship, Long>, JpaSpecificationExecutor<Friendship> {
    @Query("""
        SELECT new com.notic.projection.FriendshipProjection(
            CASE WHEN f.user1.id = :userId THEN f.user2.id ELSE f.user1.id END,
            CASE WHEN f.user1.id = :userId THEN f.user2.username ELSE f.user1.username END,
            CASE WHEN f.user1.id = :userId THEN f.user2.avatar ELSE f.user1.avatar END,
            f.friendshipDate
        )
        FROM Friendship f
        WHERE (f.user1.id = :userId OR f.user2.id = :userId)
        ORDER BY f.friendshipDate DESC
    """)
    Page<FriendshipProjection> findFriendshipsByUser(@Param("userId") long userId, Pageable pageable);


    @Modifying
    @Query("DELETE FROM Friendship f WHERE f.id =:id AND f.user1.id = :userId OR f.user2.id = :userId")
    int removeFriendship(@Param("id") long id, @Param("userId") long userId);
}
