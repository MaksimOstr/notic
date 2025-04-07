package com.notic.repository;

import com.notic.entity.User;
import com.notic.projection.JwtAuthUserProjection;
import com.notic.projection.UserCredentialsProjection;
import com.notic.projection.GetUserAvatarProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(String email);

    Optional<User> findByEmail(String email);

    @Query("""
        SELECT
               u.id as id,
               u.email AS email,
               u.password AS password,
               u.accountNonLocked AS accountNonLocked,
               u.enabled AS enabled,
               u.authProvider AS authProvider,
               r.name AS roleNames
        FROM User u
        JOIN u.roles r
        WHERE u.email = :email
        """)
    Optional<UserCredentialsProjection> findUserForAuthByEmail(String email);

    Optional<JwtAuthUserProjection> findUserForJwtAuthById(long id);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :id")
    int updateEnabledStatusById(@Param("id") long id, @Param("enabled") boolean enabled);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.avatar = :avatar WHERE u.id = :id")
    int updateUserAvatarById(@Param("id") long id, @Param("avatar") String avatarUrl);

    Optional<GetUserAvatarProjection> getUserAvatarUrlById(long id);
}
