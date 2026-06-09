<script setup lang="ts">
const { initializeAuth } = useAuth()

await initializeAuth()
</script>

<template>
  <NuxtLayout theme="dark">
    <NuxtPage />
  </NuxtLayout>
</template>
