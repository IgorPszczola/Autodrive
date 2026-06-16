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

const formRef = ref<any>(null)
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const rules = {
  required: (value: any) => !!value || 'Pole jest wymagane.',
  email: (value: any) => {
    const pattern = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
    return pattern.test(value) || 'Niepoprawny adres email.'
  },
  password: (value: any) => (value && value.length >= 8) || 'Hasło musi mieć minimum 8 znaków.',
  phone: (value: any) => {
    const pattern = /^[0-9]{9}$/
    return pattern.test(value) || 'Numer telefonu musi składać się z dokładnie 9 cyfr.'
  },
}

async function submit() {
  errorMessage.value = ''
  successMessage.value = ''

  if (!formRef.value) return

  const { valid } = await formRef.value.validate()
  if (!valid) {
    errorMessage.value = 'Popraw błędy w formularzu przed rejestracją.'
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
          <v-form ref="formRef" @submit.prevent="submit">
            <v-row class="ma-0">
              <v-col cols="12" md="6" class="pa-0 pr-md-2 pb-2">
                <v-text-field
                  v-model="form.firstName"
                  label="Imię"
                  prepend-inner-icon="mdi-account-outline"
                  variant="outlined"
                  color="primary"
                  required
                  :rules="[rules.required]"
                />
              </v-col>

              <v-col cols="12" md="6" class="pa-0 pl-md-2 pb-2">
                <v-text-field
                  v-model="form.lastName"
                  label="Nazwisko"
                  prepend-inner-icon="mdi-account-outline"
                  variant="outlined"
                  color="primary"
                  required
                  :rules="[rules.required]"
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
              :rules="[rules.required, rules.email]"
              class="mb-2"
            />

            <v-text-field
              v-model="form.password"
              label="Hasło"
              type="password"
              prepend-inner-icon="mdi-lock-outline"
              variant="outlined"
              color="primary"
              required
              :rules="[rules.required, rules.password]"
              class="mb-2"
            />

            <v-text-field
              v-model="form.driverLicenseNumber"
              label="Numer prawa jazdy"
              prepend-inner-icon="mdi-card-account-details-outline"
              variant="outlined"
              color="primary"
              required
              :rules="[rules.required]"
              class="mb-2"
            />

            <v-text-field
              v-model="form.phoneNumber"
              label="Numer telefonu"
              prepend-inner-icon="mdi-phone-outline"
              variant="outlined"
              color="primary"
              required
              :rules="[rules.required, rules.phone]"
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
