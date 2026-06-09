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
    errorMessage.value = error instanceof Error ? error.message : 'Nie udało się pobrać rezerwacji'
  }
  finally {
    loading.value = false
  }
}

async function loadModelMap() {
  try {
    const models = await rentalApi.getCarModels()
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
    errorMessage.value = error instanceof Error ? error.message : 'Nie udało się anulować rezerwacji'
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
    errorMessage.value = error instanceof Error ? error.message : 'Nie udało się zapisać opinii'
  }
}

onMounted(async () => {
  await Promise.all([loadReservations(), loadModelMap()])

})
</script>

<template>
  <v-container class="py-8">
    <h1 class="text-3xl font-bold mb-2">
      Moje konto i rezerwacje
    </h1>
    <p class="text-slate-500 mb-6">
      Historia wypożyczeń, status zwrotu i oceny po zakończonych wyjazdach.
    </p>

    <v-alert v-if="errorMessage" type="error" class="mb-4">
      {{ errorMessage }}
    </v-alert>
    <v-alert v-if="successMessage" type="success" class="mb-4">
      {{ successMessage }}
    </v-alert>
    <v-card class="mb-6">
      <v-card-title>Dane użytkownika</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="6">
            <div class="text-sm text-slate-500">
              Email
            </div>
            <div class="font-medium">
              {{ user?.email || '-' }}
            </div>
          </v-col>
          <v-col cols="12" md="6">
            <div class="text-sm text-slate-500">
              Rola
            </div>
            <div class="font-medium">
              {{ getUserRoleLabel(user?.role) }}
            </div>
          </v-col>
          <v-col cols="12" md="6">
            <div class="text-sm text-slate-500">
              Imię
            </div>
            <div class="font-medium">
              {{ user?.firstName || '-' }}
            </div>
          </v-col>
          <v-col cols="12" md="6">
            <div class="text-sm text-slate-500">
              Nazwisko
            </div>
            <div class="font-medium">
              {{ user?.lastName || '-' }}
            </div>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-card>
      <v-card-title>Historia rezerwacji</v-card-title>
      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Model</th>
            <th>Termin</th>
            <th>Ubezpieczenie</th>
            <th>Dodatki</th>
            <th>Status</th>
            <th>Kwota</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="reservation in reservations" :key="reservation.id">
            <td>{{ reservation.id }}</td>
            <td>{{ getReservationModelLabel(reservation) }}</td>
            <td>{{ reservation.startDate }} - {{ reservation.endDate }}</td>
            <td>{{ reservation.insuranceVariant?.name ?? reservation.insuranceVariantName ?? '-' }}</td>
            <td>{{ reservation.addons?.length ? reservation.addons.map((a: any) => a.name).join(', ') : '-' }}</td>
            <td>{{ reservation.status }}</td>
            <td>{{ reservation.totalPrice || '-' }}</td>
            <td>
              <div class="flex gap-2">
                <v-btn
                  v-if="canCancelReservation(reservation)"
                  size="small"
                  variant="outlined"
                  color="error"
                  @click="cancelReservation(reservation.id)"
                >
                  Anuluj
                </v-btn>
                <v-btn
                  v-if="canReviewReservation(reservation)"
                  size="small"
                  variant="outlined"
                  color="primary"
                  @click="openReviewDialog(getReservationModelId(reservation))"
                >
                  Dodaj ocenę
                </v-btn>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && !reservations.length">
            <td colspan="8" class="text-center py-6">
              Brak rezerwacji
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>

    <v-dialog v-model="reviewDialog" max-width="560">
      <v-card>
        <v-card-title>Oceń zakończony wyjazd</v-card-title>
        <v-card-text>
          <v-slider
            v-model="reviewForm.rating"
            label="Liczba gwiazdek"
            min="1"
            max="5"
            step="1"
            thumb-label
          />
          <v-textarea
            v-model="reviewForm.comment"
            label="Komentarz"
            rows="4"
          />
        </v-card-text>
        <v-card-actions>
          <v-btn variant="text" @click="reviewDialog = false">
            Zamknij
          </v-btn>
          <v-spacer />
          <v-btn color="primary" @click="submitReview">
            Zapisz
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>
