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
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Rejestracja nie powiodła się'
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="gradient-hero min-h-screen d-flex align-center justify-center py-12 animate-fade-in">
    <v-container max-width="600">
      <v-card class="pa-8 glass-card" rounded="xl" variant="flat">
        <div class="text-center mb-6">
          <h1 class="text-3xl font-weight-black text-white mb-2">
            Utwórz konto
          </h1>
          <p class="text-medium-emphasis text-sm">
            Dołącz do Autodrive i rezerwuj auta klasy premium
          </p>
        </div>

        <v-card-text class="pa-0">
          <v-form @submit.prevent="submit">
            <v-row class="ma-0">
              <v-col cols="12" md="6" class="pa-0 pr-md-2 pb-3">
                <v-text-field
                  v-model="form.firstName"
                  label="Imię"
                  prepend-inner-icon="mdi-account-outline"
                  variant="outlined"
                  color="primary"
                  required
                  hide-details
                />
              </v-col>

              <v-col cols="12" md="6" class="pa-0 pl-md-2 pb-3">
                <v-text-field
                  v-model="form.lastName"
                  label="Nazwisko"
                  prepend-inner-icon="mdi-account-outline"
                  variant="outlined"
                  color="primary"
                  required
                  hide-details
                />
              </v-col>
            </v-row>

            <v-text-field
              v-model="form.email"
              label="Adres email"
              type="email"
              prepend-inner-icon="mdi-email-outline"
              variant="outlined"
              color="primary"
              required
              class="mb-3 mt-3"
            />

            <v-text-field
              v-model="form.password"
              label="Hasło"
              type="password"
              prepend-inner-icon="mdi-lock-outline"
              variant="outlined"
              color="primary"
              required
              class="mb-3"
            />

            <v-text-field
              v-model="form.driverLicenseNumber"
              label="Numer prawa jazdy"
              prepend-inner-icon="mdi-card-account-details-outline"
              variant="outlined"
              color="primary"
              required
              class="mb-3"
            />

            <v-text-field
              v-model="form.phoneNumber"
              label="Numer telefonu"
              prepend-inner-icon="mdi-phone-outline"
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

            <v-alert
              v-if="successMessage"
              type="success"
              variant="tonal"
              class="mb-4"
              rounded="lg"
            >
              {{ successMessage }}
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
              Zarejestruj się
            </v-btn>
          </v-form>
        </v-card-text>

        <v-card-actions class="pa-0 pt-6 justify-center">
          <v-btn
            to="/login"
            variant="text"
            color="primary"
            class="text-sm font-weight-medium"
          >
            Masz już konto? Zaloguj się
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-container>
  </div>
</template>
