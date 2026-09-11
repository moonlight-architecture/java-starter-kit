package com.servicecops.project.repositories;

import com.servicecops.project.models.database.SystemUserModel;
import com.servicecops.project.models.jpahelpers.repository.JetRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public interface SystemUserRepository extends JetRepository<SystemUserModel, Long> {
    SystemUserModel findFirstByUsername(String username);

    Optional<SystemUserModel> findFirstByUsernameOrEmail(String username, String email);

    /**
     * Atomically bumps {@code token_version} so concurrent logins cannot share a version.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update SystemUserModel u
               set u.tokenVersion = coalesce(u.tokenVersion, 0) + 1,
                   u.lastLoggedInAt = :now
             where u.id = :id
            """)
    int incrementTokenVersion(@Param("id") Long id, @Param("now") Timestamp now);
}
