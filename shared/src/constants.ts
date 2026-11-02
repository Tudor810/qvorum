export const ROLES = {
  RESIDENT: 'RESIDENT',
  ADMINISTRATOR: 'ADMINISTRATOR',
  PRESIDENT: 'PRESIDENT',
} as const

export type Role = (typeof ROLES)[keyof typeof ROLES]
