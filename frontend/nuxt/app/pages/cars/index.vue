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

const visibleModels = computed(() => {
  const search = filters.search.trim().toLowerCase()
  if (!search) {
    return models.value
  }

  return models.value.filter((car) => {
    const searchable = `${car.brand ?? ''} ${car.model ?? ''}`.toLowerCase()

    return searchable.includes(search)
  })
})

function createDebouncedFn<T extends (...args: any[]) => void>(fn: T, delay: number) {
  let timeoutId: ReturnType<typeof setTimeout> | null = null

  return (...args: Parameters<T>) => {
    if (timeoutId !== null) {
      clearTimeout(timeoutId)
    }
    timeoutId = setTimeout(fn, delay, ...args)
  }
}

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
      currentPage.value - 1,
      10,
    )

    models.value = pageData.content
    totalPages.value = pageData.totalPages

    const modelUnits = await Promise.all(
      pageData.content.map(async model => [model.id, await rentalApi.getCarModelUnits(model.id)] as const),
    )

    modelUnits.forEach(([id, units]) => {
      carImage.setCachedModelImage(id, carImage.getFirstUnitImage(units))
    })
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

onMounted(async () => {
  await Promise.all([
    loadFilterOptions(),
    loadModels(),
  ])
})

watch(currentPage, loadModels)

const triggerDebouncedLoad = createDebouncedFn(() => {
  if (currentPage.value !== 1) {
    currentPage.value = 1

    return
  }
  loadModels()
}, 300)

watch(
  () => [filters.search, filters.brand, filters.segment, filters.maxPrice, filters.sortBy, filters.sortDir],
  () => {
    triggerDebouncedLoad()
  },
)
</script>

<template>
  <v-container
    class="py-8"
    max-width="1160"
  >
    <div class="mb-6">
      <h1 class="text-2xl font-semibold">
        Katalog samochodów
      </h1>

      <p class="text-medium-emphasis mt-1">
        Przeglądaj modele, filtruj i sprawdź szczegóły przed rezerwacją.
      </p>
    </div>

    <v-card
      class="mb-6"
      rounded="lg"
    >
      <v-card-text>
        <v-row>
          <v-col cols="12">
            <v-text-field
              v-model="filters.search"
              label="Szukaj: marka lub model"
              hide-details
            />
          </v-col>

          <v-col
            cols="12"
            md="4"
          >
            <v-select
              v-model="filters.brand"
              :items="brandOptions"
              label="Marka"
              hide-details
              clearable
            />
          </v-col>

          <v-col
            cols="12"
            md="2"
          >
            <v-select
              v-model="filters.segment"
              :items="segmentOptions"
              label="Segment"
              hide-details
              clearable
            />
          </v-col>

          <v-col
            cols="12"
            md="2"
          >
            <v-text-field
              v-model="filters.maxPrice"
              label="Maks. cena"
              type="number"
              hide-details
            />
          </v-col>

          <v-col
            cols="12"
            md="2"
          >
            <v-select
              v-model="filters.sortBy"
              label="Sortuj po"
              :items="sortByOptions"
              hide-details
            />
          </v-col>

          <v-col
            cols="12"
            md="2"
          >
            <v-select
              v-model="filters.sortDir"
              label="Kierunek"
              :items="sortDirOptions"
              hide-details
            />
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-alert
      v-if="errorMessage"
      type="error"
      variant="tonal"
      class="mb-4"
    >
      {{ errorMessage }}
    </v-alert>

    <v-row>
      <v-col
        v-for="car in visibleModels"
        :key="car.id"
        cols="12"
        md="6"
      >
        <v-card
          class="h-full overflow-hidden"
          rounded="lg"
        >
          <v-img
            :src="getModelImage(car.id) || undefined"
            height="220"
            cover
            class="bg-grey-lighten-3"
          >
            <template #placeholder>
              <div class="text-medium-emphasis text-xs text-black flex h-full w-full items-center justify-center">
                Brak zdjęcia
              </div>
            </template>
          </v-img>

          <v-card-title class="py-4">
            <div class="flex gap-3 w-full items-start justify-between">
              <div>
                <div class="font-weight-medium text-xl leading-tight">
                  {{ car.brand }} {{ car.model }}
                </div>

                <div class="text-medium-emphasis text-sm mt-1 flex gap-4">
                  <span>
                    <v-icon color="blue">mdi-gas-station</v-icon> {{ car.fuelType }}
                  </span>

                  <span>
                    <v-icon color="blue">mdi-cog</v-icon> {{ car.transmissionType }}
                  </span>

                  <span>
                    <v-icon color="blue">mdi-car-speed-limiter</v-icon> {{ car.powerHp }} KM
                  </span>
                </div>
              </div>

              <v-chip
                color="primary"
                size="small"
              >
                {{ car.segment }}
              </v-chip>
            </div>
          </v-card-title>

          <v-divider />

          <v-card-text class="pt-0">
            <p class="font-weight-medium text-base mb-0">
              <span class="text-sm">Cena (za dzień):</span> {{ car.pricePerDay }} PLN
            </p>
          </v-card-text>

          <v-card-actions class="pt-0">
            <v-btn
              :to="`/cars/${car.id}`"
              color="primary"
              variant="text"
            >
              Szczegóły
            </v-btn>

            <v-btn
              :to="`/client/reservations/new?carModelId=${car.id}`"
              variant="text"
            >
              Rezerwuj
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <div
      v-if="totalPages > 1"
      class="mt-6 flex justify-center"
    >
      <v-pagination
        v-model="currentPage"
        :length="totalPages"
      />
    </div>
  </v-container>
</template>
