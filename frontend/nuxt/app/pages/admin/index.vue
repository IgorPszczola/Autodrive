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

watch(monthlyEarnings, () => {
  const trySetup = (retries = 5) => {
    const sparkline = document.querySelector('.v-sparkline')
    if (sparkline) {
      const circles = sparkline.querySelectorAll('circle')
      circles.forEach((circle, idx) => {
        const value = monthlyEarningsValues.value[idx]
        const label = monthlyEarningsLabels.value[idx]
        if (value !== undefined) {
          circle.querySelectorAll('title').forEach(t => t.remove())
          const titleEl = document.createElementNS('http://www.w3.org/2000/svg', 'title')
          titleEl.textContent = `${label}: ${formatCurrency(value)}`
          circle.appendChild(titleEl)
          circle.style.cursor = 'pointer'
        }
      })
    } else if (retries > 0) {
      setTimeout(() => trySetup(retries - 1), 100)
    }
  }
  nextTick(() => trySetup())
}, { deep: true })

onMounted(loadDashboard)
</script>

<template>
  <div class="gradient-hero min-h-screen py-8 animate-fade-in">
    <v-container max-width="1200">
      <!-- Header -->
      <div class="d-flex flex-column flex-md-row justify-space-between align-md-center mb-8 ga-4">
        <div>
          <h1 class="text-3xl font-weight-black text-white">
            Panel administratora
          </h1>
          <p class="text-medium-emphasis mt-1">
            Przeglądaj statystyki finansowe, zarządzaj flotą i zatwierdzaj zwroty pojazdów.
          </p>
        </div>

        <!-- Quick actions -->
        <div class="d-flex ga-2">
          <v-btn
            color="primary"
            variant="flat"
            to="/admin/catalog"
            prepend-icon="mdi-car-multiple"
            class="font-weight-semibold"
          >
            Katalog modeli
          </v-btn>

          <v-btn
            color="secondary"
            variant="outlined"
            to="/admin/fleet"
            prepend-icon="mdi-format-list-bulleted"
            class="font-weight-semibold"
            style="border-color: rgba(255, 255, 255, 0.1);"
          >
            Flota (Egzemplarze)
          </v-btn>
        </div>
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

      <!-- Stats Grid -->
      <v-row class="mb-8">
        <v-col cols="12" sm="6" md="3">
          <v-card class="pa-5 glass-card d-flex align-center ga-4" variant="flat">
            <v-avatar color="primary/15" rounded="lg" size="52">
              <v-icon color="primary" size="28">mdi-currency-usd</v-icon>
            </v-avatar>
            <div>
              <span class="text-xs text-medium-emphasis d-block uppercase font-weight-bold" style="letter-spacing: 0.05em;">Zysk całkowity</span>
              <span class="text-2xl font-weight-black text-white">{{ stats?.totalEarnings ?? 0 }} PLN</span>
            </div>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="pa-5 glass-card d-flex align-center ga-4" variant="flat">
            <v-avatar color="success/15" rounded="lg" size="52">
              <v-icon color="success" size="28">mdi-calendar-check</v-icon>
            </v-avatar>
            <div>
              <span class="text-xs text-medium-emphasis d-block uppercase font-weight-bold" style="letter-spacing: 0.05em;">Wszystkie rezerwacje</span>
              <span class="text-2xl font-weight-black text-white">{{ stats?.totalReservationsCount ?? 0 }}</span>
            </div>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="pa-5 glass-card d-flex align-center ga-4" variant="flat">
            <v-avatar color="info/15" rounded="lg" size="52">
              <v-icon color="info" size="28">mdi-car-key</v-icon>
            </v-avatar>
            <div>
              <span class="text-xs text-medium-emphasis d-block uppercase font-weight-bold" style="letter-spacing: 0.05em;">Aktywne najmy</span>
              <span class="text-2xl font-weight-black text-white">{{ stats?.activeRentalsCount ?? 0 }}</span>
            </div>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="pa-5 glass-card d-flex align-center ga-4" variant="flat">
            <v-avatar color="warning/15" rounded="lg" size="52">
              <v-icon color="warning" size="28">mdi-wrench-clock</v-icon>
            </v-avatar>
            <div>
              <span class="text-xs text-medium-emphasis d-block uppercase font-weight-bold" style="letter-spacing: 0.05em;">W naprawie / Serwis</span>
              <span class="text-2xl font-weight-black text-white">{{ stats?.fleetInRepairCount ?? 0 }}</span>
            </div>
          </v-card>
        </v-col>
      </v-row>

      <!-- Chart Card -->
      <v-card class="mb-8 glass-card" rounded="xl" variant="flat">
        <v-card-title class="px-6 pt-6 pb-2 text-xl font-weight-bold text-white d-flex align-center ga-2">
          <v-icon color="primary">mdi-chart-areaspline</v-icon>
          Przychody w ujęciu miesięcznym (Bieżący rok)
        </v-card-title>

        <v-card-text class="pa-6">
          <div v-if="!sortedMonthlyEarnings.length" class="text-medium-emphasis text-center py-8">
            Brak dostępnych danych wykresu zarobków.
          </div>

          <div v-else>
            <!-- Sparkline Chart -->
            <v-sparkline
              :model-value="monthlyEarningsValues"
              :line-width="2"
              color="primary"
              min="0"
              show-markers
              interactive
              auto-draw
              smooth
              fill
              stroke-linecap="round"
              :gradient="['#2563eb', '#10b981']"
              height="100"
              class="mb-6"
            />

            <!-- Monthly grid breakdown -->
            <div class="mt-4 ga-3 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
              <div
                v-for="(entry, index) in sortedMonthlyEarnings"
                :key="entry.month"
                class="pa-3 border border-white/5 rounded-lg bg-white/2 d-flex flex-column"
              >
                <span class="text-xs text-medium-emphasis leading-none">{{ monthlyEarningsLabels[index] }}</span>
                <span class="text-sm font-weight-black text-white mt-1">{{ formatCurrency(Number(entry.earnings || 0)) }}</span>
              </div>
            </div>
          </div>
        </v-card-text>
      </v-card>

      <!-- Reservations List Table -->
      <v-card class="mb-8 glass-card" rounded="xl" variant="flat">
        <v-card-title class="px-6 pt-6 pb-2 text-xl font-weight-bold text-white d-flex align-center ga-2">
          <v-icon color="primary">mdi-calendar-multiple</v-icon>
          Bieżące rezerwacje klientów
        </v-card-title>

        <v-card-text class="pa-0">
          <v-table class="bg-transparent text-white" style="color: #f8fafc !important;">
            <thead>
              <tr>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5 pl-6">ID</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Klient</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Auto</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Termin</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Dodatki</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Status</th>
                <th class="text-right font-weight-bold text-medium-emphasis border-b border-white/5 pr-6">Akcja</th>
              </tr>
            </thead>

            <tbody>
              <tr
                v-for="reservation in reservations"
                :key="reservation.id"
                class="hover:bg-white/2"
              >
                <td class="pl-6 font-weight-semibold text-white py-4">#{{ reservation.id }}</td>
                <td class="text-sm text-white py-4">{{ getReservationUserEmail(reservation) }}</td>
                <td class="font-weight-bold text-white py-4">{{ getReservationCarLabel(reservation) }}</td>
                <td class="text-sm text-medium-emphasis py-4">{{ reservation.startDate }} — {{ reservation.endDate }}</td>
                <td class="text-sm text-medium-emphasis py-4">
                  {{ reservation.addons?.length
                    ? reservation.addons.map((a: any) => a.name).join(', ')
                    : '-' }}
                </td>
                <td class="py-4">
                  <v-chip
                    :color="reservation.status === 'CONFIRMED' ? 'warning' : reservation.status === 'RENTED' ? 'info' : reservation.status === 'COMPLETED' ? 'success' : 'error'"
                    size="small"
                    variant="tonal"
                    class="font-weight-medium"
                  >
                    {{ reservation.status }}
                  </v-chip>
                </td>
                <td class="pr-6 py-4 text-right">
                  <v-btn
                    v-if="canAssignKeys(reservation)"
                    size="small"
                    color="primary"
                    variant="flat"
                    class="font-weight-semibold"
                    height="32"
                    @click="assignUnit(reservation.id)"
                  >
                    Wydaj kluczyki
                  </v-btn>
                  <span v-else class="text-medium-emphasis text-xs">-</span>
                </td>
              </tr>
              <tr v-if="!reservations.length">
                <td colspan="7" class="text-center text-medium-emphasis py-6">
                  Brak rezerwacji w systemie
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>

      <!-- Return Processing Module -->
      <v-card class="glass-card" rounded="xl" variant="flat">
        <v-card-title class="px-6 pt-6 pb-2 text-xl font-weight-bold text-white d-flex align-center ga-2">
          <v-icon color="primary">mdi-keyboard-return</v-icon>
          Moduł odbioru pojazdu i rozliczenia zwrotu
        </v-card-title>

        <v-card-text class="pa-6">
          <v-row>
            <v-col cols="12" md="3" class="py-2">
              <v-text-field
                v-model.number="returnForm.reservationId"
                label="ID rezerwacji"
                type="number"
                variant="outlined"
                color="primary"
                hide-details
              />
            </v-col>

            <v-col cols="12" md="3" class="py-2">
              <v-text-field
                v-model.number="returnForm.currentMileage"
                label="Bieżący przebieg (km)"
                type="number"
                variant="outlined"
                color="primary"
                hide-details
              />
            </v-col>

            <v-col cols="12" md="3" class="py-2">
              <v-text-field
                v-model.number="returnForm.damageCost"
                label="Kalkulacja kosztów szkody (PLN)"
                type="number"
                variant="outlined"
                color="primary"
                hide-details
              />
            </v-col>

            <v-col cols="12" md="3" class="py-2 d-flex align-center">
              <v-checkbox
                v-model="returnForm.isDamaged"
                label="Pojazd uszkodzony przy zwrocie"
                color="error"
                hide-details
              />
            </v-col>
          </v-row>

          <v-textarea
            v-model="returnForm.damageNotes"
            label="Opis stanu pojazdu i ewentualnych uszkodzeń..."
            rows="3"
            variant="outlined"
            color="primary"
            class="mt-4"
            hide-details
          />
        </v-card-text>

        <!-- Form Action Bar -->
        <v-divider style="border-color: rgba(255, 255, 255, 0.05) !important;" />
        <v-card-actions class="px-6 py-4 bg-black/10">
          <v-btn
            color="primary"
            variant="flat"
            class="font-weight-semibold px-6"
            height="42"
            @click="processReturn"
          >
            Zatwierdź i rozlicz zwrot
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-container>
  </div>
</template>
