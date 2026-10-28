# Backend Refactoring Plan: Service-Oriented Architecture

**Version:** 1.0 | **Date:** 2024 | **Status:** Draft

---

## Overview

This document provides a **precise, step-by-step execution plan** for refactoring the `members-backend` from its current architecture (business logic mixed in resource classes) to a **service-oriented architecture** with high cohesion.

**Target Services:**
- `MemberService` - Core member CRUD and business logic
- `NotificationService` - All email/notification handling
- `OnBoardingService` - Keycloak integration and new member registration
- `AccessService` - Authorization and resource access control
- `HistoryService` - Enhanced (already exists)

---

## Phase 0: Preparation (1 day)

### Step 0.1: Setup Environment
```bash
cd /workspace/github__bslbckr__members/members-backend
./mvnw test                    # Verify all existing tests pass
```

### Step 0.2: Create Branches
```bash
git checkout main
git pull origin main
git checkout -b refactor/backup-before-refactoring  # Backup branch
git checkout main
git checkout -b refactor/service-layer                 # Working branch
```

### Step 0.3: Create Directory Structure
```bash
mkdir -p src/main/java/de/guc/api
mkdir -p src/main/java/de/guc/service
mkdir -p src/main/java/de/guc/repository
mkdir -p src/main/java/de/guc/infrastructure/keycloak
mkdir -p src/main/java/de/guc/templates
```

### Step 0.4: Tag Start Point
```bash
git add -A && git commit -m "refactor: initial setup - directory structure"
git tag refactor-start
```

---

## Phase 1: Repository Layer (2 days)

**Goal:** Extract data access from Panache entities into repository interfaces.

### Step 1.1: Create MemberRepository

**File:** `src/main/java/de/guc/repository/MemberRepository.java`
```java
package de.guc.repository;

import de.guc.entities.MemberEntity;
import de.guc.entities.MemberOverview;
import de.guc.entities.EntryOrCancellation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository {
    Optional<MemberEntity> findById(UUID id);
    Optional<MemberEntity> findById(UUID id, boolean locked);
    List<MemberEntity> findAll();
    List<MemberOverview> findAllActive();
    List<String> findAllActiveEmails();
    List<EntryOrCancellation> findEntriesSince(LocalDate date);
    void persist(MemberEntity entity);
}
```

**File:** `src/main/java/de/guc/repository/MemberRepositoryImpl.java`
```java
package de.guc.repository;

import de.guc.entities.*;
import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.*;

@ApplicationScoped
public class MemberRepositoryImpl implements MemberRepository {
    @Override public Optional<MemberEntity> findById(UUID id) { return MemberEntity.byId(id); }
    @Override public Optional<MemberEntity> findById(UUID id, boolean locked) { return MemberEntity.byId(id, locked); }
    @Override public List<MemberEntity> findAll() { return MemberEntity.listAll(); }
    @Override public List<MemberOverview> findAllActive() { return MemberEntity.activeMembers(); }
    @Override public List<String> findAllActiveEmails() { return MemberEntity.activeMemberEmails(); }
    @Override public List<EntryOrCancellation> findEntriesSince(LocalDate date) { return MemberEntity.entriesSinceDate(date); }
    @Override public void persist(MemberEntity entity) { entity.persist(); }
}
```

### Step 1.2: Create ResourcesRepository

**File:** `src/main/java/de/guc/repository/ResourcesRepository.java`
```java
package de.guc.repository;

import de.guc.entities.*;
import java.util.*;

public interface ResourcesRepository {
    List<ResourcesEntity> findByIdentity(String identity);
    Optional<ResourcesEntity> findAccess(UUID identity, UUID resource);
    void persist(ResourcesEntity entity);
}
```

**File:** `src/main/java/de/guc/repository/ResourcesRepositoryImpl.java`
```java
package de.guc.repository;

import de.guc.entities.*;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class ResourcesRepositoryImpl implements ResourcesRepository {
    @Override public List<ResourcesEntity> findByIdentity(String identity) { return ResourcesEntity.resourcesForIdentity(identity); }
    @Override public Optional<ResourcesEntity> findAccess(UUID identity, UUID resource) { return ResourcesEntity.hasAccess(identity, resource); }
    @Override public void persist(ResourcesEntity entity) { entity.persist(); }
}
```

