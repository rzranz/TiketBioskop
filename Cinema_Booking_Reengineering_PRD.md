# Cinema Booking Platform — Reengineering PRD

**Document type:** Product Requirements Document + Solution Blueprint  
**Version:** 1.0  
**Status:** Draft for discovery and implementation planning  
**Target platform:** Existing Java-based cinema booking application (legacy details to be audited)  
**Proposed backend:** Java + Spring Boot modular monolith  
**Proposed database:** PostgreSQL  
**Potential clients:** Existing Android app, modern Android app, web/admin dashboard  

> **Important:** This PRD describes a target cinema-booking product inspired by common ticket-booking workflows. It is not a claim about the internal implementation, APIs, business rules, or infrastructure of TIX ID or any other commercial service. The legacy application's actual framework, Java version, database, integrations, source-code condition, and operational constraints must be discovered before selecting a migration strategy.

---

## 1. Executive Summary

The Cinema Booking Platform allows customers to discover movies, find cinemas and showtimes, select seats, reserve tickets, pay, receive digital tickets, and validate tickets at the venue. Cinema operators and platform administrators manage movie schedules, auditoriums, seats, pricing, orders, and operational reports.

The reengineering initiative aims to replace or progressively modernize an existing Java-based application while preserving required business behavior and customer data. The target backend will initially use a **modular monolith** built with Spring Boot. This gives the project clear domain boundaries without introducing the operational cost of microservices too early.

The most critical technical concern is **seat inventory consistency**. Multiple customers may attempt to reserve the same seat for the same showtime simultaneously. The platform must ensure that no more than one active reservation or confirmed booking can own a seat at a given showtime.

Payment processing is another critical boundary. A booking must not be confirmed merely because a browser or mobile app displays a success page. The backend must verify an authenticated payment-provider notification or reconcile the payment through a trusted provider API.

### 1.1 Product vision

Provide a reliable, understandable, and auditable cinema-booking experience for customers and cinema operations, with correctness of booking, payment, and ticket validation taking priority over visual complexity.

### 1.2 Product principles

1. **Correctness before optimization:** booking and payment invariants must be enforced by the backend and database.
2. **Backend is authoritative:** client-side state is for UX only; it cannot decide whether a seat is available or a payment succeeded.
3. **Modular before distributed:** begin with a modular monolith; extract services only when justified by independent scaling, ownership, deployment, or reliability requirements.
4. **Auditable business actions:** important transitions must record who performed them, when, and why.
5. **Progressive reengineering:** understand and protect the existing system before replacing it.
6. **Secure by default:** least privilege, private receipt/ticket data, validated callbacks, and no secrets in source control.
7. **Measurable quality:** critical requirements must be demonstrated with automated tests, metrics, and operational runbooks.

---

## 2. Project Context and Discovery Assumptions

The user has an existing application whose base is Java. The exact nature of the legacy application is not yet confirmed. It may be an Android APK, a Java web application, a Java backend, or a combination.

### 2.1 Facts known

- The existing product is described as a cinema-booking application similar in category to TIX ID.
- The existing application's base technology is Java.
- Reengineering with Spring Boot is being considered.

### 2.2 Unknowns to audit before implementation

- Is Java used only in the Android client, only in the backend, or in both?
- Which Java version and framework are currently used (plain Servlet/JSP, Spring MVC, Spring Boot, another framework)?
- Does a backend source repository exist? Is the APK the only available artifact?
- Which database and schema are currently used?
- Which cinema partners, ticketing providers, payment gateways, and notification providers are integrated?
- Does the application own seat inventory, or does each cinema partner expose its own inventory system?
- What are the current user counts, booking volumes, peak concurrency, and service-level expectations?
- Which historical data must be migrated (accounts, bookings, payment references, tickets, refunds)?
- Are there contracts, privacy requirements, retention rules, or payment-provider requirements that constrain the migration?

### 2.3 Discovery deliverables

Before changing production behavior, produce:

- Legacy system context diagram.
- Source-code and dependency inventory.
- Existing endpoint/API inventory.
- Existing database ERD and data dictionary.
- Cinema/provider integration inventory.
- Business-rule catalogue, including edge cases.
- Current deployment topology and monitoring inventory.
- Data-quality report and migration mapping.
- Regression test suite for critical user journeys.
- Risk register and rollback plan.

Do not assume an APK alone reveals server-side business logic. If backend source code is unavailable, treat the client as evidence of user journeys only and reconstruct server requirements from API traces, database records, provider documentation, and stakeholder interviews where legally and operationally permitted.

---

## 3. Goals, Non-Goals, and Success Measures

### 3.1 Product goals

- Let customers discover movies, cinemas, showtimes, and available seats.
- Allow customers to reserve one or more seats for a limited period.
- Prevent double booking for the same seat and showtime.
- Support payment initiation, provider callback verification, and payment reconciliation.
- Issue a verifiable digital ticket only after confirmed payment.
- Allow venue staff to validate a ticket safely and prevent duplicate entry.
- Give cinema/platform administrators tools to maintain catalog, auditorium, showtime, pricing, and operational data.
- Provide useful sales, occupancy, and booking reports.
- Preserve required legacy customer and transaction records during migration.

### 3.2 Non-goals for the first release

- Building a real payment processor or storing card details.
- Reproducing every feature of any named commercial cinema app.
- AI movie recommendations or personalization models.
- Loyalty points, subscriptions, vouchers, group buying, or complex campaigns unless explicitly required by discovery.
- Multi-country tax and currency rules in the MVP.
- Microservices from day one.
- Rewriting the Android client before its current condition and compatibility requirements are known.
- Guaranteed real-time seat synchronization across independent cinema partner systems until integration contracts are understood.

### 3.3 Suggested MVP success measures

These are proposed targets for a controlled test environment. They must be revised after load testing and stakeholder agreement; they are not claims about the legacy system.

