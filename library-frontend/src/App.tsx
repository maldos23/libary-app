import { useState } from 'react'
import { BooksPanel } from './components/BooksPanel'
import { UsersPanel } from './components/UsersPanel'
import { LoansPanel } from './components/LoansPanel'
import { BookOpen, Users, BookMarked } from 'lucide-react'

type Tab = 'books' | 'users' | 'loans'

export default function App() {
  const [activeTab, setActiveTab] = useState<Tab>('books')

  const tabs: { id: Tab; label: string; icon: React.ReactNode }[] = [
    { id: 'books', label: 'Libros', icon: <BookOpen size={16} /> },
    { id: 'users', label: 'Usuarios', icon: <Users size={16} /> },
    { id: 'loans', label: 'Préstamos', icon: <BookMarked size={16} /> },
  ]

  return (
    <div className="min-h-screen bg-white">
      {/* ── Header ─────────────────────────────────────────────────────── */}
      <header className="border-b border-zinc-200">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <BookOpen size={20} className="text-zinc-900" />
            <span className="text-sm font-semibold tracking-tight text-zinc-900">
              Library
            </span>
          </div>
          <span className="hidden sm:inline text-xs text-zinc-400">Sistema de Gestión de Biblioteca</span>
        </div>
      </header>

      {/* ── Tab Navigation ─────────────────────────────────────────────── */}
      <div className="border-b border-zinc-200">
        <div className="max-w-5xl mx-auto px-4 sm:px-6">
          <nav className="flex">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`
                  flex-1 sm:flex-none flex items-center justify-center gap-1.5
                  px-3 sm:px-4 py-3 text-sm font-medium
                  border-b-2 transition-colors
                  ${activeTab === tab.id
                    ? 'border-zinc-900 text-zinc-900'
                    : 'border-transparent text-zinc-500 hover:text-zinc-700'}
                `}
              >
                {tab.icon}
                {tab.label}
              </button>
            ))}
          </nav>
        </div>
      </div>

      {/* ── Content ────────────────────────────────────────────────────── */}
      <main className="max-w-5xl mx-auto px-4 sm:px-6 py-6 sm:py-8">
        {activeTab === 'books' && <BooksPanel />}
        {activeTab === 'users' && <UsersPanel />}
        {activeTab === 'loans' && <LoansPanel />}
      </main>
    </div>
  )
}
