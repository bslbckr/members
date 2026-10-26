# Data Model Documentation

## Overview

The backend uses **Liquibase** for database versioning and supports **two database environments**:
- **Production**: MariaDB (does not natively support UUID)
- **Testing**: H2 in-memory database (local development)

The data model centers around **member management** for what appears to be a German sports club (DFV = Deutscher Fußball-Verband), with comprehensive personal data, access control, and full audit history.

---

## Database Environments

| Environment | Database | Context | Purpose |
|-------------|----------|---------|---------|
| Production | MariaDB | (default) | Live system |
| Testing | H2 | `@test` | Local development & automated tests |

> **Note**: Liquibase's `context` attribute controls which changesets are applied. Changesets with `context="@test"` are **only** executed when the `@test` context is active (H2 environment). Changesets without a context are applied to **both** environments.

---

## UUID Handling

MariaDB does **not** natively support the UUID type. Therefore, all UUID fields are stored as `binary(16)` in MariaDB, while H2 uses native UUID.

| Logical Field | MariaDB Type | H2 Type | Conversion |
|---------------|--------------|---------|------------|
| UUID fields | `binary(16)` | `UUID` | `UNHEX()` / `HEX()` |

**Affected Tables/Columns:**
- `members.id`
- `resource_access.member`
- `resource_access.identity`
- `members_AUD.id`

**Example (MariaDB):**
```sql
-- Storing a UUID
INSERT INTO members (id, ...) VALUES (UNHEX('8b3a2dfabcac4d3e8efabf9ed0abc04d'), ...);

-- Querying a UUID
SELECT HEX(id) FROM members WHERE ...;
```

---

## Tables

### 1. `members`

The central entity table storing all member information.

| Column | MariaDB Type | H2 Type | Nullable | Description |
|--------|--------------|---------|----------|-------------|
| `id` | `binary(16)` | `UUID` | NO | Primary key |
| `given_name` | `VARCHAR(64)` | `VARCHAR(64)` | YES | First name |
| `name` | `VARCHAR(64)` | `VARCHAR(64)` | YES | Last name |
| `entry_date` | `DATE` | `DATE` | YES | Date when member joined |
| `day_of_birth` | `DATE` | `DATE` | YES | Date of birth |
| `gender` | `VARCHAR(6)` | `VARCHAR(6)` | YES | Gender (e.g., "male", "female") |
| `state` | `VARCHAR(11)` | `VARCHAR(11)` | YES | Membership status (e.g., "berufstätig", "passiv") |
| `state_effective` | `DATE` | `DATE` | YES | Date when `state` becomes effective |
| `dfv_number` | `INTEGER` | `INTEGER` | YES | DFV (Deutscher Fußball-Verband) membership number |
| `dse` | `BIT` | `BOOLEAN` | NO | Data protection agreement signed |
| `rabatt` | `BIT` | `BOOLEAN` | NO | Eligible for discount |
| `street` | `VARCHAR(128)` | `VARCHAR(128)` | YES | Street address |
| `zip_code` | `VARCHAR(5)` | `VARCHAR(5)` | YES | Postal code |
| `city` | `VARCHAR(64)` | `VARCHAR(64)` | YES | City |
| `mobile` | `VARCHAR(24)` | `VARCHAR(24)` | YES | Mobile phone number |
| `email` | `VARCHAR(128)` | `VARCHAR(128)` | YES | Email address |
| `email_list` | `BIT` | `BOOLEAN` | NO | Subscribed to email mailing list |
| `exit_date` | `DATE` | `DATE` | YES | Date when member left organization |

**Constraints:**
- Primary Key: `id`

---

### 2. `resource_access`

Manages access control and permissions for members.

| Column | MariaDB Type | H2 Type | Nullable | Description |
|--------|--------------|---------|----------|-------------|
| `id` | `BIGINT` | `BIGINT` | NO | Primary key (auto-increment) |
| `member` | `binary(16)` | `UUID` | NO | Foreign key -> `members.id` |
| `identity` | `binary(16)` | `UUID` | NO | External identity reference |
| `role` | `VARCHAR(10)` | `VARCHAR(10)` | YES | Role name (e.g., "owner") |
| `type` | `TINYINT` | `TINYINT` | YES | Access type |
| `name` | `VARCHAR(128)` | `VARCHAR(128)` | NO | Human-readable display name |

**Constraints:**
- Primary Key: `id`
- Foreign Key: `FK_members_aud_member_members_id` -> `members(id)`

**Sequence:**
- `resource_access_SEQ` (start value: 1, increment by: 50)

---

### 3. `REVINFO`