| Measure | Initial target / acceptance |
|---|---|
| Double-booked seat | 0 in automated concurrency tests |
| Duplicate payment application | 0 for repeated identical provider events |
| Duplicate ticket issuance | 0 for one confirmed booking/seat |
| Ticket reuse | A second successful scan is rejected or flagged according to venue policy |
| Booking state correctness | Every transition is validated on the server |
| Audit coverage | 100% of payment, booking, hold, and ticket state transitions recorded |
| API contract | OpenAPI document generated and reviewed |
| Critical-flow automated tests | Booking, expiry, payment callback, refund/cancel, and ticket scan are covered |
| Deployment | Repeatable local deployment using documented configuration |

Performance targets (for example, p95 response time and peak reservations per second) must be set only after a realistic workload profile is available.

---

## 4. Users, Roles, and Permissions

### 4.1 Customer roles

- **Guest:** browse public movie and cinema information; may be required to authenticate before holding seats or booking.
- **Registered Customer:** manage profile, create holds, book seats, pay, view tickets, request eligible cancellations/refunds.

### 4.2 Operations roles

- **Cinema Operator:** manage assigned cinema, auditorium, seat maps, showtimes, and operational status.
- **Gate Staff:** scan and validate tickets for assigned venue/showtime according to policy.
- **Platform Admin:** manage users, cinemas, catalog, platform configuration, operational access, and audit queries.
- **Finance/Support Operator:** review payment exceptions, refunds, reconciliation, and customer support cases, with least-privilege access.

### 4.3 Authorization principles

- Authentication answers **who is the user?** Authorization answers **may this user perform this action on this resource?**
- Enforce authorization on the server for every protected operation.
- A role alone may be insufficient. A cinema operator must also be scoped to their assigned cinema; gate staff must be scoped to a venue and shift where applicable.
- Customers may read only their own bookings and tickets.
- Payment webhook authentication is provider-based and must not rely on a customer session.
- Administrative actions must be audited.
- Never trust `customerId`, `cinemaId`, `price`, `paymentStatus`, or `role` supplied by the browser/mobile client without resolving or validating them against trusted server-side data.

### 4.4 Role-permission matrix (MVP proposal)

| Capability | Guest | Customer | Cinema Operator | Gate Staff | Finance/Support | Platform Admin |
|---|---:|---:|---:|---:|---:|---:|
| Browse published movies/showtimes | Yes | Yes | Yes | Assigned venue | Yes | Yes |
| Hold seats / create booking | No | Yes | No | No | No | Support override only, audited |
| View own booking/ticket | No | Yes | No | Scan result only | Scoped support access | Audited support access |
| Manage assigned cinema/showtimes | No | No | Yes | No | No | Yes |
| Validate ticket | No | No | Optional | Assigned venue | No | Audited override |
| Reconcile payment/refund | No | No | No | No | Yes | Yes |
| Manage platform users/roles | No | No | No | No | No | Yes |

Exact role names and scopes must be confirmed during discovery.

---

## 5. Main Customer Journeys

### 5.1 Discover and select a showtime

1. Customer opens the application.
2. Customer selects city/location (if supported).
3. Customer browses currently showing or upcoming movies.
4. Customer opens movie details.
5. Customer selects a cinema, date, and showtime.
6. Backend returns the seat layout and current availability for that showtime.
7. Customer selects seats.

**Acceptance criteria:**

- Only published and bookable showtimes are shown.
- A showtime belongs to one movie and one auditorium.
- Seat availability is scoped to a showtime, not to the physical seat alone.
- The returned seat state is explicitly a snapshot; it can change before the customer creates a hold.

### 5.2 Hold seats and create a booking

1. Customer submits the selected seat identifiers and an idempotency key.
2. Backend authenticates the customer.
3. Backend validates showtime status and booking window.
4. Backend starts a database transaction.
5. Backend locks or conditionally claims the relevant showtime-seat rows.
6. Backend checks that every requested seat is eligible.
7. Backend computes authoritative price and creates a booking plus seat-hold records.
8. Backend writes an audit/domain event in the same database transaction where applicable.
9. Backend commits and returns the booking reference and hold expiry time.
10. The customer proceeds to payment.

**Acceptance criteria:**

- All requested seats are held or none are held; partial reservation is not allowed unless a future product rule explicitly supports it.
- The client cannot choose its own price or hold expiry.
- A second request for a seat already held by another customer is rejected with a conflict response.
- Retrying the same idempotent request does not create duplicate bookings.
- A hold has an explicit server-calculated expiry timestamp.

### 5.3 Payment

1. Customer requests a payment session for an eligible booking.
2. Backend validates booking ownership and current state.
3. Backend creates a payment attempt with a unique merchant/provider reference.
4. Backend calls the payment provider outside a long-running database transaction.
5. Backend stores the provider response and returns the payment redirect/QR/VA data needed by the client.
6. Provider sends a webhook to the backend.
7. Backend validates the provider signature and event authenticity.
8. Backend handles duplicate/out-of-order notifications idempotently.
9. Backend records payment state and transitions booking/seat state according to the verified event and business policy.
10. Reconciliation jobs identify unresolved payment attempts.

**Acceptance criteria:**

- A browser/mobile redirect is not proof of payment.
- Webhook signatures, timestamps/nonces (where supported), amount, currency, merchant reference, and provider transaction ID are validated.
- Repeated provider events do not apply payment twice.
- A late success after a hold has expired follows an explicit policy (for example, re-acquire seats if still available or initiate refund/manual review); it must never silently confirm an unavailable seat.
- Provider timeouts result in a pending/unknown state that can be reconciled, not an assumed failure or success.

### 5.4 Ticket issuance and validation

1. A verified successful payment triggers ticket issuance.
2. Backend creates a unique ticket per booked seat, or according to the confirmed business model.
3. Backend generates a high-entropy token or signed opaque ticket identifier.
4. Customer views the digital ticket and QR code.
5. Gate staff scans the QR code.
6. Backend verifies ticket authenticity, booking/payment status, showtime, venue scope, and usage policy.
7. Backend atomically records redemption or rejects duplicate use.

