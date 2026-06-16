# Banco Devsu — Reto Técnico

Sistema bancario full-stack: gestión de clientes, cuentas y movimientos con reporte de estado de cuenta en PDF.

## Tecnologías

- **Backend**: Java 21, Spring Boot 4, JPA/Hibernate, PostgreSQL
- **Frontend**: Angular 21, CSS puro
- **Contenedores**: Docker, Docker Compose
- **Tests**: JUnit 5 (backend), Jest (frontend)

## Levantar el proyecto

Requiere Docker Desktop instalado y corriendo.

```bash
# 1. Clonar o descomprimir el proyecto
# 2. Desde la raíz del proyecto:
docker compose up --build
```

El primer arranque tarda unos minutos mientras descarga imágenes y compila.

- Frontend: http://localhost:4200
- Backend: http://localhost:8080

Para detener:

```bash
docker compose down
```

## Endpoints

Base URL: `http://localhost:8080`

- `GET/POST /clientes` · `GET/PUT/PATCH/DELETE /clientes/{id}`
- `GET/POST /cuentas` · `GET/PUT/PATCH/DELETE /cuentas/{id}`
- `GET/POST /movimientos` · `GET/PUT/PATCH/DELETE /movimientos/{id}`
- `GET /reportes?clienteId=&fechaInicio=&fechaFin=`
- `GET /reportes/pdf?clienteId=&fechaInicio=&fechaFin=`

Formato de fechas: `2022-01-01T00:00:00`

## Reglas de negocio

- Valor positivo → Crédito; valor negativo → Débito
- Saldo insuficiente → 400
- Límite diario de retiros: $1000

## Tests

```bash
# Backend
cd backend && ./mvnw test

# Frontend
cd frontend && npm test
```

## Postman

Importar `postman/Banco.postman_collection.json`. La variable `baseUrl` apunta a `http://localhost:8080`.
