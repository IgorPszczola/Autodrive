<script setup lang="ts">
const { login, authMessage, clearAuthMessage } = useAuth()
const router = useRouter()

const form = reactive({
  email: '',
  password: '',
})

const loading = ref(false)
const errorMessage = ref('')

async function submit() {
  clearAuthMessage()
  errorMessage.value = ''

  if (!form.email.trim() || !form.password.trim()) {
    errorMessage.value = 'Uzupełnij email i hasło.'
    return
  }

  loading.value = true

  try {
    await login({
      email: form.email,
      password: form.password,
    })
    await router.push('/')
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Logowanie nie powiodło się.'
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <v-container class="py-10" max-width="520">
    <v-card>
      <v-card-title>Logowanie</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="submit">
          <v-alert
            v-if="authMessage"
            type="warning"
            variant="tonal"
            class="mb-4"
          >
            {{ authMessage }}
          </v-alert>

          <v-text-field
            v-model="form.email"
            label="Email"
            type="email"
            required
          />
          <v-text-field
            v-model="form.password"
            label="Hasło"
            type="password"
            required
          />

          <v-alert
            v-if="errorMessage"
            type="error"
            variant="tonal"
            class="mb-4"
          >
            {{ errorMessage }}
          </v-alert>

          <v-btn
            type="submit"
            color="primary"
            :loading="loading"
            block
          >
            Zaloguj się
          </v-btn>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-btn to="/register" variant="text">
          Nie masz konta? Zarejestruj się
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>
