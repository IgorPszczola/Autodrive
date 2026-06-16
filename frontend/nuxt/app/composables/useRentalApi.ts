interface CarModel {
  id: number
  brand: string
  model: string
  segment: string
  pricePerDay: number
  depositAmount?: number
  mileageLimitPerDay?: number
  extraMileageFee?: number
  powerHp?: number
  transmissionType?: string
  fuelType?: string
}

interface CarUnit {
  id: number
  licensePlate: string
  currentMileage?: number
  status: string
  vin?: string
  color?: string
  carModel?: CarModel
  imageUrl?: string
  productionYear?: number
  modelId?: number
}

interface Review {
  id: number
  carModelId: number
  carModelName: string
  userEmail: string
  rating: number
  comment: string
  createdAt: string
}

interface InsuranceVariant {
  id: number
  name: string
  pricePerDay: number
  description: string
}

interface Addon {
  id: number
  name: string
  description: string
  pricePerDay: number
}

interface Reservation {
  id: number
  startDate: string
  endDate: string
  basePrice?: number
  discountApplied?: number
  totalPrice?: number
  depositAmount?: number
  status: string
  createdAt?: string
  user?: {
    id?: number
    email?: string
    firstName?: string
    lastName?: string
  }
  carModel?: CarModel
  carUnit?: CarUnit
  addons?: Array<{ id: number, name: string }>
  insuranceVariant?: InsuranceVariant
  userEmail?: string
  carBrand?: string
  carModelName?: string
  licensePlate?: string
  insuranceVariantName?: string
}

interface ReservationCreatePayload {
  id: number
  startDate: string
  endDate: string
  addonIds: number[]
  insuranceVariantId: number
}

interface CarUnitCreatePayload {
  carModelId: number
  licensePlate: string
  vin: string
  currentMileage: number
  color: string
  productionYear: number
  imageUrl: string
}

interface CarModelCreatePayload {
  brand: string
  model: string
  segment: string
  pricePerDay: number
  depositAmount: number
  mileageLimitPerDay: number
  extraMileageFee: number
  powerHp: number
  transmissionType: string
  fuelType: string
}

interface ReturnPayload {
  currentMileage: number
  isDamaged: boolean
  damageNotes: string
  damageCost: number
}

interface DashboardStats {
  totalEarnings: number
  totalReservationsCount: number
  activeRentalsCount: number
  fleetInRepairCount: number
  fleetUtilizationRate: number
}

interface MonthlyEarning {
  month: number
  monthName: string
  earnings: number
}

interface ReturnReport {
  id: number
  returnDate: string
  endMileage: number
  extraMileageCost: number
  damageCost: number
  damageDescription: string
  totalSurcharge: number
  reservation: Reservation
}

interface PagedResponse<T> {
  content?: T[]
  data?: T[]
}

interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

function normalizeQuery(query?: Record<string, string | number | boolean | undefined>) {
  if (!query) {
    return undefined
  }

  const entries = Object.entries(query).filter(([, value]) => {
    if (value === undefined) {
      return false
    }

    if (typeof value === 'string' && value.trim() === '') {
      return false
    }

    return true
  })

  return entries.length
    ? Object.fromEntries(entries)
    : undefined
}

function normalizeListResponse<T>(response: unknown): T[] {
  if (Array.isArray(response)) {
    return response as T[]
  }

  if (response && typeof response === 'object') {
    const typed = response as PagedResponse<T>
    if (Array.isArray(typed.content)) {
      return typed.content
    }

    if (Array.isArray(typed.data)) {
      return typed.data
    }
  }

  return []
}

function toLocalDateTimeStart(value: string): string {
  return value.includes('T')
    ? value
    : `${value}T00:00:00`
}

function toLocalDateTimeEnd(value: string): string {
  return value.includes('T')
    ? value
    : `${value}T23:59:59`
}

