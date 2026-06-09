<script setup lang="ts">
definePageMeta({
  middleware: 'admin',
})

const rentalApi = useRentalApi()

const loading = ref(false)
const errorMessage = ref('')
const stats = ref<Record<string, any> | null>(null)
const reservations = ref<Array<Record<string, any>>>([])

const returnForm = reactive({
  reservationId: 0,
  currentMileage: 0,
  isDamaged: false,
  damageNotes: '',
  damageCost: 0,
})

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

async function loadDashboard() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [statsData, reservationsData] = await Promise.all([
      rentalApi.getAdminStats(),
      rentalApi.getAdminReservations(),
    ])

    stats.value = statsData as unknown as Record<string, any>
    reservations.value = reservationsData
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Nie udało się pobrać panelu admina'
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
    errorMessage.value = error instanceof Error ? error.message : 'Nie udało się wydać kluczyków'
  }
}

async function processReturn() {
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
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Nie udało się zakończyć zwrotu'
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

    <v-alert v-if="errorMessage" type="error" class="mb-4">
      {{ errorMessage }}
    </v-alert>

    <v-row>
      <v-col cols="12" md="3">
        <v-card>
          <v-card-title>Zysk całkowity</v-card-title>
          <v-card-text class="text-2xl font-semibold">
            {{ stats?.totalEarnings ?? 0 }} PLN
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="3">
        <v-card>
          <v-card-title>Rezerwacje</v-card-title>
          <v-card-text class="text-2xl font-semibold">
            {{ stats?.totalReservationsCount ?? 0 }}
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="3">
        <v-card>
          <v-card-title>Aktywne najmy</v-card-title>
          <v-card-text class="text-2xl font-semibold">
            {{ stats?.activeRentalsCount ?? 0 }}
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="3">
        <v-card>
          <v-card-title>Flota w naprawie</v-card-title>
          <v-card-text class="text-2xl font-semibold">
            {{ stats?.fleetInRepairCount ?? 0 }}
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="mt-6">
      <v-card-title>Lista rezerwacji klientów</v-card-title>
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
          <tr v-for="reservation in reservations" :key="reservation.id">
            <td>{{ reservation.id }}</td>
            <td>{{ getReservationUserEmail(reservation) }}</td>
            <td>{{ getReservationCarLabel(reservation) }}</td>
            <td>{{ reservation.startDate }} - {{ reservation.endDate }}</td>
            <td>{{ reservation.insuranceVariant?.name ?? reservation.insuranceVariantName ?? '-' }}</td>
            <td>{{ reservation.addons?.length ? reservation.addons.map((a: any) => a.name).join(', ') : '-' }}</td>
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

    <v-card class="mt-6">
      <v-card-title>Moduł zwrotu auta</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="returnForm.reservationId" label="ID rezerwacji" type="number" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="returnForm.currentMileage" label="Przebieg" type="number" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="returnForm.damageCost" label="Koszt uszkodzeń" type="number" />
          </v-col>
          <v-col cols="12" md="3" class="flex items-center">
            <v-checkbox v-model="returnForm.isDamaged" label="Auto uszkodzone" />
          </v-col>
        </v-row>
        <v-textarea
          v-model="returnForm.damageNotes"
          label="Opis uszkodzeń"
          rows="3"
        />
      </v-card-text>
      <v-card-actions>
        <v-btn color="primary" @click="processReturn">
          Zakończ zwrot
        </v-btn>
        <v-btn to="/admin/fleet" variant="text">
          Przejdź do floty
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>
