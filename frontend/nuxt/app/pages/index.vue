<script setup lang="ts">
const { isAuthenticated } = useAuth()

const heroImageUrl = 'https://images.unsplash.com/photo-1614162692292-7ac56d7f7f1e?auto=format&fit=crop&w=1200&q=80'

const highlights = [
  {
    title: 'Dostępność na żywo',
    text: 'Widzisz auta, które faktycznie można zarezerwować na wybrany termin.',
    icon: 'mdi-calendar-check',
    color: '#3b82f6'
  },
  {
    title: 'Jasne koszty',
    text: 'Cena wypożyczenia i dodatków jest czytelna jeszcze przed finalnym krokiem.',
    icon: 'mdi-cash-multiple',
    color: '#10b981'
  },
  {
    title: 'Szybki odbiór',
    text: 'Po potwierdzeniu rezerwacji proces wydania auta jest prosty i przewidywalny.',
    icon: 'mdi-key-wireless',
    color: '#8b5cf6'
  },
]

const quickActions = [
  {
    title: 'Przeglądaj modele',
    text: 'Porównaj segmenty, skrzynie i ceny dzienne w jednym miejscu.',
    to: '/cars',
    cta: 'Otwórz katalog',
    icon: 'mdi-car-multiple',
    color: '#3b82f6'
  },
  {
    title: 'Nowa rezerwacja',
    text: 'Wybierz daty i dodatki, a system od razu policzy podsumowanie.',
    to: '/client/reservations/new',
    cta: 'Przejdź do formularza',
    icon: 'mdi-calendar-plus',
    color: '#10b981'
  },
  {
    title: 'Strefa klienta',
    text: 'Sprawdź statusy, historię najmu i zarządzaj swoim kontem.',
    to: '/client/account',
    cta: 'Zobacz konto',
    icon: 'mdi-account-circle-outline',
    color: '#8b5cf6'
  },
]
</script>

