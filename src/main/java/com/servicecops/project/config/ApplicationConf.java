package com.servicecops.project.config;

import com.servicecops.project.models.database.SystemUserModel;
import com.servicecops.project.repositories.RoleAuth;
import com.servicecops.project.repositories.RolePermissionCache;
import com.servicecops.project.repositories.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ApplicationConf implements UserDetailsService {
    private final SystemUserRepository userRepository;
    private final RolePermissionCache rolePermissionCache;

    @Override
    public SystemUserModel loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SystemUserModel> usersModel = userRepository.findFirstByUsernameOrEmail(username, username);
        if (usersModel.isEmpty()) {
            throw new IllegalStateException("User not found");
        }
        SystemUserModel user = usersModel.get();
        if (user.getIsActive() == Boolean.FALSE) {
            throw new IllegalStateException("User account is not active");
        }

        RoleAuth auth = rolePermissionCache.roleAuth(user.getRoleCode());
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String code : auth.permissionCodes()) {
            authorities.add(new SimpleGrantedAuthority(code));
        }
        user.setAuthorities(authorities);
        user.setRoleName(auth.roleName());
        user.setRoleDomain(auth.domain());

        return user;
    }
}
