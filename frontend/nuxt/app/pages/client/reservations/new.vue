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
  addonIdsText: '',
})

const models = ref<Array<Record<string, any>>>([])
const insuranceVariants = ref<Array<Record<string, any>>>([])

const selectedModel = computed(() => models.value.find(model => model.id === form.carModelId) || null)
const selectedAddonIds = computed(() => form.addonIdsText
  .split(',')
  .map(item => Number(item.trim()))
  .filter(item => Number.isFinite(item) && item > 0))

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
  if (!selected || !daysCount.value) {
    return 0
  }

  return Number(selected.pricePerDay || 0) * daysCount.value
})

const estimatedTotal = computed(() => estimatedBase.value + estimatedInsurance.value)
const showSubmitButton = computed(() => isMounted.value && Number(step.value) >= 3)

function getModelLabel(model: Record<string, any>): string {
  return `${model?.brand ?? ''} ${model?.model ?? ''}`.trim()
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [modelsData, insuranceData] = await Promise.all([
      rentalApi.getCarModels(),
      rentalApi.getInsuranceVariants(),
    ])

    models.value = modelsData
    insuranceVariants.value = insuranceData

    if (!form.carModelId && modelsData[0]) {
      form.carModelId = modelsData[0].id
    }

    if (!form.insuranceVariantId && insuranceData[0]) {
      form.insuranceVariantId = insuranceData[0].id
    }
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Nie udalo sie pobrac danych kreatora'
  }
  finally {
    loading.value = false
  }
}

async function submitReservation() {
  if (!form.startDate || !form.endDate) {
    errorMessage.value = 'Uzupelnij date rozpoczecia i zakonczenia rezerwacji.'
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
    errorMessage.value = 'Data zakonczenia nie moze byc wczesniejsza niz data rozpoczecia.'
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
      addonIds: selectedAddonIds.value,
      insuranceVariantId: form.insuranceVariantId,
    })

    successMessage.value = 'Rezerwacja zostala utworzona.'
    await router.push('/client/account')
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Nie udalo sie utworzyc rezerwacji'
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
  <v-container class="py-8" max-width="920">
    <h1 class="text-2xl font-semibold mb-1">
      Kreator rezerwacji
    </h1>
    <p class="text-medium-emphasis mb-6">
      Krok 1: Daty, Krok 2: Dodatki i ubezpieczenie, Krok 3: Podsumowanie kosztow.
    </p>

    <v-alert v-if="errorMessage" type="error" variant="tonal" class="mb-4">
      {{ errorMessage }}
    </v-alert>
    <v-alert v-if="successMessage" type="success" variant="tonal" class="mb-4">
      {{ successMessage }}
    </v-alert>

    <v-card variant="outlined" rounded="lg">
      <v-card-text class="pt-5">
        <v-stepper v-model="step" :items="['Daty', 'Dodatki', 'Podsumowanie']" hide-actions>
          <template #item.1>
            <div class="grid gap-4 md:grid-cols-2 pt-4">
              <v-select
                v-model="form.carModelId"
                :items="models"
                :item-title="getModelLabel"
                item-value="id"
                label="Model"
                :loading="loading"
              >
                <template #item="{ props, item }">
                  <v-list-item
                    v-bind="props"
                    :subtitle="item?.raw
                      ? `${item.raw.brand} • ${item.raw.pricePerDay} PLN / dzien`
                      : ''"
                  />
                </template>
              </v-select>
              <v-text-field v-model="form.startDate" label="Data od" type="date" />
              <v-text-field v-model="form.endDate" label="Data do" type="date" />
            </div>
          </template>

          <template #item.2>
            <div class="grid gap-4 md:grid-cols-2 pt-4">
              <v-select
                v-model="form.insuranceVariantId"
                :items="insuranceVariants"
                item-title="name"
                item-value="id"
                label="Ubezpieczenie"
                :loading="loading"
              />
              <v-text-field
                v-model="form.addonIdsText"
                label="Dodatki (ID po przecinku)"
                hint="Przyklad: 1,2"
                persistent-hint
              />
            </div>
          </template>

          <template #item.3>
            <v-row class="pt-4">
              <v-col cols="12" md="6">
                <v-list density="compact" class="border rounded-lg">
                  <v-list-item title="Model" :subtitle="selectedModel ? `${selectedModel.brand} ${selectedModel.model}` : '-'" />
                  <v-list-item title="Termin" :subtitle="`${form.startDate || '-'} → ${form.endDate || '-'}`" />
                  <v-list-item title="Liczba dni" :subtitle="String(daysCount)" />
                  <v-list-item title="Dodatki" :subtitle="selectedAddonIds.length ? selectedAddonIds.join(', ') : 'Brak'" />
                </v-list>
              </v-col>
              <v-col cols="12" md="6">
                <v-sheet class="rounded-lg p-4 border" color="transparent">
                  <p class="mb-1 text-sm">Bazowo: {{ estimatedBase }} PLN</p>
                  <p class="mb-1 text-sm">Ubezpieczenie: {{ estimatedInsurance }} PLN</p>
                  <p class="text-xl font-weight-medium">Razem: {{ estimatedTotal }} PLN</p>
                </v-sheet>
              </v-col>
            </v-row>
          </template>
        </v-stepper>
      </v-card-text>
      <v-divider />
      <v-card-actions class="px-4 py-3">
        <v-btn :disabled="step <= 1" @click="step -= 1">
          Wstecz
        </v-btn>
        <v-spacer />
        <v-btn v-if="!showSubmitButton" color="primary" @click="step += 1">
          Dalej
        </v-btn>
        <v-btn
          v-else
          color="primary"
          :loading="submitting"
          @click="submitReservation"
        >
          Potwierdz i rezerwuj
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>
