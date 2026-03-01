import { useEffect, useState } from 'react'
import { usersApi, UserDTO } from '../lib/api'
import { Plus, Pencil, Trash2, X, Check, Search } from 'lucide-react'

const EMPTY: UserDTO = { name: '', identificationDocument: '', email: '' }

export function UsersPanel() {
  const [users, setUsers] = useState<UserDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<UserDTO | null>(null)
  const [form, setForm] = useState<UserDTO>(EMPTY)
  const [saving, setSaving] = useState(false)
  const [query, setQuery] = useState('')

  const load = async () => {
    try {
      setLoading(true)
      setUsers(await usersApi.list())
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const openCreate = () => { setEditing(null); setForm(EMPTY); setShowForm(true) }
  const openEdit = (u: UserDTO) => { setEditing(u); setForm({ ...u }); setShowForm(true) }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      setSaving(true)
      if (editing?.id) {
        await usersApi.update(editing.id, form)
      } else {
        await usersApi.create(form)
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
    if (!confirm('¿Eliminar este usuario?')) return
    try {
      await usersApi.delete(id)
      await load()
    } catch (e) {
      setError((e as Error).message)
    }
  }

  const filtered = users.filter(u => {
    const q = query.toLowerCase()
    return u.name.toLowerCase().includes(q) ||
      u.identificationDocument.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q)
  })

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div className="min-w-0">
          <h2 className="text-lg font-semibold text-zinc-900 truncate">Usuarios</h2>
          <p className="text-sm text-zinc-500 mt-0.5">{filtered.length} de {users.length} registros</p>
        </div>
        <button
          onClick={openCreate}
          className="shrink-0 flex items-center gap-1.5 px-3 py-1.5 bg-zinc-900 text-white text-sm font-medium rounded hover:bg-zinc-700 transition-colors"
        >
          <Plus size={14} /> <span className="hidden xs:inline">Agregar</span>
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
          <h3 className="text-sm font-semibold text-zinc-800">
            {editing ? 'Editar usuario' : 'Nuevo usuario'}
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {[
              { label: 'Nombre completo', key: 'name', type: 'text' },
              { label: 'Documento', key: 'identificationDocument', type: 'text' },
              { label: 'Email', key: 'email', type: 'email' },
            ].map(({ label, key, type }) => (
              <div key={key} className="space-y-1">
                <label className="text-xs font-medium text-zinc-600">{label}</label>
                <input
                  type={type}
                  value={(form as unknown as Record<string, unknown>)[key] as string ?? ''}
                  onChange={e => setForm(f => ({ ...f, [key]: e.target.value }))}
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

      <div className="relative">
        <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" />
        <input
          type="text"
          placeholder="Buscar por nombre, documento o email…"
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
        <div className="border border-zinc-200 rounded overflow-x-auto">
          <table className="w-full min-w-[580px] text-sm">
            <thead className="bg-zinc-50 border-b border-zinc-200">
              <tr>
                {['ID', 'Nombre', 'Documento', 'Email', 'Préstamos activos', ''].map(h => (
                  <th key={h} className="px-4 py-2.5 text-left text-xs font-semibold text-zinc-500 uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {users.length === 0 ? (
                <tr><td colSpan={6} className="px-4 py-8 text-center text-zinc-400">Sin registros</td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={6} className="px-4 py-8 text-center text-zinc-400">Sin resultados para esa búsqueda.</td></tr>
              ) : filtered.map(u => (
                <tr key={u.id} className="hover:bg-zinc-50">
                  <td className="px-4 py-3 text-zinc-400 font-mono text-xs">{u.id}</td>
                  <td className="px-4 py-3 font-medium text-zinc-900">{u.name}</td>
                  <td className="px-4 py-3 font-mono text-xs text-zinc-500">{u.identificationDocument}</td>
                  <td className="px-4 py-3 text-zinc-600">{u.email}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium
                      ${(u.activeLoans ?? 0) >= 3 ? 'bg-red-50 text-red-600' : 'bg-zinc-100 text-zinc-700'}`}>
                      {u.activeLoans ?? 0} / 3
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1 justify-end">
                      <button onClick={() => openEdit(u)} className="p-1.5 hover:bg-zinc-100 rounded text-zinc-500 hover:text-zinc-900">
                        <Pencil size={13} />
                      </button>
                      <button onClick={() => handleDelete(u.id!)} className="p-1.5 hover:bg-red-50 rounded text-zinc-500 hover:text-red-600">
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
