package com.servicecops.project.permissions;

import com.servicecops.project.models.jpahelpers.enums.AppDomains;

/**
 * This class will hold all our permissions we are using in the system
 *
 * It comes with an example of how to register a new permission.
 *
 * NOTE:- If your app does not follow the architecture of domains, you can always pass null as the domain in the last parameter of a permission
 */
public class Permisions {
    Permission ADMINISTRATOR = new Permission("ADMINISTRATOR", "Can administer the system", AppDomains.BACK_OFFICE, true);
    Permission ASSIGNS_PERMISSIONS = new Permission("ASSIGNS_PERMISSIONS", "Can Assign Permissions to roles", AppDomains.BACK_OFFICE, true);
}