Metadata table for **Hibernate Envers** audit revisions.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `REV` | `INTEGER` | NO | Revision number (auto-increment) |
| `REVTSTMP` | `BIGINT` | YES | Timestamp of the revision (milliseconds since epoch) |

**Constraints:**
- Primary Key: `REV`

---

### 4. `members_AUD`

**Audit history** table for the `members` entity, managed by Hibernate Envers. Tracks all changes (inserts, updates, deletes) to member records.

| Column | MariaDB Type | H2 Type | Nullable | Description |
|--------|--------------|---------|----------|-------------|
| `id` | `binary(16)` | `UUID` | NO | Member ID (from `members.id`) |
| `REV` | `INTEGER` | `INTEGER` | NO | Foreign key -> `REVINFO.REV` |
| `REVTYPE` | `TINYINT` | `TINYINT` | YES | Revision type: 0=insert, 1=update, 2=delete |
| `given_name` | `VARCHAR(64)` | `VARCHAR(64)` | YES | First name (at time of revision) |
| `name` | `VARCHAR(64)` | `VARCHAR(64)` | YES | Last name (at time of revision) |
| `entry_date` | `DATE` | `DATE` | YES | Entry date (at time of revision) |
| `day_of_birth` | `DATE` | `DATE` | YES | Date of birth (at time of revision) |
| `gender` | `VARCHAR(6)` | `VARCHAR(6)` | YES | Gender (at time of revision) |
| `state` | `VARCHAR(11)` | `VARCHAR(11)` | YES | Membership state (at time of revision) |
| `state_effective` | `DATE` | `DATE` | YES | State effective date (at time of revision) |
| `dfv_number` | `INTEGER` | `INTEGER` | YES | DFV number (at time of revision) |
| `dse` | `BIT` | `BOOLEAN` | NO | DSE flag (at time of revision) |
| `rabatt` | `BIT` | `BOOLEAN` | NO | Rabatt flag (at time of revision) |
| `street` | `VARCHAR(128)` | `VARCHAR(128)` | YES | Street (at time of revision) |
| `zip_code` | `VARCHAR(5)` | `VARCHAR(5)` | YES | ZIP code (at time of revision) |
| `city` | `VARCHAR(64)` | `VARCHAR(64)` | YES | City (at time of revision) |
| `mobile` | `VARCHAR(24)` | `VARCHAR(24)` | YES | Mobile (at time of revision) |
| `email` | `VARCHAR(128)` | `VARCHAR(128)` | YES | Email (at time of revision) |
| `email_list` | `BIT` | `BOOLEAN` | NO | Email list flag (at time of revision) |
| `exit_date` | `DATE` | `DATE` | YES | Exit date (at time of revision) |
| `city_MOD` | `BIT` | `BIT` | YES | Flag: city was modified in this revision |
| `dayOfBirth_MOD` | `BIT` | `BIT` | YES | Flag: day_of_birth was modified |
| `dfvNumber_MOD` | `BIT` | `BIT` | YES | Flag: dfv_number was modified |
| `dse_MOD` | `BIT` | `BIT` | YES | Flag: dse was modified |
| `email_MOD` | `BIT` | `BIT` | YES | Flag: email was modified |
| `emailList_MOD` | `BIT` | `BIT` | YES | Flag: email_list was modified |
| `entryDate_MOD` | `BIT` | `BIT` | YES | Flag: entry_date was modified |
| `exitDate_MOD` | `BIT` | `BIT` | YES | Flag: exit_date was modified |
| `gender_MOD` | `BIT` | `BIT` | YES | Flag: gender was modified |
| `givenName_MOD` | `BIT` | `BIT` | YES | Flag: given_name was modified |
| `mobile_MOD` | `BIT` | `BIT` | YES | Flag: mobile was modified |
| `name_MOD` | `BIT` | `BIT` | YES | Flag: name was modified |
| `rabatt_MOD` | `BIT` | `BIT` | YES | Flag: rabatt was modified |
| `state_MOD` | `BIT` | `BIT` | YES | Flag: state was modified |
| `stateEffective_MOD` | `BIT` | `BIT` | YES | Flag: state_effective was modified |
| `street_MOD` | `BIT` | `BIT` | YES | Flag: street was modified |
| `zipCode_MOD` | `BIT` | `BIT` | YES | Flag: zip_code was modified |

**Constraints:**
- Primary Key: `(id, REV)` (composite)
- Foreign Key: `FK_members_aud_REV_REVINFO_REV` -> `REVINFO(REV)`

---

### 5. `email_update`

Tracks email address change requests.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | `BIGINT` | NO | Primary key |
| `change` | `TINYINT` | YES | Type of change |
| `email_address` | `VARCHAR(255)` | YES | Email address |
| `inserted_at` | `TIMESTAMP` | YES | When the change was recorded |