### Step 1.3: Create EmailUpdateRepository

**File:** `src/main/java/de/guc/repository/EmailUpdateRepository.java`
```java
package de.guc.repository;
import de.guc.entities.EmailUpdateEntity;
import java.util.List;

public interface EmailUpdateRepository {
    void persist(EmailUpdateEntity entity);
    List<EmailUpdateEntity> findAll();
}
```

**File:** `src/main/java/de/guc/repository/EmailUpdateRepositoryImpl.java`
```java
package de.guc.repository;
import de.guc.entities.EmailUpdateEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class EmailUpdateRepositoryImpl implements EmailUpdateRepository {
    @Override public void persist(EmailUpdateEntity entity) { entity.persist(); }
    @Override public List<EmailUpdateEntity> findAll() { return EmailUpdateEntity.listAll(); }
}
```

### Step 1.4: Verify & Commit
```bash
./mvnw test
# Fix any issues
git add -A && git commit -m "refactor: add repository layer (MemberRepository, ResourcesRepository, EmailUpdateRepository)"
```

---

## Phase 2: NotificationService (3 days)

**Goal:** Centralize all email notification logic.

### Step 2.1: Create Email Templates

Create 5 template classes in `src/main/java/de/guc/templates/`:

**StateChangeTemplate.java:**
```java
package de.guc.templates;
import io.quarkus.qute.TemplateInstance;
public record StateChangeTemplate(String givenName, String name, String oldState, String newState, String dateEffective) implements TemplateInstance {}
```

**CancellationTemplate.java:**
```java
package de.guc.templates;
import io.quarkus.qute.TemplateInstance;
public record CancellationTemplate(String givenName, String name, String dateEffective) implements TemplateInstance {}
```

**WelcomeTemplate.java:**
```java
package de.guc.templates;
import io.quarkus.qute.TemplateInstance;
public record WelcomeTemplate(String firstName, String login, String password) implements TemplateInstance {}
```

**ImportedMemberTemplate.java:**
```java
package de.guc.templates;
import io.quarkus.qute.TemplateInstance;
public record ImportedMemberTemplate(String firstName, String login, String password) implements TemplateInstance {}
```

**NewMemberRegisteredTemplate.java:**
```java
package de.guc.templates;
import io.quarkus.qute.TemplateInstance;
public record NewMemberRegisteredTemplate(String givenName, String name, String newState, String dateEffective, String email) implements TemplateInstance {}
```

### Step 2.2: Create NotificationService

**File:** `src/main/java/de/guc/service/NotificationService.java`
```java
package de.guc.service;
import de.guc.entities.MemberEntity;
import java.util.List;

public interface NotificationService {
    void sendWelcomeEmail(MemberEntity member, String password);
    void sendImportedMemberEmail(MemberEntity member, String password);
    void sendStateChangeNotificationToBoard(MemberEntity member, String oldState, String newState);
    void sendCancellationNotificationToBoard(MemberEntity member);
    void sendBulkEmail(String subject, String body);
    void trackEmailChange(MemberEntity member, String oldEmail, String newEmail, boolean emailList);
    List<String> getActiveMemberEmails();
}
```