**Acceptance criteria:**

- Ticket issuance is idempotent.
- QR content does not expose unnecessary personal or payment data.
- A ticket cannot be redeemed more than once unless an explicit multi-entry policy exists.
- Scan result is auditable and includes staff identity, venue, timestamp, and outcome.

---

## 6. Functional Requirements

Priorities: **P0 = required for MVP**, **P1 = useful after core flow**, **P2 = later enhancement**.

### 6.1 Identity and customer profile

| ID | Priority | Requirement |
|---|---|---|
| ID-01 | P0 | Register/login customer using the approved authentication method. |
| ID-02 | P0 | Retrieve current authenticated profile. |
| ID-03 | P0 | Enforce server-side resource ownership. |
| ID-04 | P1 | Support profile updates and account recovery. |
| ID-05 | P1 | Support administrative roles and cinema-scoped access. |

### 6.2 Movie catalog

| ID | Priority | Requirement |
|---|---|---|
| MOV-01 | P0 | List published movies with pagination/filtering. |
| MOV-02 | P0 | View movie detail (title, synopsis, duration, language, rating/classification, poster, release dates). |
| MOV-03 | P1 | Search/filter by title, genre, language, and release state. |
| MOV-04 | P1 | Support coming-soon and now-showing states. |
| MOV-05 | P1 | Admin can create/update/archive movie metadata. |

### 6.3 Cinema, auditorium, and seats

| ID | Priority | Requirement |
|---|---|---|
| CIN-01 | P0 | List cinemas and supported locations. |
| CIN-02 | P0 | View cinema details and auditoriums where relevant. |
| CIN-03 | P0 | Maintain stable physical seat identifiers per auditorium. |
| CIN-04 | P0 | Maintain seat layout/type (regular, premium, accessible, etc., as configured). |
| CIN-05 | P1 | Operator can manage only assigned cinemas. |

### 6.4 Showtime and pricing

| ID | Priority | Requirement |
|---|---|---|
| SHO-01 | P0 | Create showtime linking movie, cinema/auditorium, start/end time, and status. |
| SHO-02 | P0 | Generate or maintain showtime-seat inventory. |
| SHO-03 | P0 | Return seat availability for one showtime. |
| SHO-04 | P0 | Calculate price server-side from price rules. |
| SHO-05 | P1 | Support seat-type pricing, day/time pricing, and promotion rules. |
| SHO-06 | P1 | Prevent overlapping showtimes in the same auditorium unless an explicit turnaround rule allows it. |

### 6.5 Seat hold and booking

| ID | Priority | Requirement |
|---|---|---|
| BKG-01 | P0 | Customer can request a hold for one or more seats. |
| BKG-02 | P0 | Hold expiry is generated and enforced by the backend. |
| BKG-03 | P0 | Booking stores price/currency snapshots. |
| BKG-04 | P0 | Booking state transitions follow a defined state machine. |
| BKG-05 | P0 | Same active showtime-seat cannot belong to two customers. |
| BKG-06 | P0 | Customer can view their own booking details. |
| BKG-07 | P1 | Customer can cancel when policy permits. |
| BKG-08 | P1 | Support can resolve exceptional booking states through audited operations. |

### 6.6 Payments

| ID | Priority | Requirement |
|---|---|---|
| PAY-01 | P0 | Create payment attempt for an eligible booking. |
| PAY-02 | P0 | Verify provider callback authenticity. |
| PAY-03 | P0 | Process provider events idempotently. |
| PAY-04 | P0 | Persist payment attempts separately from booking state. |
| PAY-05 | P0 | Support pending/success/failed/expired states. |
| PAY-06 | P1 | Support refund request and refund status tracking. |
| PAY-07 | P1 | Reconcile uncertain payment attempts against provider API/report. |
| PAY-08 | P2 | Support multiple payment providers using adapters. |

### 6.7 Tickets and venue validation

| ID | Priority | Requirement |
|---|---|---|
| TKT-01 | P0 | Issue ticket only after payment confirmation. |
| TKT-02 | P0 | Ticket is associated with booking, showtime, and seat. |
| TKT-03 | P0 | Generate QR/token without exposing sensitive data. |
| TKT-04 | P0 | Staff can validate a ticket within assigned venue scope. |
| TKT-05 | P0 | Prevent duplicate ticket redemption atomically. |
| TKT-06 | P1 | Support ticket cancellation/refund invalidation policy. |

### 6.8 Notifications and reporting

| ID | Priority | Requirement |
|---|---|---|
| NOT-01 | P1 | Send booking/payment/ticket notifications through an adapter. |
| NOT-02 | P1 | Retry transient notification failures without duplicating the business action. |
| REP-01 | P1 | Report sales, booking counts, occupancy, and payment status by period/cinema/movie. |
| REP-02 | P1 | Export authorized reports as CSV. |
| REP-03 | P2 | Dashboard charts and demand analytics. |

---

## 7. Domain Model and State Machines

### 7.1 Core entities (conceptual)

- `Customer`: customer identity and profile reference.
- `Movie`: catalog metadata.
- `Cinema`: venue and location.
- `Auditorium`: screen/studio within a cinema.
- `Seat`: physical seat definition within an auditorium.
- `Showtime`: scheduled movie screening in an auditorium.
- `ShowtimeSeat`: inventory row for a physical seat at one showtime.
- `SeatHold`: temporary allocation with owner and expiry.
- `Booking`: customer's order/reservation and price snapshot.
- `BookingSeat`: seats attached to a booking and price per seat.
- `PaymentAttempt`: one attempt to collect payment for a booking.
- `PaymentEvent`: provider event/idempotency record.
- `Ticket`: admission entitlement for a seat/booking.
- `TicketRedemption`: attempt/result when a ticket is scanned.
- `Notification`: delivery record.
- `AuditEvent`: actor/action/target/time/outcome metadata.

