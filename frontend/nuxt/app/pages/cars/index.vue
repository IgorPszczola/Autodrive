<script setup lang="ts">
import { useCarImage } from '~/composables/useCarImage'

const rentalApi = useRentalApi()
const carImage = useCarImage()

const filters = reactive({
  search: '',
  brand: '',
  segment: '',
  maxPrice: '',
  sortBy: 'pricePerDay',
  sortDir: 'asc',
})

const loading = ref(false)
const errorMessage = ref('')
const models = ref<Array<Record<string, any>>>([])
const brandOptions = ref<string[]>([])
const segmentOptions = ref<string[]>([])
const currentPage = ref(1)
const totalPages = ref(0)

const sortByOptions = [
  { title: 'Cena za dzień', value: 'pricePerDay' },
]

const sortDirOptions = [
  { title: 'Rosnąco', value: 'asc' },
  { title: 'Malejąco', value: 'desc' },
]

const allModels = ref<Array<Record<string, any>>>([])

const filteredModels = computed(() => {
  const search = filters.search.trim().toLowerCase()
  if (!search) {
    return allModels.value
  }

  return allModels.value.filter((car) => {
    const searchable = `${car.brand ?? ''} ${car.model ?? ''}`.toLowerCase()

    return searchable.includes(search)
  })
})

const visibleModels = computed(() => {
  const start = (currentPage.value - 1) * 10
  const end = start + 10
  return filteredModels.value.slice(start, end)
})

function getModelImage(modelId: number): string {
  return carImage.getCachedModelImage(modelId)
}

async function loadModels() {
  loading.value = true
  errorMessage.value = ''

  try {
    const pageData = await rentalApi.getCarModels(
      {
        brand: filters.brand || undefined,
        segment: filters.segment || undefined,
        maxPrice: filters.maxPrice
          ? Number(filters.maxPrice)
          : undefined,
        sortBy: filters.sortBy,
        sortDir: filters.sortDir,
      },
      0,
      1000,
    )

    allModels.value = pageData.content ?? []
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się pobrać katalogu'
  }
  finally {
    loading.value = false
  }
}

async function loadFilterOptions() {
  try {
    const [brands, segments] = await Promise.all([
      rentalApi.getCarBrands(),
      rentalApi.getCarSegments(),
    ])

    brandOptions.value = brands
    segmentOptions.value = segments
  }
  catch {}
}

async function loadModelImages(modelsList: any[]) {
  try {
    const modelUnits = await Promise.all(
      modelsList.map(async model => [model.id, await rentalApi.getCarModelUnits(model.id)] as const),
    )

    modelUnits.forEach(([id, units]) => {
      carImage.setCachedModelImage(id, carImage.getFirstUnitImage(units))
    })
  }
  catch (error) {
    console.error('Failed to load images:', error)
  }
}

onMounted(async () => {
  await Promise.all([
    loadFilterOptions(),
    loadModels(),
  ])
})

watch(filteredModels, (newVal) => {
  totalPages.value = Math.ceil(newVal.length / 10)
  if (currentPage.value > totalPages.value) {
    currentPage.value = Math.max(1, totalPages.value)
  }
}, { immediate: true })

watch(visibleModels, (newVal) => {
  if (newVal && newVal.length > 0) {
    loadModelImages(newVal)
  }
}, { immediate: true })

watch(
  () => [filters.brand, filters.segment, filters.maxPrice, filters.sortBy, filters.sortDir],
  () => {
    currentPage.value = 1
    loadModels()
  },
)

watch(
  () => filters.search,
  () => {
    currentPage.value = 1
  },
)
</script>

