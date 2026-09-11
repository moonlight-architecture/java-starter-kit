package com.servicecops.project.utils;

import com.servicecops.project.models.database.SystemPermissionModel;
import com.servicecops.project.models.database.SystemRoleModel;
import com.servicecops.project.models.database.SystemRolePermissionAssignmentModel;
import com.servicecops.project.models.jpahelpers.enums.AppDomains;
import com.servicecops.project.permissions.Perms;
import com.servicecops.project.repositories.SystemPermissionRepository;
import com.servicecops.project.repositories.SystemRolePermissionRepository;
import com.servicecops.project.repositories.SystemRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

/**
 * Upserts default roles and permissions on every boot. Never deletes the catalog
 * (that would CASCADE-wipe role grants).
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SetUp {
    @Value("${USE_DOMAINS:true}")
    Boolean useDomains;

    @Value("${ADMIN_ROLE_NAME:ADMINISTRATOR}")
    String adminRoleName;

    @Value("${ADMIN_ROLE_DOMAIN:BACK_OFFICE}")
    AppDomains adminDomain;

    private final SystemPermissionRepository permissionRepository;
    private final SystemRolePermissionRepository permissionAssignmentRepository;
    private final SystemRoleRepository roleRepository;

    @PostConstruct
    public void setupPermissions() {
        String roleCode = (adminRoleName == null || adminRoleName.isBlank())
                ? "ADMINISTRATOR"
                : adminRoleName.trim();

        ensureAdminRole(roleCode);

        for (Perms perm : Perms.values()) {
            SystemPermissionModel row = permissionRepository.findFirstByPermissionCode(perm.code())
                    .orElseGet(SystemPermissionModel::new);
            row.setPermissionCode(perm.code());
            row.setPermissionName(perm.displayName());
            if (Boolean.TRUE.equals(useDomains)) {
                row.setPermissionDomain(perm.domain());
            } else {
                row.setPermissionDomain(null);
            }
            permissionRepository.save(row);
            log.debug("Upserted permission {}", perm.code());

            if (perm.shipWithAdmin()) {
                grant(roleCode, perm.code());
            }
        }
        log.info("Permissions upserted successfully");
    }

    private void ensureAdminRole(String roleCode) {
        Optional<SystemRoleModel> existing = roleRepository.findFirstByRoleCode(roleCode);
        if (existing.isPresent()) {
            return;
        }
        var adminRole = SystemRoleModel.builder()
                .roleName("Administrator")
                .roleCode(roleCode);
        if (Boolean.TRUE.equals(useDomains)) {
            if (adminDomain == null) {
                throw new IllegalStateException("Please define ADMIN_ROLE_DOMAIN for administrators");
            }
            adminRole.roleDomain(adminDomain);
        }
        roleRepository.save(adminRole.build());
        log.info("Created admin role {}", roleCode);
    }

    private void grant(String roleCode, String permissionCode) {
        Optional<SystemRolePermissionAssignmentModel> existing =
                permissionAssignmentRepository.findFirstByRoleCodeAndPermissionCode(roleCode, permissionCode);
        if (existing.isPresent()) {
            return;
        }
        permissionAssignmentRepository.save(SystemRolePermissionAssignmentModel.builder()
                .permissionCode(permissionCode)
                .roleCode(roleCode)
                .build());
    }
}
