CREATE TABLE IF NOT EXISTS public.system_domain (
    id bigint NOT NULL,
    domain_name character varying
);

CREATE SEQUENCE IF NOT EXISTS public.system_domain_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE IF EXISTS public.system_domain_id_seq OWNED BY public.system_domain.id;

CREATE TABLE IF NOT EXISTS public.system_permission (
    id bigint NOT NULL,
    permission_code character varying NOT NULL,
    permission_name character varying,
    domain character varying
);

CREATE SEQUENCE IF NOT EXISTS public.system_permission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE IF EXISTS public.system_permission_id_seq OWNED BY public.system_permission.id;


CREATE TABLE IF NOT EXISTS public.system_role (
    id bigint NOT NULL,
    role_name character varying NOT NULL,
    role_code character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    domain character varying
);

CREATE SEQUENCE IF NOT EXISTS public.system_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE IF EXISTS public.system_role_id_seq OWNED BY public.system_role.id;

CREATE TABLE IF NOT EXISTS public.system_role_permission_assignment (
    id bigint NOT NULL,
    permission_code character varying NOT NULL,
    role_code character varying NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS public.system_role_permission_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE IF EXISTS public.system_role_permission_assignment_id_seq OWNED BY public.system_role_permission_assignment.id;


CREATE TABLE IF NOT EXISTS public.system_user (
    id bigint NOT NULL,
    first_name character varying,
    last_name character varying,
    password character varying NOT NULL,
    email character varying NOT NULL,
    username character varying,
    role_code character varying,
    created_at timestamp without time zone DEFAULT now(),
    last_logged_in_at timestamp without time zone,
    is_active boolean DEFAULT false
);


CREATE SEQUENCE IF NOT EXISTS public.system_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE IF EXISTS public.system_user_id_seq OWNED BY public.system_user.id;

ALTER TABLE ONLY public.system_domain ALTER COLUMN id SET DEFAULT nextval('public.system_domain_id_seq'::regclass);

ALTER TABLE ONLY public.system_permission ALTER COLUMN id SET DEFAULT nextval('public.system_permission_id_seq'::regclass);

ALTER TABLE ONLY public.system_role ALTER COLUMN id SET DEFAULT nextval('public.system_role_id_seq'::regclass);

ALTER TABLE ONLY public.system_role_permission_assignment ALTER COLUMN id SET DEFAULT nextval('public.system_role_permission_assignment_id_seq'::regclass);

ALTER TABLE ONLY public.system_user ALTER COLUMN id SET DEFAULT nextval('public.system_user_id_seq'::regclass);

ALTER TABLE ONLY public.system_domain
    ADD CONSTRAINT system_domain_domain_name_key UNIQUE (domain_name);

ALTER TABLE ONLY public.system_domain
    ADD CONSTRAINT system_domain_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.system_permission
    ADD CONSTRAINT system_permission_permission_code_key UNIQUE (permission_code);

ALTER TABLE ONLY public.system_permission
    ADD CONSTRAINT system_permission_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.system_role_permission_assignment
    ADD CONSTRAINT system_role_permission_assignment_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.system_role
    ADD CONSTRAINT system_role_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.system_role
    ADD CONSTRAINT system_role_role_code_key UNIQUE (role_code);

ALTER TABLE ONLY public.system_user
    ADD CONSTRAINT system_user_email_key UNIQUE (email);

ALTER TABLE ONLY public.system_user
    ADD CONSTRAINT system_user_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.system_user
    ADD CONSTRAINT system_user_username_key UNIQUE (username);

ALTER TABLE ONLY public.system_role_permission_assignment
    ADD CONSTRAINT system_role_permission_assignment_system_permission_code_fk FOREIGN KEY (permission_code) REFERENCES public.system_permission(permission_code) ON DELETE CASCADE;

ALTER TABLE ONLY public.system_role_permission_assignment
    ADD CONSTRAINT system_role_permission_assignment_system_role_role_code_fk FOREIGN KEY (role_code) REFERENCES public.system_role(role_code) ON DELETE CASCADE;
