# Backend ACID Compliance & Database Improvements

This document outlines structural and architectural improvements for the TurtleShop backend to enforce ACID (Atomicity, Consistency, Isolation, Durability) principles explicitly at the database and service layers.

Because the project uses raw JDBC (no Hibernate/JPA), responsibility for concurrent data integrity falls directly on SQL queries and Spring `@Transactional` boundaries.

---

# 1. Atomicity (All-or-Nothing Operations)

## Explicit Transaction Boundaries

Ensure all multi-step processes are wrapped in `@Transactional` at the **Service** layer, not the Repository (`*Access`) layer.

If any step fails, the entire business operation must roll back safely.

```java
@Transactional
public PlaceOrderResponse placeOrder(UUID customerId, PlaceOrderRequest request) {
    // Business logic
}
```

---

## Atomic SQL Updates over Read-Calculate-Write

Avoid the following anti-pattern:

1. Read value from database
2. Modify value in Java
3. Write updated value back

This introduces race conditions under concurrent load.

Instead, push calculations directly into SQL so updates become atomic.

### Example — `InventoryAccess`

```java
public int decrementInventoryAtomic(int productId, int quantityToDeduct) {

    String sql = """
        UPDATE INVENTORY
        SET quantity_available = quantity_available - :quantityToDeduct,
            quantity_reserved  = quantity_reserved + :quantityToDeduct
        WHERE product_id = :productId
          AND quantity_available >= :quantityToDeduct
        """;

    return jdbc.update(
        sql,
        new MapSqlParameterSource()
            .addValue("quantityToDeduct", quantityToDeduct)
            .addValue("productId", productId)
    );
}
```

### Why This Matters

This approach:

- Prevents overselling intrinsically
- Avoids race conditions
- Guarantees atomic stock deduction
- Eliminates stale reads during concurrent checkouts

The query returns `0` rows updated if insufficient inventory exists.

---

# 2. Consistency (Guaranteeing Valid Data States)

## Database-Level Check Constraints

Application-layer validation alone is insufficient.

Even if the backend fails, the database itself must enforce valid states.

Create a new Flyway migration such as:

```text
V20__add_acid_constraints.sql
```

### Example Constraints

```sql
-- Prevent negative inventory values
ALTER TABLE INVENTORY
ADD CONSTRAINT chk_inventory_qty_available
CHECK (quantity_available >= 0);

ALTER TABLE INVENTORY
ADD CONSTRAINT chk_inventory_qty_reserved
CHECK (quantity_reserved >= 0);

-- Prevent invalid pricing
ALTER TABLE PRODUCT
ADD CONSTRAINT chk_product_base_price
CHECK (base_price >= 0);

ALTER TABLE TRANSACTION
ADD CONSTRAINT chk_transaction_amount
CHECK (amount >= 0);
```

---

## Strict Relational Integrity

Review all `DELETE` operations carefully.

Use proper relational constraints such as:

```sql
ON DELETE CASCADE
```

where appropriate to prevent orphaned rows.

Do not rely on Java services to manually clean related records across multiple tables.

---

# 3. Isolation (Concurrency Control)

## Pessimistic Locking for High-Contention Records

Critical flows such as:

- Checkout
- Inventory deduction
- Payment processing

must not rely solely on PostgreSQL's default `READ COMMITTED` isolation level when strict consistency is required.

Use row-level locking directly in SQL.

### Example — Lock Inventory During Checkout

```sql
SELECT *
FROM INVENTORY
WHERE product_id = :productId
FOR UPDATE;
```

### Why This Matters

This forces concurrent transactions targeting the same inventory row to execute sequentially, preventing:

- Oversells
- Lost updates
- Non-repeatable reads
- Race conditions during checkout

---

## Escalating Isolation Levels

