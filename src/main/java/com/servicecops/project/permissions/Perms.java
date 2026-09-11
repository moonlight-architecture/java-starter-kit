package com.servicecops.project.permissions;

import com.jet.moonlight.security.JetPermission;
import com.servicecops.project.models.jpahelpers.enums.AppDomains;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Application permission catalog. Boot upserts these by {@link #code()} — never wipe-and-replace.
 * Use with {@link RequiresPermission} / {@link RequiresAnyPermission}.
 */
public enum Perms implements JetPermission {
    ADMINISTRATOR("Can administer the system", AppDomains.BACK_OFFICE, true),
    ASSIGNS_PERMISSIONS("Can assign permissions to roles", AppDomains.BACK_OFFICE, true),
    USERS_VIEW("View own profile", AppDomains.ALL, true);

    private final String displayName;
    private final AppDomains domain;
    private final boolean shipWithAdmin;

    Perms(String displayName, AppDomains domain, boolean shipWithAdmin) {
        this.displayName = displayName;
        this.domain = domain;
        this.shipWithAdmin = shipWithAdmin;
    }

    public String displayName() {
        return displayName;
    }

    public AppDomains domain() {
        return domain;
    }

    public boolean shipWithAdmin() {
        return shipWithAdmin;
    }

    public static Set<String> codes() {
        return Arrays.stream(values()).map(Perms::code).collect(Collectors.toUnmodifiableSet());
    }
}