**File:** `src/main/java/de/guc/service/NotificationServiceImpl.java`
```java
package de.guc.service;

import de.guc.entities.*;
import de.guc.repository.*;
import de.guc.templates.*;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class NotificationServiceImpl implements NotificationService {

    @Inject Mailer mailer;
    @Inject MemberRepository memberRepository;
    @Inject EmailUpdateRepository emailUpdateRepository;

    @Override
    public void sendWelcomeEmail(MemberEntity member, String password) {
        sendEmail(member.email, "Herzlich Willkommen beim Goldfingers Ultimate Club e.V.", 
                  new WelcomeTemplate(member.givenName, member.name, password));
    }

    @Override
    public void sendImportedMemberEmail(MemberEntity member, String password) {
        sendEmail(member.email, "GUC Mitgliederverwaltung - Neues Konto angelegt",
                  new ImportedMemberTemplate(member.givenName, member.name, password));
    }

    @Override
    public void sendStateChangeNotificationToBoard(MemberEntity member, String oldState, String newState) {
        String subject = String.format("[GUC/Mitglieder] Statuswechsel %s %s", member.givenName, member.name);
        sendEmailToBoard(subject, new StateChangeTemplate(member.givenName, member.name, oldState, newState, member.stateEffective.toString()));
        Log.infof("Member %s changed state from %s to %s", member.id, oldState, newState);
    }

    @Override
    public void sendCancellationNotificationToBoard(MemberEntity member) {
        String subject = String.format("[GUC/Mitglieder] Kuendigung %s %s", member.givenName, member.name);
        sendEmailToBoard(subject, new CancellationTemplate(member.givenName, member.name, member.exitDate.toString()));
        Log.infof("Member %s cancelled its membership", member.id);
    }

    @Override
    public void sendBulkEmail(String subject, String body) {
        List<String> emails = getActiveMemberEmails();
        Log.infof("Sending bulk email to %d recipients", emails.size());
        Mail[] mails = emails.stream().map(r -> Mail.withText(r, subject, body)).toArray(Mail[]::new);
        mailer.send(mails);
    }

    @Override
    public void trackEmailChange(MemberEntity member, String oldEmail, String newEmail, boolean emailList) {
        if (member.emailList) {
            EmailUpdateEntity remove = new EmailUpdateEntity();
            remove.emailAddress = oldEmail; remove.change = EmailUpdateEntity.ChangeType.REMOVE;
            emailUpdateRepository.persist(remove);
        }
        if (emailList) {
            EmailUpdateEntity insert = new EmailUpdateEntity();
            insert.emailAddress = newEmail; insert.change = EmailUpdateEntity.ChangeType.INSERT;
            emailUpdateRepository.persist(insert);
        }
    }

    @Override
    public List<String> getActiveMemberEmails() {
        return memberRepository.findAllActiveEmails();
    }

    private void sendEmail(String to, String subject, TemplateInstance template) {
        Log.infof("Sending email to %s with subject: %s", to, subject);
        mailer.send(Mail.withText(to, subject, template.render()));
    }

    private void sendEmailToBoard(String subject, TemplateInstance template) {
        sendEmail("board@guc.example", subject, template);
    }
}
```

### Step 2.3: Update MemberResource

**Modify:** `src/main/java/de/guc/MemberResource.java`

1. Add imports:
```java
import de.guc.service.NotificationService;
import de.guc.service.AccessService;
```

2. Add fields:
```java
@Inject NotificationService notificationService;
@Inject AccessService accessService;
```

3. Remove fields:
```java
// Remove: @Inject Mailer mailer;
// Remove: @ConfigProperty(name = "guc.member.mail.state-changed") String toAddress;
// Remove: record StateChangeOrCancellation(...)
// Remove: record NewMemberRegistered(...)
```

4. Replace `checkStateUpdate` method:
```java
private void checkStateUpdate(@Nonnull MemberEntity member, @Nonnull MemberDto dto) {
    if (member.state == null) {
        notificationService.sendStateChangeNotificationToBoard(member, "null", dto.getState());
    }
    else if (!member.state.equals(dto.getState()) || (member.exitDate == null && dto.getExitDate() != null)) {
        if (dto.getExitDate() == null) {
            Log.infof("Member %1$s changed state from %2$s to %3$s", member.id, member.state, dto.getState());
            notificationService.sendStateChangeNotificationToBoard(member, member.state, dto.getState());
        } else {
            Log.infof("Member %1$s cancelled its membership", member.id);
            notificationService.sendCancellationNotificationToBoard(member);
        }
    }
}
```

5. Replace `checkEmailUpdate` method:
```java
private void checkEmailUpdate(@Nonnull MemberEntity member, @Nonnull MemberDto dto) {
    if (!member.email.equals(dto.getEmail())) {
        notificationService.trackEmailChange(member, member.email, dto.getEmail(), dto.isEmailList());
    } else if (member.emailList != dto.isEmailList()) {
        notificationService.trackEmailChange(member, member.email, member.email, dto.isEmailList());
    }
}
```

### Step 2.4: Update AdminResource

**Modify:** `src/main/java/de/guc/AdminResource.java`

