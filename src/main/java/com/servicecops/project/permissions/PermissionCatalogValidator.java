package com.servicecops.project.permissions;

import com.jet.moonlight.core.ActionRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Fails boot when an action declares a permission slug that is not in {@link Perms}.
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class PermissionCatalogValidator {

    private final ActionRegistry actionRegistry;

    @PostConstruct
    void rejectUnknownSlugs() {
        Set<String> catalog = Perms.codes();
        for (String slug : actionRegistry.declaredPermissions()) {
            if (!catalog.contains(slug)) {
                throw new IllegalStateException("Unknown permission on an @Action: " + slug);
            }
        }
    }
}
