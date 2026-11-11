# Tugas-PBO

Monorepo — Cafe Ordering System (Sistem Pemesanan Cafe).

```
├── backend/          Spring Boot REST API, Java 21 + Maven
├── frontend/         React + Vite UI
└── docker-compose.yml  all-in-one (backend + frontend + H2 volume)
```

## Menjalankan (Docker — recommended)

```bash
docker compose up --build
```

| Service | URL |
|---|---|
| Web UI | http://localhost:5173 |
| REST API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html (atau via frontend :5173/swagger-ui.html) |
| H2 Console | http://localhost:8080/h2-console |

Database H2 tersimpan di Docker volume `h2-data`. Reset semua data:

```bash
docker compose down -v
```

Admin default: `admin@cafe.local` / `admin123`
(override via env `ADMIN_EMAIL` / `ADMIN_PASSWORD` / `JWT_SECRET` di docker-compose.yml)

## Development (tanpa Docker)

```bash
cd backend && ./mvnw spring-boot:run   # API di :8080
cd frontend && npm install && npm run dev   # UI di :5173 (proxy /api → :8080)
```

## Tests

```bash
cd backend && ./mvnw test
```

Dokumentasi lengkap API: **[backend/README.md](backend/README.md)** ·
Postman collection: `backend/postman/Cafe-API.postman_collection.json` ·
Class diagram: `Class_Diagram_Tahap2_Sistem_Pemesanan_Cafe.drawio` / `.png`
