// View: renders the form and forwards user input to the view model.
import { strings } from '@qvorum/shared'
import { useState, type FormEvent } from 'react'
import { useLogin } from '../hooks/useLogin'

export function LoginForm() {
  const { loading, error, submit } = useLogin()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    void submit({ email, password })
  }

  return (
    <form onSubmit={handleSubmit}>
      <h2>{strings.auth.title}</h2>
      <label>
        {strings.auth.email}
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
      </label>
      <label>
        {strings.auth.password}
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
      </label>
      {error && <p role="alert">{error}</p>}
      <button type="submit" disabled={loading}>
        {strings.auth.logIn}
      </button>
    </form>
  )
}
