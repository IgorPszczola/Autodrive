<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
})

const route = useRoute()
const router = useRouter()
const rentalApi = useRentalApi()

function getLocalDate(offsetDays = 0): string {
  const date = new Date()
  date.setDate(date.getDate() + offsetDays)
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset())
  return date.toISOString().slice(0, 10)
}

const step = ref(1)
const isMounted = ref(false)
const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const form = reactive({
  carModelId: Number(route.query.carModelId || 0),
  startDate: getLocalDate(0),
  endDate: getLocalDate(1),
  insuranceVariantId: 0,
  addonIds: [] as number[],
})

const models = ref<Array<Record<string, any>>>([])
const insuranceVariants = ref<Array<Record<string, any>>>([])
const addons = ref<Array<Record<string, any>>>([])

const selectedModel = computed(() => models.value.find(model => model.id === form.carModelId) || null)
const selectedAddons = computed(() => addons.value.filter(addon => form.addonIds.includes(addon.id)))

const selectedModelImage = ref('')

async function updateSelectedModelImage() {
  if (!form.carModelId) {
    selectedModelImage.value = ''
    return
  }
  try {
    const units = await rentalApi.getCarModelUnits(form.carModelId)
    const found = units.find((unit: any) => typeof unit.imageUrl === 'string' && unit.imageUrl.trim())
    selectedModelImage.value = found?.imageUrl?.trim() ?? ''
  } catch {
    selectedModelImage.value = ''
  }
}

watch(() => form.carModelId, updateSelectedModelImage, { immediate: true })

const daysCount = computed(() => {
  if (!form.startDate || !form.endDate) {
    return 0
  }

  const start = new Date(form.startDate).getTime()
  const end = new Date(form.endDate).getTime()
  const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 0
})

const estimatedBase = computed(() => {
  if (!selectedModel.value || !daysCount.value) {
    return 0
  }

  return Number(selectedModel.value.pricePerDay || 0) * daysCount.value
})

const estimatedInsurance = computed(() => {
  const selected = insuranceVariants.value.find(item => item.id === form.insuranceVariantId)
  if (!selected || !selectedModel.value || !daysCount.value) {
    return 0
  }

  const percentage = Number(selected.pricePerDay || 0)
  const baseRate = Number(selectedModel.value.pricePerDay || 0)
  const dailyInsurance = (baseRate * percentage) / 100
  return dailyInsurance * daysCount.value
})

const estimatedAddons = computed(() => selectedAddons.value.reduce((total, addon) => {
  return total + Number(addon.pricePerDay || 0) * daysCount.value
}, 0))

const estimatedTotal = computed(() => estimatedBase.value + estimatedInsurance.value + estimatedAddons.value)
const showSubmitButton = computed(() => isMounted.value && Number(step.value) >= 3)

const minEndDate = computed(() => {
  if (!form.startDate) {
    return getLocalDate(1)
  }
  const date = new Date(form.startDate)
  date.setDate(date.getDate() + 1)
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset())
  return date.toISOString().slice(0, 10)
})

watch(() => form.startDate, (newStart) => {
  if (form.endDate && newStart) {
    const startObj = new Date(newStart)
    const endObj = new Date(form.endDate)
    if (endObj <= startObj) {
      const nextDay = new Date(startObj)
      nextDay.setDate(nextDay.getDate() + 1)
      nextDay.setMinutes(nextDay.getMinutes() - nextDay.getTimezoneOffset())
      form.endDate = nextDay.toISOString().slice(0, 10)
    }
  }
})

function getModelLabel(model: Record<string, any>): string {
  return `${model?.brand ?? ''} ${model?.model ?? ''}`.trim()
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [modelsData, insuranceData, addonData] = await Promise.all([
      rentalApi.getCarModels({}, 0, 1000),
      rentalApi.getInsuranceVariants(),
      rentalApi.getAddons(),
    ])

    models.value = modelsData.content ?? []
    insuranceVariants.value = insuranceData
    addons.value = addonData

    if (!form.carModelId && models.value[0]) {
      form.carModelId = models.value[0].id
    }

    if (!form.insuranceVariantId && insuranceData[0]) {
      form.insuranceVariantId = insuranceData[0].id
    }
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Nie udało się pobrać danych kreatora'
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

  if (new Date(form.endDate).getTime() <= new Date(form.startDate).getTime()) {
    errorMessage.value = 'Data zakończenia musi być późniejsza niż data rozpoczęcia (minimum 1 dzień).'
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
    errorMessage.value = error instanceof Error ? error.message : 'Nie udało się utworzyć rezerwacji'
  }
  finally {
    submitting.value = false
  }
}

