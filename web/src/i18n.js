import { ref, computed } from 'vue'

const STORAGE_KEY = 'bodyforger-locale'

export const messages = {
  en: {
    nav: { wip: 'WIP', github: 'GitHub', langLabel: 'Language' },
    hero: {
      status: 'Native Android & Wear OS app under active development',
      titleTop: 'Forge Your Physique.',
      titleAccent: 'Wrist-First & Autonomous.',
      subtitle: 'A personal strength-training and body-composition tracker. It brings together the {opengym} exercise engine and the body-composition work from {bodygraph}, with a Wear OS engine that runs a full session on its own and writes to {health} when it is done.',
      ctaPrimary: 'Browse the source on GitHub',
      ctaSecondary: 'Read the technical Master Plan',
      badges: {
        kotlin: 'Kotlin Native Android',
        wear: 'Standalone Wear OS',
        ble: 'Huawei Scale 3 BLE',
        health: 'Google Health Connect',
        mcp: 'Gemini MCP server'
      }
    },
    pillars: {
      title: 'The four pillars',
      subtitle: 'An architecture built around watch independence, honest measurement and clean hand-off to the Google Health ecosystem.',
      items: [
        {
          badge: '100% standalone',
          title: 'Wrist-first Wear OS',
          description: 'Train without your phone. The session runs as a native foreground service on the watch, through Android Health Services.',
          features: [
            'Screen off & Ambient Display (1 Hz AOD, without draining the battery)',
            'Continuous heart rate on the low-power sensor coprocessor',
            'Rest timer with haptic feedback you can feel without looking',
            'Fast set and weight entry via the rotating crown'
          ]
        },
        {
          badge: 'DEXA-calibrated',
          title: 'BIA & BLE scales',
          description: 'Carried over from SimpleBodyGraph. Native GATT connection to smart scales, with multi-frequency modelling.',
          features: [
            'Native Bluetooth driver for Huawei Scale 3 / Pro (crypto handshake)',
            'Raw impedances kept verbatim: 6 anatomical paths × 2 frequencies',
            'Segmental analysis derived on demand, never stored',
            'Milestone goals validated on a weekly median trend'
          ]
        },
        {
          badge: '1,300+ exercises',
          title: 'openGym engine',
          description: 'Built on the open-source openGym catalogue, with detailed modelling of intensity and working volume.',
          features: [
            'Full 1,300+ exercise catalogue with instructions and animations',
            'Native drop-set support with automatic percentage load reduction',
            'Rest-pause / myo-rep clusters broken down into activation bursts',
            '1RM estimation (Epley / Brzycki) and weekly muscle heatmap'
          ]
        },
        {
          badge: 'Google ecosystem',
          title: 'Health Connect & MCP',
          description: 'Sessions are written outwards to Health Connect as properly typed records — and exposed to AI assistants through MCP.',
          features: [
            'Exercise types mapped onto Google SDK types, not opaque blobs',
            'Full session export with heart-rate time series',
            'Dedicated MCP (Model Context Protocol) server for Gemini / Claude',
            'The app keeps its own database, history and statistics'
          ]
        }
      ]
    },
    comparison: {
      badge: 'Why another app?',
      title: 'Different design trade-offs',
      subtitle: 'BodyForger is a personal project, tuned for one specific setup: the watch on the wrist and Google Health as the destination. This table compares {tradeoffs}, not quality — commercial apps are more polished, better supported and suit far more people.',
      tradeoffs: 'design choices',
      colFeature: 'Feature',
      colCommon: 'Common approach',
      rows: [
        { feature: 'Wear OS autonomy', common: 'The watch companions the phone', bodyforger: 'Standalone watch (local Room DB on board)' },
        { feature: 'Screen off / Ambient', common: 'Varies by manufacturer', bodyforger: 'Health Services & native 1 Hz AOD' },
        { feature: 'BLE scales & impedance', common: 'Manual weight and body-fat entry', bodyforger: 'BLE GATT driver + 8-electrode DEXA model' },
        { feature: 'Google Health Connect', common: 'Session export, often partial', bodyforger: 'Planned Exercises + HR time series' },
        { feature: 'AI assistants (Gemini / Claude)', common: 'Not offered', bodyforger: 'Native MCP server (routine generation)' },
        { feature: 'Model & cost', common: 'Hosted service, often subscription', bodyforger: 'Open source, local-first, no account' }
      ],
      note: 'If you train phone-in-hand, without a Wear OS watch, or outside Google Health, BodyForger will not replace your current app — and does not try to. {hevy} and {opengym} are excellent tools, more complete and better supported. This project came out of one very specific personal need: a watch that is genuinely autonomous, and a Health Connect sync that loses nothing on the way.'
    },
    roadmap: {
      title: 'Roadmap & progress',
      subtitle: 'Transparent tracking of the development cycle.',
      status: { done: 'Done', current: 'In progress', todo: 'Planned' },
      phases: [
        'Architecture & branding',
        'DEXA BIA core & BLE driver in Kotlin',
        '1,300 exercise catalogue & set models',
        'Wear OS engine (Health Services + AOD + haptics)',
        'Wearable Data Layer sync (watch ↔ phone)',
        'Google Health Connect exporter (sessions & planned)',
        'MCP server for AI routine generation'
      ]
    },
    footer: { tagline: 'Open source, offline-first & wrist-first', repo: 'GitHub repo' }
  },

  fr: {
    nav: { wip: 'WIP', github: 'GitHub', langLabel: 'Langue' },
    hero: {
      status: 'Application native Android & Wear OS en cours de développement',
      titleTop: 'Forge Your Physique.',
      titleAccent: 'Wrist-First & Autonomous.',
      subtitle: 'Un suivi personnel de musculation et de composition corporelle. Il réunit le moteur d’exercices {opengym} et le travail de composition corporelle de {bodygraph}, avec un moteur Wear OS qui mène une séance de bout en bout tout seul et écrit dans {health} une fois terminé.',
      ctaPrimary: 'Explorer le code source sur GitHub',
      ctaSecondary: 'Lire le Master Plan technique',
      badges: {
        kotlin: 'Kotlin natif Android',
        wear: 'Wear OS autonome',
        ble: 'Huawei Scale 3 BLE',
        health: 'Google Health Connect',
        mcp: 'Serveur MCP Gemini'
      }
    },
    pillars: {
      title: 'Les quatre piliers',
      subtitle: 'Une architecture pensée pour l’indépendance de la montre, l’honnêteté de la mesure et une transmission propre vers l’écosystème Google Health.',
      items: [
        {
          badge: '100 % autonome',
          title: 'Wear OS wrist-first',
          description: 'Entraînez-vous sans smartphone. La séance tourne en service de premier plan natif sur la montre, via Android Health Services.',
          features: [
            'Écran éteint & Ambient Display (AOD 1 Hz, sans vider la batterie)',
            'Rythme cardiaque en continu sur le coprocesseur basse consommation',
            'Chronomètre de repos avec retours haptiques perceptibles à l’aveugle',
            'Saisie rapide des séries et des charges à la couronne rotative'
          ]
        },
        {
          badge: 'Calibré DEXA',
          title: 'BIA & balances BLE',
          description: 'Hérité de SimpleBodyGraph. Connexion GATT native aux balances connectées, avec modélisation multi-fréquences.',
          features: [
            'Pilote Bluetooth natif Huawei Scale 3 / Pro (handshake cryptographique)',
            'Impédances brutes conservées telles quelles : 6 trajets × 2 fréquences',
            'Analyse segmentaire dérivée à la demande, jamais stockée',
            'Paliers d’objectifs validés par tendance médiane hebdomadaire'
          ]
        },
        {
          badge: '1 300+ exercices',
          title: 'Moteur openGym',
          description: 'Bâti sur le catalogue open source openGym, avec une modélisation fine de l’intensité et du volume de travail.',
          features: [
            'Catalogue complet de 1 300+ exercices avec instructions et animations',
            'Support natif des drop-sets avec réduction de charge en pourcentage',
            'Clusters rest-pause / myo-reps décomposés en salves d’activation',
            'Estimation du 1RM (Epley / Brzycki) et heatmap musculaire hebdomadaire'
          ]
        },
        {
          badge: 'Écosystème Google',
          title: 'Health Connect & MCP',
          description: 'Les séances sont écrites vers Health Connect en enregistrements correctement typés — et exposées aux assistants IA via MCP.',
          features: [
            'Types d’exercices calés sur ceux du SDK Google, pas des blocs opaques',
            'Export complet des séances avec séries temporelles de rythme cardiaque',
            'Serveur MCP (Model Context Protocol) dédié pour Gemini / Claude',
            'L’application garde sa propre base, son historique et ses statistiques'
          ]
        }
      ]
    },
    comparison: {
      badge: 'Pourquoi une app de plus ?',
      title: 'Des choix de conception différents',
      subtitle: 'BodyForger est un projet personnel, taillé pour un usage précis : la montre au poignet et Google Health comme destination. Ce tableau compare des {tradeoffs}, pas des qualités — les applications commerciales sont plus abouties, mieux supportées et conviennent à bien plus de monde.',
      tradeoffs: 'partis pris',
      colFeature: 'Fonctionnalité',
      colCommon: 'Approche courante',
      rows: [
        { feature: 'Autonomie Wear OS', common: 'La montre accompagne le téléphone', bodyforger: 'Montre autonome (Room DB locale embarquée)' },
        { feature: 'Écran éteint / Ambient', common: 'Comportement variable selon le constructeur', bodyforger: 'Health Services & AOD 1 Hz natif' },
        { feature: 'Balances BLE & impédancemétrie', common: 'Saisie manuelle du poids et du taux de gras', bodyforger: 'Pilote BLE GATT + modèle DEXA 8 électrodes' },
        { feature: 'Google Health Connect', common: 'Export des séances, souvent partiel', bodyforger: 'Planned Exercises + séries temporelles HR' },
        { feature: 'Assistants IA (Gemini / Claude)', common: 'Non proposé', bodyforger: 'Serveur MCP natif (génération de routines)' },
        { feature: 'Modèle & coût', common: 'Service hébergé, souvent sur abonnement', bodyforger: 'Open source, local-first, sans compte' }
      ],
      note: 'Si vous vous entraînez téléphone en main, sans montre Wear OS, ou en dehors de Google Health, BodyForger ne remplacera pas votre application actuelle — et ne cherche pas à le faire. {hevy} et {opengym} sont d’excellents outils, plus complets et mieux accompagnés. Ce projet est né d’un besoin personnel très précis : une montre réellement autonome et une synchronisation Health Connect qui ne perde rien en route.'
    },
    roadmap: {
      title: 'Roadmap & état d’avancement',
      subtitle: 'Suivi transparent du cycle de développement.',
      status: { done: 'Terminé', current: 'En cours', todo: 'À venir' },
      phases: [
        'Architecture & branding',
        'Socle BIA DEXA & pilote BLE en Kotlin',
        'Catalogue 1 300 exercices & modèles de séries',
        'Moteur Wear OS (Health Services + AOD + haptique)',
        'Synchronisation Wearable Data Layer (montre ↔ téléphone)',
        'Exporteur Google Health Connect (séances & planifiées)',
        'Serveur MCP pour génération IA de routines'
      ]
    },
    footer: { tagline: 'Open source, offline-first & wrist-first', repo: 'Dépôt GitHub' }
  }
}

export const AVAILABLE = [
  { code: 'en', label: 'EN' },
  { code: 'fr', label: 'FR' }
]

function detectLocale() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved && messages[saved]) return saved
  } catch (e) { /* private mode, blocked storage */ }

  try {
    const nav = navigator.languages?.[0] || navigator.language || ''
    if (nav.toLowerCase().startsWith('fr')) return 'fr'
  } catch (e) { /* no navigator */ }

  return 'en'
}

export const locale = ref(detectLocale())

export function setLocale(code) {
  if (!messages[code]) return
  locale.value = code
  try {
    localStorage.setItem(STORAGE_KEY, code)
  } catch (e) { /* storage unavailable, in-memory only */ }
  try {
    document.documentElement.lang = code
  } catch (e) { /* no document */ }
}

export const t = computed(() => messages[locale.value])
