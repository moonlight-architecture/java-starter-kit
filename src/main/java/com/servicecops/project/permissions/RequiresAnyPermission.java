package com.servicecops.project.permissions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires authentication plus any one of the listed {@link Perms} entries.
 * Compiled into {@code SecurityPolicy} at boot the same way as the library
 * string {@code @RequiresAnyPermission}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAnyPermission {
    Perms[] value();

    String message() default "NOT AUTHORIZED";
}
