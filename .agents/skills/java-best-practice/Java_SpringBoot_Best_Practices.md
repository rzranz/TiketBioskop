# Java & Spring Boot — Clean Code, Design Patterns, and Architecture Guide

**Document type:** Engineering Standards / Best-Practices Guide  
**Applies to:** Cinema Booking Platform reengineering  
**Version:** 1.0  
**Status:** Baseline engineering guidance; validate against the audited legacy constraints and selected dependency versions  
**Language baseline:** Java 21 is a conservative learning baseline; newer supported LTS versions may be selected after compatibility checks.  
**Framework baseline:** Spring Boot 4.x line, subject to the official system requirements and dependency compatibility at implementation time.

> This guide is intentionally separate from the product PRD. The PRD defines **what the system must do**; this guide defines **how the Java/Spring Boot code should be designed, implemented, tested, and operated**. These practices are defaults, not religious rules: prefer the simplest design that protects a real business invariant.

---

## 1. Engineering Philosophy

### 1.1 Primary principles

1. **Readable code over clever code.** Optimize first for the next engineer who must understand and change the behavior.
2. **Domain rules must have an owner.** Booking, payment, seat allocation, and ticket redemption rules must not be scattered across controllers or UI components.
3. **Explicit dependencies.** Prefer constructor injection and explicit method parameters over hidden global state.
4. **Small, cohesive modules.** Organize around business capabilities such as `booking`, `inventory`, `payment`, and `ticket`.
5. **Protect invariants at multiple layers.** Validate at the API boundary, enforce business rules in application/domain code, and use database constraints/transactions for concurrency-critical guarantees.
6. **Test behavior, not implementation trivia.** Verify observable outcomes, state transitions, authorization, rollback, and concurrency.
7. **Measure before optimizing.** Add indexes, caches, queues, and services based on evidence or a clear correctness requirement.
8. **Prefer a modular monolith until distribution is justified.** A distributed system adds network failure, data consistency, deployment, and observability costs.
9. **Secure defaults.** Least privilege, data minimization, safe logging, secret management, and authenticated external callbacks.
10. **Incremental refactoring.** Improve legacy code behind regression tests instead of performing speculative rewrites.

### 1.2 Definition of clean code for this project

Code is considered maintainable when:

- Names express business intent.
- A method has one clear reason to change.
- A class has a cohesive responsibility.
- State transitions are explicit and validated.
- Dependencies point in an intentional direction.
- Side effects (database, network, file system, messaging) are visible.
- Errors preserve context without leaking internal details to clients.
- Tests explain expected behavior.
- No abstraction exists solely because a pattern is fashionable.

---

## 2. Java Language Best Practices

### 2.1 Use types that represent the domain

Avoid passing primitive values everywhere when a value has domain meaning. Introduce value objects when they protect important invariants.

For money, use `BigDecimal`, never `float` or `double`.

```java
BigDecimal ticketPrice = new BigDecimal("75000.00");
BigDecimal serviceFee = new BigDecimal("5000.00");
BigDecimal total = ticketPrice.add(serviceFee);
```

Rules:

- Prefer `new BigDecimal(String)` or `BigDecimal.valueOf(...)` when appropriate; avoid `new BigDecimal(double)` for decimal business values.
- Define scale and rounding policy explicitly at the business boundary.
- Store currency code next to amount (`IDR`, etc.).
- Do not assume every currency uses the same number of minor units.
- Do not use `BigDecimal.equals()` when numeric comparison independent of scale is intended; understand `compareTo()`.

### 2.2 Use the right date/time type

- `Instant`: an absolute moment in UTC (payment received, booking created, ticket redeemed).
- `LocalDate`: a calendar date without time (movie release date, customer birth date where applicable).
- `LocalTime`: local wall-clock time without date (a recurring schedule rule).
- `LocalDateTime`: local date and time without zone; do not use it alone to represent a globally unambiguous instant.
- `ZoneId`: an IANA time zone such as `Asia/Jakarta` for cinema-local scheduling/display.

Persist absolute timestamps consistently (typically PostgreSQL `timestamptz` mapped to `Instant`) and convert to the cinema's zone for presentation.

### 2.3 Prefer immutable data at boundaries

Use Java `record` for simple request/response DTOs where its semantics fit:

```java
public record CreateSeatHoldRequest(
    @NotNull UUID showtimeId,
    @NotEmpty @Size(max = 8) List<@NotBlank String> seatIds
) {}
```

Good uses:

