package com.servicecops.project.services;

import com.jet.moonlight.services.JetService;
import com.servicecops.project.config.ApplicationConf;
import com.servicecops.project.models.database.SystemRoleModel;
import com.servicecops.project.models.database.SystemUserModel;
import com.servicecops.project.models.jpahelpers.enums.AppDomains;
import com.servicecops.project.repositories.SystemRoleRepository;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Transactional
public abstract class UniversalService extends JetService {
    @Autowired
    private SystemRoleRepository roleRepository;
    @Autowired
    private ApplicationConf userDetailService;

    /**
     * Checks if the user has access to a certain domain
     * @param domain AppDomains - The domain in quest
     */
    public void belongsTo(AppDomains domain){
        if (getUserDomain() != domain){
            throw new IllegalStateException("You have no access to the "+domain+" services");
        }
    }

    /**
     * Returns all the permissions of the logged-in user
     * @param username Optional - if this is provided, it will override and get the permission of the user with the given username
     * @return List of permissions
     */
    public List<String> userPerms(@Nullable String username){
        List<String> perms = new ArrayList<>();
        UserDetails userDetails = getContextUserDetails();

        if (username != null){
            userDetails = userDetailService.loadUserByUsername(username);
        }
        for (GrantedAuthority authority: userDetails.getAuthorities()){
            perms.add(authority.getAuthority());
        }
        return perms;
    }

    /**
     * The domain of the currently logged-in user
     * Remember, users don't belong to a domain directly but via the role assigned to them.
     * @return AppDomain -- The domain String of the user according to the role assigned to them.
     *
     * @implNote This may be null if the entire app is not supporting domains
     */
    public AppDomains getUserDomain(){
        return getRole().getRoleDomain();
    }

    /**
     * Get the role of the logged-in user
     * @return SystemRoleModel - The role object
     */
    public SystemRoleModel getRole(){
        String roleCode = authenticatedUser().getRoleCode();
        // query for the role code
        Optional<SystemRoleModel> rolesModel = roleRepository.findFirstByRoleCode(roleCode);
        if (rolesModel.isEmpty()){
            throw new IllegalStateException("UNKNOWN USER ROLE");
        }
        return rolesModel.get();
    }

    /**
     * If the user is logged in, [has provided the JWT in the Headers], calling this method will return the user
     * @return user details
     */
    public UserDetails getContextUserDetails(){
        return authenticatedUser();
    }

    /**
     * Check if the logged-in user has a certain role
     * @param roleCode The code of the role to check for.
     * @return Boolean
     */
    public Boolean hasRole(String roleCode){
        SystemUserModel usersModel = authenticatedUser();
        if (Objects.equals(usersModel.getRoleCode(), roleCode)) {
            return true;
        }
        throw new IllegalStateException("USER HAS LESS PRIVILEGES");
    }

    /**
     * Check if the user is authenticated.
     * @return Boolean - True if the user is authenticated otherwise false
     */
    public Boolean isAuthenticated(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return null != authentication
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * This prevents a user from accessing a service if they are not logged in
     */
    public void requiresAuth(){
        if (Boolean.FALSE.equals(isAuthenticated())){
            throw new IllegalArgumentException("AUTHENTICATION REQUIRED");
        }
    }

    /**
     * Returns the currently logged-in user.
     * @return SystemUserModel | UserDetails
     */
    public SystemUserModel authenticatedUser(){
        if (Boolean.TRUE.equals(isAuthenticated())){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            if (principal instanceof SystemUserModel user) {
                return user;
            }
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
        }
        throw new IllegalArgumentException("AUTHENTICATION REQUIRED");
    }
}
