// View: renders the form and forwards user input to the view model.
import { strings } from '@qvorum/shared'
import { useState, type FormEvent } from 'react'
import { useLogin } from '../hooks/useLogin'

export function LoginForm() {
  const { loading, error, submit } = useLogin()
  const [email, setEmail] = useState('')
  const [parola, setParola] = useState('')

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    void submit({ email, parola })
  }

  return (
    <form onSubmit={handleSubmit}>
      <h2>{strings.auth.titlu}</h2>
      <label>
        {strings.auth.email}
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
      </label>
      <label>
        {strings.auth.parola}
        <input type="password" value={parola} onChange={(e) => setParola(e.target.value)} />
      </label>
      {error && <p role="alert">{error}</p>}
      <button type="submit" disabled={loading}>
        {strings.auth.conectare}
      </button>
    </form>
  )
}
