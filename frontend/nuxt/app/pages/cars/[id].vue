<script setup lang="ts">
const route = useRoute()
const rentalApi = useRentalApi()

const modelId = computed(() => Number(route.params.id))

const loading = ref(false)
const errorMessage = ref('')
const model = ref<Record<string, any> | null>(null)
const units = ref<Array<Record<string, any>>>([])
const reviews = ref<Array<Record<string, any>>>([])

const modelImage = computed(() => {
  const found = units.value.find(unit => typeof unit.imageUrl === 'string' && unit.imageUrl.trim())
  return found?.imageUrl?.trim() ?? ''
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

    model.value = modelData
    units.value = unitsData
    reviews.value = reviewsData
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Nie udało się pobrać szczegółów modelu'
  }
  finally {
    loading.value = false
  }
}

watch(modelId, loadDetails, { immediate: true })
</script>

<template>
  <v-container class="py-8" max-width="1160">
    <v-alert v-if="errorMessage" type="error" variant="tonal" class="mb-4">
      {{ errorMessage }}
    </v-alert>

    <v-skeleton-loader
      v-if="loading"
      type="article, table, article"
    />

    <template v-else-if="model">
      <v-card class="mb-6 overflow-hidden" rounded="xl">
        <v-row no-gutters>
          <v-col cols="12" md="6">
            <v-img
              :src="modelImage || undefined"
              height="320"
              cover
              class="bg-grey-lighten-3"
            >
              <template #placeholder>
                <div class="h-full w-full flex items-center justify-center text-medium-emphasis text-xs text-black">
                  Brak zdjęcia dla tego modelu
                </div>
              </template>
            </v-img>
          </v-col>
          <v-col cols="12" md="6" class="p-6 md:p-8">
            <h1 class="text-2xl font-semibold mb-2">
              {{ model.brand }} {{ model.model }}
            </h1>
            <p class="text-medium-emphasis mb-5">
              Segment {{ model.segment }} | {{ model.fuelType }} | {{ model.transmissionType }}
            </p>
            <v-chip color="primary" size="large">
              {{ model.pricePerDay }} PLN / dzień
            </v-chip>
          </v-col>
        </v-row>
      </v-card>

      <v-row>
        <v-col cols="12" lg="8">
          <v-card class="mb-6" rounded="lg">
            <v-card-title>Dostępne egzemplarze</v-card-title>
            <v-table>
              <thead>
                <tr>
                  <th>Rejestracja</th>
                  <th>Status</th>
                  <th>Kolor</th>
                  <th>Rocznik</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="unit in units" :key="unit.id">
                  <td>{{ unit.licensePlate }}</td>
                  <td>{{ unit.status }}</td>
                  <td>{{ unit.color || '-' }}</td>
                  <td>{{ unit.productionYear || '-' }}</td>
                </tr>
              </tbody>
            </v-table>
          </v-card>

          <v-card rounded="lg">
            <v-card-title>Opinie kierowców</v-card-title>
            <v-list>
              <v-list-item
                v-for="review in reviews"
                :key="review.id"
                :title="`${review.userEmail} • ${review.rating}/5`"
                :subtitle="review.comment"
              />
              <v-list-item v-if="!reviews.length" title="Brak opinii dla tego modelu" />
            </v-list>
          </v-card>
        </v-col>

        <v-col cols="12" lg="4">
          <v-card rounded="lg">
            <v-card-title>Specyfikacja</v-card-title>
            <v-list density="compact">
              <v-list-item title="Moc" :subtitle="`${model.powerHp || '-'} KM`" />
              <v-list-item title="Kaucja" :subtitle="`${model.depositAmount || '-'} PLN`" />
              <v-list-item title="Limit km / dzień" :subtitle="`${model.mileageLimitPerDay || '-'} km`" />
              <v-list-item title="Dopłata za nadmiar km" :subtitle="`${model.extraMileageFee || '-'} PLN`" />
            </v-list>
            <v-card-actions>
              <v-btn :to="`/client/reservations/new?carModelId=${model.id}`" color="primary" block>
                Zarezerwuj ten model
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </template>
  </v-container>
</template>