- API request/response DTOs.
- Small command/query objects.
- Immutable event payloads.

Avoid using a record as a JPA entity. JPA entities have lifecycle/proxy requirements and mutable persistence state; use a normal non-final class designed for JPA.

### 2.4 Nullability and Optional

- Decide and document a nullability convention. Use validation annotations at API boundaries.
- Return `Optional<T>` from a lookup method when absence is a normal result.
- Do not use `Optional` for entity fields, DTO fields, method parameters, or collection elements as a default convention.
- Never call `.get()` on an Optional without checking presence; prefer `orElseThrow`, `map`, or explicit branching.
- Do not use `null` to represent multiple business states. Use a status enum or a dedicated result type.

### 2.5 Encapsulation

Do not expose unrestricted setters for critical state.

Prefer behavior-oriented methods:

```java
public void confirmPayment(Instant paidAt) {
    if (status != BookingStatus.PENDING_PAYMENT) {
        throw new InvalidBookingTransitionException(status, BookingStatus.CONFIRMED);
    }
    this.status = BookingStatus.CONFIRMED;
    this.paidAt = Objects.requireNonNull(paidAt);
}
```

The application service must still enforce authorization and transaction boundaries. A domain method protects a state invariant; it does not authenticate the caller.

### 2.6 Collections and streams

- Choose `List`, `Set`, and `Map` according to semantics, not habit.
- Use `Set` to detect duplicate requested seat IDs.
- Avoid deeply nested stream pipelines that obscure business logic.
- Do not use streams for side effects or complex transactional workflows.
- Be deliberate about mutability; return unmodifiable views/copies from domain APIs when appropriate.
- Remember `Stream` is single-use.

### 2.7 Exceptions and error handling

- Use exceptions for exceptional failures or invalid operations, not as ordinary branching for expected high-frequency outcomes.
- Define meaningful domain exceptions (`SeatUnavailableException`, `InvalidBookingTransitionException`, `PaymentVerificationException`).
- Translate exceptions to stable API error codes in a centralized exception handler.
- Preserve causes when wrapping infrastructure errors.
- Never return stack traces, SQL, provider payloads, or secret values to clients.
- Do not catch `Exception` and silently continue.
- Do not use `System.out.println` for application logging.

### 2.8 Naming and formatting

- Types: `PascalCase` (`SeatHoldService`).
- Methods/fields: `camelCase` (`createSeatHold`).
- Constants: `UPPER_SNAKE_CASE` for true constants (`MAX_SEATS_PER_HOLD`).
- Packages: lowercase (`com.example.cinema.booking`).
- Use descriptive business names; avoid vague names such as `DataManager`, `CommonHelper`, `Processor`, or `Utils` unless their scope is precise.
- Keep methods short enough to understand; do not enforce arbitrary line limits at the expense of clarity.
- Use the project's formatter/checkstyle/Spotless convention consistently.

---

## 3. Object-Oriented Design and SOLID

### 3.1 Single Responsibility Principle

A class should have one cohesive reason to change.

Bad direction:

`BookingService` validates HTTP DTOs, queries JPA directly, calls payment provider SDK, creates QR images, sends email, and calculates sales reports.

Better separation:

- `CreateBookingUseCase`: coordinates a booking command.
- `SeatInventory`: owns seat availability/hold operations.
- `PricingPolicy`: calculates authoritative price.
- `PaymentGateway`: external payment contract.
- `TicketIssuer`: creates tickets after confirmed payment.
- `NotificationPort`: notification contract.

Do not create a class for every three lines of code. Split when responsibility, dependency, lifecycle, or testing needs justify it.

### 3.2 Open/Closed Principle

Keep stable workflows open to new implementations through explicit extension points when variation is real.

For multiple payment providers, an interface can isolate provider differences:

```java
public interface PaymentGateway {
    PaymentSession createSession(CreatePaymentCommand command);
    ProviderPaymentStatus queryStatus(String providerReference);
    RefundResult refund(RefundCommand command);
}
```

Implementations:

- `ProviderAPaymentGateway`
- `ProviderBPaymentGateway`
- `FakePaymentGateway` for local/demo testing

Do not create a plugin architecture if only one implementation will ever exist and no meaningful variation is expected. A simple class can be enough initially.

### 3.3 Liskov Substitution Principle

Any implementation of an interface must honor the documented contract. A fake payment gateway should behave predictably and should not return success for invalid input merely to simplify tests.

### 3.4 Interface Segregation Principle

