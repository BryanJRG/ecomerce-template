package com.bjdev.ecomercebase.repositories.user;

import com.bjdev.ecomercebase.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN FETCH u.roles r
        LEFT JOIN FETCH r.permissions
        WHERE u.email = :email
        """)
    Optional<User> findByEmailWithRolesAndPermisos(@Param("email") String email);

    /** Active users currently holding the given role — used to fan out role-wide notifications. */
    List<User> findByRoles_NameAndIsActiveTrue(String roleName);
}
