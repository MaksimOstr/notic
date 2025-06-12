package com.notic.repository;

import com.notic.entity.User;
import com.notic.projection.UserAuthProviderProjection;
import com.notic.projection.UserWithRolesProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query(value = """
            
                    SELECT
                             u.id,
                             u.email,
                             u.auth_provider AS authProvider,
                             u.password,
                             u.account_non_locked AS accountNonLocked,
                             u.enabled,
                             STRING_AGG(r.name, ',') AS roleNames
                         FROM users u
                         JOIN users_role ur ON u.id = ur.user_id
                         JOIN roles r ON ur.role_id = r.id
                         WHERE u.email = :email
                         GROUP BY u.id
            """, nativeQuery = true)
    Optional<UserWithRolesProjection> findByEmailWithRoles(String email);

    Optional<User> findByEmail(String email);

    Optional<UserAuthProviderProjection> findUserByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :id")
    int updateEnabledStatusById(@Param("id") long id, @Param("enabled") boolean enabled);

    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :id")
    int updatePasswordById(@Param("id") long id, @Param("password") String password);
}
