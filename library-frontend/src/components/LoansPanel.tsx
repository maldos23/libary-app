import { useEffect, useState } from 'react'
import { loansApi, booksApi, usersApi, LoanDTO, BookDTO, UserDTO } from '../lib/api'
import { Plus, RotateCcw, X, Check, Search } from 'lucide-react'

const EMPTY_LOAN = { userId: 0, bookId: 0 }

export function LoansPanel() {
  const [loans, setLoans] = useState<LoanDTO[]>([])
  const [books, setBooks] = useState<BookDTO[]>([])
  const [users, setUsers] = useState<UserDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState(EMPTY_LOAN)
  const [saving, setSaving] = useState(false)
  const [query, setQuery] = useState('')
  const [userQuery, setUserQuery] = useState('')
  const [bookQuery, setBookQuery] = useState('')
  const [showUserList, setShowUserList] = useState(false)
  const [showBookList, setShowBookList] = useState(false)

  const load = async () => {
    try {
      setLoading(true)
      const [l, b, u] = await Promise.all([
        loansApi.list(),
        booksApi.list(),
        usersApi.list(),
      ])
      setLoans(l)
      setBooks(b)
      setUsers(u)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.userId || !form.bookId) {
      setError('Selecciona un usuario y un libro.')
      return
    }
    try {
      setSaving(true)
      await loansApi.create(form as LoanDTO)
      setShowForm(false)
      setForm(EMPTY_LOAN)
      setUserQuery('')
      setBookQuery('')
      await load()
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  const handleReturn = async (id: number) => {
    if (!confirm('¿Registrar devolución?')) return
    try {
      await loansApi.returnLoan(id)
      await load()
    } catch (e) {
      setError((e as Error).message)
    }
  }

  const filtered = loans.filter(l => {
    const q = query.toLowerCase()
    return (l.userName ?? '').toLowerCase().includes(q) ||
      (l.bookTitle ?? '').toLowerCase().includes(q) ||
      (l.status ?? '').toLowerCase().includes(q)
  })

  const availableUsers = users.filter(u =>
    u.name.toLowerCase().includes(userQuery.toLowerCase()) ||
    (u.identificationDocument ?? '').toLowerCase().includes(userQuery.toLowerCase())
  )

  const availableBooks = books.filter(b =>
    b.title.toLowerCase().includes(bookQuery.toLowerCase()) ||
    (b.isbn ?? '').toLowerCase().includes(bookQuery.toLowerCase())
  )

  const selectedUser = users.find(u => u.id === form.userId)
  const selectedBook = books.find(b => b.id === form.bookId)

  const statusBadge = (status?: string) =>
    status === 'ACTIVE'
      ? 'bg-zinc-900 text-white'
      : 'bg-zinc-100 text-zinc-500'

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold text-zinc-900">Préstamos</h2>
          <p className="text-sm text-zinc-500 mt-0.5">{filtered.length} de {loans.length} registros</p>
        </div>
        <button
          onClick={() => { setShowForm(true); setUserQuery(''); setBookQuery(''); setForm(EMPTY_LOAN) }}
          className="flex items-center gap-1.5 px-3 py-1.5 bg-zinc-900 text-white text-sm font-medium rounded hover:bg-zinc-700 transition-colors"
        >
          <Plus size={14} /> Nuevo préstamo
        </button>
      </div>

      {error && (
        <div className="flex items-center justify-between px-4 py-2.5 border border-red-200 bg-red-50 rounded text-sm text-red-600">
          {error}
          <button onClick={() => setError(null)}><X size={14} /></button>
        </div>
      )}

      {showForm && (
        <form onSubmit={handleSubmit} className="border border-zinc-200 rounded p-5 space-y-4 bg-zinc-50">
          <h3 className="text-sm font-semibold text-zinc-800">Registrar préstamo</h3>
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <label className="text-xs font-medium text-zinc-600">Usuario</label>
              <div className="relative">
                <Search size={13} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400 pointer-events-none" />
                <input
                  type="text"
                  placeholder="Buscar usuario…"
                  value={form.userId && selectedUser ? selectedUser.name : userQuery}
                  onFocus={() => { setShowUserList(true); if (form.userId) { setUserQuery(''); setForm(f => ({ ...f, userId: 0 })) } }}
                  onChange={e => { setUserQuery(e.target.value); setForm(f => ({ ...f, userId: 0 })); setShowUserList(true) }}
                  onBlur={() => setTimeout(() => setShowUserList(false), 150)}
                  className="w-full pl-8 pr-4 py-1.5 border border-zinc-300 rounded text-sm bg-white focus:outline-none focus:ring-1 focus:ring-zinc-900"
                />
                {showUserList && (
                  <ul className="absolute z-20 top-full left-0 right-0 mt-1 max-h-44 overflow-y-auto bg-white border border-zinc-200 rounded shadow-sm text-sm">
                    {availableUsers.length === 0 ? (
                      <li className="px-3 py-2 text-zinc-400">Sin resultados</li>
                    ) : availableUsers.map(u => (
                      <li key={u.id}
                        onMouseDown={() => { setForm(f => ({ ...f, userId: u.id! })); setUserQuery(''); setShowUserList(false) }}
                        className={`px-3 py-2 cursor-pointer flex justify-between items-center
                          ${(u.activeLoans ?? 0) >= 3 ? 'opacity-40 cursor-not-allowed' : 'hover:bg-zinc-50'}`}>
                        <span>{u.name}</span>
                        <span className="text-xs text-zinc-400">{u.activeLoans ?? 0}/3 préstamos</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
            <div className="space-y-1">
              <label className="text-xs font-medium text-zinc-600">Libro</label>
              <div className="relative">
                <Search size={13} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400 pointer-events-none" />
                <input
                  type="text"
                  placeholder="Buscar libro…"
                  value={form.bookId && selectedBook ? selectedBook.title : bookQuery}
                  onFocus={() => { setShowBookList(true); if (form.bookId) { setBookQuery(''); setForm(f => ({ ...f, bookId: 0 })) } }}
                  onChange={e => { setBookQuery(e.target.value); setForm(f => ({ ...f, bookId: 0 })); setShowBookList(true) }}
                  onBlur={() => setTimeout(() => setShowBookList(false), 150)}
                  className="w-full pl-8 pr-4 py-1.5 border border-zinc-300 rounded text-sm bg-white focus:outline-none focus:ring-1 focus:ring-zinc-900"
                />
                {showBookList && (
                  <ul className="absolute z-20 top-full left-0 right-0 mt-1 max-h-44 overflow-y-auto bg-white border border-zinc-200 rounded shadow-sm text-sm">
                    {availableBooks.length === 0 ? (
                      <li className="px-3 py-2 text-zinc-400">Sin resultados</li>
                    ) : availableBooks.map(b => (
                      <li key={b.id}
                        onMouseDown={() => { setForm(f => ({ ...f, bookId: b.id! })); setBookQuery(''); setShowBookList(false) }}
                        className={`px-3 py-2 cursor-pointer flex justify-between items-center
                          ${(b.availableQuantity ?? 0) === 0 ? 'opacity-40 cursor-not-allowed' : 'hover:bg-zinc-50'}`}>
                        <span>{b.title}</span>
                        <span className="text-xs text-zinc-400">{b.availableQuantity ?? 0} disp.</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
          </div>
          <div className="flex gap-2 justify-end">
            <button type="button" onClick={() => { setShowForm(false); setUserQuery(''); setBookQuery(''); setForm(EMPTY_LOAN) }}
              className="px-3 py-1.5 text-sm border border-zinc-300 rounded hover:bg-zinc-100">
              Cancelar
            </button>
            <button type="submit" disabled={saving}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-zinc-900 text-white text-sm rounded hover:bg-zinc-700 disabled:opacity-60">
              <Check size={14} /> {saving ? 'Guardando…' : 'Registrar'}
            </button>
          </div>
        </form>
      )}

      <div className="relative">
        <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" />
        <input
          type="text"
          placeholder="Buscar por usuario, libro o estado…"
          value={query}
          onChange={e => setQuery(e.target.value)}
          className="w-full pl-9 pr-4 py-2 border border-zinc-200 rounded text-sm bg-white focus:outline-none focus:ring-1 focus:ring-zinc-900 placeholder:text-zinc-400"
        />
        {query && (
          <button onClick={() => setQuery('')} className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-700">
            <X size={13} />
          </button>
        )}
      </div>

      {loading ? (
        <p className="text-sm text-zinc-400">Cargando…</p>
      ) : (
        <div className="border border-zinc-200 rounded overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-zinc-50 border-b border-zinc-200">
              <tr>
                {['ID', 'Usuario', 'Libro', 'Fecha préstamo', 'Fecha devolución', 'Estado', ''].map(h => (
                  <th key={h} className="px-4 py-2.5 text-left text-xs font-semibold text-zinc-500 uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {loans.length === 0 ? (
                <tr><td colSpan={7} className="px-4 py-8 text-center text-zinc-400">Sin registros</td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={7} className="px-4 py-8 text-center text-zinc-400">Sin resultados para esa búsqueda.</td></tr>
              ) : filtered.map(l => (
                <tr key={l.id} className="hover:bg-zinc-50">
                  <td className="px-4 py-3 text-zinc-400 font-mono text-xs">{l.id}</td>
                  <td className="px-4 py-3 font-medium text-zinc-900">{l.userName ?? l.userId}</td>
                  <td className="px-4 py-3 text-zinc-600">{l.bookTitle ?? l.bookId}</td>
                  <td className="px-4 py-3 font-mono text-xs text-zinc-500">{l.loanDate}</td>
                  <td className="px-4 py-3 font-mono text-xs text-zinc-400">{l.returnDate ?? '—'}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${statusBadge(l.status)}`}>
                      {l.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    {l.status === 'ACTIVE' && (
                      <button
                        onClick={() => handleReturn(l.id!)}
                        className="flex items-center gap-1 px-2.5 py-1 border border-zinc-300 rounded text-xs hover:bg-zinc-100 text-zinc-600"
                      >
                        <RotateCcw size={11} /> Devolver
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
