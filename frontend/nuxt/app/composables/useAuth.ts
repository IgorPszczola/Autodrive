import { computed } from 'vue'

interface RegisterPayload {
  email: string
  password: string
  firstName: string
  lastName: string
  driverLicenseNumber: string
  phoneNumber: string
}

interface LoginPayload {
  email: string
  password: string
}

interface AuthUser {
  id: number | string
  email: string
  firstName: string
  lastName: string
  role: string
  isActive: boolean
}

type AuthResponse = Record<string, unknown>
const INACTIVE_ACCOUNT_MESSAGE = 'Konto jest nieaktywne. Skontaktuj się z administratorem.'
const FORBIDDEN_PROFILE_MESSAGE = 'Brak dostępu do profilu użytkownika (403).'

let initializeAuthPromise: Promise<void> | null = null

function extractToken(payload: AuthResponse): string | null {
  const tokenCandidate = payload.accessToken ?? payload.token ?? payload.jwt

  return typeof tokenCandidate === 'string' && tokenCandidate.length > 0
    ? tokenCandidate.replace(/^Bearer\s+/i, '').trim()
    : null
}

function getErrorStatusCode(error: unknown): number | null {
  if (!error || typeof error !== 'object') {
    return null
  }

  const maybeError = error as {
    status?: unknown
    statusCode?: unknown
    response?: { status?: unknown }
  }

  if (typeof maybeError.status === 'number') {
    return maybeError.status
  }

  if (typeof maybeError.statusCode === 'number') {
    return maybeError.statusCode
  }

  if (typeof maybeError.response?.status === 'number') {
    return maybeError.response.status
  }

  return null
}

function getErrorMessage(error: unknown): string | null {
  if (!error || typeof error !== 'object') {
    return null
  }

  const maybeError = error as {
    data?: { error?: unknown, message?: unknown }
    response?: { _data?: { error?: unknown, message?: unknown } }
    message?: unknown
  }

  const fromData = maybeError.data
  if (fromData) {
    if (typeof fromData.error === 'string' && fromData.error.trim()) {
      return fromData.error
    }

    if (typeof fromData.message === 'string' && fromData.message.trim()) {
      return fromData.message
    }
  }

  const fromResponseData = maybeError.response?._data
  if (fromResponseData) {
    if (typeof fromResponseData.error === 'string' && fromResponseData.error.trim()) {
      return fromResponseData.error
    }

    if (typeof fromResponseData.message === 'string' && fromResponseData.message.trim()) {
      return fromResponseData.message
    }
  }

  if (typeof maybeError.message === 'string' && maybeError.message.trim()) {
    return maybeError.message
  }

  return null
}

export function useAuth() {
  const token = useCookie<string | null>('auth_token', {
    default: () => null,
    sameSite: 'lax',
  })
  const user = useState<AuthUser | null>('auth_user', () => null)
  const authMessage = useState<string | null>('auth_message', () => null)
  const isAuthInitializing = useState<boolean>('auth_initializing', () => false)

  const isAuthenticated = computed(() => Boolean(token.value && user.value))
  const isAdmin = computed(() => /ADMIN/i.test(user.value?.role ?? ''))

  function clearAuthMessage() {
    authMessage.value = null
  }

  function clearAuthState(message: string | null = null) {
    token.value = null
    user.value = null
    authMessage.value = message
  }

  async function register(payload: RegisterPayload) {
    return await apiRequest<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: payload,
      auth: false,
    })
  }

  async function fetchMe(accessToken?: string | null) {
    const tokenToUse = accessToken ?? token.value

    if (!tokenToUse) {
      user.value = null

      return null
    }

    try {
      const me = await apiRequest<AuthUser>('/api/auth/me', {
        accessToken: tokenToUse,
      })

      if (!me.isActive) {
        clearAuthState(INACTIVE_ACCOUNT_MESSAGE)
        throw new Error(INACTIVE_ACCOUNT_MESSAGE)
      }

      user.value = me

      return me
    }
    catch (error) {
      const statusCode = getErrorStatusCode(error)

      if (statusCode === 401) {
        clearAuthState('Sesja wygasła. Zaloguj się ponownie.')
      }

      if (statusCode === 403) {
        clearAuthState(FORBIDDEN_PROFILE_MESSAGE)
      }

      throw error instanceof Error
        ? error
        : new Error('Nie udało się pobrać danych użytkownika.')
    }
  }

  async function initializeAuth() {
    if (!token.value || user.value) {
      return
    }

    if (initializeAuthPromise) {
      await initializeAuthPromise

      return
    }

    isAuthInitializing.value = true
    initializeAuthPromise = (async () => {
      try {
        await fetchMe()
      }
      catch {
      }
      finally {
        isAuthInitializing.value = false
        initializeAuthPromise = null
      }
    })()

    await initializeAuthPromise
  }

  async function login(payload: LoginPayload) {
    clearAuthMessage()

    let response: AuthResponse
    try {
      response = await apiRequest<AuthResponse>('/api/auth/login', {
        method: 'POST',
        body: payload,
        auth: false,
      })
    }
    catch (error) {
      const message = getErrorMessage(error)
      throw new Error(message ?? 'Logowanie nie powiodło się.')
    }

    const parsedToken = extractToken(response)
    if (!parsedToken) {
      throw new Error('API login response does not contain a JWT token')
    }

    token.value = parsedToken
    await fetchMe(parsedToken)

    return response
  }

  function logout() {
    clearAuthState()
  }

  return {
    token,
    user,
    authMessage,
    isAuthInitializing,
    isAuthenticated,
    isAdmin,
    register,
    fetchMe,
    initializeAuth,
    login,
    logout,
    clearAuthMessage,
  }
}
