import { strings } from '@qvorum/shared'
import { LoginForm } from '../features/auth'

function App() {
  return (
    <main>
      <h1>{strings.app.nume}</h1>
      <LoginForm />
    </main>
  )
}

export default App