Prefer small interfaces focused on a consumer's needs. Do not force a ticket-validation component to depend on a large repository interface containing unrelated admin/reporting methods.

### 3.5 Dependency Inversion Principle

Business/application logic should depend on abstractions for external systems. Infrastructure adapters implement those abstractions. Avoid importing a specific payment SDK into the booking domain.

---

## 4. Architecture and Package Organization

### 4.1 Recommended baseline: modular monolith

Start with one deployable Spring Boot application organized by business module. Keep module boundaries explicit, but do not create independent deployment units until justified.

Suggested package layout:

```text
com.example.cinema
├── CinemaApplication.java
├── identity
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── catalog
├── cinema
├── showtime
├── inventory
├── booking
├── payment
├── ticket
├── notification
├── reporting
└── shared
```

Within a module:

```text
booking/
├── api/
│   ├── BookingController.java
│   ├── CreateBookingRequest.java
│   └── BookingResponse.java
├── application/
│   ├── CreateBookingUseCase.java
│   └── BookingQueryService.java
├── domain/
│   ├── Booking.java
│   ├── BookingStatus.java
│   └── BookingPolicy.java
└── infrastructure/
    ├── persistence/
    └── payment/       # only if this module owns a specific adapter
```

This is a guide, not a requirement to create every subfolder in every module. Small modules may remain thinner.

### 4.2 Layer responsibilities

| Layer | Owns | Must not own |
|---|---|---|
| API | HTTP mapping, DTOs, request validation, status codes | Business workflow and JPA query orchestration |
| Application | Use cases, orchestration, transaction boundary, authorization coordination | HTTP servlet details or provider SDK-specific payloads |
| Domain | Business invariants, state transitions, value objects | Spring annotations, JPA repositories, web framework |
| Infrastructure | Database adapters, external provider clients, file storage, messaging | Decisions about whether a business action is allowed |

For simple use cases, a conventional `Controller -> Service -> Repository` is acceptable. Add domain ports/adapters when they clarify the dependency direction or isolate external systems.

### 4.3 Dependency direction

Preferred dependency flow:

```text
HTTP/API -> Application -> Domain
Infrastructure -> Application/Domain contracts
```

The domain should not import `org.springframework.web.*`, payment provider SDK classes, or persistence implementation details.

### 4.4 Avoid architecture theatre

Do not add all of these automatically:

- Generic `BaseController`, `BaseService`, `BaseRepository`.
- Interfaces with one implementation and no substitution/testing/ownership reason.
- Five layers that only forward method calls.
- CQRS, event sourcing, microservices, Kafka, or Kubernetes only to look enterprise.
- A global `common` or `utils` package containing unrelated business behavior.

The simplest architecture that enforces the real requirements is usually best.

### 4.5 Spring Modulith

When module boundaries become important, Spring Modulith can verify module structure, detect cycles, and restrict access to internal packages. Use it as an architectural test, not as a substitute for thoughtful domain boundaries.

Example verification test (adapt package/class names to the application):

```java
class ModuleStructureTest {
    @Test
    void modulesShouldRespectBoundaries() {
        ApplicationModules.of(CinemaApplication.class).verify();
    }
}
```

---

## 5. Design Patterns: Use When They Solve a Real Problem

| Pattern | Recommended use in cinema booking | Why |
|---|---|---|
| Strategy | Select a payment provider or pricing strategy | Allows behavior to vary behind a stable contract |
| Adapter | Wrap payment, SMS/email, cinema-partner, and object-storage SDKs | Keeps third-party details out of core business logic |
| State pattern / explicit state machine | Booking, payment, hold, ticket lifecycle | Controls allowed transitions and prevents arbitrary status updates |
| Factory | Create a provider-specific payment request or ticket artifact when construction has meaningful rules | Centralizes complex construction; do not use for trivial `new` calls |
| Repository | Encapsulate aggregate persistence/query needs | Keeps application/domain logic away from persistence details |
| Specification | Compose complex search/filter rules when they become difficult to maintain | Reusable query predicates; do not create one for every simple filter |
| Domain Event | Notify other modules after an important business fact occurs | Decouples follow-up work from the initiating use case |
| Transactional Outbox | Reliably publish events after a database commit | Prevents database/message dual-write inconsistency |
| Saga / Process Manager | Coordinate a long-running cross-service flow after microservice extraction | Tracks progress and compensating actions across services |
| Decorator | Add metrics, logging, retry, or caching around a port where appropriate | Adds cross-cutting behavior without changing core implementation |
| Builder | Construct complex immutable request/configuration objects | Improves readability for many optional fields |