For sensitive financial operations, increase transaction isolation explicitly.

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public PlaceOrderResponse placeOrder(UUID customerId, PlaceOrderRequest request) {
    // Business logic
}
```

### Recommended Isolation Usage

| Isolation Level | Use Case |
|---|---|
| `READ_COMMITTED` | Standard application flows |
| `REPEATABLE_READ` | Checkout, inventory, financial flows |
| `SERIALIZABLE` | Rare ultra-critical financial operations |

---

# 4. Durability (Persistent State)

## Immutability of Flyway Migrations

Never modify existing Flyway migration files once deployed.

Do not edit:

```text
V{n}__*.sql
```

after they have been executed in shared environments.

Instead, create corrective forward migrations such as:

```text
V21__fix_constraints.sql
```

This guarantees reproducible database states across:

- Local development
- CI/CD pipelines
- Staging
- Production

---

## Precise Exception Handling in Transactions

Spring `@Transactional` rolls back only on unchecked (`RuntimeException`) exceptions by default.

Avoid swallowing critical exceptions:

```java
catch (Exception e) {
    log.error(e.getMessage());
}
```

This can accidentally commit partial transactions.

Instead:

- Re-throw exceptions as `RuntimeException`
- Or configure rollback rules explicitly

### Example

```java
@Transactional(rollbackFor = Exception.class)
public void processCheckout() {
    // Business logic
}
```

---

# Summary

To properly enforce ACID compliance in TurtleShop:

- Use transactional service boundaries
- Push calculations into SQL atomically
- Enforce constraints at the database layer
- Use row-level locking for critical flows
- Escalate isolation levels when required
- Treat Flyway migrations as immutable
- Ensure transaction failures always roll back safely

These improvements are especially important because the project intentionally uses raw JDBC rather than ORM-managed concurrency handling.


---

# 5. Advanced System-Wide Integrity (Enterprise Grade)

The following patterns extend the platform beyond standard ACID compliance into enterprise-grade resiliency, fault tolerance, and cross-system consistency.

## Idempotency Keys (Network-Level Durability & Consistency)

In e-commerce systems, network interruptions may cause users to submit the same payment or checkout request multiple times.

To prevent duplicate transactions, all critical `POST` operations related to:

- Checkout
- Orders
- Payments
- Refunds

should support idempotent request handling.

### Recommendation

Require an `Idempotency-Key` HTTP header for critical write operations.

Store the key in a dedicated PostgreSQL table such as:

```sql
IDEMPOTENCY_LOG
```

alongside:

- Request metadata
- Response payload
- Timestamp
- Request status

If the same key is received again:

- Return the previously stored response
- Do not execute the transaction a second time

### Benefits

- Prevents duplicate orders
- Prevents double charges
- Makes APIs resilient against retries
- Improves distributed system reliability

---

## The Transactional Outbox Pattern (Cross-Database Atomicity)

TurtleShop uses:

- PostgreSQL for relational transactional data
- MongoDB for reviews and document-style data

Spring `@Transactional` cannot span both databases safely.

If an operation eventually needs to write to PostgreSQL and MongoDB within the same business workflow, partial failure becomes possible.

### Recommendation

Implement the **Transactional Outbox Pattern**.

Within the same PostgreSQL transaction:

1. Persist the primary business operation
2. Persist an event entry into an `OUTBOX_EVENT` table

A background worker then:

- Polls the outbox table
- Processes pending events
- Safely updates MongoDB or external systems
- Marks events as processed

### Benefits

- Prevents cross-database inconsistency
- Guarantees eventual consistency
- Avoids distributed transaction complexity
- Improves fault tolerance and recoverability

---

## HikariCP Connection Pool Tuning (Isolation & Durability)

Poorly configured connection pools can cause:

- Thread starvation
- Transaction deadlocks
- Global application slowdowns
- Database exhaustion under load

### Recommendation

Configure HikariCP explicitly in `application.yml`.

### Example

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      connection-timeout: 3000
      max-lifetime: 1800000
      transaction-isolation: TRANSACTION_READ_COMMITTED
```

### Why This Matters

- Fails fast when the database is unavailable
- Prevents stale connections
- Improves transaction stability
- Reduces cascading failures under load

---

## DTO Sanitization (Security & Consistency)

`@Valid` ensures structural validation but does not sanitize malicious input.

Without sanitization, the system may still be vulnerable to:

- XSS (Cross-Site Scripting)
- HTML injection
- JSON injection
- Stored malicious payloads

### Recommendation

Sanitize all incoming string fields before they reach the Service or Repository layers.

Possible approaches:

- Custom Jackson deserializers
- Request sanitization filters
- `owasp-java-encoder`
- Centralized DTO sanitization utilities

### Benefits

- Reduces attack surface
- Prevents persistent XSS
- Protects frontend consumers
- Improves long-term data integrity
