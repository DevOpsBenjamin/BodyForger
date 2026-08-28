<script setup>
import { 
  Zap, 
  Watch, 
  Scale, 
  Dumbbell, 
  Activity, 
  CheckCircle2, 
  Github, 
  ArrowRight, 
  Cpu, 
  Sparkles, 
  Layers, 
  Clock, 
  HeartPulse, 
  Bluetooth, 
  FileText,
  Flame,
  ShieldCheck,
  Smartphone
} from 'lucide-vue-next'

const pillars = [
  {
    icon: Watch,
    badge: "100% Autonome",
    badgeColor: "bg-cyan-500/10 text-cyan-400 border-cyan-500/20",
    title: "Wear OS Wrist-First",
    description: "Entraînez-vous sans smartphone. L'application tourne en tâche de fond native via l'API Android Health Services.",
    features: [
      "Écran éteint & Ambient Display (AOD 1Hz sans vider la batterie)",
      "Cardiofréquencemètre en continu sur coprocesseur basse consommation",
      "Chronomètre de repos avec retours haptiques personnalisés",
      "Saisie rapide des séries & poids via couronne rotative"
    ]
  },
  {
    icon: Scale,
    badge: "DEXA-Grade",
    badgeColor: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20",
    title: "BIA & Balances BLE",
    description: "Héritage direct de SimpleBodyGraph. Connexion GATT native aux balances connectées avec algorithmes multi-fréquences.",
    features: [
      "Pilote Bluetooth natif Huawei Scale 3 / Pro (handshake crypto)",
      "Analyse segmentaire 8 électrodes (Tronc, Bras D/G, Jambes D/G)",
      "Calcul DEXA : Masse grasse, SMM, FFM, Somatotypes, VFL, Eau (ICW/ECW)",
      "Paliers d'objectifs validés par tendance médiane hebdomadaire"
    ]
  },
  {
    icon: Dumbbell,
    badge: "1 300+ Exos",
    badgeColor: "bg-amber-500/10 text-amber-400 border-amber-500/20",
    title: "Moteur OpenGym",
    description: "La puissance de la base open-source openGym avec modélisation avancée de l'intensité et de l'hypertrophie.",
    features: [
      "Catalogue exhaustif de 1 300+ exercices avec instructions & animations",
      "Support natif des Drop-sets avec réduction de charge en % automatique",
      "Clusters Rest-pause / Myo-reps décomposés en séries d'activation",
      "Estimation du 1RM (Epley/Brzycki) et Heatmap musculaire hebdomadaire"
    ]
  },
  {
    icon: Activity,
    badge: "Écosystème Google",
    badgeColor: "bg-violet-500/10 text-violet-400 border-violet-500/20",
    title: "Health Connect & MCP AI",
    description: "Synchronisation totale avec l'écosystème Google Health Connect et génération de routines par IA via MCP.",
    features: [
      "Support de PlannedExerciseSessionRecord pour importer des séances",
      "Export complet des séances avec séries temporelles de rythme cardiaque",
      "Serveur MCP (Model Context Protocol) dédié pour Gemini / Claude",
      "Génération automatique de programmes selon l'évolution BIA réelle"
    ]
  }
]

const comparisonItems = [
  { feature: "Autonomie Wear OS (Sans téléphone)", hevy: "Déconnexions & bugs", bodyforger: "100% Standalone (Room DB locale)" },
  { feature: "Mode Écran Éteint / Ambient", hevy: "App tuée ou figée", bodyforger: "Health Services & AOD 1Hz natif" },
  { feature: "Balances BLE & Impédancemétrie (BIA)", hevy: "Poids basique manuel", bodyforger: "Pilote BLE GATT + Modèle DEXA 8 électrodes" },
  { feature: "Google Health Connect", hevy: "Synchronisation partielle", bodyforger: "Planned Exercises + Séries temporelles HR" },
  { feature: "Intégration IA (Gemini / Claude)", hevy: "Aucune", bodyforger: "Serveur MCP natif (Génération de routines)" },
  { feature: "Modèle de données & Coût", hevy: "Abonnement propriétaire", bodyforger: "Open-Source & Local-First (Gratuit)" }
]

