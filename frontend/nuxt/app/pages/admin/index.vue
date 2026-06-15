<script setup lang="ts">
definePageMeta({
  middleware: 'admin',
})

const rentalApi = useRentalApi()

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const stats = ref<Record<string, any> | null>(null)
const reservations = ref<Array<Record<string, any>>>([])
const monthlyEarnings = ref<Array<Record<string, any>>>([])

const returnForm = reactive({
  reservationId: 0,
  currentMileage: 0,
  isDamaged: false,
  damageNotes: '',
  damageCost: 0,
})

function resetReturnForm() {
  returnForm.reservationId = 0
  returnForm.currentMileage = 0
  returnForm.isDamaged = false
  returnForm.damageNotes = ''
  returnForm.damageCost = 0
}

function getReservationUserEmail(reservation: Record<string, any>): string {
  return reservation.user?.email ?? reservation.userEmail ?? '-'
}

function getReservationCarLabel(reservation: Record<string, any>): string {
  const brand = reservation.carModel?.brand ?? reservation.carBrand ?? ''
  const model = reservation.carModel?.model ?? reservation.carModelName ?? reservation.carModel ?? ''

  return `${brand} ${model}`.trim() || '-'
}

function canAssignKeys(reservation: Record<string, any>): boolean {
  const status = typeof reservation.status === 'string'
    ? reservation.status.trim().toUpperCase()
    : ''

  return status === 'CONFIRMED'
}

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('pl-PL', {
    style: 'currency',
    currency: 'PLN',
    maximumFractionDigits: 2,
  }).format(value)
}

const sortedMonthlyEarnings = computed(() => {
  return [...monthlyEarnings.value].sort((a, b) => Number(a.month || 0) - Number(b.month || 0))
})

const monthlyEarningsValues = computed(() => {
  return sortedMonthlyEarnings.value.map(item => Number(item.earnings || 0))
})

const monthlyEarningsLabels = computed(() => {
  return sortedMonthlyEarnings.value.map(item => String(item.monthName || ''))
})

async function loadDashboard() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [statsData, reservationsData, monthlyEarningsData] = await Promise.all([
      rentalApi.getAdminStats(),
      rentalApi.getAdminReservations(),
      rentalApi.getMonthlyEarnings(),
    ])

    stats.value = statsData as unknown as Record<string, any>
    reservations.value = reservationsData
    monthlyEarnings.value = monthlyEarningsData
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się pobrać panelu admina'
  }
  finally {
    loading.value = false
  }
}

async function assignUnit(reservationId: number) {
  try {
    await rentalApi.assignUnit(reservationId)
    await loadDashboard()
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się wydać kluczyków'
  }
}

async function processReturn() {
  successMessage.value = ''

  if (!returnForm.reservationId || returnForm.currentMileage < 0 || returnForm.damageCost < 0) {
    errorMessage.value = 'Uzupełnij poprawnie formularz zwrotu auta.'

    return
  }

  try {
    await rentalApi.processReturn(returnForm.reservationId, {
      currentMileage: returnForm.currentMileage,
      isDamaged: returnForm.isDamaged,
      damageNotes: returnForm.damageNotes,
      damageCost: returnForm.damageCost,
    })

    await loadDashboard()
    resetReturnForm()
    successMessage.value = 'Zwrot auta został zakończony.'
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się zakończyć zwrotu'
  }
}

onMounted(loadDashboard)
</script>

