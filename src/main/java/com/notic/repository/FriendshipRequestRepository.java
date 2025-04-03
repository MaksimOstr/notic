package com.notic.repository;

import com.notic.entity.FriendshipRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long>, JpaSpecificationExecutor<FriendshipRequest> {

    @Modifying
    @Query("DELETE FROM FriendshipRequest fr where fr.id = :id")
    void deleteFriendshipRequestById(@Param("id") long id);
}
