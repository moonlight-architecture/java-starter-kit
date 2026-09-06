package com.servicecops.project.repositories;

import com.servicecops.project.models.database.SystemDomainModel;
import com.servicecops.project.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemDomainRepository extends JetRepository<SystemDomainModel, Long> {
}