### 5.1 Strategy + Adapter for payments

Core contract:

```java
public interface PaymentGateway {
    PaymentSession createSession(PaymentCommand command);
    VerifiedPaymentEvent verifyWebhook(WebhookRequest request);
}
```

Infrastructure adapter:

```java
@Component
public class ExampleGatewayAdapter implements PaymentGateway {
    private final ExampleProviderClient client;

    public ExampleGatewayAdapter(ExampleProviderClient client) {
        this.client = client;
    }

    @Override
    public PaymentSession createSession(PaymentCommand command) {
        // Map internal command to provider request.
        // Call provider and map provider response to internal model.
        throw new UnsupportedOperationException("Implement provider integration");
    }

    @Override
    public VerifiedPaymentEvent verifyWebhook(WebhookRequest request) {
        // Verify signature/timestamp/replay rules using provider documentation.
        throw new UnsupportedOperationException("Implement provider verification");
    }
}
```

Do not ship placeholder exceptions in production. The sample is a structural illustration; the adapter must be implemented and tested against the provider's official current documentation.

### 5.2 State transition as explicit behavior

Avoid:

```java
booking.setStatus(BookingStatus.CONFIRMED);
```

Prefer:

```java
booking.confirmPayment(verifiedPayment, clock.instant());
```

The method validates current state and records required domain information. The application service verifies caller/event authenticity and controls the transaction.

### 5.3 Outbox pattern

Use when reliable asynchronous publication is required:

1. Change booking/payment state and insert an outbox event in one database transaction.
2. Commit.
3. Worker reads unpublished events.
4. Worker publishes event to broker.
5. Worker marks it published (or records retry state).
6. Consumer deduplicates by event ID.

Outbox delivery is generally at-least-once, so consumers must be idempotent. Do not claim exactly-once end-to-end delivery merely because an outbox exists.

---

## 6. Spring Boot Practices

### 6.1 Use constructor injection

Preferred:

```java
@Service
public class BookingService {
    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
}
```

Avoid field injection:

```java
@Autowired
private BookingRepository bookingRepository;
```

Constructor injection makes required dependencies explicit, supports immutable fields, and makes unit tests straightforward.

### 6.2 Keep controllers thin

Controller responsibilities:

- Bind HTTP request.
- Validate request DTO.
- Invoke one application use case/query.
- Map result to response DTO and HTTP status.

A controller should not directly call multiple repositories to implement business workflow.

### 6.3 DTOs at the API boundary

- Do not expose JPA entities as API responses.
- Use dedicated request and response DTOs.
- Use `@Valid` and Jakarta Bean Validation at the boundary.
- Validate business rules again in application/domain logic where necessary; API validation alone is not enough.
- Avoid leaking internal IDs, provider metadata, or fields the caller must not control.

### 6.4 Configuration

- Use typed `@ConfigurationProperties` for grouped application settings.
- Keep non-secret defaults in version control.
- Inject secrets through environment/secret manager.
- Validate mandatory configuration at startup.
- Separate local/test/staging/production profiles carefully; do not let a production profile inherit demo credentials.

### 6.5 Error handling

- Use `@RestControllerAdvice` for consistent error mapping.
- Define stable machine-readable error codes.
- Map domain errors to appropriate HTTP status (for example, seat unavailable -> 409).
- Include correlation/trace ID where safe.
- Do not return exception class names, stack traces, SQL, or provider error bodies to customers.
- Log the exception with contextual identifiers while redacting sensitive data.

### 6.6 Avoid hidden side effects

A service method named `getBooking()` should not unexpectedly send email or charge a payment method. Keep commands and queries clear. If a read operation triggers expensive work or mutation, document it explicitly.

---

## 7. Transactions, Locking, and Booking Correctness

### 7.1 `@Transactional` belongs around a use case

A typical boundary:

```java
@Service
public class CreateSeatHoldUseCase {
    private final SeatInventoryRepository inventory;
    private final BookingRepository bookings;
    private final Clock clock;

    public CreateSeatHoldUseCase(
        SeatInventoryRepository inventory,
        BookingRepository bookings,
        Clock clock
    ) {
        this.inventory = inventory;
        this.bookings = bookings;
        this.clock = clock;
    }

    @Transactional
    public SeatHoldResult execute(CreateSeatHoldCommand command) {
        // Validate all requested seats.
        // Lock rows in deterministic order.
        // Claim all seats or fail the entire operation.
        // Persist booking/hold/audit records.
        // Return after the transaction commits.
        throw new UnsupportedOperationException("Implement use case");
    }
}
```

