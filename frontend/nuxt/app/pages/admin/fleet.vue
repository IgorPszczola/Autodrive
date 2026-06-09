<script setup lang="ts">
definePageMeta({
  middleware: 'admin',
})

const rentalApi = useRentalApi()

const statusOptions = ['AVAILABLE', 'RESERVED', 'RENTED', 'IN_REPAIR']
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const creatingModel = ref(false)
const units = ref<Array<Record<string, any>>>([])
const models = ref<Array<Record<string, any>>>([])

const modelForm = reactive({
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

const newUnit = reactive({
  carModelId: 0,
  licensePlate: '',
  vin: '',
  currentMileage: 0,
  color: '',
  productionYear: new Date().getFullYear(),
  imageUrl: '',
})

async function loadUnits() {
  loading.value = true
  errorMessage.value = ''

  try {
    models.value = await rentalApi.getCarModels()

    const unitsData = await Promise.all(models.value.map(model => rentalApi.getCarModelUnits(model.id)))
    units.value = unitsData.flat().map((unit) => {
      const ownerModel = models.value.find(model => model.id === (unit.carModel?.id ?? unit.modelId))
      return {
        ...unit,
        modelName: ownerModel
          ? `${ownerModel.brand} ${ownerModel.model}`
          : `${unit.carModel?.brand ?? ''} ${unit.carModel?.model ?? ''}`.trim(),
      }
    })

    if (!newUnit.carModelId && models.value[0]) {
      newUnit.carModelId = models.value[0].id
    }
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Nie udalo sie pobrac danych floty'
  }
  finally {
    loading.value = false
  }
}

async function createUnit() {
  if (
    !newUnit.carModelId
    || !newUnit.licensePlate.trim()
    || !newUnit.vin.trim()
    || !newUnit.color.trim()
    || !newUnit.imageUrl.trim()
    || newUnit.currentMileage < 0
    || newUnit.productionYear < 1900
  ) {
    errorMessage.value = 'Uzupelnij poprawnie wszystkie pola pojazdu.'
    return
  }

  try {
    await rentalApi.createCarUnit({
      carModelId: newUnit.carModelId,
      licensePlate: newUnit.licensePlate,
      vin: newUnit.vin,
      currentMileage: newUnit.currentMileage,
      color: newUnit.color,
      productionYear: newUnit.productionYear,
      imageUrl: newUnit.imageUrl,
    })

    successMessage.value = 'Pojazd zostal dodany do floty.'
    await loadUnits()
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Nie udalo sie dodac pojazdu'
  }
}

async function createModel() {
  if (
    !modelForm.brand.trim()
    || !modelForm.model.trim()
    || !modelForm.segment.trim()
    || !modelForm.transmissionType.trim()
    || !modelForm.fuelType.trim()
    || modelForm.pricePerDay <= 0
    || modelForm.depositAmount < 0
    || modelForm.mileageLimitPerDay <= 0
    || modelForm.extraMileageFee < 0
    || modelForm.powerHp <= 0
  ) {
    errorMessage.value = 'Uzupelnij poprawnie wszystkie pola modelu.'
    return
  }

  creatingModel.value = true

  try {
    await rentalApi.createCarModel({
      brand: modelForm.brand,
      model: modelForm.model,
      segment: modelForm.segment,
      pricePerDay: modelForm.pricePerDay,
      depositAmount: modelForm.depositAmount,
      mileageLimitPerDay: modelForm.mileageLimitPerDay,
      extraMileageFee: modelForm.extraMileageFee,
      powerHp: modelForm.powerHp,
      transmissionType: modelForm.transmissionType,
      fuelType: modelForm.fuelType,
    })

    successMessage.value = 'Model zostal dodany do katalogu.'
    await loadUnits()
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Nie udalo sie dodac modelu'
  }
  finally {
    creatingModel.value = false
  }
}

async function changeStatus(unitId: number, status: string) {
  try {
    await rentalApi.updateCarUnitStatus(unitId, status)
    await loadUnits()
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Nie udalo sie zmienic statusu pojazdu'
  }
}

onMounted(loadUnits)
</script>

<template>
  <v-container class="py-8">
    <h1 class="text-3xl font-bold mb-2">
      Zarzadzanie flota
    </h1>
    <p class="text-slate-500 mb-6">
      Statusy aut: na placu, u klienta lub w warsztacie.
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
            <v-text-field v-model="modelForm.brand" label="Marka" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="modelForm.model" label="Model" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="modelForm.segment" label="Segment" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="modelForm.pricePerDay" label="Cena za dzien" type="number" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="modelForm.depositAmount" label="Kaucja" type="number" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="modelForm.mileageLimitPerDay" label="Limit km / dzien" type="number" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="modelForm.extraMileageFee" label="Doplata za km" type="number" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model.number="modelForm.powerHp" label="Moc (KM)" type="number" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="modelForm.transmissionType" label="Skrzynia" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="modelForm.fuelType" label="Paliwo" />
          </v-col>
        </v-row>
      </v-card-text>
      <v-card-actions>
        <v-btn color="primary" :loading="creatingModel" @click="createModel">
          Dodaj model
        </v-btn>
      </v-card-actions>
    </v-card>

    <v-card class="mb-6">
      <v-card-title>Dodaj fizyczne auto</v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="3">
            <v-select
              v-model="newUnit.carModelId"
              :items="models"
              :item-title="item => `${item.brand} ${item.model}`"
              item-value="id"
              label="Model"
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="newUnit.licensePlate" label="Rejestracja" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="newUnit.vin" label="VIN" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model.number="newUnit.currentMileage" label="Przebieg" type="number" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="newUnit.color" label="Kolor" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model.number="newUnit.productionYear" label="Rocznik" type="number" />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field v-model="newUnit.imageUrl" label="URL zdjecia" />
          </v-col>
        </v-row>
      </v-card-text>
      <v-card-actions>
        <v-btn color="primary" @click="createUnit">
          Dodaj pojazd
        </v-btn>
      </v-card-actions>
    </v-card>

    <v-card>
      <v-card-title>Lista wszystkich aut</v-card-title>
      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Rejestracja</th>
            <th>Model</th>
            <th>Status</th>
            <th>Przebieg</th>
            <th>Akcja</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="unit in units" :key="unit.id">
            <td>{{ unit.id }}</td>
            <td>{{ unit.licensePlate }}</td>
            <td>{{ unit.modelName || '-' }}</td>
            <td>{{ unit.status }}</td>
            <td>{{ unit.currentMileage ?? '-' }}</td>
            <td>
              <v-menu>
                <template #activator="{ props }">
                  <v-btn size="small" variant="outlined" v-bind="props">
                    Zmien status
                  </v-btn>
                </template>
                <v-list>
                  <v-list-item
                    v-for="status in statusOptions"
                    :key="status"
                    :title="status"
                    @click="changeStatus(unit.id, status)"
                  />
                </v-list>
              </v-menu>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>
  </v-container>
</template>
