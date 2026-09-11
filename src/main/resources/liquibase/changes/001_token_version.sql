--liquibase formatted sql

--changeset moonlight:token_version
ALTER TABLE public.system_user
    ADD COLUMN IF NOT EXISTS token_version integer NOT NULL DEFAULT 0;