export function useRentalApi() {
  function getCarModels(query?: Record<string, string | number | boolean | undefined>, page?: number, size?: number) {
    const normalizedQuery = normalizeQuery(query) ?? {}

    if (typeof normalizedQuery.brand !== 'string') {
      normalizedQuery.brand = ''
    }

    if (typeof page === 'number' && page >= 0) {
      normalizedQuery.page = page
    }

    if (typeof size === 'number' && size > 0) {
      normalizedQuery.size = size
    }

    return apiRequest<SpringPage<CarModel>>('/api/cars/models', {
      method: 'GET',
      query: normalizedQuery,
    })
  }

  function getCarModel(id: number) {
    return apiRequest<SpringPage<CarModel> | CarModel>(`/api/cars/models/${id}`, { method: 'GET' }).then((response) => {
      if (!response)
        return null
      if ('content' in response && Array.isArray(response.content) && response.content.length > 0) {
        return response.content[0]
      }
      if ('id' in response && 'brand' in response) {
        return response as CarModel
      }

      return null
    })
  }

  function getCarModelUnits(id: number) {
    return apiRequest<CarUnit[] | PagedResponse<CarUnit>>(`/api/cars/models/${id}/units`, {
      method: 'GET',
    }).then(normalizeListResponse<CarUnit>)
  }

  function getCarBrands() {
    return apiRequest<string[] | PagedResponse<string>>('/api/cars/brands', {
      method: 'GET',
    }).then(normalizeListResponse<string>)
  }

  function getCarSegments() {
    return apiRequest<string[] | PagedResponse<string>>('/api/cars/segments', {
      method: 'GET',
    }).then(normalizeListResponse<string>)
  }

  function getModelReviews(carModelId: number) {
    return apiRequest<Review[] | PagedResponse<Review>>(`/api/reviews/model/${carModelId}`, {
      method: 'GET',
    }).then(normalizeListResponse<Review>)
  }

  function createReview(payload: { rating: number, comment: string, carModelId: number }) {
    return apiRequest<unknown>('/api/reviews', {
      method: 'POST',
      body: payload,
    })
  }

  function getInsuranceVariants() {
    return apiRequest<InsuranceVariant[] | PagedResponse<InsuranceVariant>>('/api/insurances', {
      method: 'GET',
    }).then(normalizeListResponse<InsuranceVariant>)
  }

  function getAddons() {
    return apiRequest<Addon[] | PagedResponse<Addon>>('/api/addons', {
      method: 'GET',
    }).then(normalizeListResponse<Addon>)
  }

  function createReservation(carModelId: number, payload: ReservationCreatePayload) {
    return apiRequest<unknown>('/api/reservations', {
      method: 'POST',
      query: { carModelId },
      body: {
        id: payload.id,
        startDate: toLocalDateTimeStart(payload.startDate),
        endDate: toLocalDateTimeEnd(payload.endDate),
        addonIds: payload.addonIds,
        insuranceVariantId: payload.insuranceVariantId,
      } satisfies ReservationCreatePayload,
    })
  }

  function cancelReservation(id: number) {
    return apiRequest<unknown>(`/api/reservations/${id}/cancel`, {
      method: 'PUT',
    })
  }

  function getMyReservations() {
    return apiRequest<Reservation[] | PagedResponse<Reservation>>('/api/reservations/my', {
      method: 'GET',
    }).then(normalizeListResponse<Reservation>)
  }

  function getAdminStats() {
    return apiRequest<DashboardStats>('/api/admin/dashboard/stats', {
      method: 'GET',
    })
  }

  function getMonthlyEarnings() {
    return apiRequest<MonthlyEarning[]>('/api/admin/dashboard/monthly-earnings', {
      method: 'GET',
    }).then((response) => {
      return Array.isArray(response)
        ? response
        : []
    })
  }

  function getAdminReservations() {
    return apiRequest<Reservation[] | PagedResponse<Reservation>>('/api/admin/reservations/all', {
      method: 'GET',
    }).then(normalizeListResponse<Reservation>)
  }

  function assignUnit(reservationId: number) {
    return apiRequest<string>(`/api/admin/reservations/${reservationId}/assign-unit`, {
      method: 'PUT',
    })
  }

  function processReturn(id: number, payload: ReturnPayload) {
    return apiRequest<unknown>(`/api/admin/reservations/${id}/return`, {
      method: 'PUT',
      body: payload,
    })
  }

  function getReturnReport(reservationId: number) {
    return apiRequest<ReturnReport>(`/api/admin/reservations/${reservationId}/return-report`, {
      method: 'GET',
    })
  }

  function createCarUnit(payload: CarUnitCreatePayload) {
    return apiRequest<unknown>('/api/admin/units', {
      method: 'POST',
      body: payload,
    })
  }

  function updateCarUnitStatus(id: number, status: string) {
    return apiRequest<unknown>(`/api/admin/units/${id}/status`, {
      method: 'PUT',
      query: { status },
    })
  }

  function createCarModel(payload: CarModelCreatePayload) {
    return apiRequest<unknown>('/api/admin/models', {
      method: 'POST',
      body: payload,
    })
  }

  return {
    getCarModels,
    getCarModel,
    getCarModelUnits,
    getCarBrands,
    getCarSegments,
    getModelReviews,
    createReview,
    getInsuranceVariants,
    getAddons,
    createReservation,
    cancelReservation,
    getMyReservations,
    getAdminStats,
    getMonthlyEarnings,
    getAdminReservations,
    assignUnit,
    processReturn,
    getReturnReport,
    createCarUnit,
    updateCarUnitStatus,
    createCarModel,
  }
}
