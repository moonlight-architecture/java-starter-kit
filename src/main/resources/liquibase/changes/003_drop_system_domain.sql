--liquibase formatted sql

--changeset moonlight:drop_system_domain
DROP TABLE IF EXISTS public.system_domain CASCADE;
DROP SEQUENCE IF EXISTS public.system_domain_id_seq;
