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
    errorMessage.value = error instanceof Error ? error.message : 'Nie udało się pobrać modeli'
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
    })

    successMessage.value = 'Nowy model został dodany do katalogu.'
    currentPage.value = 1
    await loadModels()
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Nie udało się dodać modelu'
  }
}

onMounted(loadModels)

watch(currentPage, loadModels)
</script>

<template>
  <v-container class="py-8">
    <h1 class="text-3xl font-bold mb-2">
      Zarządzanie katalogiem modeli
    </h1>
    <p class="text-slate-500 mb-6">
      Dodawanie nowych modeli oraz aktualizacja cen i parametrów.
    </p>

    <v-alert v-if="errorMessage" type="error" class="mb-4">
      {{ errorMessage }}
    </v-alert>
    <v-alert v-if="successMessage" type="success" class="mb-4">
      {{ successMessage }}
    </v-alert>

    <v-card class="mb-6">
      <v-card-title>Dodaj nowy model</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="4">
            <v-text-field v-model="form.brand" label="Marka" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="form.model" label="Model" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="form.segment" label="Segment" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="form.pricePerDay" label="Cena za dzień" type="number" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="form.depositAmount" label="Kaucja" type="number" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="form.mileageLimitPerDay" label="Limit km / dzień" type="number" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="form.extraMileageFee" label="Dopłata za km" type="number" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model.number="form.powerHp" label="Moc (KM)" type="number" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="form.transmissionType" label="Skrzynia" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="form.fuelType" label="Paliwo" />
          </v-col>
        </v-row>
      </v-card-text>
      <v-card-actions>
        <v-btn color="primary" @click="createModel">
          Dodaj model
        </v-btn>
      </v-card-actions>
    </v-card>

    <v-card>
      <v-card-title>Obecny katalog</v-card-title>
      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Model</th>
            <th>Segment</th>
            <th>Cena / dzień</th>
            <th>Paliwo</th>
            <th>Skrzynia</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in models" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.brand }} {{ item.model }}</td>
            <td>{{ item.segment }}</td>
            <td>{{ item.pricePerDay }}</td>
            <td>{{ item.fuelType }}</td>
            <td>{{ item.transmissionType }}</td>
          </tr>
        </tbody>
      </v-table>

      <div v-if="totalPages > 1" class="pa-4 flex justify-center">
        <v-pagination
          v-model="currentPage"
          :length="totalPages"
        />
      </div>
    </v-card>
  </v-container>
</template>
