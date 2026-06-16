<script setup lang="ts">
definePageMeta({
  middleware: 'admin',
})

const rentalApi = useRentalApi()

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const models = ref<Array<Record<string, any>>>([])
const currentPage = ref(1)
const totalPages = ref(0)

const form = reactive({
  brand: '',
  model: '',
  segment: '',
  pricePerDay: 0,
  depositAmount: 0,
  mileageLimitPerDay: 0,
  extraMileageFee: 0,
  powerHp: 0,
  transmissionType: '',
  fuelType: '',
  minRentDays: 1,
})

async function loadModels() {
  loading.value = true
  errorMessage.value = ''

  try {
    const pageData = await rentalApi.getCarModels({}, currentPage.value - 1, 10)
    models.value = pageData.content
    totalPages.value = pageData.totalPages
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się pobrać modeli'
  }
  finally {
    loading.value = false
  }
}

async function createModel() {
  if (
    !form.brand.trim()
    || !form.model.trim()
    || !form.segment.trim()
    || !form.transmissionType.trim()
    || !form.fuelType.trim()
    || form.pricePerDay <= 0
    || form.depositAmount < 0
    || form.mileageLimitPerDay <= 0
    || form.extraMileageFee < 0
    || form.powerHp <= 0
    || form.minRentDays <= 0
  ) {
    errorMessage.value = 'Uzupełnij poprawnie wszystkie pola modelu.'

    return
  }

  try {
    await rentalApi.createCarModel({
      brand: form.brand,
      model: form.model,
      segment: form.segment,
      pricePerDay: form.pricePerDay,
      depositAmount: form.depositAmount,
      mileageLimitPerDay: form.mileageLimitPerDay,
      extraMileageFee: form.extraMileageFee,
      powerHp: form.powerHp,
      transmissionType: form.transmissionType,
      fuelType: form.fuelType,
      minRentDays: form.minRentDays,
    })

    successMessage.value = 'Nowy model został dodany do katalogu.'
    currentPage.value = 1
    // Reset form defaults
    form.brand = ''
    form.model = ''
    form.segment = ''
    form.pricePerDay = 0
    form.depositAmount = 0
    form.mileageLimitPerDay = 0
    form.extraMileageFee = 0
    form.powerHp = 0
    form.transmissionType = ''
    form.fuelType = ''
    form.minRentDays = 1

    await loadModels()
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się dodać modelu'
  }
}

onMounted(loadModels)

watch(currentPage, loadModels)
</script>