1. Add import: `import de.guc.service.NotificationService;`
2. Add field: `@Inject NotificationService notificationService;`
3. Remove: `@Inject Mailer mailer;`
4. Remove: `@ConfigProperty(name = "guc.member.mail.state-changed") String toAddress;`
5. Remove: `public static TemplateInstance membershipCancellation(...)`
6. Update `sendNotificationMails`:
```java
private void sendNotificationMails(MemberEntity entity) {
    notificationService.sendCancellationNotificationToBoard(entity);
}
```

### Step 2.5: Verify & Commit
```bash
./mvnw test
# Fix any issues
git add -A && git commit -m "refactor: add NotificationService with all email templates and logic"
```

---

## Phase 3: AccessService (2 days)

**Goal:** Centralize authorization logic.

### Step 3.1: Create AccessService

**File:** `src/main/java/de/guc/service/AccessService.java`
```java
package de.guc.service;
import de.guc.entities.ResourcesEntity;
import de.guc.dto.ResourceDto;
import java.util.List;
import java.util.UUID;

public interface AccessService {
    void checkAuthorization(UUID identity, UUID resourceId, boolean readOnly);
    boolean hasAccess(UUID identity, UUID resourceId);
    void createAccessResource(UUID identity, UUID memberId, ResourcesEntity.ResourceType type, String role);
    List<ResourceDto> getAccessibleResources(UUID identity);
    List<ResourcesEntity> getResourcesForIdentity(String identity);
}
```

**File:** `src/main/java/de/guc/service/AccessServiceImpl.java`
```java
package de.guc.service;

import de.guc.entities.*;
import de.guc.dto.ResourceDto;
import de.guc.repository.*;
import io.quarkus.logging.Log;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.*;

@ApplicationScoped
public class AccessServiceImpl implements AccessService {

    @Inject ResourcesRepository resourcesRepository;
    @Inject MemberRepository memberRepository;

    @Override
    public void checkAuthorization(UUID identity, UUID resourceId, boolean readOnly) {
        if (!hasAccess(identity, resourceId)) {
            Log.warnf("Authorization failed: identity=%s, resource=%s", identity, resourceId);
            throw new UnauthorizedException();
        }
    }

    @Override
    public boolean hasAccess(UUID identity, UUID resourceId) {
        return resourcesRepository.findAccess(identity, resourceId).isPresent();
    }

    @Override
    @Transactional
    public void createAccessResource(UUID identity, UUID memberId, ResourcesEntity.ResourceType type, String role) {
        MemberEntity member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        ResourcesEntity access = new ResourcesEntity();
        access.identity = identity;
        access.member = member;
        access.role = role;
        access.rType = type;
        access.name = member.givenName;
        resourcesRepository.persist(access);
        Log.infof("Created access: identity=%s, member=%s, role=%s", identity, memberId, role);
    }

    @Override
    public List<ResourceDto> getAccessibleResources(UUID identity) {
        return resourcesRepository.findByIdentity(identity.toString()).stream()
            .map(res -> new ResourceDto(
                String.format("%s %s", res.member.givenName, res.member.name),
                String.format("%s", res.member.id), res.role))
            .toList();
    }

    @Override
    public List<ResourcesEntity> getResourcesForIdentity(String identity) {
        return resourcesRepository.findByIdentity(identity);
    }
}
```

### Step 3.2: Update MemberResource

Replace `checkAuthorization`:
```java
private void checkAuthorization(final UUID resourceId, final boolean read) {
    final String subject = this.accessToken.getSubject();
    accessService.checkAuthorization(UUID.fromString(subject), resourceId, read);
}
```

### Step 3.3: Update ConfigurationResource

**Modify:** `src/main/java/de/guc/ConfigurationResource.java`

1. Add: `import de.guc.service.AccessService;`
2. Add: `@Inject AccessService accessService;`
3. Update method:
```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public List<ResourceDto> getAccessibleResource() {
    final String subject = this.accessToken.getSubject();
    return accessService.getAccessibleResources(UUID.fromString(subject));
}
```

### Step 3.4: Verify & Commit
```bash
./mvnw test
git add -A && git commit -m "refactor: add AccessService for authorization"
```

---

## Phase 4: MemberService (3 days)

**Goal:** Extract member business logic.

### Step 4.1: Create MemberService

