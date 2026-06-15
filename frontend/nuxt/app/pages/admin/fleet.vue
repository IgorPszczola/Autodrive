<script setup lang="ts">
definePageMeta({
  middleware: 'admin',
})

const rentalApi = useRentalApi()

function createDebouncedFn<T extends (...args: any[]) => any>(fn: T, delay: number) {
  let timeoutId: ReturnType<typeof setTimeout> | number | null = null

  return (...args: Parameters<T>) => {
    if (timeoutId !== null)
      clearTimeout(timeoutId)
    timeoutId = setTimeout(fn, delay, ...args)
  }
}

const statusOptions = ['AVAILABLE', 'RESERVED', 'RENTED', 'IN_REPAIR']
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const units = ref<Array<Record<string, any>>>([])
const models = ref<Array<Record<string, any>>>([])
const currentPage = ref(1)
const totalPages = ref(0)
const modelSearchResults = ref<Array<Record<string, any>>>([])
const modelSearchLoading = ref(false)
const selectedUnitModel = ref<Record<string, any> | null>(null)

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
    const pageData = await rentalApi.getCarModels({}, currentPage.value - 1, 10)
    models.value = pageData.content
    totalPages.value = pageData.totalPages

    // Pokaż modele z bieżącej strony w autocomplete
    modelSearchResults.value = models.value

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
      selectedUnitModel.value = models.value[0]
    }

    if (newUnit.carModelId) {
      const selected = models.value.find(model => model.id === newUnit.carModelId)
      if (selected) {
        selectedUnitModel.value = selected
      }
    }
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się pobrać danych floty'
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
    errorMessage.value = 'Uzupełnij poprawnie wszystkie pola pojazdu.'

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

    successMessage.value = 'Pojazd został dodany do floty.'
    await loadUnits()
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się dodać pojazdu'
  }
}

async function changeStatus(unitId: number, status: string) {
  try {
    await rentalApi.updateCarUnitStatus(unitId, status)
    await loadUnits()
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się zmienić statusu pojazdu'
  }
}

function getModelLabel(model: Record<string, any> | null): string {
  if (!model)
    return ''
  // Obsługuj zarówno top-level jak i zagnieżdżone pola carModel
  const brand = model?.brand || model?.carModel?.brand || ''
  const name = model?.model || model?.carModel?.model || ''

  return `${brand} ${name}`.trim()
}

function handleUnitModelSelection(modelId: number | null) {
  if (!modelId) {
    selectedUnitModel.value = null

    return
  }

  const selected = modelSearchResults.value.find(model => model.id === modelId)
    ?? models.value.find(model => model.id === modelId)

  if (selected) {
    selectedUnitModel.value = selected
    modelSearchResults.value = [selected]
  }
}

const searchModels = createDebouncedFn(async (query: string) => {
  if (!query.trim()) {
    modelSearchResults.value = selectedUnitModel.value
      ? [selectedUnitModel.value]
      : []

    return
  }

  modelSearchLoading.value = true

  try {
    const pageData = await rentalApi.getCarModels({ brand: query }, 0, 10)
    modelSearchResults.value = pageData.content
  }
  catch {
    modelSearchResults.value = []
  }
  finally {
    modelSearchLoading.value = false
  }
}, 300)

onMounted(loadUnits)

watch(currentPage, loadUnits)
</script>

<template>
  <v-container class="py-8">
    <h1 class="text-3xl font-bold mb-2">
      Zarządzanie flotą
    </h1>

    <p class="text-slate-500 mb-6">
      Dodaj fizyczne auta do floty.
    </p>

    <div class="mb-6 flex flex-wrap gap-2">
      <v-btn
        variant="outlined"
        to="/admin"
      >
        Powrót do panelu admina
      </v-btn>

      <v-btn
        color="primary"
        variant="tonal"
        to="/admin/catalog"
      >
        Przejdź do katalogu
      </v-btn>
    </div>

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
      class="mb-4"
    >
      {{ successMessage }}
    </v-alert>

    <v-card class="mb-6">
      <v-card-title>Dodaj fizyczne auto</v-card-title>

      <v-card-text>
        <v-row>
          <v-col
            cols="12"
            md="3"
          >
            <v-autocomplete
              v-model="newUnit.carModelId"
              :items="modelSearchResults"
              :item-title="getModelLabel"
              item-value="id"
              label="Model"
              placeholder="Wpisz markę..."
              no-filter
              :loading="modelSearchLoading"
              @update:model-value="handleUnitModelSelection"
              @update:search="searchModels"
            >
              <template #item="{props, item}">
                <v-list-item
                  v-bind="props"
                  :subtitle="item?.raw
                    ? `${item.raw.pricePerDay || 0} PLN / dzień`
                    : ''"
                />
              </template>
            </v-autocomplete>
          </v-col>

          <v-col
            cols="12"
            md="3"
          >
            <v-text-field
              v-model="newUnit.licensePlate"
              label="Rejestracja"
            />
          </v-col>

          <v-col
            cols="12"
            md="3"
          >
            <v-text-field
              v-model="newUnit.vin"
              label="VIN"
            />
          </v-col>

          <v-col
            cols="12"
            md="3"
          >
            <v-text-field
              v-model.number="newUnit.currentMileage"
              label="Przebieg"
              type="number"
            />
          </v-col>

          <v-col
            cols="12"
            md="4"
          >
            <v-text-field
              v-model="newUnit.color"
              label="Kolor"
            />
          </v-col>

          <v-col
            cols="12"
            md="4"
          >
            <v-text-field
              v-model.number="newUnit.productionYear"
              label="Rocznik"
              type="number"
            />
          </v-col>

          <v-col
            cols="12"
            md="4"
          >
            <v-text-field
              v-model="newUnit.imageUrl"
              label="URL zdjęcia"
            />
          </v-col>
        </v-row>
      </v-card-text>

      <v-card-actions>
        <v-btn
          color="primary"
          @click="createUnit"
        >
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
          <tr
            v-for="unit in units"
            :key="unit.id"
          >
            <td>{{ unit.id }}</td>

            <td>{{ unit.licensePlate }}</td>

            <td>{{ unit.modelName || '-' }}</td>

            <td>{{ unit.status }}</td>

            <td>{{ unit.currentMileage ?? '-' }}</td>

            <td>
              <v-menu>
                <template #activator="{props}">
                  <v-btn
                    size="small"
                    variant="outlined"
                    v-bind="props"
                  >
                    Zmień status
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

      <div
        v-if="totalPages > 1"
        class="pa-4 flex justify-center"
      >
        <v-pagination
          v-model="currentPage"
          :length="totalPages"
        />
      </div>
    </v-card>
  </v-container>
</template>