<template>
  <div class="gradient-hero min-h-screen py-10 animate-fade-in">
    <v-container max-width="1160">
      <!-- Page Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-weight-black text-white">
          Katalog samochodów
        </h1>
        <p class="text-medium-emphasis mt-1">
          Przeglądaj naszą flotę modeli premium, filtruj na żywo i rezerwuj natychmiast.
        </p>
      </div>

      <!-- Filters Panel -->
      <v-card
        class="mb-8 glass-card"
        rounded="xl"
        variant="flat"
      >
        <v-card-text class="pa-6">
          <v-row>
            <v-col cols="12" class="pb-2">
              <v-text-field
                v-model="filters.search"
                label="Szukaj marki lub modelu..."
                prepend-inner-icon="mdi-magnify"
                variant="outlined"
                color="primary"
                hide-details
                clearable
              />
            </v-col>

            <v-col cols="12" md="4" class="py-2">
              <v-select
                v-model="filters.brand"
                :items="brandOptions"
                label="Marka"
                prepend-inner-icon="mdi-watermark"
                variant="outlined"
                color="primary"
                hide-details
                clearable
              />
            </v-col>

            <v-col cols="12" md="2" class="py-2">
              <v-select
                v-model="filters.segment"
                :items="segmentOptions"
                label="Segment"
                prepend-inner-icon="mdi-shape-outline"
                variant="outlined"
                color="primary"
                hide-details
                clearable
              />
            </v-col>

            <v-col cols="12" md="2" class="py-2">
              <v-text-field
                v-model="filters.maxPrice"
                label="Maks. cena (PLN)"
                type="number"
                prepend-inner-icon="mdi-cash"
                variant="outlined"
                color="primary"
                hide-details
                clearable
              />
            </v-col>

            <v-col cols="12" md="2" class="py-2">
              <v-select
                v-model="filters.sortBy"
                label="Sortuj według"
                :items="sortByOptions"
                prepend-inner-icon="mdi-sort"
                variant="outlined"
                color="primary"
                hide-details
              />
            </v-col>

            <v-col cols="12" md="2" class="py-2">
              <v-select
                v-model="filters.sortDir"
                label="Kierunek"
                :items="sortDirOptions"
                prepend-inner-icon="mdi-swap-vertical"
                variant="outlined"
                color="primary"
                hide-details
              />
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Alert Message -->
      <v-alert
        v-if="errorMessage"
        type="error"
        variant="tonal"
        class="mb-6"
        rounded="lg"
      >
        {{ errorMessage }}
      </v-alert>

      <!-- Loader State -->
      <div v-if="loading && visibleModels.length === 0" class="d-flex justify-center py-12">
        <v-progress-circular indeterminate color="primary" size="48" />
      </div>

      <!-- Cars Grid -->
      <v-row v-else>
        <v-col
          v-for="car in visibleModels"
          :key="car.id"
          cols="12"
          md="6"
        >
          <v-card
            class="glass-card d-flex flex-column h-fit"
            rounded="xl"
            variant="flat"
          >
            <div class="position-relative">
              <v-img
                :src="getModelImage(car.id) || undefined"
                height="240"
                cover
                class="bg-grey-darken-4 brightness-95"
              >
                <template #placeholder>
                  <div class="d-flex h-full w-full items-center justify-center text-medium-emphasis text-sm bg-slate-900">
                    <v-icon size="40" class="mr-2">mdi-car-outline</v-icon>
                    Brak zdjęcia modelu
                  </div>
                </template>
              </v-img>
              <!-- Segment Tag -->
              <div class="position-absolute top-0 right-0 pa-4 d-flex ga-2">
                <v-chip
                  v-if="car.minRentDays > 1"
                  color="warning"
                  variant="flat"
                  size="small"
                  class="font-weight-bold"
                >
                  Min. {{ car.minRentDays }} dni
                </v-chip>
                <v-chip
                  color="primary"
                  variant="flat"
                  size="small"
                  class="font-weight-bold"
                >
                  Klasa {{ car.segment }}
                </v-chip>
              </div>
            </div>

            <v-card-title class="px-6 pt-5 pb-2">
              <div class="text-2xl font-weight-black text-white leading-tight">
                {{ car.brand }} {{ car.model }}
              </div>
            </v-card-title>

            <v-card-text class="px-6 pb-4 pt-0 flex-grow-1">
              <!-- Technical Specs Icons Row -->
              <div class="text-medium-emphasis text-sm d-flex ga-4 mb-4 flex-wrap border-b border-white/5 pb-4">
                <span class="d-flex align-center ga-1">
                  <v-icon color="primary" size="18">mdi-gas-station-outline</v-icon>
                  {{ car.fuelType }}
                </span>

                <span class="d-flex align-center ga-1">
                  <v-icon color="primary" size="18">mdi-cog-outline</v-icon>
                  {{ car.transmissionType }}
                </span>

                <span class="d-flex align-center ga-1">
                  <v-icon color="primary" size="18">mdi-engine-outline</v-icon>
                  {{ car.powerHp }} KM
                </span>
              </div>

              <!-- Price Box -->
              <div class="d-flex justify-space-between align-center pt-2">
                <div>
                  <div class="text-xs text-medium-emphasis uppercase font-weight-bold mb-1" style="letter-spacing: 0.05em;">Kaucja</div>
                  <div class="text-base font-weight-semibold text-white">{{ car.depositAmount }} PLN</div>
                </div>
                <div class="text-right">
                  <div class="text-xs text-medium-emphasis uppercase font-weight-bold mb-1" style="letter-spacing: 0.05em;">Cena za dzień</div>
                  <div class="text-2xl font-weight-black text-primary">{{ car.pricePerDay }} PLN</div>
                </div>
              </div>
            </v-card-text>

            <v-divider style="border-color: rgba(255, 255, 255, 0.05) !important;" />

            <v-card-actions class="px-6 py-4 ga-2">
              <v-btn
                :to="`/cars/${car.id}`"
                color="secondary"
                variant="outlined"
                class="flex-1 font-weight-semibold"
                height="42"
                style="border-color: rgba(255, 255, 255, 0.1);"
              >
                Szczegóły
              </v-btn>

              <v-btn
                :to="`/client/reservations/new?carModelId=${car.id}`"
                color="primary"
                variant="flat"
                class="flex-1 font-weight-semibold"
                height="42"
              >
                Rezerwuj
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>

      <!-- Pagination -->
      <div
        v-if="totalPages > 1"
        class="mt-10 d-flex justify-center"
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
    </v-container>
  </div>
</template>
