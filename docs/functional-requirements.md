# Smarthome API — Functional Requirements & Status

> Last reviewed against code on **2026-11-14** (commit `a2e44be`).

## 1. Overview

Smarthome API is a home ERP backend (Spring Boot + PostgreSQL, OAuth2/JWT resource server).

**Core domain rule**

- **1 Account → many Homes** (and a Home can be shared with many Accounts — `home_account` many-to-many).
- **1 Home → many Resources**: Food, Inventory, Warranty, Waterworks bill, Electricity bill.
- Every resource is **scoped to a Home**; an account may only see/modify resources of homes it belongs to.

### Status legend

| Icon | Meaning |
|------|---------|
| ✅ | Done — implemented and working as intended |
| 🟡 | Partial — implemented but incomplete or has a known defect |
| ❌ | Not started — required, nothing implemented (or only an empty stub/entity) |
| 💡 | Proposed — suggested requirement, needs owner confirmation |

---

## 2. Summary

| Module | Endpoint base | Status | Notes |
|--------|---------------|--------|-------|
| Authentication | (JWT) | ✅ | OAuth2 resource server, external IdP |
| Account | `/accounts` | 🟡 | Create + get only; no update, no duplicate check |
| Home | `/homes` | 🟡 | List + create only |
| Home membership / sharing | — | ❌ | Many-to-many exists in DB, no API |
| Permission / roles | `/permissions` | ❌ | Empty controller/service; entity mapping broken |
| Food | `/homes/{homeId}/foods` | ✅ | Full CRUD, scoped to home |
| Inventory | `/homes/{homeId}/inventories` | ✅ | Full CRUD, scoped to home |
| Warranty | `/homes/{homeId}/warranties` | ✅ | Full CRUD, scoped to home |
| Waterworks bill | `/homes/{homeId}/waterworks` | ✅ | Full CRUD, scoped to home |
| Electricity bill | `/homes/{homeId}/electricity` | ✅ | Full CRUD, scoped to home |
| Dashboard / notifications | — | 💡 | Not started |
| Cross-cutting (validation, errors, tests) | — | 🟡 | See §6 |

---

## 3. Account & Authentication

| ID | Requirement | Status | Notes / Location |
|----|-------------|--------|------------------|
| FR-AUTH-01 | All APIs except `/hello`, `/actuator/**` require a valid JWT | ✅ | `security/oauth2/JWTSecurityConfig.java` |
| FR-AUTH-02 | Account is identified by JWT `sub` (subject) | ✅ | `AccountRepository.findBySubject` |
| FR-AUTH-03 | CORS enabled for web client | ✅ | `security/CORSFilter.java` (allows `*` — restrict for prod 💡) |
| FR-ACC-01 | Register account for the current JWT subject (`POST /accounts`) | 🟡 | Works, but no check for an already-existing account → duplicates possible |
| FR-ACC-02 | Get current user info incl. `hasHome` flag (`GET /accounts/userinfo`) | ✅ | Returns 404 if not registered |
| FR-ACC-03 | Update own profile (`PUT /accounts`) | ❌ | |
| FR-ACC-04 | Return a clear 404/403 when the JWT user has no account yet, on every endpoint | 🟡 | Home resource endpoints now return 403 (guard denies unknown subjects). Other endpoints still need a clear 404 |
| FR-ACC-05 | Delete / deactivate own account | 💡 | |

## 4. Home

