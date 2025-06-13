package com.notic.repository;

import com.notic.entity.Profile;
import com.notic.projection.GetProfileAvatarProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findProfileByUser_Id(long id);

    @Modifying
    @Transactional
    @Query("UPDATE Profile p SET p.avatar = :avatar WHERE p.user.id = :id")
    int updateProfileAvatarByUserId(@Param("id") long id, @Param("avatar") String avatar);

    @Query("SELECT p.avatar FROM Profile p WHERE p.user.id = :userId")
    Optional<String> getProfileAvatarByUser_Id(long userId);

    boolean existsByUser_Id(long id);
}
