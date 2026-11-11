# Cafe Ordering System — REST API

REST API untuk sistem pemesanan cafe (Tugas PBO), diimplementasikan dari UML
class diagram `Class_Diagram_Tahap2_Sistem_Pemesanan_Cafe.drawio` dengan
prinsip OOP: inheritance, encapsulation, abstraction, dan polymorphism.

## Tech Stack

- Java 21, Spring Boot 3.5.3, Maven
- Spring Web, Spring Data JPA (Hibernate), Spring Security, Jakarta Bean Validation
- JWT (jjwt 0.12.6), BCrypt password hashing
- H2 database (file-based, `backend/data/cafedb.mv.db`)
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito + MockMvc

## Menjalankan Aplikasi

Via Docker dari root repo (backend + frontend sekaligus):

```bash
docker compose up --build
```

Atau backend saja:

```bash
cd backend
./mvnw spring-boot:run
```

API berjalan di `http://localhost:8080`. H2 otomatis dibuat di `./data/cafedb.mv.db`
(data persist antar restart — hapus folder `data/` untuk reset).

| Tool | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 Console | http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/cafedb`, user `sa`, password kosong) |

## Menjalankan Tests

```bash
./mvnw test
```

42 tests: entity unit tests (order, payment, menu) + MockMvc integration tests
(auth, menu, order, payment, admin) memakai H2 in-memory profile `test`.

## Akun Default (development)

| Role | Email | Password |
|---|---|---|
| Admin | `admin@cafe.local` | `admin123` |

Menu seed: MENU-001 Nasi Goreng (FOOD, 25000), MENU-002 Ayam Geprek (FOOD,
20000), MENU-003 Iced Coffee (DRINK, 18000), MENU-004 Matcha Latte (DRINK,
22000), MENU-005 Chocolate Cake (DESSERT, 28000).

## Environment Variables

| Var | Default | Keterangan |
|---|---|---|
| `DB_URL` | `jdbc:h2:file:./data/cafedb` | JDBC URL |
| `DB_USER` / `DB_PASSWORD` | `sa` / kosong | DB credentials |
| `JWT_SECRET` | dev secret | **Wajib diganti untuk produksi** (min 32 bytes) |
| `JWT_EXPIRATION_MS` | `86400000` | Umur token (24 jam) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | `admin@cafe.local` / `admin123` | Admin seed |

## Struktur Paket (`com.tugaspbo.backend`)

```
config/      SecurityConfig, OpenApiConfig, DataInitializer (seeding)
controller/  AuthController, MenuController, OrderController, PaymentController, AdminController
dto/         request/ + response/ (records; ApiResponse wrapper)
entity/      User(abstract)→Customer/Admin, Menu, MenuItem, Order, OrderItem,
             Payment(abstract)→CashPayment/EWalletPayment