| ID | Requirement | Status | Notes / Location |
|----|-------------|--------|------------------|
| FR-HOME-01 | Create a home; creator becomes owner (`POST /homes`) | ✅ | Membership added via `HomeRepository.addMember`; existing homes kept. Note: `@Data` on `AccountEntity`/`HomeEntity` makes `hashCode` recursive — avoid putting these entities in Sets |
| FR-HOME-02 | List homes the user belongs to, with `isOwner` (`GET /homes`) | 🟡 | Returns **404 when list is empty** — should return `[]` |
| FR-HOME-03 | Get home detail by id (`GET /homes/{id}`) | ❌ | |
| FR-HOME-04 | Update home (name, address, lat/long) — owner only (`PUT /homes/{id}`) | ❌ | |
| FR-HOME-05 | Delete home — owner only, cascade/handle child resources (`DELETE /homes/{id}`) | ❌ | |
| FR-HOME-06 | Invite / add a member to a home (by username or email) | ❌ | `home_account` join table already exists |
| FR-HOME-07 | List members of a home | ❌ | |
| FR-HOME-08 | Remove a member / leave a home | ❌ | |
| FR-HOME-09 | Transfer ownership | 💡 | `HomeEntity.owner` is a raw `Long`, not a FK relation |
| FR-HOME-10 | `homePermission` field in `HomeRequest` | ❌ | Accepted in request but ignored |

## 5. Home Resources

All resource endpoints live under `/homes/{homeId}/...` and must verify the caller is a member of that home **and** that the resource belongs to that home.

### 5.1 Permission / Roles

| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-PERM-01 | Per-home, per-account permission set (e.g. `FOOD_READ`, `FOOD_WRITE`, …) | ❌ | `PermissionEntity` exists but `@CollectionTable(name="permission")` collides with the entity's own table name — mapping is broken |
| FR-PERM-02 | Owner can grant/revoke member permissions (`/permissions`) | ❌ | Controller & service are empty |
| FR-PERM-03 | Enforce permissions on every resource endpoint | ❌ | Today: any member can do anything |

### 5.2 Food

| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-FOOD-01 | List food in a home, sorted by expiry date | ✅ | |
| FR-FOOD-02 | Get food by id | ✅ | Scoped by home |
| FR-FOOD-03 | Create food | ✅ | |
| FR-FOOD-04 | Update food | ✅ | Scoped via `findByIdAndHomeId` |
| FR-FOOD-05 | Delete food | ✅ | Scoped to home |
| FR-FOOD-06 | Bulk delete (`DELETE /homes/{homeId}/foods`, form `ids=1,2,3`) | ✅ | All-or-nothing: 404 if any id is not in the home |
| FR-FOOD-07 | Status `FRESH` / `EXPIRED` derived from expiry date | ✅ | |
| FR-FOOD-08 | `NEAR_EXPIRY` status (e.g. ≤ 3 days) | 💡 | |
| FR-FOOD-09 | Consume / decrease quantity | 💡 | |

### 5.3 Inventory (household supplies)

| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-INV-01 | List inventory, low-stock first | ✅ | Low-stock first, then by quantity |
| FR-INV-02 | Get inventory by id | ✅ | |
| FR-INV-03 | Create inventory | ✅ | |
| FR-INV-04 | Update inventory | ✅ | Scoped to home |
| FR-INV-05 | Delete inventory | ✅ | Scoped to home |
| FR-INV-06 | Bulk delete | ✅ | All-or-nothing, scoped to home |
| FR-INV-07 | Status `IN_STOCK` / `LOW_STOCK` (≤30% of max) / `OUT_OF_STOCK` | ✅ | No `maxQuantity` counts as in stock |
| FR-INV-08 | Shopping list generated from low/out-of-stock items | 💡 | |

### 5.4 Warranty (appliances / devices)

| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-WAR-01 | List warranties sorted by warranty date | ✅ | |
| FR-WAR-02 | Get warranty by id | ✅ | |
| FR-WAR-03 | Create warranty | ✅ | |
| FR-WAR-04 | Update warranty | ✅ | |
| FR-WAR-05 | Delete warranty | ✅ | Scoped to home |
| FR-WAR-06 | Bulk delete | ✅ | All-or-nothing, scoped to home |
| FR-WAR-07 | Status `IN_WARRANTY` / `OUT_OF_WARRANTY` | ✅ | |
| FR-WAR-08 | Attach receipt / warranty card image | 💡 | |

### 5.5 Waterworks bill

| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-WATER-01 | List water bills for a home | ✅ | Sorted by invoice date |
| FR-WATER-02 | Get bill by id | ✅ | Scoped to home |
| FR-WATER-03 | Create bill | ✅ | The user types every amount from the paper bill; the API doesn't calculate `total` |
| FR-WATER-04 | Update bill | ✅ | Scoped to home |
| FR-WATER-05 | Delete bill (single / bulk) | ✅ | Bulk delete is all-or-nothing, scoped to home |
| FR-WATER-06 | Monthly usage / cost summary | 💡 | Money fields are `BigDecimal` (`numeric(12,2)`). An existing prod table needs a manual `ALTER`, see §6 NFR-07 |

### 5.6 Electricity bill

| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-ELEC-01 | CRUD electricity bills per home (list, get, create, update, delete, bulk delete) | ✅ | The user types every amount from the bill. `ft_rate` keeps 4 decimal places, money fields 2 |
| FR-ELEC-02 | Monthly usage / cost summary | 💡 | |

### 5.7 Dashboard & Notifications

| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-DASH-01 | Home dashboard: counts of expired/near-expiry food, low-stock items, expiring warranties, latest bills | 💡 | |
| FR-NOTI-01 | Notify members of food expiring, low stock, warranty expiring | 💡 | |

## 6. Cross-cutting / Non-functional

| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| NFR-01 | Consistent error response `{status, errorMessage}` | ✅ | HTTP status now matches the error (404/403/503) |
| NFR-02 | Request validation (`@Valid`, required fields, date format) | ❌ | Null dates → NPE → 500 |
| NFR-03 | Home-membership check centralised (not copy-pasted per service) | ✅ | `@RequireHomeAccess` (`security/home/`) on Food/Inventory/Warranty/Waterworks controllers → `HomeAccessGuard`; non-members get 403 |
| NFR-04 | Audit fields `createBy` / `updateBy` / dates on all resources | ✅ | |
| NFR-05 | Pagination / filtering on list endpoints | ❌ | |
| NFR-06 | Unit & integration tests | 🟡 | `RequireHomeAccessTest`, `FoodServiceTest`, `ErrorAdviserControllerTest`, `HomeScopingRepositoryTest` (H2). `contextLoads()` needs a real DB |
| NFR-07 | Schema migrations (Flyway/Liquibase) instead of `ddl-auto: update` | ❌ | `ddl-auto: update` never changes an existing column type. Until migrations exist, run by hand: `ALTER TABLE waterworks ALTER COLUMN service_charge TYPE numeric(12,2), ALTER COLUMN vat TYPE numeric(12,2), ALTER COLUMN total TYPE numeric(12,2);` and, if the `electricity` table already exists, `ALTER TABLE electricity ALTER COLUMN ft_rate TYPE numeric(10,4);` (Hibernate created it with 2 decimal places) |
| NFR-08 | API documentation (OpenAPI/Swagger) | ❌ | |
| NFR-09 | Health/readiness probes | ✅ | Actuator probes enabled |
| NFR-10 | CI build, Sonar, Trivy scan | ✅ | `ci/Jenkinsfile` |

## 7. Suggested implementation order

1. **Fix critical bugs** — NPE on missing account outside home resources (FR-ACC-04). ~~Home-scoping~~, ~~homes overwritten~~, ~~error status codes~~, ~~inventory sort~~ done.
2. **Complete Home management** — FR-HOME-02..08 (detail, update, delete, members).
3. **Permissions** — FR-PERM-01..03 (fix entity mapping first).
4. ~~**Finish bill modules**~~ done.
5. **Validation + tests** — NFR-02, NFR-06.
6. **Dashboard, notifications** once scope is confirmed.

## 8. Open questions

1. **Permissions** — fine-grained per module (`FOOD_READ`, `FOOD_WRITE`…) or simple roles (`OWNER` / `MEMBER` / `VIEWER`)? What is `HomeRequest.homePermission` meant for?
2. **Membership** — invite by username/email with accept flow, or add directly?
3. **Deleting a home** — hard delete all child resources, or soft delete?
4. **Other resource types** planned (e.g. rooms, IoT devices, maintenance schedule, internet/gas bills)?
