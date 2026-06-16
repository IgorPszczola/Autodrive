<script setup lang="ts">
import { useCarImage } from '~/composables/useCarImage'

const route = useRoute()
const rentalApi = useRentalApi()
const carImage = useCarImage()

const modelId = computed(() => Number(route.params.id))

const loading = ref(false)
const errorMessage = ref('')
const model = ref<Record<string, any> | null>(null)
const units = ref<Array<Record<string, any>>>([])
const reviews = ref<Array<Record<string, any>>>([])

const modelImage = computed(() => {
  return carImage.getFirstUnitImage(units.value)
})

async function loadDetails() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [modelData, unitsData, reviewsData] = await Promise.all([
      rentalApi.getCarModel(modelId.value),
      rentalApi.getCarModelUnits(modelId.value),
      rentalApi.getModelReviews(modelId.value),
    ])

    model.value = modelData ?? null
    units.value = unitsData
    reviews.value = reviewsData
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się pobrać szczegółów modelu'
  }
  finally {
    loading.value = false
  }
}

watch(modelId, loadDetails, { immediate: true })
</script>

<template>
  <div class="gradient-hero min-h-screen py-8 animate-fade-in">
    <v-container max-width="1160">
      <!-- Error Alert -->
      <v-alert
        v-if="errorMessage"
        type="error"
        variant="tonal"
        class="mb-6"
        rounded="lg"
      >
        {{ errorMessage }}
      </v-alert>

      <!-- Skeleton Loader -->
      <div v-if="loading" class="py-12">
        <v-skeleton-loader type="card, article, table" class="glass-card" />
      </div>

      <!-- Content -->
      <template v-else-if="model">
        <!-- Main Car Info Card -->
        <v-card
          class="mb-8 overflow-hidden glass-card"
          rounded="xl"
          variant="flat"
        >
          <v-row no-gutters>
            <!-- Car Image -->
            <v-col cols="12" md="6">
              <v-img
                :src="modelImage || undefined"
                height="360"
                cover
                class="bg-grey-darken-4 brightness-95"
              >
                <template #placeholder>
                  <div class="d-flex h-full w-full items-center justify-center text-medium-emphasis text-sm bg-slate-900">
                    <v-icon size="48" class="mr-2">mdi-car-outline</v-icon>
                    Brak zdjęcia modelu
                  </div>
                </template>
              </v-img>
            </v-col>

            <!-- Car Hero Details -->
            <v-col
              cols="12"
              md="6"
              class="pa-8 d-flex flex-column justify-center"
            >
              <div class="d-flex ga-2 mb-3">
                <v-chip
                  v-if="model.minRentDays > 1"
                  color="warning"
                  variant="flat"
                  size="small"
                  class="font-weight-bold"
                >
                  Min. {{ model.minRentDays }} dni najmu
                </v-chip>
                <v-chip
                  color="primary"
                  variant="flat"
                  size="small"
                  class="font-weight-bold"
                >
                  Segment {{ model.segment }}
                </v-chip>
              </div>

              <h1 class="text-4xl font-weight-black text-white mb-4 leading-tight">
                {{ model.brand }} {{ model.model }}
              </h1>

              <!-- Tech Row -->
              <div class="text-medium-emphasis text-sm d-flex ga-4 mb-6 flex-wrap border-b border-white/5 pb-4">
                <span class="d-flex align-center ga-1">
                  <v-icon color="primary" size="18">mdi-gas-station-outline</v-icon>
                  {{ model.fuelType }}
                </span>

                <span class="d-flex align-center ga-1">
                  <v-icon color="primary" size="18">mdi-cog-outline</v-icon>
                  {{ model.transmissionType }}
                </span>

                <span class="d-flex align-center ga-1">
                  <v-icon color="primary" size="18">mdi-engine-outline</v-icon>
                  {{ model.powerHp }} KM
                </span>
              </div>

              <!-- Price Box -->
              <div class="d-flex align-end justify-space-between mb-2">
                <div>
                  <span class="text-xs text-medium-emphasis d-block uppercase font-weight-bold mb-1" style="letter-spacing: 0.05em;">Cena za dzień</span>
                  <span class="text-3xl font-weight-black text-primary">{{ model.pricePerDay }} PLN</span>
                </div>
              </div>
            </v-col>
          </v-row>
        </v-card>

        <v-row>
          <!-- Left Column (Units and Reviews) -->
          <v-col cols="12" lg="8">
            <!-- Available Units Table -->
            <v-card
              class="mb-6 glass-card"
              rounded="xl"
              variant="flat"
            >
              <v-card-title class="px-6 pt-6 pb-2 text-xl font-weight-bold text-white d-flex align-center ga-2">
                <v-icon color="primary">mdi-format-list-bulleted</v-icon>
                Dostępne egzemplarze we flocie
              </v-card-title>

              <v-card-text class="pa-0">
                <v-table class="bg-transparent text-white" style="color: #f8fafc !important;">
                  <thead>
                    <tr>
                      <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Tablica rejestracyjna</th>
                      <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Status</th>
                      <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Kolor</th>
                      <th class="text-left font-weight-bold text-medium-emphasis border-b border-white/5">Rocznik</th>
                    </tr>
                  </thead>

                  <tbody>
                    <tr
                      v-for="unit in units"
                      :key="unit.id"
                      class="hover:bg-white/2"
                    >
                      <td class="font-weight-bold text-white py-4">{{ unit.licensePlate }}</td>
                      <td class="py-4">
                        <v-chip
                          :color="unit.status === 'AVAILABLE' ? 'success' : unit.status === 'RENTED' ? 'info' : 'warning'"
                          size="small"
                          variant="tonal"
                          class="font-weight-medium"
                        >
                          {{ unit.status === 'AVAILABLE' ? 'Dostępny' : unit.status === 'RENTED' ? 'Wypożyczony' : unit.status }}
                        </v-chip>
                      </td>
                      <td class="text-medium-emphasis py-4">{{ unit.color || '-' }}</td>
                      <td class="text-medium-emphasis py-4">{{ unit.productionYear || '-' }}</td>
                    </tr>
                    <tr v-if="!units.length">
                      <td colspan="4" class="text-center text-medium-emphasis py-6">
                        Brak zarejestrowanych pojazdów dla tego modelu
                      </td>
                    </tr>
                  </tbody>
                </v-table>
              </v-card-text>
            </v-card>

            <!-- Reviews List -->
            <v-card
              rounded="xl"
              class="glass-card"
              variant="flat"
            >
              <v-card-title class="px-6 pt-6 pb-4 text-xl font-weight-bold text-white d-flex align-center ga-2">
                <v-icon color="primary">mdi-star-outline</v-icon>
                Opinie kierowców ({{ reviews.length }})
              </v-card-title>

              <v-card-text class="px-6 pb-6 pt-0">
                <div class="d-flex flex-column ga-4">
                  <v-card
                    v-for="review in reviews"
                    :key="review.id"
                    variant="flat"
                    color="transparent"
                    class="pa-4 rounded-lg border border-white/5 bg-white/2"
                  >
                    <div class="d-flex align-center justify-space-between mb-2">
                      <div class="font-weight-bold text-white text-sm d-flex align-center">
                        <v-avatar color="primary" size="28" class="mr-2 text-xs font-weight-bold">
                          {{ review.userEmail ? review.userEmail.charAt(0).toUpperCase() : 'U' }}
                        </v-avatar>
                        {{ review.userEmail }}
                      </div>
                      <v-rating
                        :model-value="review.rating"
                        readonly
                        density="compact"
                        color="amber"
                        size="18"
                      />
                    </div>
                    <p class="text-medium-emphasis text-sm mb-0 style-italic">
                      "{{ review.comment }}"
                    </p>
                  </v-card>

                  <div v-if="!reviews.length" class="text-center text-medium-emphasis py-8 border border-dashed border-white/10 rounded-lg">
                    <v-icon size="32" class="mb-2 text-medium-emphasis">mdi-comment-text-multiple-outline</v-icon>
                    <p class="mb-0 text-sm">Brak opinii dla tego modelu samochodów</p>
                  </div>
                </div>
              </v-card-text>
            </v-card>
          </v-col>

          <!-- Right Column (Specifications and Checkout Card) -->
          <v-col cols="12" lg="4">
            <v-card
              rounded="xl"
              class="pa-6 glass-card position-sticky"
              style="top: 88px;"
              variant="flat"
            >
              <h2 class="text-lg font-weight-bold text-white mb-4 d-flex align-center ga-2">
                <v-icon color="primary">mdi-playlist-check</v-icon>
                Szczegóły techniczne
              </h2>

              <v-list density="compact" class="bg-transparent pa-0 mb-6">
                <v-list-item class="px-0 py-2 border-b border-white/5">
                  <template #title>
                    <span class="text-sm text-medium-emphasis">Moc silnika</span>
                  </template>
                  <template #subtitle>
                    <span class="text-base font-weight-bold text-white">{{ model.powerHp || '-' }} KM</span>
                  </template>
                </v-list-item>

                <v-list-item class="px-0 py-2 border-b border-white/5">
                  <template #title>
                    <span class="text-sm text-medium-emphasis">Kaucja zwrotna</span>
                  </template>
                  <template #subtitle>
                    <span class="text-base font-weight-bold text-white">{{ model.depositAmount || '-' }} PLN</span>
                  </template>
                </v-list-item>

                <v-list-item class="px-0 py-2 border-b border-white/5">
                  <template #title>
                    <span class="text-sm text-medium-emphasis">Limit kilometrów / dzień</span>
                  </template>
                  <template #subtitle>
                    <span class="text-base font-weight-bold text-white">{{ model.mileageLimitPerDay || '-' }} km</span>
                  </template>
                </v-list-item>

                <v-list-item class="px-0 py-2 border-b border-white/5">
                  <template #title>
                    <span class="text-sm text-medium-emphasis">Stawka za nadprzebieg</span>
                  </template>
                  <template #subtitle>
                    <span class="text-base font-weight-bold text-white">{{ model.extraMileageFee || '-' }} PLN / km</span>
                  </template>
                </v-list-item>

                <v-list-item v-if="model.minRentDays > 1" class="px-0 py-2 border-b border-white/5">
                  <template #title>
                    <span class="text-sm text-medium-emphasis">Minimalny czas najmu</span>
                  </template>
                  <template #subtitle>
                    <span class="text-base font-weight-bold text-warning">{{ model.minRentDays }} dni</span>
                  </template>
                </v-list-item>
              </v-list>

              <v-btn
                :to="`/client/reservations/new?carModelId=${model.id}`"
                color="primary"
                block
                size="large"
                variant="flat"
                class="font-weight-semibold"
                height="50"
              >
                Zarezerwuj ten model
              </v-btn>
            </v-card>
          </v-col>
        </v-row>
      </template>
    </v-container>
  </div>
</template>
