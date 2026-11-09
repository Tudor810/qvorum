import { useId, useState } from 'react'
import { strings } from '@qvorum/shared'
import { useLogin } from '../hooks/useLogin'

export function LoginForm() {
  const { loading, error, fieldErrors, submit } = useLogin()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const id = useId()
  const emailErrorId = `${id}-email-error`
  const passwordErrorId = `${id}-password-error`
  
  return (
    <form className="card stack" onSubmit={(e) => {
      e.preventDefault()
      void submit({email, password})
    }} noValidate>
      <h2>{strings.auth.title}</h2>

      <label className="field">
        <span className="label">{strings.auth.email}</span>
        <input
          className="input"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          aria-invalid={fieldErrors.email ? true : undefined}
          aria-describedby={fieldErrors.email ? emailErrorId : undefined}
        />
        {fieldErrors.email && (
          <span id={emailErrorId} className="error">
            {fieldErrors.email}
          </span>
        )}
      </label>

      <label className="field">
        <span className="label">{strings.auth.password}</span>
        <input
          className="input"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          aria-invalid={fieldErrors.password ? true : undefined}
          aria-describedby={fieldErrors.password ? passwordErrorId : undefined}
        />
        {fieldErrors.password && (
          <span id={passwordErrorId} className="error">
            {fieldErrors.password}
          </span>
        )}
      </label>

      {error && (
        <p className="alert alert-danger" role="alert">
          {error}
        </p>
      )}

      <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
        {strings.auth.logIn}
      </button>
    </form>
  )
}