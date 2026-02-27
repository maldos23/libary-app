// ─── Types ─────────────────────────────────────────────────────────────────

export interface BookDTO {
  id?: number
  title: string
  author: string
  isbn: string
  totalQuantity: number
  availableQuantity?: number
}

export interface UserDTO {
  id?: number
  name: string
  identificationDocument: string
  email: string
  activeLoans?: number
}

export type LoanStatus = 'ACTIVE' | 'RETURNED'

export interface LoanDTO {
  id?: number
  loanDate?: string
  returnDate?: string
  status?: LoanStatus
  userId: number
  bookId: number
  userName?: string
  bookTitle?: string
}

// ─── Base ───────────────────────────────────────────────────────────────────

/** Dominio/prefijo del API. Configurable con VITE_API_BASE_URL en .env */
const BASE = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? '/api'

async function request<T>(
  path: string,
  options?: RequestInit,
): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  })
  if (!res.ok) {
    const error = await res.json().catch(() => ({ error: res.statusText }))
    throw new Error(error.error ?? 'Error desconocido')
  }
  if (res.status === 204) return undefined as T
  return res.json()
}

// ─── Books API ──────────────────────────────────────────────────────────────

export const booksApi = {
  list: () => request<BookDTO[]>('/books'),
  get: (id: number) => request<BookDTO>(`/books/${id}`),
  create: (dto: BookDTO) =>
    request<BookDTO>('/books', { method: 'POST', body: JSON.stringify(dto) }),
  update: (id: number, dto: BookDTO) =>
    request<BookDTO>(`/books/${id}`, { method: 'PUT', body: JSON.stringify(dto) }),
  delete: (id: number) =>
    request<void>(`/books/${id}`, { method: 'DELETE' }),
}

// ─── Users API ──────────────────────────────────────────────────────────────

export const usersApi = {
  list: () => request<UserDTO[]>('/users'),
  get: (id: number) => request<UserDTO>(`/users/${id}`),
  create: (dto: UserDTO) =>
    request<UserDTO>('/users', { method: 'POST', body: JSON.stringify(dto) }),
  update: (id: number, dto: UserDTO) =>
    request<UserDTO>(`/users/${id}`, { method: 'PUT', body: JSON.stringify(dto) }),
  delete: (id: number) =>
    request<void>(`/users/${id}`, { method: 'DELETE' }),
}

// ─── Loans API ──────────────────────────────────────────────────────────────

export const loansApi = {
  list: () => request<LoanDTO[]>('/loans'),
  create: (dto: LoanDTO) =>
    request<LoanDTO>('/loans', { method: 'POST', body: JSON.stringify(dto) }),
  returnLoan: (id: number) =>
    request<LoanDTO>(`/loans/${id}/return`, { method: 'PUT' }),
}
