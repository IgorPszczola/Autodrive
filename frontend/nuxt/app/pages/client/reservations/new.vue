<script setup lang="ts">
import { useCarImage } from '~/composables/useCarImage'

definePageMeta({
  middleware: 'auth',
})

const route = useRoute()
const router = useRouter()
const rentalApi = useRentalApi()
const carImage = useCarImage()

function getLocalDate(offsetDays = 0): string {
  const date = new Date()
  date.setDate(date.getDate() + offsetDays)
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset())

  return date.toISOString().slice(0, 10)
}

function createDebouncedFn<T extends (...args: any[]) => any>(fn: T, delay: number) {
  let timeoutId: ReturnType<typeof setTimeout> | number | null = null

  return (...args: Parameters<T>) => {
    if (timeoutId !== null)
      clearTimeout(timeoutId)
    timeoutId = setTimeout(fn, delay, ...args)
  }
}

const step = ref(1)
const isMounted = ref(false)
const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const modelSearchLoading = ref(false)

const form = reactive({
  carModelId: Number(route.query.carModelId || 0),
  startDate: getLocalDate(0),
  endDate: getLocalDate(1),
  insuranceVariantId: 0,
  addonIds: [] as number[],
})

const modelSearchResults = ref<Array<Record<string, any>>>([])
const preselectedModel = ref<Record<string, any> | null>(null)
const selectedModelImage = ref('')
const insuranceVariants = ref<Array<Record<string, any>>>([])
const addons = ref<Array<Record<string, any>>>([])

const selectedModel = computed(() => {
  const found = modelSearchResults.value.find(model => model.id === form.carModelId)
  if (found)
    return found
  if (form.carModelId && preselectedModel.value?.id === form.carModelId)
    return preselectedModel.value

  return null
})
const selectedAddons = computed(() => addons.value.filter(addon => form.addonIds.includes(addon.id)))

const daysCount = computed(() => {
  if (!form.startDate || !form.endDate) {
    return 0
  }

  const start = new Date(form.startDate).getTime()
  const end = new Date(form.endDate).getTime()
  const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24))

  return diff > 0
    ? diff
    : 0
})

const estimatedBase = computed(() => {
  if (!selectedModel.value || !daysCount.value) {
    return 0
  }

  return Number(selectedModel.value.pricePerDay || 0) * daysCount.value
})

const estimatedInsurance = computed(() => {
  const selected = insuranceVariants.value.find(item => item.id === form.insuranceVariantId)
  if (!selected || !daysCount.value) {
    return 0
  }

  return Number(selected.pricePerDay || 0) * daysCount.value
})

const estimatedAddons = computed(() => selectedAddons.value.reduce((total, addon) => {
  return total + Number(addon.pricePerDay || 0) * daysCount.value
}, 0))

const estimatedTotal = computed(() => estimatedBase.value + estimatedInsurance.value + estimatedAddons.value)
const showSubmitButton = computed(() => isMounted.value && Number(step.value) >= 3)

const getModelLabel = (model: Record<string, any>): string => `${model?.brand ?? ''} ${model?.model ?? ''}`.trim()

function handleModelSelection(modelId: number | null) {
  if (!modelId) {
    preselectedModel.value = null
    selectedModelImage.value = ''

    return
  }

  const selected = modelSearchResults.value.find(model => model.id === modelId)
  if (selected) {
    preselectedModel.value = selected
  }
}

async function ensureModelOptionsLoaded() {
  modelSearchResults.value = (await rentalApi.getCarModels({}, 0, 10)).content
}

async function loadSelectedModelImage(modelId: number) {
  if (!modelId) {
    selectedModelImage.value = ''

    return
  }

  try {
    selectedModelImage.value = await carImage.fetchModelImage(modelId)
  }
  catch {
    selectedModelImage.value = ''
  }
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [insuranceData, addonData, modelsData] = await Promise.all([
      rentalApi.getInsuranceVariants(),
      rentalApi.getAddons(),
      rentalApi.getCarModels({}, 0, 10),
    ])

    insuranceVariants.value = insuranceData
    addons.value = addonData
    modelSearchResults.value = modelsData.content

    if (form.carModelId > 0) {
      try {
        const modelData = await rentalApi.getCarModel(form.carModelId)
        if (modelData) {
          preselectedModel.value = modelData
          modelSearchResults.value = [modelData]
        }
        await loadSelectedModelImage(form.carModelId)
      }
      catch (err) {
        console.error('Błąd przy ładowaniu modelu:', err)
      }
    }

    if (!form.insuranceVariantId && insuranceData[0]) {
      form.insuranceVariantId = insuranceData[0].id
    }
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się pobrać danych kreatora'
  }
  finally {
    loading.value = false
  }
}

async function submitReservation() {
  if (!form.startDate || !form.endDate) {
    errorMessage.value = 'Uzupełnij datę rozpoczęcia i zakończenia rezerwacji.'

    return
  }

  if (!form.carModelId) {
    errorMessage.value = 'Wybierz model samochodu'

    return
  }

  if (!form.insuranceVariantId) {
    errorMessage.value = 'Wybierz wariant ubezpieczenia.'

    return
  }

  if (new Date(form.endDate).getTime() < new Date(form.startDate).getTime()) {
    errorMessage.value = 'Data zakończenia nie może być wcześniejsza niż data rozpoczęcia.'

    return
  }

  submitting.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    await rentalApi.createReservation(form.carModelId, {
      id: 0,
      startDate: form.startDate,
      endDate: form.endDate,
      addonIds: form.addonIds,
      insuranceVariantId: form.insuranceVariantId,
    })

    successMessage.value = 'Rezerwacja została utworzona.'
    await router.push('/client/account')
  }
  catch (error) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Nie udało się utworzyć rezerwacji'
  }
  finally {
    submitting.value = false
  }
}

