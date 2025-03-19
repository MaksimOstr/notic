package com.notic.repository;

import com.notic.entity.User;
import com.notic.projection.UserCredentialsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(String email);

    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u.email AS email,
               u.password AS password,
               u.accountNonLocked AS accountNonLocked,
               r AS roles
        FROM User u
        JOIN u.roles r
        WHERE u.email = :email
        """)
    Optional<UserCredentialsProjection> findUserForAuthByEmail(String email);
}
