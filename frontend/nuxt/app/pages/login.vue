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
  <div class="gradient-hero min-h-screen d-flex align-center justify-center py-12 animate-fade-in">
    <v-container max-width="480">
      <v-card class="pa-8 glass-card" rounded="xl" variant="flat">
        <div class="text-center mb-6">
          <h1 class="text-3xl font-weight-black text-white mb-2">
            Witaj z powrotem
          </h1>
          <p class="text-medium-emphasis text-sm">
            Zaloguj się do swojego konta Autodrive
          </p>
        </div>

        <v-card-text class="pa-0">
          <v-form @submit.prevent="submit">
            <v-alert
              v-if="authMessage"
              type="warning"
              variant="tonal"
              class="mb-4"
              rounded="lg"
            >
              {{ authMessage }}
            </v-alert>

            <v-text-field
              v-model="form.email"
              label="Adres email"
              type="email"
              prepend-inner-icon="mdi-email-outline"
              variant="outlined"
              color="primary"
              required
              class="mb-3"
            />

            <v-text-field
              v-model="form.password"
              label="Hasło"
              type="password"
              prepend-inner-icon="mdi-lock-outline"
              variant="outlined"
              color="primary"
              required
              class="mb-4"
            />

            <v-alert
              v-if="errorMessage"
              type="error"
              variant="tonal"
              class="mb-4"
              rounded="lg"
            >
              {{ errorMessage }}
            </v-alert>

            <v-btn
              type="submit"
              color="primary"
              :loading="loading"
              block
              size="large"
              variant="flat"
              class="font-weight-semibold"
              height="50"
            >
              Zaloguj się
            </v-btn>
          </v-form>
        </v-card-text>

        <v-card-actions class="pa-0 pt-6 justify-center">
          <v-btn
            to="/register"
            variant="text"
            color="primary"
            class="text-sm font-weight-medium"
          >
            Nie masz konta? Zarejestruj się
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-container>
  </div>
</template>
