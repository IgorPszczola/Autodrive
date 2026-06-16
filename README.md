# Autodrive 🚗💨

Autodrive is a modern, premium car rental system built with a split frontend/backend architecture. Featuring a glassmorphic dark-theme user interface and a robust Spring Boot backend, it provides a seamless experience for clients booking vehicles and administrators managing fleet operations.

---

## 🌟 Key Features

### 👤 Client-Facing Application
* **Interactive Car Catalog**: Browse premium vehicles with details on daily rates, deposits, segments, and specifications (transmission, fuel type, engine power).
* **Booking Wizard**: A streamlined, 3-step checkout stepper:
  1. Select model and dates (built-in validation enforcing a minimum 1-day rental and preventing past dates).
  2. Pick insurance variants and optional add-ons.
  3. Review cost calculations with a dynamic vehicle preview.
* **Customer Dashboard**: View active and past reservations, cancel bookings (up to 24h prior), and rate completed rentals.

### 👑 Employee & Admin Panel
* **Interactive Analytics**: Real-time stats on total earnings, total reservations, active rentals, and vehicles in service.
* **Earnings Chart**: Interactive monthly earnings Sparkline graph with native mouse-hover tooltips detailing month-by-month revenue.
* **Catalog Manager**: Add new car models, set mileage limits, extra mileage fees, and adjust minimum rental days constraints.
* **Fleet Manager**: Register physical car units (VIN, license plates, mileage, production year) and update availability status (`AVAILABLE`, `RENTED`, `RESERVED`, `IN_REPAIR`).
* **Return Processing Module**: Complete bookings by recording return mileage, calculating damage costs, and lodging vehicle inspection notes.

---

## 🛠️ Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Frontend** | [Nuxt 4](https://nuxt.com/) (Vue 3, Composition API), [Vuetify 3](https://vuetifyjs.com/) UI Library, UnoCSS, Custom HSL gradients & glassmorphism |
| **Backend** | [Spring Boot 3](https://spring.io/projects/spring-boot) (Spring Web, Spring Security), JPA / Hibernate, JWT Token Authentication |
| **Database** | [PostgreSQL 15](https://www.postgresql.org/) |
| **DevOps** | [Docker](https://www.docker.com/), Docker Compose |

---

## 🚀 Getting Started

### Prerequisites
Make sure you have [Docker & Docker Compose](https://www.docker.com/products/docker-desktop/) installed on your machine.

### Quick Start (Production/Preview Mode)

To spin up the entire application stack including the database, backend service, and Nuxt frontend, run the following command in the project root:

```bash
docker-compose up --build
```

Once all containers are initialized, the services will be available at:
* **Frontend Application**: [http://localhost:3000](http://localhost:3000)
* **Backend API**: [http://localhost:8080](http://localhost:8080)
* **PostgreSQL Database**: Port `5432`

---

## 💻 Local Development Workflow (Recommended)

To enjoy **Instant Hot Reloading (HMR)** for frontend tweaks without rebuilding Docker containers constantly, run the database and backend in Docker and run Nuxt locally:

1. **Spin up database and backend containers only:**
   ```bash
   docker compose up -d postgres-db backend-app
   ```
2. **Launch the Nuxt development server:**
   Navigate to the frontend folder and start the dev script:
   ```bash
   cd frontend/nuxt
   npm install
   npm run dev
   ```
3. **Open browser:** Go to [http://localhost:3000](http://localhost:3000) (Any changes saved in code will reflect instantly).
