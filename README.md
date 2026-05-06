<div align="center">

# 🅿️ ParkEase

### *Smart Parking Lot Booking System*

**A production-grade microservices backend built with Spring Boot & Spring Cloud**

---

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023.0.3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

[**Overview**](#-overview) • [**Architecture**](#-architecture) • [**Tech Stack**](#-tech-stack) • [**Getting Started**](#-getting-started) • [**Services**](#-microservices) • [**API Reference**](#-api-reference)

</div>

---

## 📖 Overview

**ParkEase** is a cloud-native, microservices-based parking lot booking backend — think of it as *Zomato for parking spots*. Drivers can discover nearby parking lots in real-time, reserve specific spots, check-in on arrival, and pay online — all powered by a robust, scalable REST API backend.

### 👥 Who Uses ParkEase?

<table>
<tr>
<td align="center" width="33%">

### 🚗 **DRIVER**
*(Vehicle Owner)*

Search nearby parking <br>
Book a spot in seconds <br>
Check-in & pay online

</td>
<td align="center" width="33%">

### 🏢 **MANAGER**
*(Lot Owner)*

Register parking lots <br>
Add spots in bulk <br>
Manage availability

</td>
<td align="center" width="33%">

### 👑 **ADMIN**
*(Platform Operator)*

Approve/reject lots <br>
Monitor analytics <br>
System-wide control

</td>
</tr>
</table>

### 🔄 End-to-End Booking Flow

```mermaid
flowchart LR
    A[🔐 Login<br/>JWT Token] --> B[🔍 Search<br/>Nearby Lots]
    B --> C[🎯 Select<br/>a Spot]
    C --> D[📝 Book<br/>Reserve Spot]
    D --> E[✅ Check-In<br/>On Arrival]
    E --> F[🚪 Check-Out<br/>Fare Calculated]
    F --> G[💳 Pay<br/>+ Receipt]

    style A fill:#667eea,stroke:#4c51bf,color:#fff
    style B fill:#667eea,stroke:#4c51bf,color:#fff
    style C fill:#667eea,stroke:#4c51bf,color:#fff
    style D fill:#764ba2,stroke:#553c9a,color:#fff
    style E fill:#764ba2,stroke:#553c9a,color:#fff
    style F fill:#f093fb,stroke:#c026d3,color:#fff
    style G fill:#f093fb,stroke:#c026d3,color:#fff
```

---

## 🏛 Architecture

ParkEase follows a **distributed microservices architecture** with service discovery, API gateway routing, and database-per-service isolation.

### 🗺 System Architecture Diagram

```mermaid
graph TB
    subgraph Client["🌐 Client Layer"]
        U[👤 User<br/>Web / Mobile]
    end

    subgraph Edge["🚪 Edge Layer"]
        GW[API Gateway<br/>:8080]
    end

    subgraph Discovery["🧭 Service Discovery"]
        EUR[Eureka Server<br/>:8761]
    end

    subgraph Services["⚙️ Business Microservices"]
        AUTH[Auth Service<br/>:8081]
        VEH[Vehicle Service<br/>:8082]
        LOT[ParkingLot Service<br/>:8083]
        SPOT[Spot Service<br/>:8084]
        BOOK[Booking Service<br/>:8085]
        PAY[Payment Service<br/>:8086]
        NOTIF[Notification Service<br/>:8087]
        ANA[Analytics Service<br/>:8088]
    end

    subgraph Data["🗄️ Data Layer — DB per Service"]
        DB1[(parkease_auth)]
        DB2[(parkease_vehicle)]
        DB3[(parkease_parkinglot)]
        DB4[(parkease_spot)]
        DB5[(parkease_booking)]
        DB6[(parkease_payment)]
        DB7[(parkease_notification)]
        DB8[(parkease_analytics)]
    end

    U --> GW
    GW -.registers.-> EUR
    GW --> AUTH
    GW --> VEH
    GW --> LOT
    GW --> SPOT
    GW --> BOOK
    GW --> PAY
    GW --> NOTIF
    GW --> ANA

    AUTH -.registers.-> EUR
    VEH -.registers.-> EUR
    LOT -.registers.-> EUR
    SPOT -.registers.-> EUR
    BOOK -.registers.-> EUR

    AUTH --> DB1
    VEH --> DB2
    LOT --> DB3
    SPOT --> DB4
    BOOK --> DB5
    PAY --> DB6
    NOTIF --> DB7
    ANA --> DB8

    classDef client fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#0c4a6e
    classDef edge fill:#fef3c7,stroke:#d97706,stroke-width:2px,color:#78350f
    classDef discovery fill:#fce7f3,stroke:#db2777,stroke-width:2px,color:#831843
    classDef service fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    classDef data fill:#ede9fe,stroke:#7c3aed,stroke-width:2px,color:#4c1d95

    class U client
    class GW edge
    class EUR discovery
    class AUTH,VEH,LOT,SPOT,BOOK,PAY,NOTIF,ANA service
    class DB1,DB2,DB3,DB4,DB5,DB6,DB7,DB8 data
```

### 🧩 Core Architectural Principles

| # | Principle | Why It Matters |
|:-:|:--|:--|
| **1** | **Single Entry Point** | Clients only talk to the API Gateway (`:8080`). Internal services remain insulated from the public internet. |
| **2** | **Service Discovery via Eureka** | Services register themselves. When one service needs another, it queries Eureka — no hardcoded IPs. |
| **3** | **Database per Service** | Every service owns its own MySQL database. Services communicate **only** via REST APIs — never via shared tables. |
| **4** | **Stateless Authentication** | JWT tokens carry the user identity. Any service can validate them without hitting Auth Service. |
| **5** | **Independent Deployability** | Each service can be developed, tested, deployed, and scaled independently. |

### 🔁 How Service Discovery Works

```mermaid
sequenceDiagram
    autonumber
    participant U as 👤 User
    participant GW as 🚪 API Gateway
    participant EUR as 🧭 Eureka
    participant AUTH as 🔐 Auth Service

    AUTH->>EUR: 📍 Register: "I'm at 10.0.0.5:8081"
    Note over EUR: Service registry updated

    U->>GW: POST /auth/login
    GW->>EUR: Where is auth-service?
    EUR-->>GW: 10.0.0.5:8081
    GW->>AUTH: Forward request
    AUTH-->>GW: JWT token
    GW-->>U: JWT token ✅
```

---

## 🛠 Tech Stack

<table>
<tr>
<td width="50%" valign="top">

### ⚙️ **Backend**
- **Java 17** — LTS runtime
- **Spring Boot 3.2.0** — Application framework
- **Spring Cloud 2023.0.3** — Distributed systems toolkit
- **Spring Data JPA** — ORM & persistence
- **Spring Security** — Auth & authorization
- **Netflix Eureka** — Service discovery
- **Spring Cloud Gateway** — API gateway
- **JJWT 0.12.3** — JSON Web Tokens
- **Lombok** — Boilerplate reduction
- **BCrypt** — Password hashing

</td>
<td width="50%" valign="top">

### 🗄 **Data & Infrastructure**
- **MySQL 8.0** — Relational database
- **Hibernate** — JPA implementation
- **Maven** — Build & dependency management

### 🧪 **Tooling**
- **IntelliJ IDEA** — IDE
- **Postman** — API testing
- **Git + GitHub** — Version control
- **MySQL Workbench** — DB GUI

</td>
</tr>
</table>

---

## 🚀 Getting Started

### 📋 Prerequisites

Install the following in **this exact order**:

| # | Tool | Version | Purpose | Download |
|:-:|:--|:--|:--|:--|
| 1 | **Java JDK** | 17 (Temurin LTS) | Runtime | [adoptium.net](https://adoptium.net/) |
| 2 | **IntelliJ IDEA** | Community Edition | IDE | [jetbrains.com](https://www.jetbrains.com/idea/download/) |
| 3 | **MySQL + Workbench** | 8.0+ | Database | [mysql.com](https://dev.mysql.com/downloads/installer/) |
| 4 | **Postman** | Latest | API testing | [postman.com](https://www.postman.com/downloads/) |
| 5 | **Git** | Latest | Version control | [git-scm.com](https://git-scm.com/downloads) |

**Verify installations:**
```bash
java -version      # → openjdk 17.x.x
git --version      # → git 2.x.x
```

### 📥 Clone the Repository

```bash
git config --global user.name "Your Name"
git config --global user.email "you@email.com"

git clone https://github.com/<your-username>/parkease.git
cd parkease
```

### 🗄 Create MySQL Databases

Open **MySQL Workbench** and execute:

```sql
CREATE DATABASE parkease_auth;
CREATE DATABASE parkease_vehicle;
CREATE DATABASE parkease_parkinglot;
CREATE DATABASE parkease_spot;
CREATE DATABASE parkease_booking;
CREATE DATABASE parkease_payment;
CREATE DATABASE parkease_notification;
CREATE DATABASE parkease_analytics;

SHOW DATABASES;  -- verify all 8 exist
```

> 💡 **Note:** Tables are auto-created by Hibernate when each service first starts (`ddl-auto: update`).

### ▶️ Startup Order

Services **must** be launched in this order to satisfy dependencies:

```mermaid
flowchart LR
    E[1. Eureka Server<br/>:8761] --> A[2. Auth Service<br/>:8081]
    A --> V[3. Vehicle Service<br/>:8082]
    V --> L[4. ParkingLot Service<br/>:8083]
    L --> S[5. Spot Service<br/>:8084]
    S --> B[6. Booking Service<br/>:8085]
    B --> P[7. Payment Service<br/>:8086]
    P --> N[8. Notification Service<br/>:8087]
    N --> AN[9. Analytics Service<br/>:8088]
    AN --> GW[10. API Gateway<br/>:8080]

    style E fill:#fce7f3,stroke:#db2777,stroke-width:3px,color:#831843
    style A fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    style V fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    style L fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    style S fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    style B fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    style P fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    style N fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    style AN fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    style GW fill:#fef3c7,stroke:#d97706,stroke-width:2px,color:#78350f
```

For each service: open the folder in IntelliJ → run the `*Application.java` main class.

### ✅ Verification

1. Open **http://localhost:8761** — Eureka dashboard should load
2. After starting each service, it should appear under **"Instances currently registered with Eureka"**

---

## 📦 Project Structure

```
parkease/
├── 📂 eureka-server/            # Service discovery registry       ✅ Complete
├── 📂 auth-service/             # Login, register, JWT             ✅ Complete
├── 📂 vehicle-service/          # User vehicle management          ✅ Complete
├── 📂 parkinglot-service/       # Parking lot profiles + geo-search ✅ Complete
├── 📂 spot-service/             # Individual spot management       ✅ Complete
├── 📂 booking-service/          # Booking orchestrator             ✅ Complete
├── 📂 payment-service/          # Payments & receipts              ✅ Complete
├── 📂 notification-service/     # Email / SMS                      ✅ Complete
├── 📂 analytics-service/        # Reports & dashboards             ✅ Complete
├── 📂 api-gateway/              # Single entry point               ✅ Complete
├── 📄 .gitignore
└── 📄 README.md
```

### 🧱 Standard Service Anatomy

Every business service follows a **5-layered architecture**:

```mermaid
graph TD
    CTRL["🎯 REST Controller<br/><code>*Resource.java</code><br/>API endpoints"]
    SVC_I["📜 Service Interface<br/><code>*Service.java</code><br/>Method contracts"]
    SVC_IMPL["⚙️ Service Implementation<br/><code>*ServiceImpl.java</code><br/>Business logic"]
    REPO["🗃️ Repository<br/><code>*Repository.java</code><br/>DB queries"]
    ENT["📦 Entity<br/><code>*.java</code><br/>Table structure"]

    CTRL --> SVC_I
    SVC_I --> SVC_IMPL
    SVC_IMPL --> REPO
    REPO --> ENT

    style CTRL fill:#fef3c7,stroke:#d97706,color:#78350f
    style SVC_I fill:#dbeafe,stroke:#2563eb,color:#1e3a8a
    style SVC_IMPL fill:#dcfce7,stroke:#16a34a,color:#14532d
    style REPO fill:#ede9fe,stroke:#7c3aed,color:#4c1d95
    style ENT fill:#fce7f3,stroke:#db2777,color:#831843
```

**Request Flow:** `User → Controller → ServiceImpl → Repository → Database`
**Response Flow:** `Database → Repository → ServiceImpl → Controller → User`

Typical per-service folder layout:

```
<service-name>/
└── src/main/
    ├── java/com/parkease/<service>/
    │   ├── <Service>Application.java     # @SpringBootApplication
    │   ├── entity/                       # JPA entities
    │   ├── repository/                   # JpaRepository interfaces
    │   ├── service/                      # Business logic
    │   ├── resource/ (or controller/)    # @RestController
    │   ├── dto/                          # Request/response DTOs
    │   ├── config/                       # SecurityConfig, JwtUtil, etc.
    │   └── exception/                    # Custom exceptions + handlers
    └── resources/
        └── application.properties        # or application.yml
```

---

## 🧬 Microservices

### 🗂 Service Registry

| # | Service | Port | Status | Database | Responsibility |
|:-:|:--|:-:|:-:|:--|:--|
| 1 | **eureka-server** | `8761` | ✅ Complete | — | Service discovery |
| 2 | **auth-service** | `8081` | ✅ Complete | `parkease_auth` | Authentication + JWT |
| 3 | **vehicle-service** | `8082` | ✅ Complete | `parkease_vehicle` | Vehicle management |
| 4 | **parkinglot-service** | `8083` | ✅ Complete | `parkease_parkinglot` | Lot profiles + geo-search |
| 5 | **spot-service** | `8084` | ✅ Complete | `parkease_spot` | Individual spot CRUD |
| 6 | **booking-service** | `8085` | ✅ Complete | `parkease_booking` | Booking orchestration |
| 7 | **payment-service** | `8086` | ✅ Complete | `parkease_payment` | Transactions |
| 8 | **notification-service** | `8087` | ✅ Complete | `parkease_notification` | Email / SMS |
| 9 | **analytics-service** | `8088` | ✅ Complete | `parkease_analytics` | Reports & insights |
| 10 | **api-gateway** | `8080` | ✅ Complete | — | Request routing |

---

### 1️⃣ Eureka Server — `:8761`

**Role:** The **directory of services**. Every running microservice registers itself with Eureka on startup. When one service needs another, it asks Eureka *"where is X?"* — no hardcoded IPs, no config sprawl.

> 💡 **Analogy:** Think of Eureka as the **reception desk** of an office building. Visitors don't wander — they ask reception, and reception knows where everyone sits.

**Key files:**
- `EurekaServerApplication.java` — with `@EnableEurekaServer`
- `application.yml` — port `8761`, self-registration disabled
- `pom.xml` — `spring-cloud-starter-netflix-eureka-server`

**Verify:** Open [http://localhost:8761](http://localhost:8761) — the Eureka dashboard loads.

---

### 2️⃣ Auth Service — `:8081`

**Role:** Handles **user registration, login, and JWT token lifecycle**. Every other service trusts JWTs issued here to authenticate users.

> 💡 **Analogy:** JWT is like an **office ID card** — flash it anywhere inside the building and you're recognized. No need to re-authenticate at every door.

#### 📋 Data Model — `User`

| Field | Type | Notes |
|:--|:--|:--|
| `userId` | `Long` | Primary key, auto-generated |
| `fullName` | `String` | — |
| `email` | `String` | **Unique** |
| `passwordHash` | `String` | BCrypt-hashed — never plain text |
| `phone` | `String` | — |
| `role` | `Enum` | `DRIVER` / `MANAGER` / `ADMIN` |
| `vehiclePlate` | `String` | Optional |
| `isActive` | `Boolean` | Soft-delete flag |
| `createdAt` | `Timestamp` | Auto-set |

#### 🔌 REST Endpoints

| Method | Endpoint | Auth | Purpose |
|:--|:--|:-:|:--|
| `POST` | `/auth/register` | 🌐 Public | Create a new user |
| `POST` | `/auth/login` | 🌐 Public | Authenticate, return JWT |
| `POST` | `/auth/logout` | 🔒 JWT | Invalidate session |
| `GET` | `/auth/profile` | 🔒 JWT | Fetch own profile |
| `PUT` | `/auth/profile` | 🔒 JWT | Update profile |
| `PUT` | `/auth/password` | 🔒 JWT | Change password |
| `DELETE` | `/auth/deactivate` | 🔒 JWT | Soft-delete account |

#### 🧪 Sample: Register

```http
POST http://localhost:8081/auth/register
Content-Type: application/json

{
  "fullName": "Rahul Sharma",
  "email": "rahul@gmail.com",
  "password": "test123",
  "phone": "9876543210",
  "role": "DRIVER"
}
```

#### 🧪 Sample: Login

```http
POST http://localhost:8081/auth/login
Content-Type: application/json

{
  "email": "rahul@gmail.com",
  "password": "test123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "rahul@gmail.com",
  "role": "DRIVER"
}
```

---

### 3️⃣ Vehicle Service — `:8082`

**Role:** Manages user-owned vehicles. A single user can have multiple registered vehicles (car, bike, EV, truck). Booking service references these vehicles.

#### 📋 Data Model — `Vehicle`

| Field | Type | Notes |
|:--|:--|:--|
| `vehicleId` | `Long` | PK |
| `ownerId` | `Long` | References `User.userId` |
| `licensePlate` | `String` | **Unique** |
| `make` | `String` | Honda, Maruti, etc. |
| `model` | `String` | Activa, Swift, etc. |
| `color` | `String` | — |
| `vehicleType` | `Enum` | `TWO_WHEELER` / `FOUR_WHEELER` / `HEAVY` |
| `isEV` | `Boolean` | Electric vehicle flag |
| `isActive` | `Boolean` | — |
| `registeredAt` | `Timestamp` | — |

#### 🔌 REST Endpoints

| Method | Endpoint | Purpose |
|:--|:--|:--|
| `POST` | `/vehicles` | Register a new vehicle |
| `GET` | `/vehicles/{id}` | Fetch by ID |
| `GET` | `/vehicles/owner/{ownerId}` | All vehicles of a user |
| `PUT` | `/vehicles/{id}` | Update vehicle details |
| `DELETE` | `/vehicles/{id}` | Remove vehicle |

---

### 4️⃣ ParkingLot Service — `:8083`

**Role:** Maintains the catalog of parking lots — name, address, GPS coordinates, total/available spots, operating hours. Powers the **"nearby parking"** search via the **Haversine formula**. Admins approve lots here.

#### 📋 Data Model — `ParkingLot`

| Field | Type | Notes |
|:--|:--|:--|
| `lotId` | `Long` | PK |
| `name` | `String` | Display name |
| `address`, `city` | `String` | Postal info |
| `latitude`, `longitude` | `Double` | GPS coordinates |
| `totalSpots` | `Integer` | Capacity |
| `availableSpots` | `Integer` | Live counter |
| `managerId` | `Long` | Owner (references User) |
| `isOpen` | `Boolean` | Real-time open/closed |
| `openTime`, `closeTime` | `LocalTime` | Operating hours |
| `isApproved` | `Boolean` | Defaults to `false` until admin approves |
| `imageUrl` | `String` | Lot photo |

#### 🌍 The Haversine Formula

Computes the **great-circle distance** between two GPS points on Earth — accounting for the planet's curvature. Used in `findNearby(lat, lng, radius)`:

```sql
SELECT * FROM parking_lots p
WHERE p.is_approved = true
  AND (6371 * acos(
        cos(radians(:lat)) * cos(radians(p.latitude)) *
        cos(radians(p.longitude) - radians(:lng)) +
        sin(radians(:lat)) * sin(radians(p.latitude))
      )) <= :radius
```

> `6371` is Earth's radius in **km**, so the `radius` parameter is expressed in kilometers.

#### 🔌 REST Endpoints

| Method | Endpoint | Purpose |
|:--|:--|:--|
| `POST` | `/lots` | Manager creates a lot (defaults `isApproved=false`) |
| `GET` | `/lots` | List all **approved** lots |
| `GET` | `/lots/{id}` | Fetch by ID |
| `GET` | `/lots/nearby?lat=X&lng=Y&radius=5` | Geo-search within radius (km) |
| `GET` | `/lots/city/{city}` | Filter by city |
| `PUT` | `/lots/{id}` | Update lot details |
| `PUT` | `/lots/{id}/approve` | **Admin-only** approval |
| `PUT` | `/lots/{id}/toggle` | Flip open/closed state |
| `DELETE` | `/lots/{id}` | Remove lot |

#### 🧪 Sample: Find Nearby Lots

```http
GET http://localhost:8083/lots/nearby?lat=27.4924&lng=77.6737&radius=5
```

Returns all approved lots within a **5 km radius** of the given coordinates.

---

## 📚 API Reference

### 🔐 Authentication Header

All protected endpoints require the JWT token obtained from `/auth/login`:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 🧪 Testing with Postman

1. **Register** a user → `POST /auth/register`
2. **Login** → `POST /auth/login` → copy the `token` field
3. In every subsequent request, add header: `Authorization: Bearer <token>`
4. **Register a vehicle** → `POST /vehicles`
5. **Create a parking lot** → `POST /lots`
6. **Search nearby** → `GET /lots/nearby?lat=...&lng=...&radius=5`

### 📬 Response Envelope

Errors follow a consistent format:

```json
{
  "timestamp": "2026-04-18T10:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "User with id 42 not found"
}
```

---

## 🗺 Roadmap

- [x] **Phase 1** — Core infra (Eureka, Auth, Vehicle, ParkingLot)
- [x] **Phase 2** — Spot & Booking services (the heart of the platform)
- [x] **Phase 3** — Payment & Notification integrations
- [x] **Phase 4** — Analytics dashboards & reports
- [x] **Phase 5** — API Gateway + centralized security
- [ ] **Phase 6** — Dockerization + Kubernetes deployment
- [ ] **Phase 7** — Distributed tracing (Zipkin) + centralized logging (ELK)
- [ ] **Phase 8** — Frontend (ParkEase Web) *(out of scope — backend only)*

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. **Fork** the repository
2. Create your feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'feat: add amazing feature'`
4. Push: `git push origin feature/amazing-feature`
5. Open a **Pull Request**

---

## 📄 License

Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for details.

---

<div align="center">

### ⭐ If this project helped you learn microservices, give it a star!

**Built with ☕ and a lot of `@Autowired` by the ParkEase team**

[⬆ Back to top](#-parkease)

</div>