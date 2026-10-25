# Members Backend - Agent Context

## Overview

Java backend service for managing member operations, written with Quarkus 3.40.1.1.

## Tech Stack

- **Framework**: Quarkus 3.40.1.1
- **Java**: 17+
- **Build**: Maven
- **Persistence**: Mariadb + Hibernate ORM Panache
- **Schema migrations**: Liquibase
- **REST**: Quarkus REST with Jackson
- **Auth**: OIDC (Keycloak)
- **Email**: Quarkus Mailer
- **Validation**: Hibernate Validator
- **API docs**: SmallRye OpenAPI
- **Testing**: JUnit, REST Assured (H2 test DB)
- **Native**: Native image support enabled
- **Audit**: Hibernate Envers

## Architecture

### REST APIs

- `/member/{id}`
  - `MemberResource.java`
  - CRUD for member entities, change tracking

- `/changes` (GET)
  - `ChangeResource.java`
  - List history entries

- `/history/emails/{email}`
  - `EmailResource.java`
  - History of email address changes

- `/set-dfv-number/{id}` (GET/POST)
  - `ConfigurationResource.java`
  - Set DFV (Digital Fingerprint) numbers

- `/on-boarding/{id}` (GET/POST)
  - `OnBoardingResource.java`
  - Member onboarding flow
  - dto: `OnBoardingDto.java`, `ValidOnBoarding.java`

- `/member/{id}` (GET)
  - returns `MemberWithLoginDto`

- `/admin` (POST/DELETE)
  - `AdminResource.java`
  - Administrative operations

- `/resource` (GET/POST/DELETE)
  - resources CRUD
  - dto: `ResourceDto.java`

- `/member-overviews` (GET)
  - `MemberOverview`
  - List members

### Dto Layer (`/dto`)

- `MemberDto.java`
- `MemberWithLoginDto.java`
- `ResourceDto.java`
- `OnBoardingDto.java` (nested `ValidOnBoarding.java`)
- `StateChangeDto.java`
- `CancelMembershipDto.java`
- `EmailDto.java`
- `SetDFVNumberDto.java`
- `HistoryEntryDto.java`

### Entities (Panache)

- `MemberEntity.java`
- `ResourcesEntity.java`
- `EmailUpdateEntity.java`
- `EntryOrCancellation.java`

### Audit / History

`hibernate-envers` tracks entity changes.

- `HistoryServiceImpl.java` / `HistoryService.java` (service interface)

### Services

- `EmailResource.java` (email CRUD)
- `ConfigurationResource.java` (config changes)

### Validation

- `OnBoardingDtoValidator.java` (DTO validation)
- `PasswordMatcherTest.java` (test for password matching)

## Layout

```
src/
  main/java/de/guc/
    MemberResource.java
    MemberOverview.java
    MemberEntity.java
    EmailUpdateEntity.java
    EntryOrCancellation.java
    dto/OnBoardingDto.java
    dto/ValidOnBoarding.java
    dto/StateChangeDto.java
    dto/SetDFVNumberDto.java
    dto/ResourceDto.java
   .dto/OnBoardingDtoValidator.java
    dto/MemberWithLoginDto.java
    dto/MemberDto.java
    dto/HistoryEntryDto.java
    dto/EmailDto.java
    dto/CancelMembershipDto.java
    OnBoardingResource.java
    ChangeResource.java
    HistoryServiceImpl.java
    HistoryService.java
    EmailResource.java
    ConfigurationResource.java
    GreetingResource.java
    AdminResource.java
    Constants.java
  test/java/de/guc/
    PasswordMatcherTest.java
    ConfigurationResourceTest.java
    UUIDTest.java
    dto/OnBoardingDtoTest.java
    dto/OnBoardingDtoValidatorTest.java
```

## Development

Run `mvn compile quarkus:dev` to start the dev server.

## Profiles

- `dev` → loads resources from `src/dev/resources`
- `native` → builds native executable
