package com.servicecops.project.repositories;

import com.servicecops.project.models.database.SystemRoleModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@code roleCode → role auth}. Invalidate after grant changes.
 */
@Component
@RequiredArgsConstructor
public class RolePermissionCache {

    private final SystemRolePermissionRepository permissionAssignmentRepository;
    private final SystemRoleRepository roleRepository;
    private final ConcurrentHashMap<String, RoleAuth> cache = new ConcurrentHashMap<>();

    public RoleAuth roleAuth(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return new RoleAuth(null, null, List.of());
        }
        return cache.computeIfAbsent(roleCode, this::load);
    }

    /**
     * Permission codes only (back-compat helper).
     */
    public List<String> permissionCodes(String roleCode) {
        return roleAuth(roleCode).permissionCodes();
    }

    public int size() {
        return cache.size();
    }

    public void invalidate(String roleCode) {
        if (roleCode != null) {
            cache.remove(roleCode);
        }
    }

    public void invalidateAll() {
        cache.clear();
    }

    private RoleAuth load(String roleCode) {
        List<String> codes = permissionAssignmentRepository.findPermissionCodesByRoleCode(roleCode);
        SystemRoleModel role = roleRepository.findFirstByRoleCode(roleCode).orElse(null);
        if (role == null) {
            return new RoleAuth(null, null, codes);
        }
        return new RoleAuth(role.getRoleName(), role.getRoleDomain(), codes);
    }
}
