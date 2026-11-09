import { Link } from 'react-router'

export function ChangePasswordForm() {
  return (
    <div className="card stack">
      <h2>Sign up</h2>
      <p className="text-sm text-muted">
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </div>
  )
}