package com.servicecops.project.config;

import com.servicecops.project.models.database.SystemPermissionModel;
import com.servicecops.project.models.database.SystemRolePermissionAssignmentModel;
import com.servicecops.project.models.database.SystemUserModel;
import com.servicecops.project.repositories.SystemPermissionRepository;
import com.servicecops.project.repositories.SystemRolePermissionRepository;
import com.servicecops.project.repositories.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ApplicationConf implements UserDetailsService {
    private final SystemUserRepository userRepository;
    private final SystemRolePermissionRepository permissionAssignmentRepository;
    private final SystemPermissionRepository permissionRepository;
    @Override
    public SystemUserModel loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SystemUserModel> usersModel = userRepository.findFirstByUsernameOrEmail(username, username);
        if (usersModel.isPresent()) {
            SystemUserModel user = usersModel.get();
            // if the user account is not activated, then we eject from here.
            if (user.getIsActive() == Boolean.FALSE) {
                throw new IllegalStateException("User account is not active");
            }
            Collection<SimpleGrantedAuthority> authorities = usersModel.get().getAuthorities();
            // get the user permissions
            Collection<SystemRolePermissionAssignmentModel> permissions = permissionAssignmentRepository.findAllByRoleCode(user.getRoleCode());
            for (SystemRolePermissionAssignmentModel permissionAssignmentModel: permissions) {
                Optional<SystemPermissionModel> permissionsModel = permissionRepository.findFirstByPermissionCode(permissionAssignmentModel.getPermissionCode());
                if (permissionsModel.isPresent()){
                    SystemPermissionModel permission = permissionsModel.get();
                    authorities.add(new SimpleGrantedAuthority(permission.getPermissionCode()));
                }
            }
            user.setAuthorities(authorities);
            return user;
        } else {
            throw new IllegalStateException("User not found");
        }
    }
}


