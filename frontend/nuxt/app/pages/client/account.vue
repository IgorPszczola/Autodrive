<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
})

const rentalApi = useRentalApi()
const { user } = useAuth()

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const reservations = ref<Array<Record<string, any>>>([])
const modelIdByName = ref<Record<string, number>>({})

const reviewDialog = ref(false)
const reviewForm = reactive({
  carModelId: 0,
  rating: 5,
  comment: '',
})

async function loadReservations() {
  loading.value = true
  errorMessage.value = ''

  try {
    reservations.value = await rentalApi.getMyReservations()
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się pobrać rezerwacji'
  }
  finally {
    loading.value = false
  }
}

async function loadModelMap() {
  try {
    const pageData = await rentalApi.getCarModels({}, 0, 1000)
    const models = pageData.content ?? []
    modelIdByName.value = models.reduce<Record<string, number>>((acc, model) => {
      const key = `${model.brand}|${model.model}`
      acc[key] = model.id

      return acc
    }, {})
  }
  catch {
    modelIdByName.value = {}
  }
}

async function cancelReservation(id: number) {
  try {
    await rentalApi.cancelReservation(id)
    successMessage.value = 'Rezerwacja została anulowana.'
    await loadReservations()
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się anulować rezerwacji'
  }
}

function getReservationModelId(reservation: Record<string, any>): number {
  if (reservation.carModel?.id) {
    return Number(reservation.carModel.id)
  }

  const fallbackModel = reservation.carModelName ?? reservation.carModel
  const key = `${reservation.carBrand ?? reservation.carModel?.brand ?? ''}|${fallbackModel ?? reservation.carModel?.model ?? ''}`

  return modelIdByName.value[key] ?? 0
}

function getReservationModelLabel(reservation: Record<string, any>): string {
  const brand = reservation.carModel?.brand ?? reservation.carBrand ?? ''
  const model = reservation.carModel?.model ?? reservation.carModelName ?? reservation.carModel ?? ''

  return `${brand} ${model}`.trim() || '-'
}

function getUserRoleLabel(role?: string): string {
  if (!role) {
    return '-'
  }

  if (role === 'ROLE_ADMIN') {
    return 'Administrator'
  }

  if (role === 'ROLE_USER') {
    return 'Użytkownik'
  }

  return role
}

function canCancelReservation(reservation: Record<string, any>): boolean {
  const status = typeof reservation.status === 'string'
    ? reservation.status.trim().toUpperCase()
    : ''

  return status === 'CONFIRMED'
}

function canReviewReservation(reservation: Record<string, any>): boolean {
  const status = typeof reservation.status === 'string'
    ? reservation.status.trim().toUpperCase()
    : ''

  return status === 'COMPLETED' && Boolean(getReservationModelId(reservation))
}

function openReviewDialog(carModelId: number) {
  reviewForm.carModelId = carModelId
  reviewForm.rating = 5
  reviewForm.comment = ''
  reviewDialog.value = true
}

async function submitReview() {
  if (!reviewForm.carModelId || !reviewForm.comment.trim()) {
    errorMessage.value = 'Wybierz model i wpisz treść opinii.'

    return
  }

  try {
    await rentalApi.createReview({
      rating: reviewForm.rating,
      comment: reviewForm.comment,
      carModelId: reviewForm.carModelId,
    })

    reviewDialog.value = false
    successMessage.value = 'Dziękujemy za opinię.'
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się zapisać opinii'
  }
}

onMounted(async () => {
  await Promise.all([loadReservations(), loadModelMap()])
})
</script>

