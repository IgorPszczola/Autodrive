// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  ssr: false,
  devtools: { enabled: true },
  modules: ['@unocss/nuxt', 'vuetify-nuxt-module'],
  routeRules: {
    '/api/**': {
      proxy: 'http://localhost:8080/api/**',
    },
  },
})
