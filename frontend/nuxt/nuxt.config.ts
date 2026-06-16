import process from 'node:process'

const backendUrl = process.env.NUXT_PUBLIC_BACKEND_URL || 'http://localhost:8080'

export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  ssr: false,
  devtools: { enabled: true },
  modules: ['@unocss/nuxt', 'vuetify-nuxt-module'],
  css: ['~/assets/main.css'],
  vuetify: {
    vuetifyOptions: {
      theme: {
        defaultTheme: 'dark',
        themes: {
          dark: {
            colors: {
              primary: '#2563eb', // Modern royal blue
              secondary: '#64748b', // Slate gray
              background: '#050811', // Deep dark blue-black
              surface: '#0d1321', // Slate card surface
              error: '#ef4444',
              success: '#10b981',
              warning: '#f59e0b',
            }
          }
        }
      }
    }
  },
  routeRules: {
    '/api/**': {
      proxy: `${backendUrl}/api/**`,
    },
  },
})
