# Changelog

Versioning starts at **0.0.1**. Git tags are the template version. Maven coordinates stay `com.servicecops:project:0.0.1-SNAPSHOT`.

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