### 7.2 Important Spring transaction caveats

- In default proxy mode, an internal self-invocation (one method calling another method on the same instance) does not pass through the proxy; the called method's `@Transactional` advice may not run.
- A transaction annotation is metadata; transaction behavior depends on the configured transaction infrastructure and invocation path.
- By default, Spring rolls back on `RuntimeException` and `Error`, not on every checked exception. Use explicit rollback rules where checked exceptions must roll back, and test the behavior.
- Keep transactional methods on Spring-managed beans and normally public application-service methods.
- Do not call remote payment providers while holding a database lock/transaction.
- Keep write transactions short.

### 7.3 Pessimistic vs optimistic locking

Use **pessimistic locking** when conflicting writes to the same seat must be serialized at reservation time. Spring Data JPA supports lock metadata with `@Lock` on repository methods.

Use **optimistic locking** (`@Version`) when conflicts are expected to be relatively uncommon and retry/conflict handling is acceptable, for example editing showtime metadata or some administrative records.

Neither lock replaces a correct database constraint or explicit state validation.

### 7.4 Lock ordering and deadlocks

When reserving multiple seats, lock rows in a deterministic order (for example, sorted seat IDs). This reduces deadlock probability but does not eliminate every possible deadlock. Handle database deadlock/serialization failures with bounded retries only when the operation is safe to retry and idempotency is in place.

### 7.5 One source of truth

PostgreSQL should enforce the final showtime-seat allocation invariant. Redis may improve coordination or caching, but a Redis key alone must not be the only protection against overselling.

---

## 8. JPA / Hibernate Best Practices

### 8.1 Model entities carefully

- Keep JPA entities non-final and provide the required no-argument constructor with appropriate visibility.
- Avoid unrestricted setters for domain state.
- Do not use Lombok `@Data` on JPA entities by default: generated `equals`, `hashCode`, and `toString` can include lazy relationships, trigger queries, recurse through bidirectional associations, or behave badly with generated IDs.
- Be deliberate about `equals/hashCode`; use a strategy compatible with entity identity and lifecycle.
- Avoid bidirectional relationships unless navigation in both directions is truly needed.
- Avoid cascading `REMOVE` across independent business aggregates.
- Consider storing IDs/references and querying explicitly across module boundaries rather than navigating a large object graph.

### 8.2 Fetching and query behavior

- Prefer lazy relationships as a starting point for associations; fetch required data explicitly for each use case.
- Use projections/DTO queries for read-heavy list pages.
- Avoid returning an entity graph to the API and allowing serialization to walk lazy relationships.
- Detect and eliminate N+1 queries using SQL logging/profiling and integration tests.
- Avoid large `findAll()` calls; paginate.
- Use `@EntityGraph`, fetch joins, or explicit queries selectively; understand how they interact with pagination.
- Consider `readOnly = true` for read-only transaction boundaries when appropriate, but do not treat it as a universal performance switch.

### 8.3 Persistence context and dirty checking

An entity managed by the persistence context can be tracked; changing its state may be synchronized during flush/commit without calling `save()` again in every case. Understand managed vs detached entities, flush, clear, and transaction boundaries before relying on this behavior.

### 8.4 Batch and large operations

- Use pagination/streaming or JDBC/batch approaches for large exports/migrations.
- Do not load thousands of entities into one persistence context without controlling memory and flush/clear behavior.
- For high-volume writes, benchmark JPA batching and consider JDBC or batch tooling where justified.

---

## 9. API Design Practices

- Use resource-oriented nouns: `/movies`, `/showtimes`, `/bookings`, `/tickets`.
- Use HTTP methods according to intended semantics; action endpoints are acceptable for domain commands such as `/bookings/{id}/cancel`.
- Version public APIs (`/api/v1`) when compatibility requires it.
- Use consistent pagination, filtering, sorting, and response shapes.
- Return `201 Created` for creation, `409 Conflict` for state/concurrency conflicts, `401` for missing/invalid authentication, and `403` for authenticated but unauthorized requests.
- Use RFC 9457 Problem Details or a consistent equivalent for errors.
- Document with OpenAPI.
- Validate path/query/body inputs and enforce maximum sizes.
- Do not accept a client-supplied price, payment status, ticket status, or owner ID as authoritative.
- Avoid leaking whether another user's booking exists if that reveals sensitive information; define a consistent 403/404 policy.
- Add idempotency keys to booking/payment creation commands where retries can cause duplicate business effects.

