interface ApiRequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  query?: Record<string, string | number | boolean | undefined>
  headers?: HeadersInit
  auth?: boolean
  accessToken?: string | null
}

interface ApiErrorShape {
  message?: unknown
  status?: unknown
  statusCode?: unknown
  data?: { message?: unknown, error?: unknown }
  response?: {
    status?: unknown
    _data?: { message?: unknown, error?: unknown }
  }
}

function normalizeAccessToken(token: string): string {
  return token.replace(/^Bearer\s+/i, '').trim()
}

function extractBackendMessage(error: ApiErrorShape): string | null {
  if (typeof error.data?.message === 'string' && error.data.message.trim()) {
    return error.data.message
  }

  if (typeof error.data?.error === 'string' && error.data.error.trim()) {
    return error.data.error
  }

  if (typeof error.response?._data?.message === 'string' && error.response._data.message.trim()) {
    return error.response._data.message
  }

  if (typeof error.response?._data?.error === 'string' && error.response._data.error.trim()) {
    return error.response._data.error
  }

  return null
}

function normalizeApiError(error: unknown): Error {
  if (error instanceof Error) {
    const typed = error as Error & ApiErrorShape
    const backendMessage = extractBackendMessage(typed)

    if (backendMessage) {
      const normalized = new Error(backendMessage) as Error & ApiErrorShape
      normalized.status = typed.status
      normalized.statusCode = typed.statusCode
      normalized.data = typed.data
      normalized.response = typed.response

      return normalized
    }

    return error
  }

  return new Error('Wystapil nieznany blad API.')
}

export function apiRequest<TResponse>(
  path: string,
  options: ApiRequestOptions = {},
): Promise<TResponse> {
  const token = useCookie<string | null>('auth_token')
  const headers = new Headers(options.headers)
  const shouldUseAuth = options.auth !== false
  const currentToken = options.accessToken ?? token.value

  if (shouldUseAuth && typeof currentToken === 'string' && currentToken.trim()) {
    headers.set('Authorization', `Bearer ${normalizeAccessToken(currentToken)}`)
  }

  return $fetch<TResponse>(path, {
    method: options.method ?? 'GET',
    body: options.body as BodyInit | Record<string, any> | null | undefined,
    query: options.query,
    headers,
  }).catch((error: unknown) => {
    throw normalizeApiError(error)
  })
}