### 7.2 Booking state (suggested)

```text
PENDING_PAYMENT
  ├── payment verified ───────────────> CONFIRMED
  ├── hold expires without payment ───> EXPIRED
  └── customer/system cancellation ──> CANCELLED

CONFIRMED
  ├── eligible cancellation ──────────> CANCELLED (policy-controlled)
  └── showtime completed ─────────────> COMPLETED

EXPIRED / CANCELLED / COMPLETED
  └── terminal for normal customer flow
```

Do not use one generic status field to represent every sub-process. Booking state, payment state, seat inventory state, refund state, and ticket redemption state are related but distinct state machines.

### 7.3 Seat inventory state (conceptual)

```text
AVAILABLE
  └── successful hold ──> HELD
HELD
  ├── payment confirmed ──> BOOKED
  ├── hold expires ───────> AVAILABLE
  └── hold cancelled ─────> AVAILABLE
BOOKED
  └── eligible cancellation/refund ──> AVAILABLE (only after policy and payment/refund rules permit)
```

The physical seat is not globally `HELD` or `BOOKED`; the state is specific to a showtime.

### 7.4 Payment state (suggested)

```text
CREATED -> PENDING -> SUCCEEDED
                   -> FAILED
                   -> EXPIRED
                   -> UNKNOWN (reconciliation required)
SUCCEEDED -> REFUND_PENDING -> REFUNDED / PARTIALLY_REFUNDED
```

A payment provider may send duplicate or out-of-order events. Define allowed transitions and store provider event identifiers. Do not blindly overwrite a final state with an older event.

### 7.5 Ticket state (suggested)

`VALID -> REDEEMED`; `VALID -> CANCELLED/VOID` only under the applicable cancellation/refund policy. A second redemption attempt must not create another successful usage record.

---

## 8. Booking Correctness and Concurrency Design

### 8.1 Required invariant

For a given `(showtime_id, seat_id)`, at most one active hold/booking may exist at a time.

### 8.2 Recommended first implementation: PostgreSQL as the source of truth

Use a transaction around the operation that claims all requested seats. Options include:

- Pessimistic row locking (`SELECT ... FOR UPDATE` or JPA `PESSIMISTIC_WRITE`) on the relevant `showtime_seats` rows.
- Conditional updates with a state predicate and affected-row count checks.
- Database constraints that prevent duplicate active assignments, where the schema and database support the required constraint semantics.

For a multi-seat request:

1. Validate request size and duplicate seat IDs.
2. Sort seat IDs before locking to reduce deadlock risk.
3. Start a short transaction.
4. Lock the requested showtime-seat rows in deterministic order.
5. Verify that all rows exist and are available or safely expired.
6. Create a booking and hold rows; update inventory state.
7. Commit.
8. Return the hold expiry time.

If any seat is unavailable, roll back the entire transaction.

### 8.3 Hold expiration

- Store timestamps in UTC (`timestamptz` in PostgreSQL); display using the cinema's configured time zone.
- Expiration is enforced on the backend, not by a browser timer.
- A scheduled worker may release expired holds, but the booking/hold creation path must also treat an expired hold as expired even if the cleanup worker has not run yet.
- Cleanup must be idempotent and safe when multiple worker instances run.
- Avoid holding a database transaction open while the customer completes payment.

### 8.4 Redis decision

Redis is optional in MVP. Add it only after measuring or identifying a need for:

- Fast read caching of catalog/showtime data.
- Temporary coordination or rate limiting.
- Distributed ephemeral state.

If Redis participates in seat holds, define recovery and consistency behavior. PostgreSQL must still enforce the final booking invariant. A cache lock alone is not a substitute for a database uniqueness/locking strategy.

### 8.5 Load and contention testing

Create a test where many independent requests attempt to hold the same showtime-seat. Exactly one request may succeed. Also test multi-seat atomicity, expired-hold reclamation, transaction rollback, and deadlock/retry behavior.

---

## 9. Payment and Event Reliability

### 9.1 Payment adapter boundary

Define a provider-neutral interface, for example:

- Create payment session.
- Verify webhook/authenticity.
- Parse provider event into an internal payment event.
- Query/reconcile provider transaction state.
- Request refund (when supported).

Provider-specific DTOs and SDK types must remain inside the payment infrastructure adapter. Booking/domain code must not depend directly on one gateway's SDK.

### 9.2 Idempotency

- Require an idempotency key for booking creation and payment creation where appropriate.
- Store the key, user/operation scope, request fingerprint, and resulting resource ID.
- Enforce a database unique constraint for the idempotency scope.
- A retry with the same key and same request should return the original result.
- A retry with the same key but a different payload should return a conflict.
- Persist webhook provider event IDs uniquely.

### 9.3 Transaction boundaries

Never keep a database transaction open while waiting for a payment provider network call.

Recommended flow:

1. Persist the payment attempt as `CREATED/PENDING` and an outbox/event record in a short DB transaction.
2. Call the provider outside the DB transaction.
3. Persist the provider response in another short transaction.
4. Process webhook/reconciliation idempotently.
5. Update payment, booking, seat inventory, ticket issuance, and outbox records according to a consistent local transaction boundary.

### 9.4 Transactional outbox

If asynchronous event delivery is needed, write the event to an outbox table in the same transaction as the business state change. A worker publishes it and marks it delivered. Consumers must be idempotent. The outbox pattern avoids a dual-write gap between database commit and message publication.

### 9.5 Refunds and late payment success

Define explicit policy for:

- Payment success arriving after the hold expired.
- Payment success arriving after booking cancellation.
- Partial refunds where some seats are cancelled.
- Provider reports disagreeing with internal state.
- Customer support overrides.

Never silently reassign a seat or mark a payment complete without resolving the booking and inventory implications.

---

## 10. Proposed Database Schema (Logical)