enums/       OrderStatus, PaymentStatus, MenuCategory, UserRole
exception/   GlobalExceptionHandler + custom exceptions
repository/  Spring Data JPA repositories
security/    JwtService, JwtAuthenticationFilter, UserPrincipal, CustomUserDetailsService
service/     AuthService, MenuService, OrderService, PaymentService, AdminService
```

## Desain OOP (sesuai UML)

- **Inheritance**: `Customer`/`Admin` extends `User` (JPA `JOINED`);
  `CashPayment`/`EWalletPayment` extends `Payment` (JPA `JOINED`).
- **Abstraction**: `User` (abstract `getRole()`), `Payment` (abstract `pay()`,
  `getMethodName()`), `markSuccess()`/`markFailed()` protected.
- **Polymorphism**: `Order.checkout(Payment)` memanggil `payment.pay()` lewat
  referensi abstrak — `CashPayment` menghitung kembalian, `EWalletPayment`
  memvalidasi provider (DANA/OVO/GOPAY/SHOPEEPAY) + nomor akun.
- **Encapsulation**: field private; mutasi lewat method bisnis
  (`updatePrice`, `addItem`, `transitionTo`) dengan validasi.

### Catatan implementasi vs UML

- `double` pada UML → `BigDecimal` untuk semua nilai uang.
- `Order.calculateTotal()` mengembalikan `BigDecimal` (bukan `double`).
- `Menu.items: Map<String, MenuItem>` → `List<MenuItem>` `@OneToMany`
  (repository-backed; `menus` adalah tabel dengan 1 baris logika).
- `User.login()` ada di entity (memakai BCrypt internal), tapi autentikasi
  HTTP/JWT sepenuhnya di `AuthService`.
- `OrderItem` menyimpan snapshot `unitPrice` + `itemName` saat item ditambahkan
  → harga historis tidak berubah saat menu di-update/dihapus.
- `Order` memegang FK `payment_id`; `@Version` untuk optimistic locking
  (mencegah double payment konkuren).

## API Endpoints

Semua response memakai format:
```json
{ "success": true, "message": "...", "data": { }, "timestamp": "..." }
```

### Auth

| Method | Endpoint | Akses | Body |
|---|---|---|---|
| POST | `/api/auth/register` | public | `{name,email,password,phone}` → 201 + JWT |
| POST | `/api/auth/login` | public | `{email,password}` → JWT |
| POST | `/api/auth/admin/register` | ADMIN | `{name,email,password}` → 201 |

### Menu (GET public, write = ADMIN)

| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/api/menu` | semua item |
| GET | `/api/menu/available` | hanya yang available |
| GET | `/api/menu/{id}` | detail |
| GET | `/api/menu/category/{FOOD\|DRINK\|DESSERT}` | filter kategori |
| POST | `/api/menu` | `{name,price,category,available}` → 201 |
| PUT | `/api/menu/{id}` | update item |
| PATCH | `/api/menu/{id}/price` | `{price}` |
| PATCH | `/api/menu/{id}/availability` | `{available}` |
| DELETE | `/api/menu/{id}` | hard delete; soft delete jika direferensikan order → 204 |

### Orders

| Method | Endpoint | Akses | Keterangan |
|---|---|---|---|
| POST | `/api/orders` | CUSTOMER | `{items:[{menuItemId,quantity}]}` → 201 |
| GET | `/api/orders` | auth | customer: milik sendiri; admin: semua |
| GET | `/api/orders/{orderId}` | owner/admin | detail |
| GET | `/api/orders/{orderId}/total` | owner/admin | total |
| POST | `/api/orders/{orderId}/items` | CUSTOMER owner | tambah item (PENDING only) |
| DELETE | `/api/orders/{orderId}/items/{menuItemId}` | CUSTOMER owner | hapus item (PENDING only) |
| POST | `/api/orders/{orderId}/cancel` | CUSTOMER owner | PENDING → CANCELLED |

### Payments

| Method | Endpoint | Akses | Keterangan |
|---|---|---|---|
| POST | `/api/orders/{orderId}/payments/cash` | CUSTOMER owner | `{cashGiven}` → kembalian di response |
| POST | `/api/orders/{orderId}/payments/ewallet` | CUSTOMER owner | `{provider,accountNumber}` (DANA/OVO/GOPAY/SHOPEEPAY) |
| GET | `/api/orders/{orderId}/payments` | owner/admin | info pembayaran |

### Admin

| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/api/admin/orders` | semua order |
| PATCH | `/api/admin/orders/{orderId}/processing` | PAID → PROCESSING |
| PATCH | `/api/admin/orders/{orderId}/complete` | PROCESSING → COMPLETED |
| GET | `/api/admin/dashboard` | statistik order + total revenue |

## Alur Status

```
Order:   PENDING → PAID → PROCESSING → COMPLETED
         PENDING → CANCELLED        (PAID ke atas: tidak bisa cancel — no refund)
Payment: PENDING → SUCCESS | FAILED (sekali proses saja)
```

## Contoh Request

```bash
# login admin
curl -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"admin@cafe.local","password":"admin123"}'

# register customer → response berisi token
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Budi","email":"budi@test.com","password":"password123","phone":"08123456789"}'

# buat order (pakai token customer)
curl -X POST localhost:8080/api/orders -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"menuItemId":"MENU-003","quantity":2}]}'

# bayar cash
curl -X POST localhost:8080/api/orders/<orderId>/payments/cash \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"cashGiven":50000}'
```

Koleksi Postman: `postman/Cafe-API.postman_collection.json`
(set variable `baseUrl` + jalankan login/register untuk mengisi `token`).
