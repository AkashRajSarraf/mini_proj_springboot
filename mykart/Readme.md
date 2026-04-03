# 🛒 MyKart — Back-Office Inventory & Trading Management System

A Spring Boot REST API for managing product inventory and executing buy/sell trades, secured with **JWT (JSON Web Token) authentication**.

---

## 📋 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Project Structure](#-project-structure)
- [JWT Authentication — Kaise Kaam Karta Hai?](#-jwt-authentication--kaise-kaam-karta-hai)
- [Security Architecture](#-security-architecture)
- [API Reference](#-api-reference)
- [How to Run](#-how-to-run)
- [Testing with cURL](#-testing-with-curl)
- [Testing with Postman](#-testing-with-postman)
- [Tech Stack](#-tech-stack)

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (Postman/cURL)                    │
│                                                                 │
│   1. POST /auth/register  →  Register & get JWT token           │
│   2. POST /auth/login     →  Login & get JWT token              │
│   3. GET /inventory       →  (with JWT) View inventory          │
│   4. POST /trade/buy      →  (with JWT) Buy products            │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│                     SPRING BOOT APPLICATION                      │
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐   │
│  │ JwtAuthFilter │───→│ SecurityConfig│───→│   Controllers    │   │
│  │ (Token Check) │    │ (Rules)      │    │ (Business Logic) │   │
│  └──────────────┘    └──────────────┘    └──────────────────┘   │
│         │                                        │               │
│         ▼                                        ▼               │
│  ┌──────────────┐                        ┌──────────────────┐   │
│  │  JwtService   │                        │    Services       │   │
│  │ (Token Utils) │                        │ (Trade/Inventory) │   │
│  └──────────────┘                        └──────────────────┘   │
│                                                  │               │
│                                                  ▼               │
│                                          ┌──────────────────┐   │
│                                          │   H2 Database     │   │
│                                          │ (In-Memory)       │   │
│                                          └──────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
src/main/java/com/mykart/mykart/
│
├── MykartApplication.java          # Main entry point
│
├── controller/
│   ├── AuthController.java         # Register & Login endpoints
│   ├── InventoryController.java    # Inventory CRUD endpoints
│   └── TradeController.java        # Buy/Sell trade endpoints
│
├── model/
│   ├── User.java                   # User entity (implements UserDetails)
│   ├── Product.java                # Product entity
│   ├── Inventory.java              # Inventory entity
│   └── Trade.java                  # Trade entity
│
├── dto/
│   ├── AuthRequest.java            # Login/Register request body
│   └── AuthResponse.java           # Auth response with JWT token
│
├── repository/
│   ├── UserRepository.java         # User DB operations
│   ├── ProductRepository.java      # Product DB operations
│   ├── InventoryRepository.java    # Inventory DB operations
│   └── TradeRepository.java        # Trade DB operations
│
├── security/
│   ├── JwtService.java             # JWT token generate/validate
│   ├── JwtAuthFilter.java          # Request filter for JWT
│   └── SecurityConfig.java         # Spring Security configuration
│
├── service/
│   ├── InventoryService.java       # Inventory business logic
│   └── TradeService.java           # Trade business logic
│
└── exception/
    ├── GlobalExceptionHandler.java # Centralized error handling
    ├── ErrorResponse.java          # Error response structure
    ├── ResourceNotFoundException.java
    └── InsufficientStockException.java
```

---

## 🔐 JWT Authentication — Kaise Kaam Karta Hai?

### JWT kya hai? (Simple explanation)

JWT (JSON Web Token) ek **encoded string** hai jo 3 parts se bani hoti hai:

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJha2FzaCJ9.abc123signature
│                     │                       │
└── HEADER            └── PAYLOAD             └── SIGNATURE
    (Algorithm info)      (User data)             (Tamper-proof seal)
```

| Part | Kya hai? | Example |
|------|----------|---------|
| **Header** | Algorithm + Token type | `{"alg": "HS256", "typ": "JWT"}` |
| **Payload** | User data (claims) | `{"sub": "akash", "iat": 1700000, "exp": 1700086400}` |
| **Signature** | Header + Payload ko secret key se sign kiya | Tamper-proof — agar payload change ho toh signature invalid |

### Authentication Flow (Step-by-step)

```
 Step 1: REGISTER
 ═══════════════════════════════════════════════════════
  Client                              Server
    │                                    │
    │──POST /auth/register──────────────→│
    │  {"username":"akash",              │
    │   "password":"pass123"}            │
    │                                    │
    │                        ┌───────────┤
    │                        │ 1. Check: username exists? No ✓
    │                        │ 2. Hash password (BCrypt)
    │                        │ 3. Save user to DB
    │                        │ 4. Generate JWT token
    │                        └───────────┤
    │                                    │
    │←── 201 Created ───────────────────│
    │  {"token":"eyJhbGci...",           │
    │   "username":"akash",              │
    │   "message":"Registration          │
    │    successful!"}                   │

 Step 2: LOGIN (if already registered)
 ═══════════════════════════════════════════════════════
  Client                              Server
    │                                    │
    │──POST /auth/login─────────────────→│
    │  {"username":"akash",              │
    │   "password":"pass123"}            │
    │                                    │
    │                        ┌───────────┤
    │                        │ 1. Verify credentials
    │                        │ 2. Password BCrypt match? ✓
    │                        │ 3. Generate JWT token
    │                        └───────────┤
    │                                    │
    │←── 200 OK ────────────────────────│
    │  {"token":"eyJhbGci...",           │
    │   "username":"akash"}              │

 Step 3: ACCESS PROTECTED API
 ═══════════════════════════════════════════════════════
  Client                              Server
    │                                    │
    │──GET /inventory───────────────────→│
    │  Header: Authorization:            │
    │  Bearer eyJhbGci...                │
    │                                    │
    │                        ┌───────────┤
    │                        │ JwtAuthFilter:
    │                        │ 1. Extract token from header
    │                        │ 2. Verify signature ✓
    │                        │ 3. Check expiry ✓
    │                        │ 4. Load user from DB ✓
    │                        │ 5. Set SecurityContext
    │                        └───────────┤
    │                                    │
    │←── 200 OK ────────────────────────│
    │  [inventory data...]               │

 Step 4: INVALID/MISSING TOKEN
 ═══════════════════════════════════════════════════════
  Client                              Server
    │                                    │
    │──GET /inventory───────────────────→│
    │  (No Authorization header)         │
    │                                    │
    │←── 403 Forbidden ────────────────│
```

### Key Security Concepts

| Concept | Kya hai? | MyKart mein kahan? |
|---------|----------|---------------------|
| **BCrypt** | Password hashing algorithm. "pass123" → "$2a$10$xyz..." (irreversible) | `SecurityConfig.passwordEncoder()` |
| **HMAC-SHA256** | Token signing algorithm. Secret key + data = unique signature | `JwtService.getSigningKey()` |
| **Stateless Auth** | Server mein koi session nahi, har request apna token laata hai | `SecurityConfig` → `STATELESS` |
| **CSRF Disabled** | REST APIs mein cookies nahi use karte, toh CSRF protection zaroori nahi | `SecurityConfig` → `csrf.disable()` |
| **OncePerRequestFilter** | Filter ek request mein sirf ek baar chalega | `JwtAuthFilter` |

---

## 🛡️ Security Architecture

### Which endpoints are public vs protected?

| Endpoint | Method | Auth Required? | Description |
|----------|--------|---------------|-------------|
| `/auth/register` | POST | ❌ No | Naya user register karo |
| `/auth/login` | POST | ❌ No | Existing user login karo |
| `/h2-console/**` | GET | ❌ No | H2 DB console (dev only) |
| `/inventory` | GET | ✅ Yes (JWT) | Saari inventory dekho |
| `/inventory/{id}` | GET | ✅ Yes (JWT) | Specific product ki inventory |
| `/inventory/update-price/{id}` | PUT | ✅ Yes (JWT) | Sale price update karo |
| `/trade/history` | GET | ✅ Yes (JWT) | Saari trade history |
| `/trade/history/{id}` | GET | ✅ Yes (JWT) | Product ki trade history |
| `/trade/buy` | POST | ✅ Yes (JWT) | Product kharido |
| `/trade/sell` | POST | ✅ Yes (JWT) | Product becho |

---

## 📖 API Reference

### 🔓 Auth Endpoints (Public)

#### Register a new user
```http
POST /auth/register
Content-Type: application/json

{
  "username": "akash",
  "password": "pass123"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "akash",
  "message": "Registration successful!"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "akash",
  "password": "pass123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "akash",
  "message": "Login successful!"
}
```

### 🔒 Inventory Endpoints (JWT Required)

#### Get all inventory
```http
GET /inventory
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Get inventory by product ID
```http
GET /inventory/{productId}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Update sale price
```http
PUT /inventory/update-price/{productId}?price=150.00
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 🔒 Trade Endpoints (JWT Required)

#### Get all trade history
```http
GET /trade/history
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Get trade history by product ID
```http
GET /trade/history/{productId}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Buy a product
```http
POST /trade/buy?productId=1&quantity=10&price=100.00
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Sell a product
```http
POST /trade/sell?productId=1&quantity=5&price=150.00
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- Maven 3.6+

### Steps

```bash
# 1. Clone the repository
git clone <repository-url>
cd mykart

# 2. Build the project
mvn clean compile

# 3. Run the application
mvn spring-boot:run
```

The app will start at `http://localhost:8080`.

Access H2 Console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:shopdb`
- Username: `sa`
- Password: (leave empty)

---

## 🧪 Testing with cURL

### Step 1: Register a user
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"akash","password":"pass123"}'
```

### Step 2: Login (get token)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"akash","password":"pass123"}'
```

### Step 3: Copy the token from response and use it

```bash
# Replace <YOUR_TOKEN> with actual token from login response
curl -X GET http://localhost:8080/inventory \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### Step 4: Try without token (should get 403)
```bash
curl -X GET http://localhost:8080/inventory
# Response: 403 Forbidden
```

---

## 📮 Testing with Postman

1. **Register**: POST `http://localhost:8080/auth/register`
   - Body (raw JSON): `{"username":"akash","password":"pass123"}`
   - Copy the `token` from response

2. **Login**: POST `http://localhost:8080/auth/login`
   - Body (raw JSON): `{"username":"akash","password":"pass123"}`
   - Copy the `token` from response

3. **Access Protected API**: GET `http://localhost:8080/inventory`
   - Go to **Authorization** tab → Type: **Bearer Token**
   - Paste the token
   - Send request → You should get inventory data

4. **Without Token**: GET `http://localhost:8080/inventory`
   - Don't add Authorization header
   - Send → You should get **403 Forbidden**

---

## 🛠️ Tech Stack

| Technology | Purpose |
|-----------|---------|
| **Java 17** | Programming language |
| **Spring Boot 3.4.3** | Application framework |
| **Spring Security** | Authentication & Authorization |
| **JJWT 0.12.6** | JWT token generation & validation |
| **Spring Data JPA** | Database ORM |
| **H2 Database** | In-memory database (dev/test) |
| **Lombok** | Boilerplate code reduction |
| **Maven** | Build tool & dependency management |
| **BCrypt** | Password hashing |

---

## 📝 License

This project is for educational & development purposes.