This is a logical starting point, not a final migration script. Column types, constraints, indexes, retention, and provider-specific requirements must be validated during design.

| Table | Important fields | Notes / constraints |
|---|---|---|
| `users` | `id`, `email`, `password_hash`, `display_name`, `status`, timestamps | Unique normalized email; never store plaintext password |
| `roles` / `user_roles` | role definitions and assignments | Or use a constrained enum for a small fixed role set |
| `cinemas` | `id`, `name`, `address`, `timezone`, `status` | Time zone is required for showtime display |
| `cinema_user_scopes` | `user_id`, `cinema_id`, `scope` | Restricts operator/staff access |
| `movies` | `id`, `title`, `synopsis`, `duration_minutes`, `classification`, `status` | Poster stored as private/public object reference as appropriate |
| `genres` / `movie_genres` | movie/genre relation | Optional catalog feature |
| `auditoriums` | `id`, `cinema_id`, `name`, `status` | Unique name within cinema |
| `seats` | `id`, `auditorium_id`, `row_label`, `seat_number`, `seat_type` | Unique physical seat within auditorium |
| `showtimes` | `id`, `movie_id`, `auditorium_id`, `starts_at`, `ends_at`, `status` | Index by auditorium/time and movie/time |
| `showtime_seats` | `id`, `showtime_id`, `seat_id`, `status`, `current_hold_id` (nullable), `current_booking_id` (nullable), `hold_expires_at` (nullable), `version` | Unique `(showtime_id, seat_id)`; this row is the authoritative current inventory state; version for optimistic locking where used |
| `bookings` | `id`, `booking_code`, `customer_id`, `showtime_id`, `status`, `total_amount`, `currency`, timestamps | Unique booking code; price/currency snapshot |
| `booking_seats` | `id`, `booking_id`, `showtime_seat_id`, `unit_price`, `seat_label_snapshot` | Historical record of seats/prices in a booking; do not use this table alone as the current inventory authority |
| `seat_holds` | `id`, `showtime_seat_id`, `booking_id` (nullable until booking is created), `customer_id`, `expires_at`, `state`, timestamps | Hold lifecycle/history; current availability remains authoritative in `showtime_seats` |
| `payment_attempts` | `id`, `booking_id`, `provider`, `provider_reference`, `amount`, `currency`, `status` | Unique provider reference; attempts can be multiple per booking under policy |
| `payment_events` | `id`, `provider`, `provider_event_id`, payload hash, processed state | Unique provider event ID; minimize raw sensitive payload retention |
| `refunds` | `id`, `payment_attempt_id`, amount, status, provider reference | Refund lifecycle is separate from original payment |
| `tickets` | `id`, `booking_seat_id`, `ticket_token_hash`, `status`, issued_at | Unique token hash; ticket issuance idempotent |
| `ticket_redemptions` | `id`, `ticket_id`, `staff_id`, `cinema_id`, scanned_at, outcome | Enforce atomic single redemption where required |
| `outbox_events` | `id`, aggregate type/id, event type, payload, created_at, published_at | Used when reliable async publication is required |
| `idempotency_records` | scope, key, request_hash, resource reference, response metadata | Unique scope/key; expiry/retention policy |
| `audit_events` | actor, action, target, timestamp, outcome, correlation_id | Append-only by application policy; do not log secrets |

### 10.1 Money and currency

- Use `NUMERIC/DECIMAL` in PostgreSQL and `BigDecimal` in Java for money calculations.
- Never use `float` or `double` for currency arithmetic.
- Define currency code (for example `IDR`) alongside every monetary amount.
- Define rounding and minor-unit rules per currency/provider.
- Store authoritative unit-price snapshots in the booking.

### 10.2 Date/time

- Persist instants in UTC using PostgreSQL `timestamptz` and Java `Instant` where representing an absolute point in time.
- Store the cinema's IANA time zone (for example `Asia/Jakarta`) for local display and schedule interpretation.
- Use `LocalDate` for calendar dates and `LocalTime` for recurring local time rules; do not use `LocalDateTime` alone to represent an absolute instant across zones.

### 10.3 Index and constraint candidates

- Unique `showtime_seats(showtime_id, seat_id)`.
- Unique booking code.
- Unique provider event ID per provider.
- Unique provider transaction reference where guaranteed by provider semantics.
- Unique ticket token hash.
- Index showtimes by `(cinema_id/auditorium_id, starts_at)` as query patterns require.
- Index bookings by `(customer_id, created_at)` and `(status, created_at)`.
- Index active holds by `expires_at`.
- Index payment attempts by provider reference and status.

**Avoid dual sources of truth:** for the initial design, `showtime_seats.status` (plus its current hold/booking references) is the authoritative current state. `seat_holds` and `booking_seats` preserve lifecycle/history and commercial snapshots. Update these records in the same transaction. If a different schema is chosen (for example, a partial unique index over active allocations), document and test its exact semantics, including expiry and reassignment.

Database constraints are a final line of defense; application validation still provides useful, friendly errors.

---

## 11. API Design Standards

### 11.1 Base path and media types

- Base path: `/api/v1`.
- JSON request/response using `application/json` unless uploading multipart content.
- Use resource-oriented nouns and HTTP methods according to semantics.
- Use action sub-resources only when they represent a domain command (for example, `POST /bookings/{id}/cancel` or `/tickets/{id}/redeem`).
- Do not expose database table names or ORM entities directly.

