package com.servicecops.project.repositories;

import com.servicecops.project.models.database.SystemRolePermissionAssignmentModel;
import com.servicecops.project.models.jpahelpers.repository.JetRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemRolePermissionRepository extends JetRepository<SystemRolePermissionAssignmentModel, Long> {
    Optional<SystemRolePermissionAssignmentModel> findFirstByRoleCodeAndPermissionCode(String role_code, String permission_code);

    Collection<SystemRolePermissionAssignmentModel> findAllByRoleCode(String role_code);

    @Query("select a.permissionCode from SystemRolePermissionAssignmentModel a where a.roleCode = :roleCode")
    List<String> findPermissionCodesByRoleCode(@Param("roleCode") String roleCode);
}
