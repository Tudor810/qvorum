import { Outlet } from 'react-router'
// import { strings } from '@qvorum/shared'

export function AuthLayout() {
  return (
    <main className="container stack" style={{ maxWidth: 420, paddingTop: 96 }}>
      {/* <h1 style={{ color: 'var(--color-primary)' }}>{strings.app.name}</h1> */}
      <Outlet />
    </main>
  )
}