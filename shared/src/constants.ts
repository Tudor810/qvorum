export const ROLURI = {
  LOCATAR: 'LOCATAR',
  ADMINISTRATOR: 'ADMINISTRATOR',
  PRESEDINTE: 'PRESEDINTE',
} as const

export type Rol = (typeof ROLURI)[keyof typeof ROLURI]