const roadmapPhases = [
  { phase: "Phase 0", title: "Architecture & Branding", status: "Terminé", done: true },
  { phase: "Phase 1", title: "Socle BIA DEXA & Driver BLE en Kotlin", status: "En cours", current: true },
  { phase: "Phase 2", title: "Intégration Catalogue 1 300 Exos & Modèles de séries", status: "À venir", done: false },
  { phase: "Phase 3", title: "Moteur Wear OS (Health Services + AOD + Haptique)", status: "À venir", done: false },
  { phase: "Phase 4", title: "Synchronisation Wearable Data Layer (Montre ↔ Tel)", status: "À venir", done: false },
  { phase: "Phase 5", title: "Exporteur Google Health Connect (Sessions & Planned)", status: "À venir", done: false },
  { phase: "Phase 6", title: "Serveur MCP pour génération IA de routines", status: "À venir", done: false }
]
</script>

<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-cyan-500 selection:text-slate-950">
    <!-- Glow Background Accents -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none z-0">
      <div class="absolute -top-40 -left-40 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl"></div>
      <div class="absolute top-1/3 -right-40 w-96 h-96 bg-violet-500/10 rounded-full blur-3xl"></div>
      <div class="absolute -bottom-40 left-1/3 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl"></div>
    </div>

    <!-- Header Navigation -->
    <header class="relative z-10 border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-md sticky top-0">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-tr from-cyan-500 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
            <Zap class="w-5 h-5 text-slate-950 stroke-[2.5]" />
          </div>
          <div>
            <span class="font-extrabold text-lg tracking-tight bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">BodyForger</span>
            <span class="ml-2 text-xs font-semibold px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">WIP</span>
          </div>
        </div>

        <div class="flex items-center gap-4">
          <a 
            href="https://github.com/DevOpsBenjamin/BodyForger" 
            target="_blank" 
            rel="noopener noreferrer"
            class="flex items-center gap-2 text-sm font-medium text-slate-300 hover:text-white px-3.5 py-1.5 rounded-lg hover:bg-slate-800/60 transition-colors border border-transparent hover:border-slate-700"
          >
            <Github class="w-4 h-4" />
            <span class="hidden sm:inline">GitHub</span>
          </a>
        </div>
      </div>
    </header>

    <!-- Hero Section -->
    <main class="relative z-10 flex-1">
      <section class="max-w-5xl mx-auto px-4 sm:px-6 pt-16 pb-20 text-center">
        <!-- Status Pill -->
        <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-900 border border-slate-800 text-xs font-medium text-slate-300 mb-8 shadow-inner">
          <span class="w-2 h-2 rounded-full bg-cyan-400 animate-pulse"></span>
          Application Native Android & Wear OS en cours de développement
        </div>

        <h1 class="text-4xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight text-white mb-6 leading-tight">
          Forge Your Physique. <br />
          <span class="bg-gradient-to-r from-cyan-400 via-teal-300 to-emerald-400 bg-clip-text text-transparent">
            Wrist-First & Autonomous.
          </span>
        </h1>

        <p class="text-lg sm:text-xl text-slate-400 max-w-3xl mx-auto mb-10 leading-relaxed">
          L'écosystème de musculation et de suivi de composition corporelle tout-en-un. 
          Fusionne le meilleur de <strong class="text-slate-200">openGym</strong> et de <strong class="text-slate-200">SimpleBodyGraph</strong> avec un moteur Wear OS autonome et une intégration profonde à <strong class="text-slate-200">Google Health Connect</strong>.
        </p>

        <!-- CTA Buttons -->
        <div class="flex flex-wrap items-center justify-center gap-4 mb-14">
          <a 
            href="https://github.com/DevOpsBenjamin/BodyForger" 
            target="_blank" 
            rel="noopener noreferrer"
            class="flex items-center gap-2.5 px-6 py-3.5 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-sm shadow-xl shadow-cyan-500/25 transition-all hover:scale-105 active:scale-95"
          >
            <Github class="w-4 h-4" />
            Explorer le code source sur GitHub
            <ArrowRight class="w-4 h-4" />
          </a>
          <a 
            href="https://github.com/DevOpsBenjamin/BodyForger/blob/main/PLAN.md" 
            target="_blank" 
            rel="noopener noreferrer"
            class="flex items-center gap-2 px-6 py-3.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-200 font-semibold text-sm border border-slate-800 hover:border-slate-700 transition-all"
          >
            <FileText class="w-4 h-4 text-cyan-400" />
            Lire le Master Plan technique
          </a>
        </div>

        <!-- Tech Badges Strip -->
        <div class="flex flex-wrap justify-center items-center gap-3 text-xs text-slate-400">
          <span class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-900/80 border border-slate-800">
            <Smartphone class="w-3.5 h-3.5 text-cyan-400" /> Kotlin Native Android
          </span>
          <span class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-900/80 border border-slate-800">
            <Watch class="w-3.5 h-3.5 text-cyan-400" /> Standalone Wear OS
          </span>
          <span class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-900/80 border border-slate-800">
            <Bluetooth class="w-3.5 h-3.5 text-emerald-400" /> Huawei Scale 3 BLE
          </span>
          <span class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-900/80 border border-slate-800">
            <HeartPulse class="w-3.5 h-3.5 text-rose-400" /> Google Health Connect
          </span>
          <span class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-900/80 border border-slate-800">
            <Cpu class="w-3.5 h-3.5 text-violet-400" /> Serveur MCP Gemini
          </span>
        </div>
      </section>

      <!-- 4 Core Pillars Section -->
      <section class="max-w-6xl mx-auto px-4 sm:px-6 py-16">
        <div class="text-center mb-14">
          <h2 class="text-2xl sm:text-4xl font-bold tracking-tight text-white mb-3">
            Les 4 Piliers Fondateurs de BodyForger
          </h2>
          <p class="text-slate-400 text-sm sm:text-base max-w-2xl mx-auto">
            Une architecture pensée pour l'indépendance de la montre, la précision clinique et la synchro d'écosystème.
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div 
            v-for="pillar in pillars" 
            :key="pillar.title"
            class="p-7 rounded-2xl bg-slate-900/50 border border-slate-800/80 hover:border-slate-700/80 transition-all hover:bg-slate-900/80 relative overflow-hidden group flex flex-col justify-between"
          >
            <div>
              <div class="flex items-center justify-between mb-5">
                <div class="w-12 h-12 rounded-xl bg-slate-800 border border-slate-700/60 flex items-center justify-center text-cyan-400 group-hover:scale-110 group-hover:bg-cyan-500/10 transition-all">
                  <component :is="pillar.icon" class="w-6 h-6" />
                </div>
                <span class="text-xs font-semibold px-2.5 py-1 rounded-full border" :class="pillar.badgeColor">
                  {{ pillar.badge }}
                </span>
              </div>

              <h3 class="text-xl font-bold text-white mb-2">{{ pillar.title }}</h3>
              <p class="text-slate-400 text-sm mb-6 leading-relaxed">{{ pillar.description }}</p>
            </div>

            <ul class="space-y-2.5 pt-4 border-t border-slate-800/60">
              <li v-for="feat in pillar.features" :key="feat" class="flex items-start gap-2.5 text-xs sm:text-sm text-slate-300">
                <CheckCircle2 class="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
                <span>{{ feat }}</span>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <!-- Why BodyForger (Comparison Table) -->
      <section class="max-w-5xl mx-auto px-4 sm:px-6 py-16">
        <div class="text-center mb-12">
          <span class="text-xs font-semibold px-3 py-1 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/20 mb-3 inline-block">
            Pourquoi une nouvelle app ?
          </span>
          <h2 class="text-2xl sm:text-3xl font-bold text-white mb-2">
            La rupture avec Hevy & les applications actuelles
          </h2>
          <p class="text-slate-400 text-sm max-w-xl mx-auto">
            Résoudre les blocages du quotidien qui gâchent l'expérience d'entraînement et le suivi de santé.
          </p>
        </div>

        <div class="overflow-x-auto rounded-2xl border border-slate-800 bg-slate-900/40">
          <table class="w-full text-left text-sm">
            <thead class="bg-slate-900/90 text-xs uppercase text-slate-400 border-b border-slate-800">
              <tr>
                <th class="py-4 px-5">Fonctionnalité</th>
                <th class="py-4 px-5 text-rose-400/80">Hevy / Apps classiques</th>
                <th class="py-4 px-5 text-cyan-400">BodyForger</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-800/60 text-slate-300">
              <tr v-for="item in comparisonItems" :key="item.feature" class="hover:bg-slate-900/40">
                <td class="py-3.5 px-5 font-medium text-white">{{ item.feature }}</td>
                <td class="py-3.5 px-5 text-slate-400">{{ item.hevy }}</td>
                <td class="py-3.5 px-5 text-cyan-300 font-medium flex items-center gap-2">
                  <CheckCircle2 class="w-4 h-4 text-cyan-400 shrink-0" />
                  {{ item.bodyforger }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- Roadmap Section -->
      <section class="max-w-4xl mx-auto px-4 sm:px-6 py-16">
        <div class="text-center mb-12">
          <h2 class="text-2xl sm:text-3xl font-bold text-white mb-2">Roadmap & État d'avancement</h2>
          <p class="text-slate-400 text-sm">Suivi transparent du cycle de développement.</p>
        </div>

        <div class="space-y-3">
          <div 
            v-for="step in roadmapPhases" 
            :key="step.phase"
            class="p-4 rounded-xl border flex items-center justify-between"
            :class="[
              step.done ? 'bg-slate-900/80 border-slate-800 text-slate-300' :
              step.current ? 'bg-cyan-950/30 border-cyan-500/40 text-white shadow-lg shadow-cyan-500/5' :
              'bg-slate-950/40 border-slate-900 text-slate-500'
            ]"
          >
            <div class="flex items-center gap-3">
              <span 
                class="text-xs font-bold px-2 py-0.5 rounded"
                :class="[
                  step.done ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
                  step.current ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 animate-pulse' :
                  'bg-slate-900 text-slate-600'
                ]"
              >
                {{ step.phase }}
              </span>
              <span class="font-medium text-sm">{{ step.title }}</span>
            </div>
            <span 
              class="text-xs font-semibold"
              :class="[
                step.done ? 'text-emerald-400' :
                step.current ? 'text-cyan-400' :
                'text-slate-600'
              ]"
            >
              {{ step.status }}
            </span>
          </div>
        </div>
      </section>
    </main>

    <!-- Footer -->
    <footer class="relative z-10 border-t border-slate-900 py-10 bg-slate-950">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
        <div class="flex items-center gap-2">
          <Zap class="w-4 h-4 text-cyan-400" />
          <span class="font-semibold text-slate-400">BodyForger</span>
          <span>— Open-Source, Offline-First & Wrist-First</span>
        </div>
        <div class="flex items-center gap-4">
          <a href="https://github.com/DevOpsBenjamin/SimpleBodyGraph" target="_blank" class="hover:text-slate-300 transition-colors">SimpleBodyGraph</a>
          <span>•</span>
          <a href="https://gitlab.com/DuarteSantos8/opengym" target="_blank" class="hover:text-slate-300 transition-colors">openGym</a>
          <span>•</span>
          <a href="https://github.com/DevOpsBenjamin/BodyForger" target="_blank" class="hover:text-slate-300 transition-colors">GitHub Repo</a>
        </div>
      </div>
    </footer>
  </div>
</template>
