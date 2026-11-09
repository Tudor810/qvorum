import { strings } from '@qvorum/shared'
import { LoginForm } from '../features/auth'

function App() {
  return (
    <main className="container stack" style={{ maxWidth: 420, paddingTop: 96 }}>
      <h1 style={{ color: 'var(--color-primary)' }}>{strings.app.name}</h1>
      <LoginForm />
    </main>
  )
}

export default App
