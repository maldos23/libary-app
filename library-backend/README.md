# Library Backend

API REST del Sistema de Gestión de Biblioteca, construida con **Quarkus 3.22.3**, **Hibernate ORM + Panache** y **SQLite**.

---

## Tabla de contenido

1. [Requisitos previos](#requisitos-previos)
2. [Levantar con Docker](#levantar-con-docker)
3. [Levantar sin Docker (Java directo)](#levantar-sin-docker-java-directo)
4. [Variables de entorno](#variables-de-entorno)
5. [Endpoints disponibles](#endpoints-disponibles)
6. [Clientes de API recomendados](#clientes-de-api-recomendados)

---

## Requisitos previos

### Con Docker
| Herramienta | Versión mínima | Descarga |
|---|---|---|
| Docker Desktop | 24+ | https://www.docker.com/products/docker-desktop |

### Sin Docker
| Herramienta | Versión mínima | Descarga |
|---|---|---|
| JDK | 17+ | https://adoptium.net (Eclipse Temurin recomendado) |
| Maven | 3.9+ | https://maven.apache.org/download.cgi |

Verifica que estén instalados:

```bash
java -version   # debe mostrar 17+
mvn -version    # debe mostrar 3.9+
docker version  # solo si usas Docker
```

---

## Levantar con Docker

### 1. Construir la imagen

Desde la raíz del directorio `library-backend/`:

```bash
docker build -t library-backend:local .
```

El proceso tarda ~2-3 min la primera vez (descarga dependencias Maven). Las siguientes ejecuciones usan el caché de Docker y son mucho más rápidas.

### 2. Iniciar el contenedor

```bash
docker run -d \
  --name library-backend \
  -p 8080:8080 \
  -e DB_PATH=/data/library.db \
  -v library-data:/data \
  library-backend:local
```

| Flag | Descripción |
|---|---|
| `-d` | Ejecuta en segundo plano |
| `-p 8080:8080` | Mapea el puerto 8080 del host al contenedor |
| `-e DB_PATH=/data/library.db` | Ruta del archivo SQLite dentro del contenedor |
| `-v library-data:/data` | Volumen Docker para persistir la base de datos |

### 3. Verificar que está corriendo

```bash
docker logs library-backend
```

Deberías ver:

```
Listening on: http://0.0.0.0:8080
Profile prod activated.
```

### 4. Detener y eliminar el contenedor

```bash
docker stop library-backend
docker rm library-backend
```

> **Nota:** la base de datos persiste en el volumen `library-data` aunque elimines el contenedor. Para borrarla también ejecuta `docker volume rm library-data`.

---

## Levantar sin Docker (Java directo)

### 1. Clonar y entrar al directorio

```bash
cd library-backend
```

### 2. Modo desarrollo (hot-reload)

Ideal para desarrollo: cualquier cambio en el código se refleja automáticamente sin reiniciar.

```bash
mvn quarkus:dev
```

La API queda disponible en `http://localhost:8080`.

### 3. Modo producción

Primero compila el proyecto:

```bash
mvn package -DskipTests
```

Luego ejecuta el fast-jar generado:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

### 4. Ejecutar con una ruta de DB personalizada

```bash
java -DDB_PATH=/ruta/personalizada/library.db \
     -jar target/quarkus-app/quarkus-run.jar
```

---

## Variables de entorno

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `PORT` | `8080` | Puerto HTTP del servidor |
| `DB_PATH` | `./library.db` | Ruta del archivo SQLite |
| `CORS_ORIGINS` | `http://localhost:5173` | Orígenes permitidos para CORS |

---

## Endpoints disponibles

La API base es `http://localhost:8080`.

### Libros

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/books` | Listar todos los libros |
| `GET` | `/api/books/{id}` | Obtener un libro por ID |
| `POST` | `/api/books` | Crear un nuevo libro |
| `PUT` | `/api/books/{id}` | Actualizar un libro |
| `DELETE` | `/api/books/{id}` | Eliminar un libro |

**Ejemplo de cuerpo para crear un libro:**
```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "totalQuantity": 5
}
```

### Usuarios

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/users` | Listar todos los usuarios |
| `GET` | `/api/users/{id}` | Obtener un usuario por ID |
| `POST` | `/api/users` | Crear un nuevo usuario |
| `PUT` | `/api/users/{id}` | Actualizar un usuario |
| `DELETE` | `/api/users/{id}` | Eliminar un usuario |

**Ejemplo de cuerpo para crear un usuario:**
```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "identificationDocument": "12345678"
}
```

### Préstamos

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/loans` | Listar todos los préstamos |
| `POST` | `/api/loans` | Registrar un préstamo |
| `PUT` | `/api/loans/{id}/return` | Devolver un libro |
| `GET` | `/api/loans/user/{userId}/active` | Préstamos activos de un usuario |

**Ejemplo de cuerpo para registrar un préstamo:**
```json
{
  "bookId": 1,
  "userId": 1
}
```

### Documentación interactiva (Swagger UI)

Abre en el navegador:

```
http://localhost:8080/q/swagger-ui
```

Swagger UI permite explorar y probar todos los endpoints directamente desde el navegador sin instalar ningún cliente adicional.

---

## Clientes de API recomendados

Si prefieres una herramienta dedicada para probar la API, estas son las opciones más populares:

### Postman ⭐ Recomendado para principiantes
> Interfaz gráfica completa, colecciones reutilizables, variables de entorno y generación automática de código.

- **Descarga:** https://www.postman.com/downloads/
- **Plataformas:** Windows, macOS, Linux
- **Precio:** Gratuito (plan básico)

**Inicio rápido:**
1. Descarga e instala Postman.
2. Crea una nueva petición (`New → HTTP Request`).
3. Selecciona el método (`GET`, `POST`, etc.).
4. Escribe la URL, por ejemplo `http://localhost:8080/api/books`.
5. Para `POST`/`PUT`, ve a la pestaña **Body → raw → JSON** y pega el cuerpo.
6. Haz clic en **Send**.

---

### Bruno
> Cliente moderno y de código abierto; guarda las colecciones como archivos en tu repositorio.

- **Descarga:** https://www.usebruno.com/downloads
- **Plataformas:** Windows, macOS, Linux
- **Precio:** Gratuito y open-source

---

### Insomnia
> Ligero, rápido y con soporte para GraphQL y gRPC además de REST.

- **Descarga:** https://insomnia.rest/download
- **Plataformas:** Windows, macOS, Linux
- **Precio:** Gratuito (plan básico)

---

### httpie (terminal)
> Cliente HTTP de línea de comandos con sintaxis amigable para quienes prefieren la terminal.

- **Descarga:** https://httpie.io/cli
- **Instalación rápida en macOS:** `brew install httpie`

```bash
# Listar libros
http GET http://localhost:8080/api/books

# Crear un libro
http POST http://localhost:8080/api/books \
  title="Clean Code" author="Robert C. Martin" isbn="9780132350884" totalQuantity:=5
```

---

### curl (ya incluido en macOS/Linux)
> Disponible en cualquier terminal sin instalar nada extra.

```bash
# Listar libros
curl http://localhost:8080/api/books

# Crear un libro
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","isbn":"9780132350884","totalQuantity":5}'
```
