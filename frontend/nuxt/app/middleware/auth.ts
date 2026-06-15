export default defineNuxtRouteMiddleware(async () => {
  const { token, user, isAuthenticated, initializeAuth } = useAuth()

  if (token.value && !user.value) {
    await initializeAuth()
  }

  if (!isAuthenticated.value) {
    return navigateTo('/login')
  }

  return undefined
})