**File:** `src/main/java/de/guc/service/MemberService.java`
```java
package de.guc.service;
import de.guc.dto.MemberDto;
import de.guc.entities.MemberEntity;
import de.guc.entities.MemberOverview;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MemberService {
    MemberDto getMemberById(UUID memberId);
    List<MemberOverview> getAllActiveMembers();
    MemberDto updateMember(UUID memberId, MemberDto memberDto);
    void cancelMembership(UUID memberId, LocalDate exitDate);
}
```

**File:** `src/main/java/de/guc/service/MemberServiceImpl.java`
```java
package de.guc.service;

import de.guc.dto.MemberDto;
import de.guc.entities.MemberEntity;
import de.guc.entities.MemberOverview;
import de.guc.repository.MemberRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MemberServiceImpl implements MemberService {

    @Inject MemberRepository memberRepository;
    @Inject NotificationService notificationService;

    @Override
    public MemberDto getMemberById(UUID memberId) {
        MemberEntity member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("Member not found: " + memberId));
        return MemberDto.fromEntity(member);
    }

    @Override
    public List<MemberOverview> getAllActiveMembers() {
        return memberRepository.findAllActive();
    }

    @Override
    @Transactional
    public MemberDto updateMember(UUID memberId, MemberDto memberDto) {
        if (!memberId.equals(memberDto.getId())) {
            Log.warnf("Attempt to store member %s under id %s", memberDto.getId(), memberId);
            throw new BadRequestException();
        }
        
        MemberEntity existing = memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("Member not found: " + memberId));
        
        boolean stateChanged = memberDto.getState() != null && !memberDto.getState().equals(existing.state);
        boolean emailChanged = memberDto.getEmail() != null && !memberDto.getEmail().equals(existing.email);
        
        if (stateChanged) {
            String oldState = existing.state;
            existing.state = memberDto.getState();
            existing.stateEffective = memberDto.getStateEffective();
            notificationService.sendStateChangeNotificationToBoard(existing, oldState, memberDto.getState());
        }
        
        if (emailChanged) {
            String oldEmail = existing.email;
            boolean oldList = existing.emailList;
            existing.email = memberDto.getEmail();
            existing.emailList = memberDto.isEmailList();
            notificationService.trackEmailChange(existing, oldEmail, memberDto.getEmail(), memberDto.isEmailList());
        }
        
        updateFields(existing, memberDto);
        memberRepository.persist(existing);
        return MemberDto.fromEntity(existing);
    }

    @Override
    @Transactional
    public void cancelMembership(UUID memberId, LocalDate exitDate) {
        MemberEntity member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("Member not found: " + memberId));
        member.exitDate = exitDate;
        memberRepository.persist(member);
        notificationService.sendCancellationNotificationToBoard(member);
    }

    private void updateFields(MemberEntity member, MemberDto dto) {
        member.city = dto.getCity();
        member.dayOfBirth = dto.getDayOfBirth();
        member.dse = dto.isDse();
        member.gender = dto.getGender();
        member.givenName = dto.getGivenName();
        member.mobile = dto.getMobile();
        member.name = dto.getName();
        member.rabatt = dto.isDfvDiscount();
        member.street = dto.getStreet();
        member.zipCode = dto.getZipCode();
    }
}
```

### Step 4.2: Update MemberResource

Replace all method bodies to delegate to MemberService:

```java
@GET
@Path("{id}")
@NoCache
@Produces(MediaType.APPLICATION_JSON)
public MemberDto getMemberById(@RestPath("id") String memberId) {
    checkAuthorization(UUID.fromString(memberId), true);
    return memberService.getMemberById(UUID.fromString(memberId));
}

@GET
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(Constants.ROLE_BOARD_MEMBER)
public List<MemberOverview> getAllActiveMembers() {
    return memberService.getAllActiveMembers();
}

@PUT
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Transactional
@Path("{id}")
public MemberDto updateMember(@RestPath("id") String memberId, @Valid MemberDto member) {
    UUID memberUuid = UUID.fromString(memberId);
    this.checkAuthorization(memberUuid, false);
    return memberService.updateMember(memberUuid, member);
}
```

Remove private methods: `checkEmailUpdate`, `checkStateUpdate`, `updateMember`

