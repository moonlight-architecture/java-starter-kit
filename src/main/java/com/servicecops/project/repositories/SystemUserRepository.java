package com.servicecops.project.repositories;

import com.servicecops.project.models.database.*;
import com.servicecops.project.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemUserRepository extends JetRepository<SystemUserModel, Long> {
    SystemUserModel findFirstByUsername(String username);
    Optional<SystemUserModel> findFirstByUsernameOrEmail(String username, String email);
}

