// View: renders the form and forwards user input to the view model.
import { strings } from '@qvorum/shared'
import { useState} from 'react'
import { useLogin } from '../hooks/useLogin'

export function LoginForm() {
  const { loading, error, submit } = useLogin()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  function handleSubmit() {
    void submit({ email, password })
  }

  return (
    <div className="card stack">
      <h2>{strings.auth.title}</h2>
      <label className="field">
        <span className="label">{strings.auth.email}</span>
        <input className="input" type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} />
      </label>
      <label className="field">
        <span className="label">{strings.auth.password}</span>
        <input className="input" type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} />
      </label>
      {error && <p className="alert alert-danger" role="alert">{error}</p>}
      <button onClick = {handleSubmit} className="btn btn-primary btn-block" disabled={loading}>
        {strings.auth.logIn}
      </button>
    </div>
  )
}