Add field: `@Inject MemberService memberService;`

### Step 4.3: Verify & Commit
```bash
./mvnw test
git add -A && git commit -m "refactor: add MemberService for member business logic"
```

---

## Phase 5: OnBoardingService (4 days)

**Goal:** Extract Keycloak and onboarding logic.

### Step 5.1: Create KeycloakClient Wrapper

**File:** `src/main/java/de/guc/infrastructure/keycloak/KeycloakClient.java`
```java
package de.guc.infrastructure.keycloak;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class KeycloakClient {

    @Inject Keycloak adminClient;

    public UserResource getUserResource(String userId) {
        return adminClient.realm("guc").users().get(userId);
    }

    public List<UserRepresentation> searchUsersByEmail(String email, boolean exact) {
        return adminClient.realm("guc").users().searchByEmail(email, exact);
    }

    public String createUser(UserRepresentation user) {
        var response = adminClient.realm("guc").users().create(user);
        return org.keycloak.admin.client.CreatedResponseUtil.getCreatedId(response);
    }

    public void resetPassword(UserResource userResource, String password) {
        CredentialRepresentation creds = new CredentialRepresentation();
        creds.setTemporary(true);
        creds.setType(CredentialRepresentation.PASSWORD);
        creds.setValue(password);
        userResource.resetPassword(creds);
    }

    public UserRepresentation getUser(String userId) {
        return adminClient.realm("guc").users().get(userId).toRepresentation();
    }

    public void updateUser(UserResource userResource, UserRepresentation user) {
        userResource.update(user);
    }
}
```

### Step 5.2: Create OnBoardingService

**File:** `src/main/java/de/guc/service/OnBoardingService.java`
```java
package de.guc.service;

import de.guc.dto.*;
import java.util.List;
import java.util.UUID;

public interface OnBoardingService {
    void onboardNewMember(OnBoardingDto newMember);
    void importExistingMembers(List<MemberWithLoginDto> members);
}
```

