interface CarUnitLike {
  imageUrl?: string | null
}

export function useCarImage() {
  const rentalApi = useRentalApi()
  const modelImageCache = ref<Record<number, string>>({})

  function getFirstUnitImage(units: CarUnitLike[]): string {
    const found = units.find(unit => typeof unit.imageUrl === 'string' && unit.imageUrl.trim())

    return found?.imageUrl?.trim() ?? ''
  }

  async function fetchModelImage(modelId: number): Promise<string> {
    if (!modelId) {
      return ''
    }

    if (modelImageCache.value[modelId] !== undefined) {
      return modelImageCache.value[modelId]
    }

    const units = await rentalApi.getCarModelUnits(modelId)
    const image = getFirstUnitImage(units)
    modelImageCache.value = {
      ...modelImageCache.value,
      [modelId]: image,
    }

    return image
  }

  function getCachedModelImage(modelId: number): string {
    return modelImageCache.value[modelId] ?? ''
  }

  function setCachedModelImage(modelId: number, imageUrl: string): void {
    modelImageCache.value = {
      ...modelImageCache.value,
      [modelId]: imageUrl,
    }
  }

  return {
    getFirstUnitImage,
    fetchModelImage,
    getCachedModelImage,
    setCachedModelImage,
  }
}