<template>
  <div class="gradient-hero min-h-screen py-md-12 py-8 animate-fade-in">
    <v-container max-width="1200">
      <!-- Hero Section -->
      <v-row align="stretch" class="mb-10">
        <v-col cols="12" md="7">
          <v-card
            class="pa-8 pa-md-12 h-100 glass-card d-flex flex-column justify-center"
            rounded="xl"
            variant="flat"
          >
            <div class="d-flex mb-4">
              <v-chip
                color="primary"
                variant="flat"
                size="small"
                class="font-weight-bold px-3 py-1"
                style="letter-spacing: 0.1em;"
              >
                PREMIUM CAR RENTAL
              </v-chip>
            </div>

            <h1 class="font-weight-black text-4xl mb-6 md:text-5xl lg:text-6xl leading-tight" style="line-height: 1.15 !important;">
              Rezerwuj auto <span class="gradient-text">szybciej</span>,<br>
              jedź <span class="gradient-text">pewniej</span>.
            </h1>

            <p class="text-medium-emphasis text-lg mb-8" style="max-width: 520px; line-height: 1.6;">
              Nowoczesna wypożyczalnia online: sprawdzasz modele premium, wybierasz termin i zarządzasz rezerwacją bez zbędnych kroków i ukrytych opłat.
            </p>

            <div class="d-flex ga-3 flex-wrap">
              <v-btn
                to="/cars"
                color="primary"
                size="large"
                variant="flat"
                class="px-8 font-weight-semibold"
                height="50"
              >
                Sprawdź katalog
              </v-btn>

              <v-btn
                v-if="!isAuthenticated"
                to="/login"
                size="large"
                variant="outlined"
                class="px-8"
                height="50"
                style="border-color: rgba(255, 255, 255, 0.15);"
              >
                Zaloguj się
              </v-btn>

              <v-btn
                v-else
                to="/client/account"
                size="large"
                variant="outlined"
                class="px-8"
                height="50"
                style="border-color: rgba(255, 255, 255, 0.15);"
              >
                Przejdź do konta
              </v-btn>
            </div>
          </v-card>
        </v-col>

        <v-col cols="12" md="5">
          <v-card
            class="h-100 overflow-hidden glass-card position-relative"
            rounded="xl"
            variant="flat"
            min-height="350"
          >
            <v-img
              :src="heroImageUrl"
              cover
              height="100%"
              class="brightness-90"
            />
            <div class="position-absolute bottom-0 left-0 right-0 pa-6 bg-gradient-to-t from-black/85 to-transparent text-white">
              <div class="text-xs text-primary font-weight-bold mb-1">WYBRANY MODEL</div>
              <div class="text-xl font-weight-bold">Porsche 911 Carrera</div>
              <div class="text-sm text-medium-emphasis">Dostępny w naszej flocie</div>
            </div>
          </v-card>
        </v-col>
      </v-row>

      <!-- Highlights Section -->
      <v-row class="mb-12">
        <v-col
          v-for="item in highlights"
          :key="item.title"
          cols="12"
          md="4"
        >
          <v-card
            rounded="xl"
            class="pa-6 h-100 glass-card d-flex flex-column"
            variant="flat"
          >
            <div class="mb-4 d-flex">
              <v-avatar :color="item.color + '20'" rounded="lg" size="48">
                <v-icon :color="item.color" size="28">{{ item.icon }}</v-icon>
              </v-avatar>
            </div>
            <h3 class="font-weight-bold text-lg mb-2 text-white">
              {{ item.title }}
            </h3>
            <p class="text-medium-emphasis text-sm mb-0" style="line-height: 1.5;">
              {{ item.text }}
            </p>
          </v-card>
        </v-col>
      </v-row>

      <!-- Call to Action Banner -->
      <v-card
        rounded="xl"
        variant="flat"
        class="mb-12 glass-card overflow-hidden position-relative"
        style="border: 1px solid rgba(59, 130, 246, 0.1) !important;"
      >
        <!-- Decorative light blob -->
        <div class="position-absolute" style="top: -50px; right: -50px; width: 200px; height: 200px; background: rgba(59, 130, 246, 0.15); filter: blur(50px); border-radius: 50%;"></div>
        
        <v-card-text class="d-flex flex-column flex-md-row align-md-center justify-space-between ga-6 pa-8 pa-md-10">
          <div>
            <div class="text-primary text-xs font-weight-bold mb-1" style="letter-spacing: 0.1em;">
              SZYBKI START
            </div>
            <h2 class="text-white text-2xl font-weight-bold mb-2">
              Gotowy na kolejny wyjazd?
            </h2>
            <p class="text-medium-emphasis mb-0" style="max-width: 600px;">
              Zobacz dostępne modele, sprawdź warunki i zarezerwuj idealne auto w kilka minut, zanim ktoś Cię ubiegnie.
            </p>
          </div>

          <v-btn
            to="/cars"
            color="primary"
            size="large"
            variant="flat"
            class="px-8 font-weight-semibold"
            height="50"
          >
            Rezerwuj teraz
          </v-btn>
        </v-card-text>
      </v-card>

      <!-- Quick Actions Grid -->
      <v-row>
        <v-col
          v-for="card in quickActions"
          :key="card.title"
          cols="12"
          md="4"
        >
          <v-card
            class="pa-6 h-100 glass-card d-flex flex-column justify-space-between"
            variant="flat"
          >
            <div>
              <div class="d-flex mb-4">
                <v-icon :color="card.color" size="24">{{ card.icon }}</v-icon>
              </div>
              <v-card-title class="pa-0 font-weight-bold text-lg mb-2 text-white">{{ card.title }}</v-card-title>
              <v-card-text class="pa-0 text-medium-emphasis text-sm mb-4" style="line-height: 1.5;">
                {{ card.text }}
              </v-card-text>
            </div>

            <v-card-actions class="pa-0">
              <v-btn
                :to="card.to"
                :color="card.color"
                variant="text"
                class="px-0 font-weight-semibold"
                append-icon="mdi-arrow-right"
              >
                {{ card.cta }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>
