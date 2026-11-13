# Hotel Booking Portal

Spring Boot (Java 17) REST API + React (Vite) frontend + MySQL.

## Prerequisites
- JDK 17+ (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Node.js 18+ and npm (`node -v`)
- MySQL 8+ running locally (default port 3306)

## 1. Database
The backend creates the `hotel_booking` database, tables and sample data (6 hotels) automatically on start.
If you prefer to do it by hand, run the scripts in `database/`:
```
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS hotel_booking"
mysql -u root -p hotel_booking < database/schema.sql
mysql -u root -p hotel_booking < database/data.sql
```

## 2. Backend (http://localhost:8080)
Credentials are read from environment variables (defaults: user `root`, empty password).

macOS / Linux:
```
cd backend
export DB_USER=root DB_PASSWORD=your_password
mvn spring-boot:run
```
Windows PowerShell:
```
cd backend
$env:DB_USER="root"; $env:DB_PASSWORD="your_password"
mvn spring-boot:run
```
Optional: `DB_HOST`, `DB_PORT`.

Run the tests: `cd backend && mvn test` (the tests need no database).

## 3. Frontend (http://localhost:5173)
```
cd frontend
npm install
npm run dev
```
Open http://localhost:5173. Vite proxies `/api` to the backend on port 8080.

## Try the main flow
1. Search (e.g. "Goa") with check-in/check-out dates, open a hotel, see the total, enter name + email, confirm.
2. Copy the booking reference, open **Manage booking**, look it up with the same email, cancel it.
3. Surprise feature: book all rooms of "Amber Haveli" (3 rooms) for the same dates, then search again - the hotel shows
   "Sold out" and suggests nearby dates with a free room.

## Surprise feature: alternative dates when sold out
See DESIGN.md.

## Time spent / unfinished
- Time spent: **TODO - fill in your real number of hours**
- Not done: integration tests against a real MySQL, automated frontend tests.

## AI assistance disclosure
**TODO - edit this before submitting:** this project was generated with help from Claude (Anthropic) and then reviewed by me.
