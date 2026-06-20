# 🛒 E-Commerce REST API

\# Intern Details

\- \*\*Intern ID:\*\* CTTS095

\- \*\*Name:\*\* Nandini Gupta

A **production-ready** Spring Boot 3 E-Commerce Backend API with JWT authentication, role-based access control, and full CRUD operations.

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (JJWT 0.12) |
| Database | MySQL 8 |
| ORM | Spring Data JPA + Hibernate |
| Build | Maven 3.9 |
| Docs | Swagger / OpenAPI 3 (SpringDoc) |
| Utilities | Lombok |
| Container | Docker + Docker Compose |

---

## 📁 Project Structure

```
ecommerce-api/
├── src/main/java/com/ecommerce/api/
│   ├── EcommerceApiApplication.java       # Entry point
│   ├── config/
│   │   ├── SecurityConfig.java            # Spring Security filter chain
│   │   └── SwaggerConfig.java             # OpenAPI 3 configuration
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── CategoryController.java
│   │   ├── ProductController.java
│   │   ├── CartController.java
│   │   └── OrderController.java
│   ├── dto/
│   │   ├── request/                       # Incoming request bodies
│   │   └── response/                      # Outgoing response bodies
│   ├── entity/                            # JPA entities (User, Product, Order, …)
│   ├── exception/                         # Custom exceptions + global handler
│   ├── repository/                        # Spring Data JPA interfaces
│   ├── security/
│   │   ├── jwt/                           # JwtUtils, JwtFilter, JwtEntryPoint
│   │   └── service/                       # UserDetailsImpl, UserDetailsServiceImpl
│   └── service/
│       ├── *.java                         # Service interfaces
│       └── impl/                          # Service implementations
├── src/main/resources/
│   ├── application.properties
│   └── schema.sql                         # DDL + seed data
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── EcommerceAPI.postman_collection.json
```

---

## ⚙️ Local Setup (Without Docker)

### Prerequisites
- Java 21 JDK
- Maven 3.9+
- MySQL 8.0+
- VS Code (with Extension Pack for Java) or IntelliJ IDEA

### 1. MySQL Setup

```sql
-- Run in MySQL Workbench or terminal
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4;
CREATE USER 'ecommerce_user'@'localhost' IDENTIFIED BY 'ecommerce_pass';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'ecommerce_user'@'localhost';
FLUSH PRIVILEGES;

-- Then run the seed script
SOURCE src/main/resources/schema.sql;
```

### 2. Configure application.properties

Edit `src/main/resources/application.properties` if your credentials differ:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=ecommerce_user
spring.datasource.password=ecommerce_pass
```

### 3. Build & Run

```bash
# Clone and enter project
cd ecommerce-api

# Build (skip tests for speed)
mvn clean package -DskipTests

# Run
java -jar target/ecommerce-api-1.0.0.jar

# OR run directly with Maven
mvn spring-boot:run
```

The API starts at: **http://localhost:8080/api**

---

## 🐳 Docker Setup (Recommended)

```bash
# Start both MySQL + API
docker-compose up --build

# Run in background
docker-compose up -d --build

# View logs
docker-compose logs -f app

# Stop everything
docker-compose down

# Stop and delete volumes (fresh DB)
docker-compose down -v
```

---

## 📖 API Documentation (Swagger UI)

Once running, open:

```
http://localhost:8080/api/swagger-ui.html
```

**To test protected endpoints:**
1. Call `POST /api/auth/login`
2. Copy the `token` from the response
3. Click **Authorize** in Swagger UI
4. Paste the token (without `Bearer ` prefix)

---

## 🔐 Default Credentials (Seed Data)

| Role | Username | Password |
|---|---|---|
| ADMIN | `admin` | `Admin@1234` |
| CUSTOMER | `johndoe` | `Customer@1234` |

---

## 📡 API Endpoints Reference

### Authentication (Public)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register new customer |
| POST | `/api/auth/login` | Login → get JWT token |

### Users (Authenticated)
| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/users/me` | Any | Get my profile |
| PUT | `/api/users/me` | Any | Update my profile |
| PUT | `/api/users/me/change-password` | Any | Change password |
| GET | `/api/users/me/orders` | Any | My order history |
| GET | `/api/users` | ADMIN | All users |
| GET | `/api/users/{id}` | ADMIN | User by ID |