onMounted(async () => {
  isMounted.value = true
  await loadData()
})
</script>

<template>
  <div class="gradient-hero min-h-screen py-10 animate-fade-in">
    <v-container max-width="920">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-weight-black text-white">
          Kreator rezerwacji
        </h1>
        <p class="text-medium-emphasis mt-1">
          Przejdź przez 3 proste kroki, aby sfinalizować rezerwację wybranego pojazdu.
        </p>
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

      <!-- Main Stepper Card -->
      <v-card class="glass-card overflow-hidden" rounded="xl" variant="flat">
        <v-card-text class="pa-6">
          <v-stepper
            v-model="step"
            :items="['Daty i model', 'Dodatki i ubezpieczenie', 'Podsumowanie kosztów']"
            hide-actions
            class="bg-transparent"
          >
            <!-- Step 1: Model and Dates -->
            <template #item.1>
              <div class="pt-6">
                <v-row>
                  <v-col cols="12" class="pb-2">
                    <v-select
                      v-model="form.carModelId"
                      :items="models"
                      :item-title="getModelLabel"
                      item-value="id"
                      label="Wybierz model samochodu"
                      prepend-inner-icon="mdi-car-outline"
                      variant="outlined"
                      color="primary"
                      :loading="loading"
                    >
                      <template #item="{ props, item }">
                        <v-list-item
                          v-bind="props"
                          :subtitle="item?.raw ? `Segment ${item.raw.segment} • ${item.raw.pricePerDay} PLN / dzień` : ''"
                        />
                      </template>
                    </v-select>
                  </v-col>

                  <v-col cols="12" md="6" class="py-2">
                    <v-text-field
                      v-model="form.startDate"
                      label="Data odbioru"
                      type="date"
                      :min="getLocalDate(0)"
                      prepend-inner-icon="mdi-calendar-import"
                      variant="outlined"
                      color="primary"
                    />
                  </v-col>

                  <v-col cols="12" md="6" class="py-2">
                    <v-text-field
                      v-model="form.endDate"
                      label="Data zwrotu"
                      type="date"
                      :min="minEndDate"
                      prepend-inner-icon="mdi-calendar-export"
                      variant="outlined"
                      color="primary"
                    />
                  </v-col>
                </v-row>
              </div>
            </template>

            <!-- Step 2: Addons and Insurance -->
            <template #item.2>
              <div class="pt-6">
                <v-row>
                  <v-col cols="12" md="6" class="py-2">
                    <v-select
                      v-model="form.insuranceVariantId"
                      :items="insuranceVariants"
                      item-title="name"
                      item-value="id"
                      label="Wariant ubezpieczenia"
                      prepend-inner-icon="mdi-shield-check-outline"
                      variant="outlined"
                      color="primary"
                      :loading="loading"
                    >
                      <template #item="{ props, item }">
                        <v-list-item
                          v-bind="props"
                          :subtitle="item?.raw ? `${item.raw.description} • +${((Number(selectedModel?.pricePerDay || 0) * Number(item.raw.pricePerDay || 0)) / 100).toFixed(2)} PLN / dzień` : ''"
                        />
                      </template>
                    </v-select>
                  </v-col>

                  <v-col cols="12" md="6" class="py-2">
                    <v-select
                      v-model="form.addonIds"
                      :items="addons"
                      item-title="name"
                      item-value="id"
                      label="Dodatki opcjonalne"
                      prepend-inner-icon="mdi-plus-box-outline"
                      variant="outlined"
                      color="primary"
                      multiple
                      chips
                      clearable
                      :loading="loading"
                    >
                      <template #item="{ props, item }">
                        <v-list-item
                          v-bind="props"
                          :subtitle="item?.raw ? `+${item.raw.pricePerDay} PLN / dzień` : ''"
                        />
                      </template>
                    </v-select>
                  </v-col>
                </v-row>
              </div>
            </template>

            <!-- Step 3: Checkout Summary -->
            <template #item.3>
              <div class="pt-6">
                <v-row>
                  <!-- Configuration Summary List -->
                  <v-col cols="12" md="6">
                    <h3 class="text-white text-base font-weight-bold mb-4">Podsumowanie konfiguracji</h3>
                    
                    <v-img
                      v-if="selectedModelImage"
                      :src="selectedModelImage"
                      height="180"
                      cover
                      class="rounded-lg mb-4 bg-slate-900 border border-white/5 brightness-95"
                    >
                      <template #placeholder>
                        <div class="d-flex h-full w-full items-center justify-center text-medium-emphasis text-xs bg-slate-900">
                          Ładowanie podglądu...
                        </div>
                      </template>
                    </v-img>

                    <v-list density="compact" class="bg-transparent pa-0 border border-white/5 rounded-lg overflow-hidden">
                      <v-list-item class="py-3 border-b border-white/5">
                        <template #title>
                          <span class="text-xs text-medium-emphasis">Wybrany pojazd</span>
                        </template>
                        <template #subtitle>
                          <span class="text-sm font-weight-bold text-white">{{ selectedModel ? `${selectedModel.brand} ${selectedModel.model}` : '-' }}</span>
                        </template>
                      </v-list-item>

                      <v-list-item class="py-3 border-b border-white/5">
                        <template #title>
                          <span class="text-xs text-medium-emphasis">Termin rezerwacji</span>
                        </template>
                        <template #subtitle>
                          <span class="text-sm font-weight-bold text-white">{{ form.startDate || '-' }} → {{ form.endDate || '-' }}</span>
                        </template>
                      </v-list-item>

                      <v-list-item class="py-3 border-b border-white/5">
                        <template #title>
                          <span class="text-xs text-medium-emphasis">Liczba dni najmu</span>
                        </template>
                        <template #subtitle>
                          <span class="text-sm font-weight-bold text-white">{{ daysCount }} dni</span>
                        </template>
                      </v-list-item>

                      <v-list-item class="py-3">
                        <template #title>
                          <span class="text-xs text-medium-emphasis">Wybrane dodatki</span>
                        </template>
                        <template #subtitle>
                          <span class="text-sm font-weight-bold text-white">{{ selectedAddons.length ? selectedAddons.map(addon => addon.name).join(', ') : 'Brak dodatków' }}</span>
                        </template>
                      </v-list-item>
                    </v-list>
                  </v-col>

                  <!-- Invoice Calculations Box -->
                  <v-col cols="12" md="6">
                    <h3 class="text-white text-base font-weight-bold mb-4">Kalkulacja kosztów</h3>
                    <v-sheet class="rounded-lg pa-6 border border-white/5 bg-white/2 d-flex flex-column ga-3" color="transparent">
                      <div class="d-flex justify-space-between text-sm">
                        <span class="text-medium-emphasis">Wynajem bazowy:</span>
                        <span class="text-white font-weight-semibold">{{ estimatedBase }} PLN</span>
                      </div>
                      <div class="d-flex justify-space-between text-sm">
                        <span class="text-medium-emphasis">Pakiet ubezpieczenia:</span>
                        <span class="text-white font-weight-semibold">+ {{ estimatedInsurance }} PLN</span>
                      </div>
                      <div class="d-flex justify-space-between text-sm border-b border-white/5 pb-3">
                        <span class="text-medium-emphasis">Dodatki opcjonalne:</span>
                        <span class="text-white font-weight-semibold">+ {{ estimatedAddons }} PLN</span>
                      </div>
                      <div class="d-flex justify-space-between align-center pt-2">
                        <span class="text-base font-weight-bold text-white">Razem:</span>
                        <span class="text-2xl font-weight-black text-primary">{{ estimatedTotal }} PLN</span>
                      </div>
                    </v-sheet>
                  </v-col>
                </v-row>
              </div>
            </template>
          </v-stepper>
        </v-card-text>

        <!-- Stepper Actions Bar -->
        <v-divider style="border-color: rgba(255, 255, 255, 0.05) !important;" />
        <v-card-actions class="px-6 py-4 ga-2 bg-black/10">
          <v-btn
            :disabled="step <= 1"
            variant="outlined"
            color="secondary"
            class="px-6 font-weight-semibold"
            height="42"
            style="border-color: rgba(255, 255, 255, 0.1);"
            @click="step -= 1"
          >
            Wstecz
          </v-btn>

          <v-spacer />

          <v-btn
            v-if="!showSubmitButton"
            color="primary"
            variant="flat"
            class="px-6 font-weight-semibold"
            height="42"
            @click="step += 1"
          >
            Dalej
          </v-btn>
          
          <v-btn
            v-else
            color="primary"
            variant="flat"
            :loading="submitting"
            class="px-6 font-weight-semibold"
            height="42"
            @click="submitReservation"
          >
            Potwierdź i rezerwuj
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-container>
  </div>
</template>
