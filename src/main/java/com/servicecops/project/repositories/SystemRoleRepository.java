package com.servicecops.project.repositories;

import com.servicecops.project.models.database.SystemRoleModel;
import com.servicecops.project.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemRoleRepository extends JetRepository<SystemRoleModel, Long> {
    Optional<SystemRoleModel> findFirstByRoleCode(String code);
}
