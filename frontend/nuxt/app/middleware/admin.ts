export default defineNuxtRouteMiddleware(async () => {
  const { token, user, isAuthenticated, isAdmin, authMessage, initializeAuth } = useAuth()

  if (token.value && !user.value) {
    await initializeAuth()
  }

  if (!isAuthenticated.value) {
    return navigateTo('/login')
  }

  if (!isAdmin.value) {
    authMessage.value = 'Brak uprawnien administratora.'

    return navigateTo('/')
  }

  return undefined
})
