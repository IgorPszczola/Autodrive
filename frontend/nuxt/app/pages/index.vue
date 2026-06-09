<script setup lang="ts">
const { isAuthenticated } = useAuth()

const heroImageUrl = 'https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=1800&q=80'

const highlights = [
  {
    title: 'Dostępność na żywo',
    text: 'Widzisz auta, które faktycznie można zarezerwować na wybrany termin.',
  },
  {
    title: 'Jasne koszty',
    text: 'Cena wypożyczenia i dodatków jest czytelna jeszcze przed finalnym krokiem.',
  },
  {
    title: 'Szybki odbiór',
    text: 'Po potwierdzeniu rezerwacji proces wydania auta jest prosty i przewidywalny.',
  },
]

const quickActions = [
  {
    title: 'Przeglądaj modele',
    text: 'Porównaj segmenty, skrzynie i ceny dzienne w jednym miejscu.',
    to: '/cars',
    cta: 'Otwórz katalog',
  },
  {
    title: 'Nowa rezerwacja',
    text: 'Wybierz daty i dodatki, a system od razu policzy podsumowanie.',
    to: '/client/reservations/new',
    cta: 'Przejdź do formularza',
  },
  {
    title: 'Strefa klienta',
    text: 'Sprawdź statusy, historię najmu i zarządzaj swoim kontem.',
    to: '/client/account',
    cta: 'Zobacz konto',
  },
]
</script>

<template>
  <v-container class="py-8 py-md-12">
    <v-row align="stretch" class="mb-8">
      <v-col cols="12" md="7">
        <v-card color="grey-darken-4" class="pa-8 h-100" rounded="xl" variant="flat">
          <v-chip color="primary" variant="flat" class="mb-4">
            AUTODRIVE
          </v-chip>

          <h1 class="text-3xl md:text-4xl font-weight-bold mb-4">
            Rezerwuj auto szybciej,
            <br>
            jedź pewniej.
          </h1>

          <p class="text-base text-medium-emphasis mb-6">
            Nowoczesna wypożyczalnia online: sprawdzasz modele, wybierasz termin i zarządzasz rezerwacją bez zbędnych kroków.
          </p>

          <div class="d-flex flex-wrap ga-3">
            <v-btn to="/cars" color="primary" size="large" variant="flat">
              Sprawdź katalog
            </v-btn>
            <v-btn
              v-if="!isAuthenticated"
              to="/login"
              size="large"
              variant="outlined"
            >
              Zaloguj się
            </v-btn>
            <v-btn
              v-else
              to="/client/account"
              size="large"
              variant="outlined"
            >
              Przejdź do konta
            </v-btn>
          </div>
        </v-card>
      </v-col>

      <v-col cols="12" md="5">
        <v-card class="overflow-hidden h-100" rounded="xl" variant="flat" color="grey-darken-4">
          <v-img
            :src="heroImageUrl"
            cover
            height="100%"
            min-height="320"
          />
        </v-card>
      </v-col>
    </v-row>

    <v-row class="mb-8" density="comfortable">
      <v-col v-for="item in highlights" :key="item.title" cols="12" md="4">
        <v-sheet rounded="lg" color="grey-darken-4" class="pa-5 h-40">
          <div class="text-xl font-weight-bold mb-2">
            {{ item.title }}
          </div>
          <div class="text-medium-emphasis">
            {{ item.text }}
          </div>
        </v-sheet>
      </v-col>
    </v-row>

    <v-card rounded="xl" variant="flat" color="blue-grey-darken-4" class="mb-8">
      <v-card-text class="d-flex flex-column flex-md-row align-md-center justify-space-between ga-4 pa-10">
        <div>
          <div class="text-xs text-medium-emphasis mb-1">
            SZYBKI START
          </div>
          <h2 class="text-2xl mb-6 text-primary">
            Gotowy na kolejny wyjazd?
          </h2>
          <p class="text-medium-emphasis mb-0">
            Zobacz dostępne modele i zablokuj termin zanim zrobi to ktoś inny.
          </p>
        </div>
        <v-btn to="/cars" color="primary" size="large" variant="flat">
          Rezerwuj teraz
        </v-btn>
      </v-card-text>
    </v-card>

    <v-row>
      <v-col v-for="card in quickActions" :key="card.title" cols="12" md="4">
        <v-card class="h-50 pa-4" variant="flat" color="grey-darken-4">
          <v-card-title>{{ card.title }}</v-card-title>
          <v-card-text class="text-medium-emphasis">
            {{ card.text }}
          </v-card-text>
          <v-card-actions>
            <v-btn :to="card.to" color="primary" variant="text">
              {{ card.cta }}
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
