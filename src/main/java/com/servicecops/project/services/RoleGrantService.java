package com.servicecops.project.services;

import com.servicecops.project.models.database.SystemRolePermissionAssignmentModel;
import com.servicecops.project.repositories.RolePermissionCache;
import com.servicecops.project.repositories.SystemRolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grant / revoke role permissions and always invalidate {@link RolePermissionCache}.
 */
@Service
@RequiredArgsConstructor
public class RoleGrantService {
    private final SystemRolePermissionRepository permissionAssignmentRepository;
    private final RolePermissionCache rolePermissionCache;

    @Transactional
    public void grant(String roleCode, String permissionCode) {
        permissionAssignmentRepository
                .findFirstByRoleCodeAndPermissionCode(roleCode, permissionCode)
                .orElseGet(() -> permissionAssignmentRepository.save(
                        SystemRolePermissionAssignmentModel.builder()
                                .roleCode(roleCode)
                                .permissionCode(permissionCode)
                                .build()
                ));
        rolePermissionCache.invalidate(roleCode);
    }

    @Transactional
    public void revoke(String roleCode, String permissionCode) {
        permissionAssignmentRepository
                .findFirstByRoleCodeAndPermissionCode(roleCode, permissionCode)
                .ifPresent(permissionAssignmentRepository::delete);
        rolePermissionCache.invalidate(roleCode);
    }
}
