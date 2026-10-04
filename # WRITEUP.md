# WRITEUP.md

## Q1. How do you make sure two users cannot book the same seat?

I use **database-level pessimistic locking** with JPA's `PESSIMISTIC_WRITE` inside a `@Transactional` method.

Before checking a seat, I lock its database row. So if two users try to book `A1` at the same time, one gets the lock first and confirms it. The other waits, then sees that `A1` is already confirmed and gets `409 Conflict`.

I chose a database lock instead of a Java `synchronized`/in-memory lock because it also works when multiple application instances are running.

### How do you avoid deadlocks for multiple seats?

I always sort the requested seats before locking them.

For example:

```text
A3, A1, A2 → A1, A2, A3
```

So competing transactions acquire locks in the same order.

---

## Q2. How does idempotency work?

The `Idempotency-Key` is stored in the `idempotency_keys` table.

There is a unique constraint on:

```text
(user_id, idempotency_key)
```

The first request inserts the key using `INSERT ... ON CONFLICT DO NOTHING`.

If the same request is retried, I find the existing record and return the original reservation instead of creating another one.

For the same key with a different seat request, I compare the stored request hash with the new request hash and return `409 Conflict`.

---

## Q3. Do you support seat holds and expiry?

Not currently.

The model has `AVAILABLE`, `HELD`, and `CONFIRMED`, but the current reservation flow directly confirms seats:

```text
AVAILABLE → CONFIRMED
```

There is no temporary hold or expiry worker yet.

If I added it, I would use an `expires_at` timestamp and make the expiry operation transactional so that an old expiry cannot release a seat that has already been booked by someone else.

---

## Q4. What happens if the database goes down?

For seat booking, I prefer **consistency over availability**.

If I cannot safely communicate with PostgreSQL, I would rather reject the reservation than risk selling the same seat twice.

The database is the source of truth for seat ownership.

---

## Q5. What would you monitor at 2am?

I would mainly care about:

- Increased HTTP 5xx errors
- Database connectivity/readiness failures
- Transaction or deadlock errors
- High reservation latency
- Connection pool exhaustion
- Unexpected application restarts
- Any indication that seat counts are inconsistent

A high number of `seat_taken` responses isn't necessarily an alert during a ticket sale — it can simply mean many people are competing for the same seats.

---

## Q6. How did you use AI?

I used AI mainly as a **development assistant**.

It helped me think through concurrency, idempotency, edge cases, debugging, load testing and documentation.

But I made the final implementation decisions based on the assignment requirements and verified the important behavior against the actual application and database.

For example, I verified that the pessimistic locking was actually reaching PostgreSQL instead of simply assuming it worked.

---

## Q7. What would you improve next?

My next priorities would be:

1. Implement proper temporary holds and expiry.
2. Include `showId` in the idempotency request hash.
3. Add more concurrency/integration tests.
4. Improve the 20K load-test script to capture detailed client/network errors.
5. Replace `ddl-auto=update` with proper Flyway migrations.
6. Add better dashboards, latency metrics and alerts.

The main principle I would keep is simple:

> **Never confirm a seat unless the database has safely established that I own it.**
