# Changelog

Versioning starts at **0.0.1**. Git tags are the template version. Maven coordinates stay `com.servicecops:project:0.0.1-SNAPSHOT`.

## v0.0.3 — 2026-09-11

- Ships Moonlight Java SDK `com.jet.moonlight:jet:0.0.3`
- Login returns `UserView` (password never serialized); JWT `token_version` bumped atomically for single-session
- Permission catalog is `Perms` (`JetPermission` enum) — upsert on boot, fail on unknown action slugs
- `RolePermissionCache` caches role name/domain/permissions; `RoleGrantService` always invalidates after grant/revoke
- Authenticated request target: **1** `system_user` read after cache warm (role metadata from cache)
- `auth/me` demonstrates `@Authenticated` + `@RequiresPermission`; login uses `@RateLimit` (per-node / per-IP)
- `JetRepository`: `getRequired`, `saveAndRefresh`, `page(JetRequest)`
- `SystemUserModel` mapped with `schema = "public"` so PostgreSQL does not treat `system_user` as a reserved keyword
- Dropped `system_domain` table/model; tighter CORS, security headers, Hikari defaults; prod/uat reject dummy JWT secret
- Removed direct `fastjson2` / `commons-lang3` dependencies
- MIT license; local Maven repo ships only SDK `0.0.3`; template `src/test` stays empty; stress harness is gitignored

## v0.0.2 — 2026-09-07

- Ships Moonlight Java SDK `com.jet.moonlight:jet:0.0.2`
- Datasource URL, username, and password live in `application.properties` so Spring Boot 4 binds them on first start (IntelliJ and `moonlight run`). Profile files only override.
- Removed unused search/filter package, SPA routing, empty annotation/api folders, Quartz, ModelMapper, `project_db` catalog on entities, and old `UniversalService.requires` JSONObject checks

## v0.0.1 — 2026-09-06

Initial public release of the Moonlight Java starter kit (`java-starter-kit`).

### Library

- Ships Moonlight Java SDK `com.jet.moonlight:jet:0.0.1` in `local-maven-repo`
- HTTP is served by `jet.api` auto-routes — no `WebActionsController`
- `JetRequest` field map, compiled security, observers, JSON / form / multipart

### Platform

- Java 25, Spring Boot 4.1.0 / Framework 7
- `spring-boot-starter-webmvc` + Jetty
- JWT via `JWT_SECRET`; docs public until `JET_DOCS_TOKEN` is set
