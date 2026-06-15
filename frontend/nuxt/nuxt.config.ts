import process from 'node:process'

const backendUrl = process.env.NUXT_PUBLIC_BACKEND_URL || 'http://localhost:8080'

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  ssr: false,
  devtools: { enabled: true },
  modules: ['@unocss/nuxt', 'vuetify-nuxt-module'],
  routeRules: {
    '/api/**': {
      proxy: `${backendUrl}/api/**`,
    },
  },
})