const searchModels = createDebouncedFn(async (query: string) => {
  if (!query.trim()) {
    if (modelSearchResults.value.length > 0) {
      return
    }

    try {
      await ensureModelOptionsLoaded()
    }
    catch {}

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

watch(() => form.carModelId, async modelId => await loadSelectedModelImage(modelId))
onMounted(async () => {
  isMounted.value = true
  await loadData()
})
</script>

<template>
  <v-container
    class="py-8"
    max-width="920"
  >
    <h1 class="text-2xl font-semibold mb-1">
      Kreator rezerwacji
    </h1>

    <p class="text-medium-emphasis mb-6">
      Krok 1: Daty, Krok 2: Dodatki i ubezpieczenie, Krok 3: Podsumowanie kosztów.
    </p>

    <v-alert
      v-if="errorMessage"
      type="error"
      variant="tonal"
      class="mb-4"
    >
      {{ errorMessage }}
    </v-alert>

    <v-alert
      v-if="successMessage"
      type="success"
      variant="tonal"
      class="mb-4"
    >
      {{ successMessage }}
    </v-alert>

    <v-card
      variant="outlined"
      rounded="lg"
    >
      <v-card-text class="pt-5">
        <v-stepper
          v-model="step"
          :items="[
            'Daty',
            'Dodatki',
            'Podsumowanie',
          ]"
          hide-actions
        >
          <template #[`item.1`]>
            <div class="pt-4 gap-4 grid md:grid-cols-2">
              <v-autocomplete
                v-model="form.carModelId"
                :items="modelSearchResults"
                :item-title="getModelLabel"
                item-value="id"
                label="Model"
                placeholder="Wpisz markę..."
                no-filter
                :loading="modelSearchLoading"
                @focus="ensureModelOptionsLoaded"
                @update:model-value="handleModelSelection"
                @update:search="searchModels"
              >
                <template #item="{props, item}">
                  <v-list-item
                    v-bind="props"
                    :subtitle="item?.raw
                      ? `${item.raw.brand} • ${item.raw.pricePerDay} PLN / dzień`
                      : ''"
                  />
                </template>
              </v-autocomplete>

              <v-text-field
                v-model="form.startDate"
                label="Data od"
                type="date"
              />

              <v-text-field
                v-model="form.endDate"
                label="Data do"
                type="date"
              />
            </div>
          </template>

          <template #[`item.2`]>
            <div class="pt-4 gap-4 grid md:grid-cols-2">
              <v-select
                v-model="form.insuranceVariantId"
                :items="insuranceVariants"
                item-title="name"
                item-value="id"
                label="Ubezpieczenie"
                :loading="loading"
              />

              <v-select
                v-model="form.addonIds"
                :items="addons"
                item-title="name"
                item-value="id"
                label="Dodatki"
                multiple
                chips
                clearable
                :loading="loading"
              >
                <template #item="{props, item}">
                  <v-list-item
                    v-bind="props"
                    :subtitle="item?.raw
                      ? `${item.raw.description} • ${item.raw.pricePerDay} PLN / dzień`
                      : ''"
                  />
                </template>
              </v-select>
            </div>
          </template>

          <template #[`item.3`]>
            <v-row class="pt-4">
              <v-col cols="12">
                <v-img
                  :src="selectedModelImage || undefined"
                  height="350"
                  cover
                  class="bg-grey-lighten-3 rounded-lg mb-2"
                >
                  <template #placeholder>
                    <div class="text-medium-emphasis text-xs text-black flex h-full w-full items-center justify-center">
                      Brak zdjęcia
                    </div>
                  </template>
                </v-img>
              </v-col>

              <v-col
                cols="12"
                md="6"
              >
                <v-list
                  density="compact"
                  class="border rounded-lg"
                >
                  <v-list-item
                    title="Model"
                    :subtitle="selectedModel
                      ? `${selectedModel.brand} ${selectedModel.model}`
                      : '-'"
                  />

                  <v-list-item
                    title="Termin"
                    :subtitle="`${form.startDate || '-'} → ${form.endDate || '-'}`"
                  />

                  <v-list-item
                    title="Liczba dni"
                    :subtitle="String(daysCount)"
                  />

                  <v-list-item
                    title="Dodatki"
                    :subtitle="selectedAddons.length
                      ? selectedAddons.map(addon => addon.name).join(', ')
                      : 'Brak'"
                  />
                </v-list>
              </v-col>

              <v-col
                cols="12"
                md="6"
              >
                <v-sheet
                  class="p-4 border rounded-lg"
                  color="transparent"
                >
                  <p class="text-sm mb-1">
                    Bazowo: {{ estimatedBase }} PLN
                  </p>

                  <p class="text-sm mb-1">
                    Ubezpieczenie: {{ estimatedInsurance }} PLN
                  </p>

                  <p class="text-sm mb-1">
                    Dodatki: {{ estimatedAddons }} PLN
                  </p>

                  <p class="font-weight-medium text-xl">
                    Razem: {{ estimatedTotal }} PLN
                  </p>
                </v-sheet>
              </v-col>
            </v-row>
          </template>
        </v-stepper>
      </v-card-text>

      <v-divider />

      <v-card-actions class="px-4 py-3">
        <v-btn
          :disabled="step <= 1"
          @click="step -= 1"
        >
          Wstecz
        </v-btn>

        <v-spacer />

        <v-btn
          v-if="!showSubmitButton"
          color="primary"
          @click="step += 1"
        >
          Dalej
        </v-btn>

        <v-btn
          v-else
          color="primary"
          :loading="submitting"
          @click="submitReservation"
        >
          Potwierdź i rezerwuj
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>
