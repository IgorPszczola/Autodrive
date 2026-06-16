export default defineNuxtRouteMiddleware(async (to) => {
  const { token, user, isAuthenticated, initializeAuth } = useAuth()

  if (token.value && !user.value) {
    await initializeAuth()
  }

  if (!isAuthenticated.value) {
    return navigateTo(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
  }

  return undefined
})
