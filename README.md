# NeuroMart — Spring Boot Backend (Java 17 & MySQL)

This is the production-ready Java Spring Boot backend for **NeuroMart**, migrated from the original Node.js/Express/MongoDB stack to **Java 17**, **Spring Boot 3.3.5**, **Spring Data JPA**, and **MySQL**.

---

## 🚀 Key Features & Architectural Parity

- **Full REST API Parity**: Preserves all original `/api/v1` endpoints (User, Products, Reviews, Orders, Payments).
- **Relational Persistence**: MySQL using Spring Data JPA / Hibernate with automated schema creation & update (`ddl-auto=update`).
- **Complete Security**:
  - Spring Security 6 with stateless JWT authentication (`Bearer <token>` and HTTP-only cookie `"token"`).
  - BCrypt password hashing.
  - Role-based authorization (`ROLE_USER`, `ROLE_ADMIN`).
  - Cross-Origin Resource Sharing (CORS) configured for React frontend (`http://localhost:5173`).
- **Media Management**: Cloudinary Java SDK integration for avatar and multi-image product uploads.
- **Payment Gateway**: Razorpay Java SDK integration with HMAC SHA-256 signature verification.
- **Notifications**: JavaMailSender (SMTP) for password reset workflows with SHA-256 token hashing and 30-minute expiration.
- **Error Handling**: Centralized `GlobalExceptionHandler` and `HandleError` replicating Node.js error formats.

---

## 🛠️ Technology Stack

| Component | Technology | Version |
| :--- | :--- | :--- |
| **Language** | Java (JDK) | 17 LTS |
| **Framework** | Spring Boot | 3.3.5 |
| **Database** | MySQL | 8.x / 9.x |
| **ORM** | Spring Data JPA / Hibernate | 6.x |
| **Security** | Spring Security | 6.x |
| **JWT** | JJWT (Java JWT) | 0.12.6 |
| **Build Tool** | Apache Maven with Wrapper (`mvnw`) | 3.10.0 |
| **Cloud Services** | Cloudinary & Razorpay | Official Java SDKs |

---

## ⚙️ Configuration (`src/main/resources/application.properties`)

```properties
server.port=5000

# MySQL DataSource
spring.datasource.url=jdbc:mysql://localhost:3306/neuromart?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Security & JWT
neuromart.jwt.secret=ujjvalpateliya@170105
neuromart.jwt.expire-hours=48
neuromart.jwt.issuer=NeuroMarth_Backend
neuromart.jwt.audience=http://localhost:5173
neuromart.client.url=http://localhost:5173

# Cloudinary
cloudinary.cloud-name=dp2xua7qf
cloudinary.api-key=275131136268192
cloudinary.api-secret=jnc2D3vwYSdr7SGlZiQT-CJ6eqA

# Razorpay
razorpay.key-id=rzp_test_RDd2Pd7LEYMgKU
razorpay.key-secret=9GS7fHiRv102dxsUFEEZvPC4
```

> You can override any of these via environment variables (e.g. `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET_KEY`, `CLOUDINARY_NAME`, etc.).

---

## 🏃 Quick Start

### 1. Ensure MySQL is Running
Make sure your MySQL service is running on `localhost:3306` (or set `DB_URL` in environment / properties). Spring Boot will automatically create the `neuromart` database and tables on startup.

### 2. Run the Application
Open a terminal in `server-java` and run:

```powershell
# Windows PowerShell / CMD
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

The server starts at `http://localhost:5000`.

### 3. Build Production JAR
```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target/server-java-0.0.1-SNAPSHOT.jar
```

---

## 📡 REST API Reference (`/api/v1`)

### 👤 Authentication & User Management
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/register` | Public | Register new user with avatar upload |
| `POST` | `/api/v1/login` | Public | User login, returns JWT token & cookie |
| `GET/POST`| `/api/v1/logout` | Public | Clear auth cookie |
| `POST` | `/api/v1/password/forgot` | Public | Send password reset link to user email |
| `POST` | `/api/v1/reset/{token}` | Public | Reset password using token |
| `GET` | `/api/v1/profile` | User | Get current logged-in user profile |
| `PUT` | `/api/v1/profile/update` | User | Update user profile and avatar |
| `PUT` | `/api/v1/password/update` | User | Update password (old vs new) |
| `GET` | `/api/v1/admin/users` | Admin | Get list of all registered users |
| `GET` | `/api/v1/admin/user/{id}` | Admin | Get single user details |
| `PUT` | `/api/v1/admin/user/{id}` | Admin | Update user role (`user` / `admin`) |
| `DELETE`| `/api/v1/admin/user/{id}`| Admin | Delete user and Cloudinary avatar |

### 🛍️ Products & Reviews
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/products` | Public | Search products (`keyword`, `category`, `price[gte]`, `price[lte]`, `ratings[gte]`, `page`) |
| `GET` | `/api/v1/product/{id}` | Public | Get single product details |
| `POST` | `/api/v1/admin/product/create` | Admin | Create product with Cloudinary images |
| `PUT` | `/api/v1/admin/product/{id}` | Admin | Update product details / images |
| `DELETE`| `/api/v1/admin/product/{id}` | Admin | Delete product and Cloudinary images |
| `GET` | `/api/v1/admin/products` | Admin | Get all products for admin dashboard |
| `PUT` | `/api/v1/review` | User | Submit or update product rating & review |
| `GET` | `/api/v1/admin/reviews?id={id}` | Admin | Get all reviews for a product |
| `DELETE`| `/api/v1/admin/reviews?productId={id}&id={reviewId}` | Admin | Delete a review and recalculate rating |

### 📦 Orders
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/new/order` | User | Place new order with shipping, payment info, items |
| `GET` | `/api/v1/order/{id}` | User | Get single order details |
| `GET` | `/api/v1/orders/user` | User | Get all orders of logged-in user |
| `GET` | `/api/v1/admin/orders` | Admin | Get all orders and total sales amount |
| `PUT` | `/api/v1/admin/order/{id}` | Admin | Update order status (automatically reduces stock on "Delivered") |
| `DELETE`| `/api/v1/admin/order/{id}` | Admin | Delete delivered order |

### 💳 Payments
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/payment/process` | User | Initialize Razorpay order (amount in INR) |
| `GET` | `/api/v1/getKey` | User | Retrieve Razorpay public API key |
| `POST` | `/api/v1/paymentVerification` | Public | Verify Razorpay payment signature (HMAC SHA256) |