### 11.2 Candidate endpoint catalogue

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| POST | `/auth/register` | Create customer account | Public |
| POST | `/auth/login` | Authenticate | Public |
| POST | `/auth/logout` | End session | Authenticated |
| GET | `/auth/me` | Current user | Authenticated |
| GET | `/movies` | Browse published movies | Public |
| GET | `/movies/{movieId}` | Movie detail | Public |
| GET | `/cinemas` | Browse cinemas | Public |
| GET | `/cinemas/{cinemaId}` | Cinema detail | Public |
| GET | `/showtimes` | Search showtimes | Public |
| GET | `/showtimes/{showtimeId}/seats` | Current seat snapshot | Public or authenticated per policy |
| POST | `/seat-holds` | Hold requested seats | Customer |
| GET | `/seat-holds/{holdId}` | Retrieve own hold | Owner |
| DELETE | `/seat-holds/{holdId}` | Release eligible hold | Owner |
| POST | `/bookings` | Create booking from valid hold | Customer |
| GET | `/bookings` | List own bookings | Customer |
| GET | `/bookings/{bookingId}` | Booking detail | Owner/support scoped |
| POST | `/bookings/{bookingId}/cancel` | Request cancellation | Owner, policy-controlled |
| POST | `/payments` | Create payment attempt | Booking owner |
| GET | `/payments/{paymentId}` | Payment status | Owner/support scoped |
| POST | `/payments/webhooks/{provider}` | Provider notification | Provider-authenticated |
| POST | `/refunds` | Request refund | Finance/support policy |
| GET | `/tickets` | List own tickets | Customer |
| GET | `/tickets/{ticketId}` | Ticket detail | Owner/staff policy |
| POST | `/ticket-validations` | Validate/redeem ticket | Assigned gate staff |
| POST | `/admin/movies` | Create movie | Catalog admin |
| PATCH | `/admin/movies/{id}` | Update movie | Catalog admin |
| POST | `/admin/cinemas/{id}/showtimes` | Create showtime | Cinema operator/admin |
| GET | `/admin/reports/sales` | Sales report | Authorized operator/finance |

### 11.3 Response conventions

Use consistent error responses, preferably aligned with RFC 9457 Problem Details (`application/problem+json`) where supported by the chosen framework version.

Example:

```json
{
  "type": "https://api.example.test/problems/seat-unavailable",
  "title": "Seat unavailable",
  "status": 409,
  "detail": "One or more selected seats are no longer available.",
  "instance": "/api/v1/seat-holds",
  "code": "SEAT_UNAVAILABLE",
  "traceId": "00-example"
}
```

Do not leak stack traces, SQL, secrets, internal hostnames, or provider credentials in API responses.

### 11.4 HTTP status guidance

- `200 OK`: successful read/action returning a representation.
- `201 Created`: resource created; include `Location` where appropriate.
- `202 Accepted`: asynchronous operation accepted but not complete.
- `204 No Content`: successful operation with no response body.
- `400 Bad Request`: invalid syntax/validation where appropriate.
- `401 Unauthorized`: authentication missing or invalid.
- `403 Forbidden`: authenticated but not authorized.
- `404 Not Found`: resource not found or intentionally concealed under policy.
- `409 Conflict`: seat conflict, invalid state transition, idempotency payload mismatch, or optimistic-lock conflict.
- `422 Unprocessable Content`: semantically invalid content where the API convention chooses this status.
- `429 Too Many Requests`: rate limit exceeded.
- `500 Internal Server Error`: unexpected server failure.

Choose and document one validation-error convention; do not alternate inconsistently between 400 and 422.

### 11.5 Pagination and filtering

Use explicit pagination for list endpoints. Establish a maximum page size. Support stable sorting and validated filter fields. For high-volume or changing datasets, consider cursor/keyset pagination rather than large offsets.

---

## 12. Target Architecture

### 12.1 Initial recommendation: modular monolith

Deploy one Spring Boot application, with modules organized around business capabilities:

- `identity`
- `catalog`
- `cinema`
- `showtime`
- `inventory`
- `booking`
- `payment`
- `ticket`
- `notification`
- `reporting`
- `admin`
- `shared` (strictly limited to genuinely cross-cutting abstractions)

Each module should expose a small public API and keep its internal persistence and implementation details private. Avoid a giant global `common` package that becomes a dumping ground.

### 12.2 Suggested package layout

```text
com.example.cinema
├── CinemaApplication.java
├── identity
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── catalog
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── cinema
├── showtime
├── inventory
├── booking
├── payment
├── ticket
├── notification
├── reporting
└── shared
    ├── clock
    ├── error
    └── observability
```

Do not create empty layers for every module by default. Add packages when there is a real responsibility to separate.

### 12.3 Layer responsibilities

- **API:** HTTP routes/controllers, request/response DTOs, validation, HTTP mapping.
- **Application:** use cases, transaction orchestration, authorization coordination, external port interfaces.
- **Domain:** business rules, state transitions, value objects, domain events, invariants; should not depend on web or ORM frameworks where practical.
- **Infrastructure:** JPA mappings/repositories, payment provider adapters, messaging, file/object storage, external clients.

A simpler feature may use a thinner structure. The goal is dependency direction and clarity, not maximum folder count.

### 12.4 Dependency direction

Prefer:

```text
API -> Application -> Domain
Infrastructure -> Application/Domain contracts
```

The domain should not import Spring MVC, web request classes, payment SDKs, or database-specific code. Application services coordinate use cases. Infrastructure implements required ports.

### 12.5 When to choose architecture styles

| Architecture | Suitable when | Avoid when |
|---|---|---|
| Simple monolith | Small scope, simple domain, one team, low operational complexity | Boundaries have become tangled and independent modules need stronger separation |
| Modular monolith | Several business capabilities, one primary deployment/team, strong consistency requirements | Independent teams need truly independent deployments or scaling |
| Microservices | Independent scaling/deployment/ownership, clear bounded contexts, mature operations and observability | Small team, unclear domain, limited tests/automation, or when chosen only for resume appeal |
| Event-driven architecture | Multiple consumers need reliable reactions to domain events and asynchronous decoupling | A simple synchronous call is sufficient and operational complexity is not justified |

Do not split a service simply because it has many tables or controllers. Split by business capability, data ownership, operational need, and consistency boundary.

### 12.6 Spring Modulith (optional)

Consider Spring Modulith after the modular boundaries exist. It can help document and verify module dependencies and prevent cyclic dependencies. Do not introduce it before the team understands the module boundaries.

