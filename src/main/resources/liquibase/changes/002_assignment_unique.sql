--liquibase formatted sql

--changeset moonlight:assignment_unique
ALTER TABLE public.system_role_permission_assignment
    DROP CONSTRAINT IF EXISTS system_role_permission_assignment_role_perm_uq;

ALTER TABLE public.system_role_permission_assignment
    ADD CONSTRAINT system_role_permission_assignment_role_perm_uq
        UNIQUE (role_code, permission_code);
