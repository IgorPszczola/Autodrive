<script setup lang="ts">
const { register } = useAuth()
const router = useRouter()

const form = reactive({
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  driverLicenseNumber: '',
  phoneNumber: '',
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

async function submit() {
  errorMessage.value = ''
  successMessage.value = ''

  if (
    !form.email.trim()
    || !form.password.trim()
    || !form.firstName.trim()
    || !form.lastName.trim()
    || !form.driverLicenseNumber.trim()
    || !form.phoneNumber.trim()
  ) {
    errorMessage.value = 'Uzupełnij wszystkie wymagane pola formularza.'
    return
  }

  loading.value = true

  try {
    await register({
      email: form.email,
      password: form.password,
      firstName: form.firstName,
      lastName: form.lastName,
      driverLicenseNumber: form.driverLicenseNumber,
      phoneNumber: form.phoneNumber,
    })
    successMessage.value = 'Rejestracja zakończona sukcesem. Możesz się zalogować.'
    await router.push('/login')
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Rejestracja nie powiodła się'
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <v-container class="py-10" max-width="640">
    <v-card class="pa-4">
      <v-card-title>Rejestracja</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="submit">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.firstName"
                label="Imię"
                required
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.lastName"
                label="Nazwisko"
                required
              />
            </v-col>
          </v-row>

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
          <v-text-field
            v-model="form.driverLicenseNumber"
            label="Numer prawa jazdy"
            required
          />
          <v-text-field
            v-model="form.phoneNumber"
            label="Numer telefonu"
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

          <v-alert
            v-if="successMessage"
            type="success"
            variant="tonal"
            class="mb-4"
          >
            {{ successMessage }}
          </v-alert>

          <v-btn
            type="submit"
            color="primary"
            :loading="loading"
            block
          >
            Utwórz konto
          </v-btn>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-btn to="/login" variant="text">
          Masz już konto? Zaloguj się
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>