**File:** `src/main/java/de/guc/service/OnBoardingServiceImpl.java`
```java
package de.guc.service;

import de.guc.Constants;
import de.guc.dto.*;
import de.guc.entities.*;
import de.guc.infrastructure.keycloak.KeycloakClient;
import de.guc.repository.*;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.keycloak.representations.idm.GroupRepresentation;
import java.time.LocalDate;
import java.util.*;

@ApplicationScoped
public class OnBoardingServiceImpl implements OnBoardingService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[\\Q!\"\u00a7$%&/()?#+*,.;:\\E])");

    @Inject KeycloakClient keycloakClient;
    @Inject MemberRepository memberRepository;
    @Inject AccessService accessService;
    @Inject NotificationService notificationService;
    @Inject EmailUpdateRepository emailUpdateRepository;

    @Override
    @Transactional
    public void onboardNewMember(OnBoardingDto newMember) {
        Log.infof("Starting on-boarding of %s %s", newMember.memberFirstName(), newMember.memberName());
        OnBoardingResult result = registerUser(newMember);
        MemberEntity member = createMemberFromDto(newMember);
        memberRepository.persist(member);

        if (!newMember.memberIsChild()) {
            EmailUpdateEntity emailUpdate = new EmailUpdateEntity();
            emailUpdate.emailAddress = newMember.email();
            emailUpdate.change = EmailUpdateEntity.ChangeType.INSERT;
            emailUpdateRepository.persist(emailUpdate);
        }

        accessService.createAccessResource(UUID.fromString(result.keycloakId()), member.id,
            ResourcesEntity.ResourceType.MEMBER, newMember.memberIsChild() ? "PARENT" : "SELF");
        notificationService.sendWelcomeEmail(member, result.password());
    }

    @Override
    @Transactional
    public void importExistingMembers(List<MemberWithLoginDto> importedMembers) {
        importedMembers.forEach(mwl -> {
            boolean isChild = mwl.isChild();
            OnBoardingDto dto = new OnBoardingDto(
                isChild ? mwl.getParentFirstName() : mwl.getGivenName(),
                isChild ? mwl.getParentLastName() : mwl.getName(),
                mwl.getEmail(), mwl.getLogin(), mwl.getDayOfBirth(), isChild, "", "");
            OnBoardingResult result = registerUser(dto);

            MemberEntity member = new MemberEntity();
            member.city = mwl.getCity();
            member.givenName = isChild ? mwl.getParentFirstName() : mwl.getGivenName();
            member.name = isChild ? mwl.getParentLastName() : mwl.getName();
            member.gender = mwl.getGender();
            member.dayOfBirth = mwl.getDayOfBirth();
            member.entryDate = mwl.getEntryDate();
            member.state = mwl.getState();
            member.stateEffective = mwl.getStateEffective();
            member.dfvNumber = mwl.getDfvNumber();
            member.dse = mwl.isDse();
            member.rabatt = mwl.isDfvDiscount();
            member.street = mwl.getStreet();
            member.zipCode = mwl.getZipCode();
            member.mobile = mwl.getMobile();
            member.email = mwl.getEmail();
            member.emailList = mwl.isEmailList();
            memberRepository.persist(member);

            accessService.createAccessResource(UUID.fromString(result.keycloakId()), member.id,
                ResourcesEntity.ResourceType.MEMBER, isChild ? "PARENT" : "SELF");
            notificationService.sendImportedMemberEmail(member, result.password());
        });
    }

    private OnBoardingResult registerUser(OnBoardingDto dto) {
        var users = keycloakClient.searchUsersByEmail(dto.email(), true);
        if (users.isEmpty()) {
            UserRepresentation ur = mapDto(dto);
            String userId = keycloakClient.createUser(ur);
            UserResource userRes = keycloakClient.getUserResource(userId);
            String password = generateSecurePassword();
            keycloakClient.resetPassword(userRes, password);
            return new OnBoardingResult(userId, password, ur.getUsername(), null);
        } else {
            UserRepresentation existing = users.get(0);
            var resources = accessService.getResourcesForIdentity(existing.getId());
            boolean isMember = resources.stream().anyMatch(r -> "SELF".equals(r.role));
            if (dto.memberIsChild() || !isMember) {
                existing.setEnabled(true);
                UserResource userRes = keycloakClient.getUserResource(existing.getId());
                boolean inGroup = userRes.groups().stream().map(GroupRepresentation::getName)
                    .anyMatch(g -> Constants.GROUP_GUC_MEMBERS.equals(g));
                if (!inGroup) existing.getGroups().add(Constants.GROUP_GUC_MEMBERS);
                keycloakClient.updateUser(userRes, existing);
                return new OnBoardingResult(existing.getId(), "UNCHANGED", existing.getUsername(), null);
            }
            throw new IllegalStateException("User with email exists as member");
        }
    }

    private UserRepresentation mapDto(OnBoardingDto d) {
        UserRepresentation ur = new UserRepresentation();
        ur.setEnabled(true);
        ur.setFirstName(d.firstName());
        ur.setLastName(d.name());
        ur.setEmail(d.email());
        ur.setEmailVerified(true);
        ur.setUsername(d.login());
        ur.setGroups(Arrays.asList("GUC Members"));
        return ur;
    }

    private MemberEntity createMemberFromDto(OnBoardingDto dto) {
        MemberEntity member = new MemberEntity();
        member.name = dto.consolidatedName();
        member.givenName = dto.consolidatedFirstName();
        member.email = dto.email();
        member.entryDate = LocalDate.now();
        member.dayOfBirth = dto.dob();
        return member;
    }

    private String generateSecurePassword() {
        String password;
        Matcher matcher;
        do {
            password = RandomStringUtils.random(16,
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\u00a7$%&/()?#+*,.;:");
            matcher = PASSWORD_PATTERN.matcher(password);
        } while (!matcher.find());
        return password;
    }

    private record OnBoardingResult(String keycloakId, String password, String login, UUID memberId) {}
}
```

### Step 5.3: Update OnBoardingResource

**Modify:** `src/main/java/de/guc/OnBoardingResource.java`

1. Add: `import de.guc.service.OnBoardingService;`
2. Add: `@Inject OnBoardingService onBoardingService;`
3. Remove: `@Inject Keycloak adminClient;`
4. Remove: `@Inject Mailer mailer;`
5. Remove all private methods (move to service)
6. Simplify methods:

