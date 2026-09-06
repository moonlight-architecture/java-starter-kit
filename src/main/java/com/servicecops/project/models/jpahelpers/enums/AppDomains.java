package com.servicecops.project.models.jpahelpers.enums;

/**
 * App domains are a way of categorizing your users.
 * This will become more handy while categorizing permissions
 *
 * Please feel free to change the default domains here to anything you want.
 * After Changing, remember to re-run the project for these changes to be reflected in your database
 */
public enum AppDomains {
    BACK_OFFICE, // Users in this domain are more of administrators
    CLIENT_SIDE, // roles here will only affect the client side, should not even be visible on the BACK_OFFICE
    ALL // roles that cut across all domains example is like "VIEW_USERS", everyone can view em but with extra perms
}