---

## 10. Security Best Practices

### 10.1 Authentication and authorization

- Choose session-cookie authentication or token authentication based on client needs; do not mix patterns without a clear design.
- For browser-based cookie sessions: use `HttpOnly`, `Secure`, and an appropriate `SameSite` policy; implement CSRF protection for state-changing requests.
- For mobile access tokens: define expiry, refresh rotation/revocation, secure client storage, issuer/audience validation, and replay risk.
- Enforce authorization at the service/use-case boundary for critical resource operations, not only by hiding frontend buttons.
- Scope cinema operators and gate staff to assigned venues.
- Use least-privilege roles and avoid a generic all-powerful `ADMIN` path for routine operations.

### 10.2 Payment security

- Never store card PAN/CVV or sensitive payment credentials unless the organization is explicitly designed and certified to handle them; prefer a hosted/tokenized provider flow.
- Verify webhooks using provider-specific signature rules and constant-time comparison where applicable.
- Validate merchant reference, amount, currency, provider transaction ID, event timestamp, and allowed state transition.
- Deduplicate provider event IDs.
- Never trust a frontend redirect as payment proof.
- Keep provider secrets in secret management.
- Redact provider payloads before logging.

### 10.3 File upload security

- Allowlist expected file types and size limits.
- Check actual file signatures where feasible; do not trust filename/MIME header alone.
- Generate storage keys server-side; never use a raw client filename as a storage path.
- Store files outside a public web root or in a private object store.
- Scan files if the operating environment provides a suitable malware-scanning capability.
- Authorize every download.
- Define retention and deletion rules.

### 10.4 Application security checklist

- Parameterized SQL/JPA queries; no string-concatenated user SQL.
- Output encoding and safe rendering on clients.
- CORS allowlist, not wildcard credentials.
- Rate limiting for login, hold creation, payment initiation, and ticket scanning.
- Secure headers and TLS termination.
- Dependency vulnerability scanning and timely patches.
- No secrets, tokens, personal data, or raw payment payloads in logs.
- Audit sensitive operations.

---

## 11. Concurrency, Asynchrony, and Messaging

### 11.1 Do not use `@Async` as a durable job queue

`@Async` runs work asynchronously within the application process. It does not by itself guarantee persistence, retry after process crash, or delivery exactly once. Use a durable broker or database-backed job/outbox approach for critical background operations.

### 11.2 Suitable asynchronous work

- Email/push notification after booking/payment events.
- Reconciliation polling.
- Expired hold cleanup.
- Report generation.
- Partner synchronization.

### 11.3 Retry strategy

- Retry only transient failures.
- Use bounded retries with backoff and jitter.
- Make the operation idempotent first.
- Define dead-letter/manual recovery for exhausted retries.
- Do not retry validation failures, unauthorized actions, or permanent provider rejections.

### 11.4 Scheduler safety

A scheduled method may run in multiple application instances. Use database claiming/locking or a distributed scheduler/lease when only one worker should process a job. Make cleanup jobs idempotent. Do not assume `@Scheduled` runs globally once across a cluster.

---

## 12. Testing Strategy

### 12.1 Unit tests

Test domain/business rules without Spring context where possible:

- Valid and invalid booking transitions.
- Price calculation and rounding.
- Hold expiry decisions using an injected `Clock`.
- Payment event transition rules.
- Ticket redemption rules.

Use deterministic time in tests; avoid depending on wall-clock time.

### 12.2 Spring slice tests

Use web, data, or security test slices to verify a focused layer without booting the entire system when possible.

### 12.3 Integration tests

Use a real PostgreSQL test database (for example via Testcontainers) for:

- Migration correctness.
- Unique constraints.
- Pessimistic locking.
- Optimistic locking.
- Transaction rollback.
- Query behavior and indexes where measurable.

An in-memory database can be useful for fast tests, but it may not reproduce PostgreSQL lock/constraint semantics. Critical concurrency behavior must be tested against the production database engine.

### 12.4 Concurrency tests

- Launch concurrent requests for the same seat.
- Assert exactly one hold succeeds.
- Assert no partial multi-seat hold remains.
- Repeat payment webhooks and assert one business effect.
- Scan one ticket concurrently and assert no more than one successful redemption.
- Test the behavior after transaction rollback and retry.

