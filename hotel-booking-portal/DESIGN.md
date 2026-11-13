# Design

## Architecture
React SPA -> REST/JSON (`/api/...`) -> Spring Boot -> MySQL.
Backend layers: `controller` (HTTP only) -> `service` (rules, transactions) -> `repository` (Spring Data JPA).
`StayRules` holds the pure rules (nights, total, date validation, availability) so they are unit-testable without a DB.
The browser never sends a price; all money is calculated server-side.

## Database
- `hotels(id, name, location, description, amenities, price_per_night, total_rooms)` - one room category per hotel,
  so `total_rooms` is the capacity. CHECK constraints keep price and rooms > 0. Index on `location`.
- `bookings(id, reference UNIQUE, hotel_id FK, guest_name, email, check_in, check_out, total_amount, status, created_at)`
  with `CHECK (check_out > check_in)` and an index on `(hotel_id, status, check_in, check_out)` for availability queries.
- One hotel has many bookings. Availability is **derived** from bookings rather than stored per day.

## API
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/hotels?location&checkIn&checkOut` | List/search; with dates adds availability and total |
| GET | `/api/hotels/{id}?checkIn&checkOut` | Detail, rooms left, total, alternatives if sold out |
| POST | `/api/bookings` | Create booking (201) |
| GET | `/api/bookings/{ref}?email=` | Retrieve booking (needs reference + email) |
| POST | `/api/bookings/{ref}/cancel?email=` | Cancel booking |

Errors always use `{status, error, message, details, timestamp}`: 400 validation, 404 not found, 409 conflict
(sold out / already cancelled), 500 generic.

## Booking logic
- Dates: check-in not in the past, check-out strictly after check-in (checked in the service and again by a DB CHECK).
- Price: `nights x price_per_night`, `nights = check_out - check_in`.
- **Overlap assumption:** a stay occupies nights `check_in .. check_out-1`. Check-out day is free, so back-to-back
  stays do not clash. Two stays overlap when `a.in < b.out AND a.out > b.in`.
- Availability: for every night of the requested stay count CONFIRMED bookings covering it; rooms left =
  `total_rooms - busiest night`. (Summing all overlapping bookings would wrongly reject some valid stays.)
- Cancellation sets status to CANCELLED. Only CONFIRMED bookings count, so the room is released automatically.
  Cancelling after the stay has started or twice is rejected.
- Booking lookup needs both reference and email, so a guessed reference alone reveals nothing.

## Simultaneous bookings
`BookingService.create` is `@Transactional` and first runs `SELECT ... FOR UPDATE` on the hotel row. A second request for
the same hotel waits, then recalculates availability and gets a 409 if the last room is gone. Different hotels never block each other.
Alternatives: optimistic locking with a version column, or a per-night `inventory` table with a conditional `UPDATE ... WHERE rooms_left > 0`.

## Surprise feature: alternative dates
- **What:** if the chosen dates are sold out, the hotel page suggests up to 3 stays of the same length (within +/-14 days)
  that still have a room, with the total price; one click applies them.
- **Why:** a "sold out" dead end makes users leave; most people have some flexibility.
- **How:** `HotelService.alternatives` fetches nearby bookings once, then reuses `StayRules.availableRooms` for each
  candidate start date, closest first. Frontend renders them as buttons that update the date pickers.

## Decisions and trade-offs
- Derived availability (no per-day table): simple and always consistent with bookings; slower for huge data.
- Pessimistic lock: simplest correct option for a small app; lock is per hotel, short-lived.
- Plain SQL scripts instead of Flyway: fewer moving parts; Flyway would be the next step.
- Amenities as a comma-separated column: fine for this scope; a separate table would be better for filtering.

## Limitations / improvements
No authentication, payments, emails or admin panel (out of scope). Hotel list queries availability per hotel (N+1);
a single grouped query would scale better. Add integration tests with Testcontainers, pagination, multiple room types,
and time-zone-aware "today" per hotel.