```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Transactional
public void OnBoardNewMember(@Valid OnBoardingDto newMember) {
    onBoardingService.onboardNewMember(newMember);
}

@POST
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Transactional
@Path("import")
public void ImportExistingMembers(@Valid @NotEmpty List<MemberWithLoginDto> importedMembers) {
    onBoardingService.importExistingMembers(importedMembers);
}
```

### Step 5.4: Verify & Commit
```bash
./mvnw test
git add -A && git commit -m "refactor: add OnBoardingService with Keycloak integration"
```

---

## Phase 6: Final Updates (2 days)

### Step 6.1: Move Resources to API Package
```bash
cd src/main/java/de/guc
mv MemberResource.java OnBoardingResource.java EmailResource.java ChangeResource.java AdminResource.java ConfigurationResource.java api/
```

Update package declarations in all moved files to `package de.guc.api;`

### Step 6.2: Update EmailResource

**Modify:** `src/main/java/de/guc/api/EmailResource.java`
```java
package de.guc.api;

import de.guc.dto.EmailDto;
import de.guc.service.NotificationService;
import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;

@Path("v1/email")
@Authenticated
public class EmailResource {
    @Inject NotificationService notificationService;

    @POST
    @RolesAllowed("board-member")
    @Consumes(MediaType.APPLICATION_JSON)
    public void send(@Valid EmailDto email) {
        Log.infof("Sending mail with subject: %s", email.getSubject());
        notificationService.sendBulkEmail(email.getSubject(), email.getBody());
    }
}
```

### Step 6.3: Update AdminResource

**Modify:** `src/main/java/de/guc/api/AdminResource.java`
```java
package de.guc.api;

import de.guc.Constants;
import de.guc.dto.*;
import de.guc.service.MemberService;
import io.quarkus.logging.Log;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import java.time.LocalDate;
import java.util.List;

@Path("v1/admin")
@RolesAllowed("onboarding")
public class AdminResource {

    @Inject MemberService memberService;

    @POST @Path("updateDfv")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateDfvNumbers(@Valid List<SetDFVNumberDto> updatedNumbers) {
        boolean allOk = updatedNumbers.stream().allMatch(dto -> {
            var member = MemberEntity.byId(dto.getUuid());
            if (member.isPresent()) {
                member.get().dfvNumber = dto.getDfvNumber();
                member.get().persist();
                Log.infof("Set dfvNumber of %s to %d", dto.getUuid(), dto.getDfvNumber());
                return true;
            }
            Log.warnf("Member %s not found", dto.getUuid());
            return false;
        });
        return allOk ? Response.ok().build() : Response.serverError().build();
    }

    @POST @Path("cancelMembership")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public void cancelMemberships(@Valid CancelMembershipDto cancelled) {
        cancelled.ids().forEach(id -> memberService.cancelMembership(id, cancelled.cancellationDate()));
    }
}
```

### Step 6.4: Clean Up
- Remove all unused imports
- Remove duplicate code
- Run tests

### Step 6.5: Verify & Commit
```bash
./mvnw test
git add -A && git commit -m "refactor: move resources to api package and final updates"
```

---

## Phase 7: Documentation (1 day)

- Update README.md with new architecture
- Add architecture diagram
- Final verification

```bash
git add -A && git commit -m "refactor: complete service-oriented architecture - update docs"
```

---

## Summary Checklist

- [ ] Phase 0: Preparation complete
- [ ] Phase 1: Repository layer complete
- [ ] Phase 2: NotificationService complete
- [ ] Phase 3: AccessService complete
- [ ] Phase 4: MemberService complete
- [ ] Phase 5: OnBoardingService complete
- [ ] Phase 6: Final updates complete
- [ ] Phase 7: Documentation complete
- [ ] All tests pass
- [ ] All commits pushed

---

## Quick Reference

### Test Commands
```bash
# Full test suite
./mvnw test

# Compile only
./mvnw compile

# Run specific test
./mvnw test -Dtest=PasswordMatcherTest
```

### Git Commands
```bash
# Check status
git status

# Add all changes
git add -A

# Commit
git commit -m "message"

# Create branch
git checkout -b branch-name
```

---

**Note:** Follow phases in order. After each phase, run `./mvnw test` and commit only when all tests pass.