### 12.5 Contract and end-to-end tests

Test provider adapter mappings with recorded/synthetic fixtures that do not contain real secrets. Run a complete flow: discover showtime -> hold -> create payment -> mock verified webhook -> issue ticket -> scan ticket.

### 12.6 Test naming

Prefer behavior-oriented names:

```java
@Test
void shouldRejectSecondHoldForSameShowtimeSeat() { ... }

@Test
void shouldNotIssueTicketWhenPaymentIsNotConfirmed() { ... }
```

Avoid test names that describe only implementation details.

---

## 13. Performance and Observability

### 13.1 Performance practices

- Measure p50/p95/p99 latency and throughput under a representative load.
- Monitor DB connection pool utilization and query latency.
- Add indexes for actual query patterns; inspect query plans.
- Use pagination and projections for listing endpoints.
- Avoid N+1 queries and excessive eager fetching.
- Keep seat-lock transactions short.
- Do not make remote calls inside seat allocation transactions.
- Apply caching only to read data with explicit invalidation/TTL policy.
- Set timeouts for outbound HTTP clients and database operations.
- Tune JVM and thread pools based on profiling, not guesswork.

### 13.2 Observability

Use structured logs, metrics, and tracing. Spring Boot Actuator and Micrometer are common building blocks.

Key metrics:

- HTTP request count, latency, and error rate.
- Active database connections and pool wait time.
- Seat hold success/conflict rate.
- Hold expiration lag.
- Booking-to-payment conversion and payment pending age.
- Webhook verification failures and duplicate event count.
- Outbox backlog and oldest unpublished event age.
- Ticket scan success/rejection rate.
- Reconciliation mismatch count.

Avoid high-cardinality labels such as customer ID, booking ID, or ticket token in metrics. Put safe identifiers in trace/log fields instead.

---

## 14. Build, Dependency, and Repository Hygiene

### 14.1 Build tool

Choose Maven or Gradle and standardize the project. Maven is a reasonable beginner-friendly default for this project. Use the Maven Wrapper (`mvnw`) so developers and CI use the project's declared Maven version.

### 14.2 Dependency management

- Use Spring Boot's dependency management/BOM where appropriate.
- Avoid manually overriding Spring transitive dependency versions without a reason.
- Keep production dependencies minimal.
- Review licenses and known vulnerabilities.
- Do not add libraries for functionality already handled well by the JDK/Spring ecosystem.

### 14.3 Repository layout

```text
cinema-booking/
├── backend/
│   ├── mvnw
│   ├── pom.xml
│   └── src/
├── admin-frontend/
├── android-app/              # existing client or future modernization
├── docs/
│   ├── architecture/
│   ├── adr/
│   ├── api/
│   └── runbooks/
├── infra/
│   └── docker/
├── .github/workflows/
└── README.md
```

Do not move the existing source into this exact structure until the legacy audit confirms the repository and migration plan.

### 14.4 Database migrations

- Use Flyway or Liquibase as the single schema migration authority.
- Never rely on Hibernate `ddl-auto=update` in production.
- Make migrations forward-only in production unless an explicit rollback strategy is tested.
- Review destructive changes and use expand/migrate/contract for zero/low-downtime evolution where required.
- Back up and validate before production migrations.

### 14.5 CI quality gates

A pull request should run:

1. Formatting/lint/static checks.
2. Compile/package.
3. Unit tests.
4. Integration tests requiring containers where supported.
5. Architecture/module tests.
6. Dependency/security scan.
7. Migration validation.
8. API/OpenAPI contract checks where configured.

---

## 15. When to Use Which Design Pattern or Architecture

### 15.1 Pattern decision guide

| Problem | Start with | Add complexity when |
|---|---|---|
| One payment provider | Direct service behind a small interface if replacement/testing warrants it | A second provider or meaningful provider variation exists -> Strategy + Adapter |
| Booking lifecycle | Explicit enum + transition methods | Complex branching/guards grow -> dedicated state machine/policy model |
| External provider SDK | Adapter | Multiple protocols/providers require routing -> Strategy/Factory |
| Reliable event publication | In-process event only for non-critical same-process reactions | Event must survive process crash -> transactional outbox + broker |
| Reusable complex search filters | Explicit query method | Filters become composable/reused -> Specification/query object |
| Multiple deployment units | Modular monolith | Independent deployment/scaling/team ownership is measurable -> consider microservices |
| Cross-service booking/payment workflow | Local DB transaction in a monolith | Domain split requires long-running distributed workflow -> Saga/process manager |

