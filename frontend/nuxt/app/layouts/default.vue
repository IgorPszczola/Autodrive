<script setup lang="ts">
const {
  authMessage,
  isAuthenticated,
  isAdmin,
  clearAuthMessage,
  logout,
} = useAuth()
const router = useRouter()

async function handleLogout() {
  logout()
  await router.push('/login')
}
</script>

<template>
  <v-app>
    <v-app-bar>
      <v-app-bar-title>
        <NuxtLink class="decoration-none color-white" to="/">Autodrive</NuxtLink>
      </v-app-bar-title>
      <v-spacer />
      <v-btn to="/" variant="text">
        Start
      </v-btn>
      <v-btn to="/cars" variant="text">
        Katalog
      </v-btn>
      <v-btn
        v-if="!isAuthenticated"
        to="/login"
        variant="text"
      >
        Logowanie
      </v-btn>
      <v-btn
        v-if="!isAuthenticated"
        to="/register"
        variant="text"
      >
        Rejestracja
      </v-btn>
      <v-btn
        v-if="isAuthenticated"
        to="/client/reservations/new"
        variant="text"
      >
        Rezerwacja
      </v-btn>
      <v-btn
        v-if="isAuthenticated"
        to="/client/account"
        variant="text"
      >
        Moje konto
      </v-btn>
      <v-btn
        v-if="isAuthenticated && isAdmin"
        to="/admin"
        variant="text"
      >
        Admin
      </v-btn>
      <v-btn
        v-if="isAuthenticated"
        variant="text"
        @click="handleLogout"
      >
        Wyloguj
      </v-btn>
    </v-app-bar>

    <v-main>
      <v-container v-if="authMessage" class="pt-4 pb-0">
        <v-alert
          type="warning"
          variant="tonal"
          closable
          @click:close="clearAuthMessage"
        >
          {{ authMessage }}
        </v-alert>
      </v-container>
      <slot />
    </v-main>
  </v-app>
</template>
