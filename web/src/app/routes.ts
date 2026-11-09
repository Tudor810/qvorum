import { createBrowserRouter, redirect } from 'react-router'
import { AuthLayout } from './AuthLayout'
import { LoginForm, ChangePasswordForm } from '../features/auth'

export const router = createBrowserRouter([
  {
    Component: AuthLayout,
    children: [
      { path: '/login', Component: LoginForm },
      { path: '/change-password', Component: ChangePasswordForm },
    ],
  },
  { path: '/', loader: () => redirect('/login') },
  { path: '*', loader: () => redirect('/login') },
])