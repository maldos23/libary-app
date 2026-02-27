#!/usr/bin/env python3
"""
seed.py — Pobla la base de datos de la biblioteca con usuarios y libros de muestra.

Uso:
    python seed.py [--base-url http://localhost:8080]

Requisitos:
    pip install requests
"""

import argparse
import json
import sys
import time
import urllib.request
import urllib.error


# ─── Datos de muestra ──────────────────────────────────────────────────────────

USERS = [
    {
        "name": "María García López",
        "identificationDocument": "12345678A",
        "email": "maria.garcia@biblioteca.mx",
    },
    {
        "name": "Carlos Rodríguez Pérez",
        "identificationDocument": "23456789B",
        "email": "carlos.rodriguez@biblioteca.mx",
    },
    {
        "name": "Ana Martínez Torres",
        "identificationDocument": "34567890C",
        "email": "ana.martinez@biblioteca.mx",
    },
    {
        "name": "Luis Hernández Díaz",
        "identificationDocument": "45678901D",
        "email": "luis.hernandez@biblioteca.mx",
    },
    {
        "name": "Sofía González Ruiz",
        "identificationDocument": "56789012E",
        "email": "sofia.gonzalez@biblioteca.mx",
    },
    {
        "name": "Diego López Sánchez",
        "identificationDocument": "67890123F",
        "email": "diego.lopez@biblioteca.mx",
    },
    {
        "name": "Valentina Castro Jiménez",
        "identificationDocument": "78901234G",
        "email": "valentina.castro@biblioteca.mx",
    },
    {
        "name": "Andrés Morales Vargas",
        "identificationDocument": "89012345H",
        "email": "andres.morales@biblioteca.mx",
    },
]

BOOKS = [
    {
        "title": "El Quijote",
        "author": "Miguel de Cervantes",
        "isbn": "978-84-206-0000-1",
        "totalQuantity": 5,
    },
    {
        "title": "Cien años de soledad",
        "author": "Gabriel García Márquez",
        "isbn": "978-84-397-0495-1",
        "totalQuantity": 4,
    },
    {
        "title": "1984",
        "author": "George Orwell",
        "isbn": "978-0-452-28423-4",
        "totalQuantity": 3,
    },
    {
        "title": "El Principito",
        "author": "Antoine de Saint-Exupéry",
        "isbn": "978-84-9838-388-3",
        "totalQuantity": 6,
    },
    {
        "title": "Fundación",
        "author": "Isaac Asimov",
        "isbn": "978-84-450-7640-3",
        "totalQuantity": 3,
    },
    {
        "title": "El Señor de los Anillos",
        "author": "J.R.R. Tolkien",
        "isbn": "978-84-450-7770-7",
        "totalQuantity": 4,
    },
    {
        "title": "Fahrenheit 451",
        "author": "Ray Bradbury",
        "isbn": "978-84-450-7642-7",
        "totalQuantity": 2,
    },
    {
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "isbn": "978-0-13-235088-4",
        "totalQuantity": 3,
    },
    {
        "title": "Design Patterns",
        "author": "Gang of Four",
        "isbn": "978-0-20-163361-5",
        "totalQuantity": 2,
    },
    {
        "title": "The Pragmatic Programmer",
        "author": "David Thomas & Andrew Hunt",
        "isbn": "978-0-13-595705-9",
        "totalQuantity": 3,
    },
    {
        "title": "Crimen y Castigo",
        "author": "Fiódor Dostoyevski",
        "isbn": "978-84-376-0299-2",
        "totalQuantity": 3,
    },
    {
        "title": "Sapiens: De animales a dioses",
        "author": "Yuval Noah Harari",
        "isbn": "978-84-9992-255-0",
        "totalQuantity": 4,
    },
]


# ─── HTTP helpers ───────────────────────────────────────────────────────────────

def post(url: str, payload: dict) -> dict:
    """Realiza un POST JSON y retorna la respuesta como dict."""
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=data,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8")
        try:
            error_data = json.loads(body)
            raise RuntimeError(error_data.get("error", body))
        except json.JSONDecodeError:
            raise RuntimeError(body)


