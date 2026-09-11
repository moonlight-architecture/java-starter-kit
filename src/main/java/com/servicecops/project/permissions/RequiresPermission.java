package com.servicecops.project.permissions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires authentication plus one {@link Perms} catalog entry.
 * Compiled into {@code SecurityPolicy} at boot the same way as the library
 * string {@code @RequiresPermission}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    Perms value();

    String message() default "NOT AUTHORIZED";
}
