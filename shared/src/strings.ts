// All visible texts live here, so they can be reviewed in one place.
export const strings = {
  app: {
    name: 'Qvorum',
  },
  common: {
    save: 'Save',
    cancel: 'Cancel',
    delete: 'Delete',
    back: 'Back',
    loading: 'Loading…',
    genericError: 'Something went wrong. Please try again.',
  },
  auth: {
    title: 'Log in',
    email: 'Email',
    password: 'Password',
    logIn: 'Log in',
    logOut: 'Log out',
    emailRequired: 'Enter your email address.',
    emailInvalid: 'Enter a valid email address.',
    passwordRequired: 'Enter your password.',
    networkError: "Can't reach the server. Check your connection and try again.",
    invalidCredentials: 'Incorrect email or password.',
    invalidInput: 'Please check your details and try again.',
    tooManyAttempts: 'Too many attempts. Please wait a moment and try again.',
  },
} as const
