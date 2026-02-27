import { useEffect, useState } from 'react'
import { booksApi, BookDTO } from '../lib/api'
import { Plus, Pencil, Trash2, X, Check, Search } from 'lucide-react'

const EMPTY: BookDTO = { title: '', author: '', isbn: '', totalQuantity: 1 }

export function BooksPanel() {
  const [books, setBooks] = useState<BookDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<BookDTO | null>(null)
  const [form, setForm] = useState<BookDTO>(EMPTY)
  const [saving, setSaving] = useState(false)
  const [query, setQuery] = useState('')

  const load = async () => {
    try {
      setLoading(true)
      setBooks(await booksApi.list())
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const openCreate = () => { setEditing(null); setForm(EMPTY); setShowForm(true) }
  const openEdit = (b: BookDTO) => { setEditing(b); setForm({ ...b }); setShowForm(true) }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      setSaving(true)
      if (editing?.id) {
        await booksApi.update(editing.id, form)
      } else {
        await booksApi.create(form)
      }
      setShowForm(false)
      await load()
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('¿Eliminar este libro?')) return
    try {
      await booksApi.delete(id)
      await load()
    } catch (e) {
      setError((e as Error).message)
    }
  }

  const filtered = books.filter(b => {
    const q = query.toLowerCase()
    return (
      b.title.toLowerCase().includes(q) ||
      b.author.toLowerCase().includes(q) ||
      b.isbn.toLowerCase().includes(q)
    )
  })

  return (
    <div className="space-y-6">
      {/* Header row */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold text-zinc-900">Catálogo de Libros</h2>
          <p className="text-sm text-zinc-500 mt-0.5">
            {filtered.length} de {books.length} registros
          </p>
        </div>
        <button
          onClick={openCreate}
          className="flex items-center gap-1.5 px-3 py-1.5 bg-zinc-900 text-white text-sm font-medium rounded hover:bg-zinc-700 transition-colors"
        >
          <Plus size={14} /> Agregar
        </button>
      </div>

      {/* Search */}
      <div className="relative">
        <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" />
        <input
          type="text"
          placeholder="Buscar por título, autor o ISBN…"
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

      {/* Error */}
      {error && (
        <div className="flex items-center justify-between px-4 py-2.5 border border-red-200 bg-red-50 rounded text-sm text-red-600">
          {error}
          <button onClick={() => setError(null)}><X size={14} /></button>
        </div>
      )}

      {/* Form */}
      {showForm && (
        <form onSubmit={handleSubmit} className="border border-zinc-200 rounded p-5 space-y-4 bg-zinc-50">
          <h3 className="text-sm font-semibold text-zinc-800">
            {editing ? 'Editar libro' : 'Nuevo libro'}
          </h3>
          <div className="grid grid-cols-2 gap-4">
            {[
              { label: 'Título', key: 'title', type: 'text' },
              { label: 'Autor', key: 'author', type: 'text' },
              { label: 'ISBN', key: 'isbn', type: 'text' },
              { label: 'Cantidad total', key: 'totalQuantity', type: 'number' },
            ].map(({ label, key, type }) => (
              <div key={key} className="space-y-1">
                <label className="text-xs font-medium text-zinc-600">{label}</label>
                <input
                  type={type}
                  value={(form as Record<string, unknown>)[key] as string ?? ''}
                  min={type === 'number' ? 1 : undefined}
                  onChange={e =>
                    setForm(f => ({
                      ...f,
                      [key]: type === 'number' ? Number(e.target.value) : e.target.value,
                    }))
                  }
                  required
                  className="w-full px-3 py-1.5 border border-zinc-300 rounded text-sm bg-white focus:outline-none focus:ring-1 focus:ring-zinc-900"
                />
              </div>
            ))}
          </div>
          <div className="flex gap-2 justify-end">
            <button type="button" onClick={() => setShowForm(false)}
              className="px-3 py-1.5 text-sm border border-zinc-300 rounded hover:bg-zinc-100">
              Cancelar
            </button>
            <button type="submit" disabled={saving}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-zinc-900 text-white text-sm rounded hover:bg-zinc-700 disabled:opacity-60">
              <Check size={14} /> {saving ? 'Guardando…' : 'Guardar'}
            </button>
          </div>
        </form>
      )}

      {/* Table */}
      {loading ? (
        <p className="text-sm text-zinc-400">Cargando…</p>
      ) : (
        <div className="border border-zinc-200 rounded overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-zinc-50 border-b border-zinc-200">
              <tr>
                {['ID', 'Título', 'Autor', 'ISBN', 'Total', 'Disponibles', ''].map(h => (
                  <th key={h} className="px-4 py-2.5 text-left text-xs font-semibold text-zinc-500 uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {filtered.length === 0 ? (
                <tr><td colSpan={7} className="px-4 py-8 text-center text-zinc-400">
                  {query ? 'Sin resultados para esa búsqueda.' : 'Sin registros'}
                </td></tr>
              ) : filtered.map(b => (
                <tr key={b.id} className="hover:bg-zinc-50">
                  <td className="px-4 py-3 text-zinc-400 font-mono text-xs">{b.id}</td>
                  <td className="px-4 py-3 font-medium text-zinc-900">{b.title}</td>
                  <td className="px-4 py-3 text-zinc-600">{b.author}</td>
                  <td className="px-4 py-3 font-mono text-xs text-zinc-500">{b.isbn}</td>
                  <td className="px-4 py-3 text-zinc-600">{b.totalQuantity}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium
                      ${(b.availableQuantity ?? 0) > 0 ? 'bg-zinc-100 text-zinc-700' : 'bg-red-50 text-red-600'}`}>
                      {b.availableQuantity}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1 justify-end">
                      <button onClick={() => openEdit(b)} className="p-1.5 hover:bg-zinc-100 rounded text-zinc-500 hover:text-zinc-900">
                        <Pencil size={13} />
                      </button>
                      <button onClick={() => handleDelete(b.id!)} className="p-1.5 hover:bg-red-50 rounded text-zinc-500 hover:text-red-600">
                        <Trash2 size={13} />
                      </button>
                    </div>
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