<template>
  <div class="gradient-hero min-h-screen py-10 animate-fade-in">
    <v-container max-width="1160">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-weight-black text-white">
          Moje konto
        </h1>
        <p class="text-medium-emphasis mt-1">
          Historia wypożyczeń, status płatności/kaucji oraz oceny pojazdów po zakończonej rezerwacji.
        </p>
      </div>

      <!-- Alerts -->
      <v-alert
        v-if="errorMessage"
        type="error"
        variant="tonal"
        class="mb-6"
        rounded="lg"
      >
        {{ errorMessage }}
      </v-alert>

      <v-alert
        v-if="successMessage"
        type="success"
        variant="tonal"
        class="mb-6"
        rounded="lg"
      >
        {{ successMessage }}
      </v-alert>

      <v-row>
        <!-- User Info Panel -->
        <v-col cols="12" md="4">
          <v-card class="pa-6 glass-card text-center" rounded="xl" variant="flat">
            <v-avatar color="primary" size="80" class="mb-4 font-weight-black text-h4 shadow-lg">
              {{ user?.firstName ? user.firstName.charAt(0).toUpperCase() : 'U' }}
            </v-avatar>

            <h2 class="text-xl font-weight-bold text-white mb-1">
              {{ user?.firstName }} {{ user?.lastName }}
            </h2>
            <p class="text-sm text-medium-emphasis mb-6">
              {{ getUserRoleLabel(user?.role) }}
            </p>

            <v-divider class="mb-6" style="border-color: rgba(255, 255, 255, 0.05) !important;" />

            <div class="text-left ga-4 d-flex flex-column">
              <div>
                <span class="text-xs text-medium-emphasis d-block uppercase font-weight-bold mb-1" style="letter-spacing: 0.05em;">Adres email</span>
                <span class="text-sm text-white font-weight-medium">{{ user?.email || '-' }}</span>
              </div>
              <div>
                <span class="text-xs text-medium-emphasis d-block uppercase font-weight-bold mb-1" style="letter-spacing: 0.05em;">Telefon kontaktowy</span>
                <span class="text-sm text-white font-weight-medium">{{ user?.phoneNumber || '-' }}</span>
              </div>
              <div>
                <span class="text-xs text-medium-emphasis d-block uppercase font-weight-bold mb-1" style="letter-spacing: 0.05em;">Numer prawa jazdy</span>
                <span class="text-sm text-white font-weight-medium">{{ user?.driverLicenseNumber || '-' }}</span>
              </div>
            </div>
          </v-card>
        </v-col>

        <!-- Reservations List -->
        <v-col cols="12" md="8">
          <v-card class="pa-6 glass-card" rounded="xl" variant="flat">
            <h2 class="text-xl font-weight-bold text-white mb-4 d-flex align-center ga-2">
              <v-icon color="primary">mdi-history</v-icon>
              Historia rezerwacji
            </h2>

            <v-table class="bg-transparent text-white" style="color: #f8fafc !important;">
              <thead>
                <tr>
                  <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5 pl-0">ID</th>
                  <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Model</th>
                  <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Termin</th>
                  <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Koszt</th>
                  <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Status</th>
                  <th class="text-right font-weight-bold text-medium-emphasis border-b border-white/5 pr-0">Akcje</th>
                </tr>
              </thead>

              <tbody>
                <tr
                  v-for="reservation in reservations"
                  :key="reservation.id"
                  class="hover:bg-white/2"
                >
                  <td class="pl-0 font-weight-semibold text-white py-4">#{{ reservation.id }}</td>
                  <td class="font-weight-bold text-white py-4">{{ getReservationModelLabel(reservation) }}</td>
                  <td class="text-sm text-medium-emphasis py-4">{{ reservation.startDate }} — {{ reservation.endDate }}</td>
                  <td class="font-weight-bold text-primary py-4">{{ reservation.totalPrice || '-' }} PLN</td>
                  <td class="py-4">
                    <v-chip
                      :color="reservation.status === 'CONFIRMED' ? 'warning' : reservation.status === 'COMPLETED' ? 'success' : reservation.status === 'CANCELLED' ? 'error' : 'info'"
                      size="small"
                      variant="tonal"
                      class="font-weight-medium"
                    >
                      {{ reservation.status }}
                    </v-chip>
                  </td>
                  <td class="pr-0 py-4 text-right">
                    <v-btn
                      v-if="canCancelReservation(reservation)"
                      size="small"
                      variant="flat"
                      color="error"
                      class="font-weight-semibold px-4"
                      height="32"
                      @click="cancelReservation(reservation.id)"
                    >
                      Anuluj
                    </v-btn>

                    <v-btn
                      v-if="canReviewReservation(reservation)"
                      size="small"
                      variant="flat"
                      color="primary"
                      class="font-weight-semibold px-4"
                      height="32"
                      @click="openReviewDialog(getReservationModelId(reservation))"
                    >
                      Oceń
                    </v-btn>

                    <span v-if="!canCancelReservation(reservation) && !canReviewReservation(reservation)" class="text-medium-emphasis text-xs">-</span>
                  </td>
                </tr>

                <tr v-if="!loading && !reservations.length">
                  <td colspan="6" class="text-center text-medium-emphasis py-8">
                    <v-icon size="36" class="mb-2 text-medium-emphasis">mdi-calendar-blank-outline</v-icon>
                    <p class="mb-0 text-sm">Brak historii rezerwacji na tym koncie</p>
                  </td>
                </tr>
              </tbody>
            </v-table>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Review Dialog -->
    <v-dialog
      v-model="reviewDialog"
      max-width="500"
    >
      <v-card class="pa-6 glass-card" rounded="xl" variant="flat">
        <h3 class="text-xl font-weight-bold text-white mb-2">
          Oceń swój wyjazd
        </h3>
        <p class="text-medium-emphasis text-sm mb-4">
          Twoja opinia pomoże innym kierowcom w wyborze idealnego auta.
        </p>

        <v-card-text class="pa-0 mb-4">
          <div class="d-flex align-center justify-center mb-6">
            <v-rating
              v-model="reviewForm.rating"
              color="amber"
              active-color="amber"
              hover
              size="40"
              density="comfortable"
            />
          </div>

          <v-textarea
            v-model="reviewForm.comment"
            label="Napisz kilka słów o pojeździe (wrażenia z jazdy, czystość, stan techniczny)..."
            rows="4"
            variant="outlined"
            color="primary"
            hide-details
          />
        </v-card-text>

        <v-card-actions class="pa-0 ga-2">
          <v-btn
            variant="outlined"
            color="secondary"
            class="flex-grow-1 font-weight-semibold"
            height="46"
            style="border-color: rgba(255, 255, 255, 0.1);"
            @click="reviewDialog = false"
          >
            Zamknij
          </v-btn>

          <v-btn
            color="primary"
            variant="flat"
            class="flex-grow-1 font-weight-semibold"
            height="46"
            @click="submitReview"
          >
            Zapisz opinię
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>