def get(url: str) -> list | dict:
    """Realiza un GET y retorna la respuesta como dict o list."""
    with urllib.request.urlopen(url) as resp:
        return json.loads(resp.read().decode("utf-8"))


# ─── Seed functions ─────────────────────────────────────────────────────────────

def wait_for_api(base_url: str, retries: int = 15, delay: float = 2.0) -> bool:
    """Espera a que el backend esté disponible."""
    print(f"⏳  Esperando a la API en {base_url} …", flush=True)
    for i in range(retries):
        try:
            get(f"{base_url}/api/books")
            print("✅  API disponible.\n")
            return True
        except Exception:
            print(f"   Intento {i + 1}/{retries} — reintentando en {delay}s …")
            time.sleep(delay)
    print("❌  No se pudo conectar con la API.")
    return False


def seed_users(base_url: str) -> list[dict]:
    """Inserta todos los usuarios de muestra y retorna los creados."""
    print("👤  Insertando usuarios …")
    created = []
    # Obtener existentes para evitar duplicados
    existing: list = get(f"{base_url}/api/users")  # type: ignore[assignment]
    existing_docs = {u["identificationDocument"] for u in existing}

    for user in USERS:
        if user["identificationDocument"] in existing_docs:
            print(f"   ↩  {user['name']} — ya existe, omitido.")
            continue
        try:
            result = post(f"{base_url}/api/users", user)
            created.append(result)
            print(f"   ✔  [{result['id']:>3}] {result['name']}")
        except RuntimeError as e:
            print(f"   ⚠  {user['name']} — {e}")

    print(f"   → {len(created)} usuarios nuevos insertados.\n")
    return created


def seed_books(base_url: str) -> list[dict]:
    """Inserta todos los libros de muestra y retorna los creados."""
    print("📚  Insertando libros …")
    created = []
    existing: list = get(f"{base_url}/api/books")  # type: ignore[assignment]
    existing_isbns = {b["isbn"] for b in existing}

    for book in BOOKS:
        if book["isbn"] in existing_isbns:
            print(f"   ↩  {book['title']} — ya existe, omitido.")
            continue
        try:
            result = post(f"{base_url}/api/books", book)
            created.append(result)
            print(f"   ✔  [{result['id']:>3}] {result['title']}  ({result['availableQuantity']} disp.)")
        except RuntimeError as e:
            print(f"   ⚠  {book['title']} — {e}")

    print(f"   → {len(created)} libros nuevos insertados.\n")
    return created


def print_summary(base_url: str) -> None:
    """Imprime un resumen del estado actual de la base de datos."""
    users: list = get(f"{base_url}/api/users")   # type: ignore[assignment]
    books: list = get(f"{base_url}/api/books")   # type: ignore[assignment]
    loans: list = get(f"{base_url}/api/loans")   # type: ignore[assignment]

    print("─" * 50)
    print("📊  Resumen de la base de datos:")
    print(f"   • Usuarios: {len(users)}")
    print(f"   • Libros:   {len(books)}")
    print(f"   • Préstamos: {len(loans)}")
    print("─" * 50)


# ─── Main ────────────────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(description="Seed de usuarios y libros para la API de biblioteca.")
    parser.add_argument(
        "--base-url",
        default="http://localhost:8080",
        help="URL base de la API (default: http://localhost:8080)",
    )
    parser.add_argument(
        "--skip-wait",
        action="store_true",
        help="Omitir la espera de disponibilidad de la API",
    )
    args = parser.parse_args()

    base_url = args.base_url.rstrip("/")

    print()
    print("╔══════════════════════════════════════════╗")
    print("║   Library — Script de Seed de Datos      ║")
    print("╚══════════════════════════════════════════╝")
    print()

    if not args.skip_wait:
        if not wait_for_api(base_url):
            sys.exit(1)

    seed_users(base_url)
    seed_books(base_url)
    print_summary(base_url)

    print("\n🎉  Seed completado exitosamente.")
    print(f"   Swagger UI: {base_url}/q/swagger-ui")
    print()


if __name__ == "__main__":
    main()