---

## 13. Non-Functional Requirements

### 13.1 Security

- Use a maintained Spring Security configuration.
- Store passwords with a modern adaptive password hash; never store plaintext or reversible passwords.
- Prefer secure, HttpOnly, SameSite cookies for browser sessions when the client topology supports it; define CSRF protection for cookie-authenticated state-changing requests.
- If mobile token authentication is required, define token lifetime, refresh rotation/revocation, storage, and replay protection explicitly.
- Use TLS for all external traffic.
- Enforce object-level authorization for booking, payment, and ticket resources.
- Verify webhook signatures and prevent replay where the provider supports it.
- Validate file size, content type, extension, and actual file signature; generate storage keys server-side.
- Keep uploaded receipts and ticket artifacts outside the public web root or in private object storage; issue short-lived authorized access where appropriate.
- Never log passwords, session tokens, full payment credentials, provider secrets, or unnecessary personal information.
- Use rate limits for login, booking, hold creation, payment initiation, and ticket validation.
- Use least-privilege database and provider credentials.
- Store secrets in environment/secret management, not Git.
- Record security-sensitive admin and staff actions in an audit trail.

### 13.2 Reliability

- Use database constraints and short transactions for critical invariants.
- Make payment webhook and notification consumers idempotent.
- Add reconciliation for uncertain external payment state.
- Use timeouts, bounded retries, and circuit breakers for external services where justified.
- Do not retry non-idempotent operations without an idempotency strategy.
- Define backup/restore procedures and periodically test restoration.

### 13.3 Performance and scalability

- Add pagination and database indexes according to measured query patterns.
- Avoid ORM N+1 queries; use projections/fetch plans intentionally.
- Cache only suitable data and define invalidation behavior.
- Separate high-volume read paths from correctness-critical seat writes when measurements justify it.
- Use connection pools with sensible limits and monitor saturation.
- Do not hold database transactions during network calls or user think-time.
- Establish load-test scenarios based on movie-release peaks and seat contention.

### 13.4 Observability

- Structured logs with timestamp, level, service, environment, trace/correlation ID, and safe business identifiers.
- Metrics for request rate, latency, error rate, DB pool, booking creation, seat conflicts, hold expiration, payment outcomes, webhook lag, ticket scan results, and outbox backlog.
- Distributed tracing across backend and external dependencies when the topology requires it.
- Health/readiness/liveness endpoints with appropriate dependency checks.
- Alerts for payment reconciliation backlog, stuck holds, outbox backlog, elevated seat conflicts, payment mismatch, and ticket validation failures.

### 13.5 Maintainability

- Code must be formatted, tested, and reviewed.
- API contracts must be documented.
- Database changes use versioned migrations.
- Modules have explicit dependencies.
- Critical workflows have sequence diagrams and runbooks.
- Configuration differs by environment without code changes.

---

## 14. Legacy Reengineering and Migration Strategy

### 14.1 Do not start with a big-bang rewrite

Use a **strangler-style migration** if the legacy system is live and can route traffic through a controlled boundary. If it is an offline/abandoned application with no production users or required data, a clean replacement may be more practical. Decide after discovery.

### 14.2 Migration stages

1. **Inventory:** map apps, APIs, databases, providers, jobs, users, and integrations.
2. **Characterize behavior:** add tests around current booking, payment, cancellation, and ticket flows.
3. **Define canonical domain:** establish IDs, state machines, data ownership, and API contracts.
4. **Build target foundation:** authentication, observability, catalog, cinema, showtime, and migration tooling.
5. **Shadow/read-only validation:** compare new catalog/showtime responses to legacy data where possible.
6. **Migrate low-risk capabilities:** move catalog/admin reads or selected features first.
7. **Migrate booking with controlled cutover:** only one system may own seat allocation for a given showtime at a time. Never allow both legacy and new systems to independently sell the same inventory without a shared atomic authority.
8. **Migrate payments/tickets carefully:** reconcile every payment and ticket state; define provider callback routing.
9. **Progressive traffic shift:** use a feature flag, routing layer, or controlled client release.
10. **Reconcile and retire:** verify totals and records, then decommission legacy components only after sign-off and rollback window.

### 14.3 Data migration requirements

- Define source-to-target mapping for every table/entity.
- Preserve stable external identifiers where required; otherwise maintain a mapping table.
- Deduplicate customers and booking references using deterministic rules.
- Preserve payment provider references and ticket redemption history.
- Validate row counts, monetary totals, state distributions, orphan references, and date/time conversions.
- Use repeatable, restartable migration jobs with checkpointing.
- Encrypt backups and migration files; limit access and define retention/deletion.
- Test migration against a production-like copy with sensitive data masked where possible.
- Prepare a rollback/forward-recovery plan.

### 14.4 Cutover rule

There must be one authoritative owner of seat inventory for each showtime at any given moment. Dual writes to independent inventory stores are prohibited unless a formally designed consistency protocol exists and is tested.

---

## 15. Testing Strategy and Definition of Done

### 15.1 Test layers

- **Unit tests:** domain transitions, pricing, validation, state machines, idempotency decisions.
- **Slice tests:** MVC/controller validation, repository queries, security authorization.
- **Integration tests:** PostgreSQL transaction behavior, locks, migrations, webhook processing.
- **Concurrency tests:** multiple requests for the same seat; duplicate payment event; duplicate ticket redemption.
- **Contract tests:** payment provider adapter and API response schema.
- **End-to-end tests:** discover -> hold -> pay (mock) -> ticket -> scan.
- **Migration tests:** source/target reconciliation and restartability.
- **Performance tests:** realistic showtime/seat contention and movie-release peak scenario.

### 15.2 Required critical scenarios