**Constraints:**
- Primary Key: `id`

**Sequence:**
- `email_update_SEQ` (start value: 1, increment by: 50)

---

## Relationships

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│  ┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐  │
│  │     members       │       │  resource_access  │       │    REVINFO        │  │
│  │──────────────────│       │──────────────────│       │──────────────────│  │
│  │ PK: id           │◄──────│ FK: member        │       │ PK: REV          │  │
│  │   given_name     │   1:N │ PK: id           │       │   REVTSTMP       │  │
│  │   name           │       │   identity       │       └──────────────────┘  │
│  │   entry_date     │       │   role           │              ▲                │
│  │   day_of_birth   │       │   type           │              │                │
│  │   gender         │       │   name (NN)      │              │                │
│  │   state          │       └──────────────────┘              │                │
│  │   state_effective│                                            │                │
│  │   dfv_number     │       ┌──────────────────┐              │                │
│  │   dse (NN)       │       │   members_AUD    │──────────────┘                │
│  │   rabatt (NN)    │       │──────────────────│     (FK: REV -> REVINFO.REV)   │
│  │   street         │       │ PK: (id, REV)    │                                │
│  │   zip_code       │       │ FK: id           │                                │
│  │   city           │       │   REV (NN)       │                                │
│  │   mobile         │       │   REVTYPE        │                                │
│  │   email          │       │   ... (all       │                                │
│  │   email_list (NN)│       │   members cols)  │                                │
│  │   exit_date      │       │   ..._MOD flags  │                                │
│  └──────────────────┘       └──────────────────┘                                │
│                                                                             │
│  ┌──────────────────┐                                                          │
│  │   email_update    │                                                          │
│  │──────────────────│                                                          │
│  │ PK: id           │                                                          │
│  │   change         │                                                          │
│  │   email_address  │                                                          │
│  │   inserted_at    │                                                          │
│  └──────────────────┘                                                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Liquibase Changelog Structure

The changelog is organized as follows:

```
db/
├── changeLog.xml              # Master changelog (includes all)
└── changelog/
    └── db.changelog-001.xml   # All changesets
```

### Changeset Summary

| ID | Author | Description | Context | DBMS-Specific |
|----|--------|-------------|---------|---------------|
| 1 | bslbckr | Create `resource_access` table | - | - |
| 2 | bslbckr | Create `members` table | - | - |
| 4 | bslbckr | Alter types for MariaDB (binary(16), BIT) | - | mariadb |
| 5 | bslbckr | Create `resource_access_SEQ` sequence | - | - |
| 6 | bslbckr | Insert test data into `resource_access` | `@test` | mariadb, h2 |
| 7 | bslbckr | Add `name` column to `resource_access` | - | - |
| 8 | bslbckr | Update test data: set `name` | `@test` | - |
| 9 | bslbckr | Add NOT NULL constraint to `resource_access.name` | - | - |
| 10 | bslbckr | Create `REVINFO` table | - | - |
| 11 | bslbckr | Create `members_AUD` table | - | - |
| 12 | bslbckr | Add FK: `members_AUD.REV` -> `REVINFO.REV` | - | - |
| 13 | bslbckr | Alter types for MariaDB in `members_AUD` | - | mariadb |
| 14 | bslbckr | Insert test data into `members` | `@test` | mariadb, h2 |
| 15 | bslbckr | Add FK: `resource_access.member` -> `members.id` | - | - |
| 16 | bslbckr | Add composite PK to `members_AUD` | - | - |
| 17 | bslbckr | Add 20 `*_MOD` BIT columns to `members_AUD` | - | - |
| 18 | bslbckr | Create `email_update` table | - | - |
| 19 | bslbckr | Create `email_update_SEQ` sequence | - | - |
| 20 | bslbckr | Add NOT NULL constraint to `email_update.id` | - | - |
| 21 | bslbckr | Add PK to `email_update` | - | - |
| 22 | bslbckr | Insert test data into `REVINFO` | `@test` | - |
| 23 | bslbckr | Insert test data into `members_AUD` | `@test` | mariadb, h2 |

---

## Test Data

Test data is inserted **only** when running with the `@test` context (H2 environment). It includes:

- 1 `resource_access` record (Paul Test, role: owner, type: 0)
- 1 `members` record (Paul Tester, with full address and contact info)
- 4 `REVINFO` records (revision timestamps)
- 8 `members_AUD` records (full audit history for the test member)

**UUID Values (Test Data):**
- Member ID: `8b3a2dfa-bcac-4d3e-8efa-bf9ed0abc04d`
- Identity ID: `df8ea850-52a3-4856-aea0-79361d48ff8c`
