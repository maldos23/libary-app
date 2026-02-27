# Sistema de Gestión de Biblioteca

POO Final — Fase 2 | Quarkus + React/Vite + SQLite

---

## Estructura del proyecto

```
poo-final/
├── library-backend/        ← API REST (Quarkus 3 + SQLite)
├── library-frontend/       ← UI (React + Vite + Tailwind)
├── seed.py                 ← Script de datos iniciales (Python)
└── REPORTE.md              ← Reporte académico de implementación
```

---

## Requisitos previos

| Herramienta | Versión |
|-------------|---------|
| Java JDK    | 17+     |
| Maven       | 3.9+    |
| Node.js     | 18+     |
| Python      | 3.8+    |

---

## 1 — Backend (Quarkus)

```bash
# Desde la raíz del proyecto poo-final
mvn quarkus:dev -f library-backend/pom.xml -Dquarkus.http.port=8080 -Dquarkus.analytics.disabled=true
```

La API queda disponible en `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/q/swagger-ui`

---

## 2 — Seed de datos

Con el backend corriendo, ejecuta el script de seed para poblar usuarios y libros:

```bash
# Desde la raíz del proyecto
python seed.py

# Con una URL base diferente
python seed.py --base-url http://localhost:8080
```

---

## 3 — Frontend (React + Vite)

```bash
cd library-frontend

# Instalar dependencias
npm install

# Servidor de desarrollo
npm run dev
```

Abre `http://localhost:5173` en tu navegador.

---

## Endpoints de la API

### Libros
| Método | Ruta              | Descripción              |
|--------|-------------------|--------------------------|
| GET    | /api/books        | Listar todos los libros  |
| GET    | /api/books/{id}   | Obtener libro por ID     |
| POST   | /api/books        | Crear libro              |
| PUT    | /api/books/{id}   | Actualizar libro         |
| DELETE | /api/books/{id}   | Eliminar libro           |

### Usuarios
| Método | Ruta              | Descripción               |
|--------|-------------------|---------------------------|
| GET    | /api/users        | Listar todos los usuarios |
| GET    | /api/users/{id}   | Obtener usuario por ID    |
| POST   | /api/users        | Crear usuario             |
| PUT    | /api/users/{id}   | Actualizar usuario        |
| DELETE | /api/users/{id}   | Eliminar usuario          |

### Préstamos
| Método | Ruta                         | Descripción                          |
|--------|------------------------------|--------------------------------------|
| GET    | /api/loans                   | Listar todos los préstamos           |
| POST   | /api/loans                   | Registrar nuevo préstamo             |
| PUT    | /api/loans/{id}/return       | Registrar devolución                 |
| GET    | /api/loans/user/{id}/active  | Préstamos activos de un usuario      |

---

## Reglas de negocio

- Un usuario puede tener **máximo 3 préstamos activos** simultáneos.
- Un libro no puede prestarse si `availableQuantity == 0`.
- Al registrar un préstamo: `availableQuantity--` y `activeLoans++`.
- Al devolver un préstamo: `availableQuantity++` y `activeLoans--`.