<template>
  <div class="gradient-hero min-h-screen py-8 animate-fade-in">
    <v-container max-width="1160">
      <!-- Header -->
      <div class="d-flex flex-column flex-md-row justify-space-between align-md-center mb-8 ga-4">
        <div>
          <h1 class="text-3xl font-weight-black text-white">
            Zarządzanie katalogiem modeli
          </h1>
          <p class="text-medium-emphasis mt-1">
            Wprowadzaj nowe modele samochodów i modyfikuj stawki dzienne najmu.
          </p>
        </div>

        <!-- Navigation -->
        <div class="d-flex ga-2">
          <v-btn
            variant="outlined"
            to="/admin"
            prepend-icon="mdi-arrow-left"
            class="font-weight-semibold"
            style="border-color: rgba(255, 255, 255, 0.1);"
          >
            Panel główny
          </v-btn>

          <v-btn
            color="secondary"
            variant="flat"
            to="/admin/fleet"
            prepend-icon="mdi-format-list-bulleted"
            class="font-weight-semibold"
          >
            Flota
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

      <!-- Add New Model Form Card -->
      <v-card class="mb-8 glass-card" rounded="xl" variant="flat">
        <v-card-title class="px-6 pt-6 pb-2 text-xl font-weight-bold text-white d-flex align-center ga-2">
          <v-icon color="primary">mdi-plus-circle-outline</v-icon>
          Dodaj nowy model do oferty
        </v-card-title>

        <v-card-text class="pa-6">
          <v-form @submit.prevent="createModel">
            <v-row>
              <v-col cols="12" md="4" class="py-2">
                <v-text-field
                  v-model="form.brand"
                  label="Marka (np. Audi, Porsche)"
                  variant="outlined"
                  color="primary"
                  hide-details
                />
              </v-col>

              <v-col cols="12" md="4" class="py-2">
                <v-text-field
                  v-model="form.model"
                  label="Model (np. RS6, 911)"
                  variant="outlined"
                  color="primary"
                  hide-details
                />
              </v-col>

              <v-col cols="12" md="4" class="py-2">
                <v-text-field
                  v-model="form.segment"
                  label="Segment (np. SUV, SPORT, E)"
                  variant="outlined"
                  color="primary"
                  hide-details
                />
              </v-col>

              <v-col cols="12" sm="6" md="3" class="py-2">
                <v-text-field
                  v-model.number="form.pricePerDay"
                  label="Cena za dzień (PLN)"
                  type="number"
                  variant="outlined"
                  color="primary"
                  hide-details
                />
              </v-col>

              <v-col cols="12" sm="6" md="3" class="py-2">
                <v-text-field
                  v-model.number="form.depositAmount"
                  label="Kwota kaucji (PLN)"
                  type="number"
                  variant="outlined"
                  color="primary"
                  hide-details
                />
              </v-col>

              <v-col cols="12" sm="6" md="3" class="py-2">
                <v-text-field
                  v-model.number="form.mileageLimitPerDay"
                  label="Limit km / dzień"
                  type="number"
                  variant="outlined"
                  color="primary"
                  hide-details
                />
              </v-col>

              <v-col cols="12" sm="6" md="3" class="py-2">
                <v-text-field
                  v-model.number="form.extraMileageFee"
                  label="Koszt nadprzebiegu (PLN/km)"
                  type="number"
                  variant="outlined"
                  color="primary"
                  hide-details
                />
              </v-col>

              <v-col cols="12" sm="6" md="3" class="py-2">
                <v-text-field
                  v-model.number="form.powerHp"
                  label="Moc silnika (KM)"
                  type="number"
                  variant="outlined"
                  color="primary"
                  hide-details
                />
              </v-col>

              <v-col cols="12" sm="6" md="3" class="py-2">
                <v-text-field
                  v-model="form.transmissionType"
                  label="Skrzynia (MANUAL / AUTOMATIC)"
                  variant="outlined"
                  color="primary"
                  hide-details
                />
              </v-col>

              <v-col cols="12" sm="6" md="3" class="py-2">
                <v-text-field
                  v-model="form.fuelType"
                  label="Paliwo (PETROL / DIESEL / ELECTRIC)"
                  variant="outlined"
                  color="primary"
                  hide-details
                />
              </v-col>

              <v-col cols="12" sm="6" md="3" class="py-2">
                <v-text-field
                  v-model.number="form.minRentDays"
                  label="Min. dni najmu"
                  type="number"
                  variant="outlined"
                  color="primary"
                  hide-details
                />
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>

        <!-- Form Actions Bar -->
        <v-divider style="border-color: rgba(255, 255, 255, 0.05) !important;" />
        <v-card-actions class="px-6 py-4 bg-black/10">
          <v-btn
            color="primary"
            variant="flat"
            class="font-weight-semibold px-6"
            height="42"
            @click="createModel"
          >
            Dodaj model do bazy
          </v-btn>
        </v-card-actions>
      </v-card>

      <!-- Existing Catalog List Table -->
      <v-card class="glass-card" rounded="xl" variant="flat">
        <v-card-title class="px-6 pt-6 pb-2 text-xl font-weight-bold text-white d-flex align-center ga-2">
          <v-icon color="primary">mdi-book-open-outline</v-icon>
          Zapisany katalog pojazdów
        </v-card-title>

        <v-card-text class="pa-0">
          <v-table class="bg-transparent text-white" style="color: #f8fafc !important;">
            <thead>
              <tr>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5 pl-6">ID</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Model</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Segment</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Stawka / Dzień</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Kaucja</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Min. najem</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Skrzynia</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5 pr-6">Paliwo</th>
              </tr>
            </thead>

            <tbody>
              <tr
                v-for="item in models"
                :key="item.id"
                class="hover:bg-white/2"
              >
                <td class="pl-6 font-weight-semibold text-white py-4">#{{ item.id }}</td>
                <td class="font-weight-bold text-white py-4">{{ item.brand }} {{ item.model }}</td>
                <td class="py-4">
                  <v-chip color="primary" size="small" variant="tonal" class="font-weight-medium">
                    {{ item.segment }}
                  </v-chip>
                </td>
                <td class="font-weight-bold text-primary py-4">{{ item.pricePerDay }} PLN</td>
                <td class="text-medium-emphasis py-4">{{ item.depositAmount }} PLN</td>
                <td class="py-4">
                  <span :class="item.minRentDays > 1 ? 'text-warning font-weight-bold' : 'text-medium-emphasis'">
                    {{ item.minRentDays }} {{ item.minRentDays === 1 ? 'dzień' : 'dni' }}
                  </span>
                </td>
                <td class="text-medium-emphasis py-4">{{ item.transmissionType }}</td>
                <td class="text-medium-emphasis py-4 pr-6">{{ item.fuelType }}</td>
              </tr>
              <tr v-if="!models.length">
                <td colspan="8" class="text-center text-medium-emphasis py-6">
                  Brak modeli w katalogu
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>

        <!-- Pagination -->
        <div
          v-if="totalPages > 1"
          class="pa-4 d-flex justify-center"
        >
          <v-pagination
            v-model="currentPage"
            :length="totalPages"
            active-color="primary"
            rounded="circle"
            variant="flat"
            class="glass-panel"
          />
        </div>
      </v-card>
    </v-container>
  </div>
</template>