### 15.2 Architecture decision record (ADR)

For important decisions, create a short ADR:

```text
ADR-0001: Use modular monolith for initial backend
Status: Accepted
Context: Booking and seat inventory require strong consistency; team/ops capacity is limited.
Decision: One Spring Boot deployment with explicit business modules.
Alternatives: Layered monolith without boundaries; microservices from day one.
Consequences: Simpler operations and local transactions; module boundaries must be enforced.
Review trigger: Independent scaling/ownership or operational isolation becomes necessary.
```

Record decisions about seat locking, payment source of truth, auth/session, file storage, and legacy cutover.

---

## 16. Code Review Checklist

### Java and clean code
- [ ] Names express domain intent.
- [ ] Methods/classes have cohesive responsibilities.
- [ ] No unnecessary inheritance or generic base classes.
- [ ] Interfaces exist for a real boundary or substitution need.
- [ ] Money uses `BigDecimal`; time semantics are explicit.
- [ ] No uncontrolled setters for important state.
- [ ] No catch-and-ignore exception handling.

### Spring and architecture
- [ ] Constructor injection is used.
- [ ] Controller contains no business workflow.
- [ ] Domain does not depend on HTTP/persistence/provider SDKs.
- [ ] Transaction boundary is at the application use case.
- [ ] `@Transactional` proxy/self-invocation behavior is understood.
- [ ] DTOs are separate from persistence entities.
- [ ] Module dependencies are intentional and acyclic.

### Database and concurrency
- [ ] Constraints protect unique business invariants.
- [ ] Seat holds are atomic and showtime-scoped.
- [ ] Lock acquisition order is deterministic for multi-seat operations.
- [ ] No external network call occurs while holding booking locks.
- [ ] Payment/webhook operations are idempotent.
- [ ] Queries are paginated and avoid N+1 behavior.

### Security and reliability
- [ ] Authorization checks resource ownership/scope.
- [ ] CSRF/CORS/session/token design matches client topology.
- [ ] Webhook authenticity is verified.
- [ ] Uploads are validated and stored privately.
- [ ] Sensitive fields are excluded from logs/responses.
- [ ] Retries are bounded and only used for idempotent/transient operations.

### Tests and operations
- [ ] Unit and integration tests cover changed behavior.
- [ ] Critical concurrency tests use PostgreSQL semantics.
- [ ] Migration is tested.
- [ ] Metrics/logs exist for important workflow transitions.
- [ ] Documentation and OpenAPI are updated.
- [ ] CI quality gates pass.

---

## 17. Recommended Learning and Implementation Order

1. Java language essentials: types, records, classes, interfaces, collections, exceptions, `BigDecimal`, date/time.
2. Spring Boot basics: application startup, configuration, dependency injection, REST controllers.
3. Validation and error handling: DTOs, Bean Validation, Problem Details.
4. Persistence: Spring Data JPA, Hibernate, PostgreSQL, Flyway, transaction boundaries.
5. Domain modeling: booking/payment/hold/ticket state transitions and invariants.
6. Security: authentication, authorization, session/CSRF or mobile token flow.
7. Concurrency: pessimistic/optimistic locking, idempotency, unique constraints.
8. Integration: payment adapter, webhook processing, outbox, notifications.
9. Testing: unit, slice, integration, concurrency, contract, end-to-end.
10. Production: Actuator, metrics, tracing, Docker, CI/CD, deployment and recovery.
11. Reengineering: data migration, progressive cutover, reconciliation, rollback.

Do not skip the domain and database correctness stages to reach microservices faster.

---

## 18. Official References

Always verify compatibility and current behavior against documentation for the exact versions used by the project.

- [Spring Boot — System Requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/reference/)
- [Spring Framework — Dependency Injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)
- [Spring Framework — Declarative Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
- [Spring Data JPA — Locking](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Modulith Reference](https://docs.spring.io/spring-modulith/reference/)
- [Java 21 BigDecimal API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html)
- [Java 21 java.time API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/package-summary.html)
- [PostgreSQL Explicit Locking](https://www.postgresql.org/docs/current/explicit-locking.html)

---

**Engineering owner:** Backend engineering / project owner  
**Companion document:** `Cinema_Booking_Reengineering_PRD.md`  
**Review cadence:** Revisit this guide when framework versions, security requirements, provider integrations, or architecture constraints change.
