# Tugas-PBO

Monorepo — Cafe Ordering System (Sistem Pemesanan Cafe).

```
├── backend/    Spring Boot REST API, Java 21 + Maven (http://localhost:8080)
└── frontend/   React + Vite scaffold (belum dipakai — API-only untuk sekarang)
```

Lihat **[backend/README.md](backend/README.md)** untuk dokumentasi lengkap:
menjalankan app, menjalankan tests, endpoint, akun default, dan contoh request.

## Quick start

```bash
cd backend
./mvnw spring-boot:run
```

- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console
- DB: H2 file `backend/data/cafedb.mv.db` (hapus folder `data/` untuk reset)
- Admin default: `admin@cafe.local` / `admin123`

## Tests

```bash
cd backend && ./mvnw test
```

Class diagram sumber: `Class_Diagram_Tahap2_Sistem_Pemesanan_Cafe.drawio` / `.png`.
