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
  <div class="gradient-hero min-h-screen py-8 animate-fade-in">
    <v-container max-width="1160">
      <!-- Header -->
      <div class="d-flex flex-column flex-md-row justify-space-between align-md-center mb-8 ga-4">
        <div>
          <h1 class="text-3xl font-weight-black text-white">
            Zarządzanie flotą
          </h1>
          <p class="text-medium-emphasis mt-1">
            Dodaj fizyczne auta do floty i kontroluj ich statusy.
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
            to="/admin/catalog"
            prepend-icon="mdi-book-open-outline"
            class="font-weight-semibold"
          >
            Katalog modeli
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


      <!-- Add New Unit Form Card -->
      <v-card class="mb-8 glass-card" rounded="xl" variant="flat">
        <v-card-title class="px-6 pt-6 pb-2 text-xl font-weight-bold text-white d-flex align-center ga-2">
          <v-icon color="primary">mdi-plus-circle-outline</v-icon>
          Dodaj fizyczne auto
        </v-card-title>

        <v-card-text class="pa-6">
          <v-row>
            <v-col
              cols="12"
              md="3"
              class="py-2"
            >
              <v-autocomplete
                v-model="newUnit.carModelId"
                :items="modelSearchResults"
                :item-title="getModelLabel"
                item-value="id"
                label="Model samochodu"
                placeholder="Wpisz markę..."
                no-filter
                :loading="modelSearchLoading"
                variant="outlined"
                color="primary"
                hide-details
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
              class="py-2"
            >
              <v-text-field
                v-model="newUnit.licensePlate"
                label="Numer rejestracyjny"
                variant="outlined"
                color="primary"
                hide-details
              />
            </v-col>

            <v-col
              cols="12"
              md="3"
              class="py-2"
            >
              <v-text-field
                v-model="newUnit.vin"
                label="Numer VIN"
                variant="outlined"
                color="primary"
                hide-details
              />
            </v-col>

            <v-col
              cols="12"
              md="3"
              class="py-2"
            >
              <v-text-field
                v-model.number="newUnit.currentMileage"
                label="Przebieg (km)"
                type="number"
                variant="outlined"
                color="primary"
                hide-details
              />
            </v-col>

            <v-col
              cols="12"
              md="4"
              class="py-2"
            >
              <v-text-field
                v-model="newUnit.color"
                label="Kolor"
                variant="outlined"
                color="primary"
                hide-details
              />
            </v-col>

            <v-col
              cols="12"
              md="4"
              class="py-2"
            >
              <v-text-field
                v-model.number="newUnit.productionYear"
                label="Rocznik"
                type="number"
                variant="outlined"
                color="primary"
                hide-details
              />
            </v-col>

            <v-col
              cols="12"
              md="4"
              class="py-2"
            >
              <v-text-field
                v-model="newUnit.imageUrl"
                label="URL zdjęcia"
                variant="outlined"
                color="primary"
                hide-details
              />
            </v-col>
          </v-row>
        </v-card-text>

        <!-- Form Actions Bar -->
        <v-divider style="border-color: rgba(255, 255, 255, 0.05) !important;" />
        <v-card-actions class="px-6 py-4 bg-black/10">
          <v-btn
            color="primary"
            variant="flat"
            class="font-weight-semibold px-6"
            height="42"
            prepend-icon="mdi-plus"
            @click="createUnit"
          >
            Dodaj pojazd do bazy
          </v-btn>
        </v-card-actions>
      </v-card>

      <!-- Existing Fleet List Table -->
      <v-card class="glass-card" rounded="xl" variant="flat">
        <v-card-title class="px-6 pt-6 pb-2 text-xl font-weight-bold text-white d-flex align-center ga-2">
          <v-icon color="primary">mdi-car-multiple</v-icon>
          Zapisana flota pojazdów
        </v-card-title>

        <v-card-text class="pa-0">
          <v-table class="bg-transparent text-white" style="color: #f8fafc !important;">
            <thead>
              <tr>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5 pl-6">ID</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Rejestracja</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Model</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Status</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Przebieg</th>
                <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5 pr-6">Akcja</th>
              </tr>
            </thead>

            <tbody>
              <tr
                v-for="unit in units"
                :key="unit.id"
                class="hover:bg-white/2"
              >
                <td class="pl-6 font-weight-semibold text-white py-4">#{{ unit.id }}</td>
                <td class="font-weight-bold text-white py-4">
                  <span class="font-mono bg-white/5 px-2 py-1 rounded border border-white/10 text-sm">
                    {{ unit.licensePlate }}
                  </span>
                </td>
                <td class="font-weight-bold text-white py-4">{{ unit.modelName || '-' }}</td>
                <td class="py-4">
                  <v-chip
                    :color="unit.status === 'AVAILABLE' ? 'success' : unit.status === 'RENTED' ? 'info' : unit.status === 'RESERVED' ? 'warning' : 'error'"
                    size="small"
                    variant="tonal"
                    class="font-weight-semibold"
                  >
                    {{ unit.status }}
                  </v-chip>
                </td>
                <td class="text-medium-emphasis py-4">{{ unit.currentMileage ?? '-' }} km</td>
                <td class="py-4 pr-6">
                  <v-menu>
                    <template #activator="{props}">
                      <v-btn
                        size="small"
                        variant="outlined"
                        v-bind="props"
                        append-icon="mdi-chevron-down"
                        class="text-caption font-weight-semibold"
                        style="border-color: rgba(255, 255, 255, 0.1);"
                      >
                        Zmień status
                      </v-btn>
                    </template>

                    <v-list class="glass-panel" rounded="lg">
                      <v-list-item
                        v-for="status in statusOptions"
                        :key="status"
                        :title="status"
                        class="hover:bg-white/5 cursor-pointer text-white"
                        @click="changeStatus(unit.id, status)"
                      />
                    </v-list>
                  </v-menu>
                </td>
              </tr>
              <tr v-if="!units.length">
                <td colspan="6" class="text-center text-medium-emphasis py-6">
                  Brak pojazdów we flocie
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