1. Two customers attempt to hold the same seat at the same time; exactly one succeeds.
2. Customer requests multiple seats where one is unavailable; no partial hold remains.
3. Hold expires and a later customer can reserve the seat.
4. A payment success webhook is delivered twice; payment is applied once.
5. A payment event arrives out of order; final state does not regress incorrectly.
6. A payment is successful after its seat hold expires; policy is applied without overselling.
7. Ticket issue event is repeated; only one valid ticket per booking-seat is created.
8. Two staff members scan the same ticket concurrently; at most one redemption succeeds.
9. Customer A cannot access customer B's booking by guessing an ID.
10. Cinema operator cannot manage another cinema's showtime.
11. Database failure after status mutation rolls back audit/outbox writes in the same transaction.
12. Migration can restart safely without duplicating records.

### 15.3 Definition of Done

A feature is complete only when:

- Acceptance criteria are met.
- Server-side authorization is enforced.
- Validation and error mapping are consistent.
- Database migrations and constraints are included.
- Relevant unit/integration tests pass.
- Critical concurrency and idempotency paths are tested where applicable.
- Logs/metrics are added for operationally important actions.
- API documentation and examples are updated.
- No secrets or sensitive payloads are exposed.
- Code review and quality gates pass.

---

## 16. Delivery Roadmap

This is a suggested sequence, not a fixed estimate. Actual schedule depends on legacy complexity, available source code, integration access, and team size.

| Phase | Deliverable | Exit criteria |
|---|---|---|
| 0. Discovery | Legacy audit, domain map, integration inventory | Unknowns and risks documented |
| 1. Foundation | Spring Boot app, PostgreSQL, migrations, CI, observability | Repeatable local build/deploy |
| 2. Catalog | Movies, cinemas, auditoriums, seats, showtimes | Customer can discover a valid showtime |
| 3. Inventory | Showtime-seat availability and atomic hold | Concurrency invariant passes |
| 4. Booking | Booking lifecycle and customer history | Booking state machine and ownership tests pass |
| 5. Payment | Mock gateway adapter, callback verification, reconciliation design | Duplicate/out-of-order events handled |
| 6. Ticket | Ticket issuance and atomic redemption | Duplicate scan test passes |
| 7. Operations | Admin dashboard, staff access, reporting, notification | Role scopes and audit trail verified |
| 8. Reengineering | Data migration and progressive cutover | Reconciliation and rollback sign-off |
| 9. Hardening | Load, security, recovery, operational runbooks | Production readiness review passed |

---

## 17. Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Legacy business rules are undocumented | Incorrect rewrite | Discovery, stakeholder validation, characterization tests |
| Both old and new systems sell the same seat | Double booking | Single inventory authority and controlled cutover |
| Payment callback is duplicated or delayed | Wrong booking/payment state | Idempotency, provider event ledger, reconciliation |
| Hold expiry races with payment callback | Overselling or lost payment | Explicit late-payment policy and atomic state transition |
| Cinema partner API is unreliable | Stale schedule/seat data | Timeouts, circuit breaker, reconciliation, source-of-truth definition |
| High contention at ticket release | Booking failures/latency | Load testing, short transactions, deterministic lock ordering, DB tuning |
| QR token is copied or replayed | Fraudulent entry | High-entropy token, server verification, atomic redemption, audit |
| Personal/payment data leaks | Legal and trust impact | Data minimization, access control, secure storage, safe logs |
| Premature microservices | High operational cost | Modular monolith and evidence-based extraction |
| Data migration mismatch | Lost/incorrect booking history | Repeatable migration, checksums/totals, reconciliation, rollback |

---

## 18. Open Decisions

The following decisions must be made after legacy discovery:

1. Android client: retain Java, migrate incrementally to Kotlin, or rebuild with another mobile framework?
2. Is the legacy backend still active and serving production users?
3. Which cinema partner/provider is the source of truth for showtimes and seats?
4. Which payment provider(s) must be integrated?
5. Is a guest checkout allowed?
6. What is the exact seat-hold duration and extension policy?
7. Are partial cancellations/refunds allowed?
8. Is a ticket one per seat or one per booking?
9. What is the venue's ticket scanning/offline behavior?
10. Which customer and transaction data must be retained and for how long?
11. What are the expected peak users and booking throughput?
12. What deployment environment, security, and operational standards apply?

Do not freeze the production architecture until these questions have owners and documented answers.

---

## 19. Glossary

- **Showtime:** one scheduled screening of a movie in an auditorium.
- **Seat inventory:** availability state of a physical seat for a particular showtime.
- **Seat hold:** temporary allocation of a seat before confirmed payment.
- **Booking:** customer's reservation/order for a showtime and seats.
- **Payment attempt:** one attempt to collect payment for a booking.
- **Webhook:** server-to-server event notification sent by a payment provider.
- **Idempotency:** repeated delivery of the same logical command/event produces no duplicate business effect.
- **Reconciliation:** comparing internal payment/booking records with an external provider's authoritative records.
- **Outbox:** database table used to reliably publish events after a transaction commits.
- **Optimistic locking:** detecting conflicting updates using a version field.
- **Pessimistic locking:** locking database rows to serialize conflicting operations.
- **Modular monolith:** one deployable application composed of modules with explicit boundaries.
- **Strangler migration:** progressively replacing parts of a legacy system while keeping the old system operating during transition.

---

## 20. References

Use official documentation and the selected provider's current integration/security documentation. Re-check versions and compatibility before implementation.

1. [Spring Boot System Requirements](https://docs.spring.io/spring-boot/system-requirements.html)
2. [Spring Boot Reference](https://docs.spring.io/spring-boot/reference/)
3. [Spring Framework Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
4. [Spring Data JPA Locking](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html)
5. [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
6. [Spring Modulith Reference](https://docs.spring.io/spring-modulith/reference/)
7. [PostgreSQL Explicit Locking](https://www.postgresql.org/docs/current/explicit-locking.html)
8. [Java BigDecimal API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html)

---

**Document owner:** Project owner / engineering team  
**Next recommended action:** Complete Section 2 discovery before choosing the final migration path or modifying the existing production system.