### Categories (GET = Public, POST/PUT/DELETE = Admin)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/categories` | List all categories |
| GET | `/api/categories/{id}` | Get by ID |
| POST | `/api/categories` | Create (Admin) |
| PUT | `/api/categories/{id}` | Update (Admin) |
| DELETE | `/api/categories/{id}` | Delete (Admin) |

### Products (GET = Public, POST/PUT/DELETE = Admin)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/products?page=0&size=10` | Paginated list |
| GET | `/api/products/{id}` | Get by ID |
| GET | `/api/products/category/{categoryId}` | By category |
| GET | `/api/products/search?keyword=iphone` | Search |
| POST | `/api/products` | Create (Admin) |
| PUT | `/api/products/{id}` | Update (Admin) |
| DELETE | `/api/products/{id}` | Soft delete (Admin) |

### Cart (Authenticated)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/cart` | View cart |
| POST | `/api/cart/items` | Add item |
| PUT | `/api/cart/items/{cartItemId}` | Update quantity |
| DELETE | `/api/cart/items/{cartItemId}` | Remove item |
| DELETE | `/api/cart` | Clear cart |

### Orders (Authenticated / Admin)
| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/orders` | Any | Place order |
| GET | `/api/orders` | Any | My orders |
| GET | `/api/orders/{id}` | Any | Order by ID |
| PUT | `/api/orders/{id}/cancel` | Any | Cancel (PENDING only) |
| GET | `/api/admin/orders` | ADMIN | All orders |
| PUT | `/api/admin/orders/{id}/status` | ADMIN | Update status |

---

## 🧪 Sample Requests & Responses

### Register
```json
POST /api/auth/register
{
  "firstName": "Alice",
  "lastName": "Johnson",
  "username": "alicejohnson",
  "email": "alice@example.com",
  "password": "Alice@1234",
  "phoneNumber": "9876543212"
}
```

Response `201 Created`:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 3,
    "username": "alicejohnson",
    "email": "alice@example.com",
    "roles": ["ROLE_CUSTOMER"]
  }
}
```

### Place Order
```json
POST /api/orders
Authorization: Bearer <token>
{
  "shippingAddress": "123 MG Road, Bangalore, Karnataka 560001",
  "paymentMethod": "CASH_ON_DELIVERY",
  "notes": "Ring the doorbell"
}
```

### Order Status Flow (Admin)
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
PENDING → CANCELLED
CONFIRMED → CANCELLED
```

---

## 🧰 VS Code Setup

1. Install the **Extension Pack for Java** (Microsoft)
2. Install **Spring Boot Extension Pack**
3. Open the project folder: `File → Open Folder → ecommerce-api`
4. VS Code auto-detects the Maven project
5. Open `EcommerceApiApplication.java` and click **Run** above `main()`

Or use the integrated terminal:
```bash
mvn spring-boot:run
```

---

## 🧾 Postman Collection

Import `EcommerceAPI.postman_collection.json` into Postman:

1. Open Postman → **Import** → select the JSON file
2. Run **Login (Admin)** or **Login (Customer)** first
3. The token is **auto-saved** to collection variables
4. All other requests automatically use it via `{{adminToken}}` / `{{customerToken}}`

---

## ✅ Security Features

- **JWT tokens** expire after 24 hours
- **BCrypt** password hashing (strength 10)
- **Stateless** session (no server-side sessions)
- **Role-based** endpoint protection
- **Input validation** on all request bodies
- **Soft delete** for products (never truly removed from DB)
- **Price snapshot** in OrderItem (historical price preservation)
- **Stock validation** at cart and order placement time
- **Ownership check** — users can only access their own orders and cart

---

## 🗄️ Database Schema (Summary)

```
users (id, first_name, last_name, username, email, password, phone, address)
roles (id, name)
user_roles (user_id, role_id)           ← join table
categories (id, name, description)
products (id, name, description, price, stock_quantity, category_id, active)
carts (id, user_id)
cart_items (id, cart_id, product_id, quantity)
orders (id, user_id, status, total_amount, shipping_address)
order_items (id, order_id, product_id, product_name, price_at_purchase, quantity)
```

---

## 📜 License

MIT License — free to use in portfolios, learning, and production projects.