<template>
  <v-container class="py-8">
    <h1 class="text-3xl font-bold mb-2">
      Panel pracownika / administratora
    </h1>

    <p class="text-slate-500 mb-6">
      Rezerwacje klientów, wydawanie kluczyków i obsługa zwrotów.
    </p>

    <div class="mb-6 flex flex-wrap gap-2">
      <v-btn
        color="primary"
        variant="tonal"
        to="/admin/fleet"
      >
        Przejdź do floty
      </v-btn>

      <v-btn
        color="primary"
        variant="tonal"
        to="/admin/catalog"
      >
        Przejdź do katalogu
      </v-btn>
    </div>

    <v-row>
      <v-col
        cols="12"
        md="3"
      >
        <v-card>
          <v-card-title>Zysk całkowity</v-card-title>

          <v-card-text class="text-2xl font-semibold">
            {{ stats?.totalEarnings ?? 0 }} PLN
          </v-card-text>
        </v-card>
      </v-col>

      <v-col
        cols="12"
        md="3"
      >
        <v-card>
          <v-card-title>Rezerwacje</v-card-title>

          <v-card-text class="text-2xl font-semibold">
            {{ stats?.totalReservationsCount ?? 0 }}
          </v-card-text>
        </v-card>
      </v-col>

      <v-col
        cols="12"
        md="3"
      >
        <v-card>
          <v-card-title>Aktywne najmy</v-card-title>

          <v-card-text class="text-2xl font-semibold">
            {{ stats?.activeRentalsCount ?? 0 }}
          </v-card-text>
        </v-card>
      </v-col>

      <v-col
        cols="12"
        md="3"
      >
        <v-card>
          <v-card-title>Flota w naprawie</v-card-title>

          <v-card-text class="text-2xl font-semibold">
            {{ stats?.fleetInRepairCount ?? 0 }}
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-card
      class="mt-6"
      rounded="lg"
    >
      <v-card-title class="d-flex align-center justify-space-between pa-6 gap-2">
        <span>Miesięczne zarobki</span>
      </v-card-title>

      <v-card-text>
        <div
          v-if="!sortedMonthlyEarnings.length"
          class="text-medium-emphasis"
        >
          Brak danych do wyświetlenia.
        </div>

        <div v-else>
          <v-sparkline
            :model-value="monthlyEarningsValues"
            :line-width="1"
            color="primary"
            min="0"
            show-markers
            interactive
            :tooltip="{'class': 'pl-0 bg-grey-darken-4'}"
            marker-size="1"
          >
            <template #tooltip="{index, value}">
              {{ monthlyEarningsLabels[index] }}: {{ formatCurrency(Number(value || 0)) }}
            </template>
          </v-sparkline>

          <div class="mt-4 gap-2 grid grid-cols-1 lg:grid-cols-4 md:grid-cols-3 sm:grid-cols-2">
            <div
              v-for="(entry, index) in sortedMonthlyEarnings"
              :key="entry.month"
              class="px-2 py-1.5 border border-[rgba(var(--v-theme-primary),0.2)] rounded-[10px] bg-[rgba(var(--v-theme-primary),0.04)]"
            >
              <v-tooltip location="top">
                <template #activator="{props}">
                  <div v-bind="props">
                    <div class="text-[11px] text-[rgba(var(--v-theme-on-surface),0.72)] leading-tight">
                      {{ monthlyEarningsLabels[index] }}
                    </div>

                    <div class="text-xs font-semibold mt-1">
                      {{ formatCurrency(Number(entry.earnings || 0)) }}
                    </div>
                  </div>
                </template>

                {{ monthlyEarningsLabels[index] }}: {{ formatCurrency(Number(entry.earnings || 0)) }}
              </v-tooltip>
            </div>
          </div>
        </div>
      </v-card-text>
    </v-card>

    <v-card class="mb-6 mt-6">
      <v-card-title class="pa-6">
        Lista rezerwacji klientów
      </v-card-title>

      <v-table>
        <thead>
          <tr>
            <th>ID</th>

            <th>Klient</th>

            <th>Auto</th>

            <th>Termin</th>

            <th>Ubezpieczenie</th>

            <th>Dodatki</th>

            <th>Status</th>

            <th>Akcje</th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="reservation in reservations"
            :key="reservation.id"
          >
            <td>{{ reservation.id }}</td>

            <td>{{ getReservationUserEmail(reservation) }}</td>

            <td>{{ getReservationCarLabel(reservation) }}</td>

            <td>{{ reservation.startDate }} - {{ reservation.endDate }}</td>

            <td>{{ reservation.insuranceVariant?.name ?? reservation.insuranceVariantName ?? '-' }}</td>

            <td>
              {{ reservation.addons?.length
                ? reservation.addons.map((a: any) => a.name).join(', ')
                : '-' }}
            </td>

            <td>{{ reservation.status }}</td>

            <td>
              <div class="flex gap-2">
                <v-btn
                  v-if="canAssignKeys(reservation)"
                  size="small"
                  color="primary"
                  variant="outlined"
                  @click="assignUnit(reservation.id)"
                >
                  Wydaj kluczyki
                </v-btn>
              </div>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>

    <v-alert
      v-if="errorMessage"
      type="error"
      class="mb-4"
    >
      {{ errorMessage }}
    </v-alert>

    <v-alert
      v-if="successMessage"
      type="success"
      class="my-2"
    >
      {{ successMessage }}
    </v-alert>

    <v-card class="mt-6 pa-4">
      <v-card-title class="mb-4">
        Moduł zwrotu auta
      </v-card-title>

      <v-card-text>
        <v-row>
          <v-col
            cols="12"
            md="3"
          >
            <v-text-field
              v-model.number="returnForm.reservationId"
              label="ID rezerwacji"
              type="number"
            />
          </v-col>

          <v-col
            cols="12"
            md="3"
          >
            <v-text-field
              v-model.number="returnForm.currentMileage"
              label="Przebieg"
              type="number"
            />
          </v-col>

          <v-col
            cols="12"
            md="3"
          >
            <v-text-field
              v-model.number="returnForm.damageCost"
              label="Koszt uszkodzeń"
              type="number"
            />
          </v-col>

          <v-col
            cols="12"
            md="3"
            class="flex items-center"
          >
            <v-checkbox
              v-model="returnForm.isDamaged"
              label="Auto uszkodzone"
            />
          </v-col>
        </v-row>

        <v-textarea
          v-model="returnForm.damageNotes"
          label="Opis uszkodzeń"
          rows="3"
        />
      </v-card-text>

      <v-card-actions>
        <v-btn
          color="primary"
          @click="processReturn"
        >
          Zakończ zwrot
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>
