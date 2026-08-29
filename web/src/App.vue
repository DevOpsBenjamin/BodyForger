<script setup>
import { computed, onMounted } from 'vue'
import {
  Zap, Watch, Scale, Dumbbell, Activity, CheckCircle2, Github,
  ArrowRight, Cpu, HeartPulse, Bluetooth, FileText, Smartphone, Languages
} from 'lucide-vue-next'
import { t, locale, setLocale, AVAILABLE } from './i18n.js'

const REPO = 'https://github.com/DevOpsBenjamin/BodyForger'

const LINKS = {
  opengym: { href: 'https://gitlab.com/DuarteSantos8/opengym', label: 'openGym' },
  bodygraph: { href: 'https://github.com/DevOpsBenjamin/SimpleBodyGraph', label: 'SimpleBodyGraph' },
  health: { href: 'https://health.google/health-connect-android/', label: 'Google Health Connect' },
  hevy: { href: 'https://www.hevyapp.com/', label: 'Hevy' }
}

/** Splits "text {key} text" into renderable segments, so links stay real
 *  anchors instead of going through v-html. */
function segments(str, extras = {}) {
  return str
    .split(/(\{[a-z]+\})/gi)
    .filter((chunk) => chunk !== '')
    .map((chunk, i) => {
      const match = /^\{([a-z]+)\}$/i.exec(chunk)
      if (!match) return { k: i, type: 'text', value: chunk }
      const key = match[1]
      if (LINKS[key]) return { k: i, type: 'link', href: LINKS[key].href, value: LINKS[key].label }
      if (extras[key]) return { k: i, type: 'strong', value: extras[key] }
      return { k: i, type: 'text', value: chunk }
    })
}

const PILLAR_ICONS = [Watch, Scale, Dumbbell, Activity]
const PILLAR_ACCENT = [
  'bg-neon/10 text-neon border-neon/25',
  'bg-electric/10 text-electric border-electric/25',
  'bg-gold/10 text-gold border-gold/25',
  'bg-txt/5 text-txt-2 border-line'
]

const pillars = computed(() =>
  t.value.pillars.items.map((item, i) => ({
    ...item,
    icon: PILLAR_ICONS[i],
    accent: PILLAR_ACCENT[i]
  }))
)

const PHASE_STATE = ['done', 'current', 'todo', 'todo', 'todo', 'todo', 'todo']

const roadmap = computed(() =>
  t.value.roadmap.phases.map((title, i) => ({
    phase: `Phase ${i}`,
    title,
    state: PHASE_STATE[i],
    status: t.value.roadmap.status[PHASE_STATE[i]]
  }))
)

onMounted(() => {
  document.documentElement.lang = locale.value
})
</script>

<template>
  <div class="min-h-screen bg-obsidian text-txt flex flex-col font-sans selection:bg-neon selection:text-obsidian">
    <!-- Ambient glow -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none z-0" aria-hidden="true">
      <div class="absolute -top-40 -left-40 w-[30rem] h-[30rem] rounded-full bg-neon/8 blur-3xl"></div>
      <div class="absolute top-1/3 -right-40 w-[28rem] h-[28rem] rounded-full bg-electric/6 blur-3xl"></div>
    </div>

    <!-- Header: z-50 so page content scrolls underneath instead of over it -->
    <header class="sticky top-0 z-50 border-b border-line bg-obsidian/90 backdrop-blur-md">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-neon flex items-center justify-center shadow-lg shadow-neon/20">
            <Zap class="w-5 h-5 text-obsidian stroke-[2.5]" />
          </div>
          <div class="flex items-center">
            <span class="font-extrabold text-lg tracking-tight text-txt">BodyForger</span>
            <span class="ml-2 text-xs font-semibold px-2 py-0.5 rounded-full bg-gold/10 text-gold border border-gold/25">
              {{ t.nav.wip }}
            </span>
          </div>
        </div>

        <div class="flex items-center gap-2 sm:gap-3">
          <!-- Language switch -->
          <div
            class="flex items-center rounded-lg border border-line bg-surface p-0.5"
            role="group"
            :aria-label="t.nav.langLabel"
          >
            <Languages class="w-3.5 h-3.5 text-txt-3 mx-1.5 hidden sm:block" aria-hidden="true" />
            <button
              v-for="lang in AVAILABLE"
              :key="lang.code"
              type="button"
              :aria-pressed="locale === lang.code"
              :class="[
                'px-2.5 py-1 text-xs font-bold rounded-md transition-colors',
                locale === lang.code
                  ? 'bg-neon text-obsidian'
                  : 'text-txt-2 hover:text-txt hover:bg-surface-2'
              ]"
              @click="setLocale(lang.code)"
            >
              {{ lang.label }}
            </button>
          </div>

          <a
            :href="REPO"
            target="_blank"
            rel="noopener noreferrer"
            class="flex items-center gap-2 text-sm font-medium text-txt-2 hover:text-txt px-3 py-1.5 rounded-lg hover:bg-surface transition-colors border border-transparent hover:border-line"
          >
            <Github class="w-4 h-4" />
            <span class="hidden sm:inline">{{ t.nav.github }}</span>
          </a>
        </div>
      </div>
    </header>

    <!-- pb-24 clears the fixed footer -->
    <main class="relative z-10 flex-1 pb-24">
      <!-- Hero -->
      <section class="max-w-5xl mx-auto px-4 sm:px-6 pt-16 pb-20 text-center">
        <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-surface border border-line text-xs font-medium text-txt-2 mb-8">
          <span class="w-2 h-2 rounded-full bg-neon animate-pulse"></span>
          {{ t.hero.status }}
        </div>

        <h1 class="text-4xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight text-txt mb-6 leading-tight">
          {{ t.hero.titleTop }} <br />
          <span class="text-neon">{{ t.hero.titleAccent }}</span>
        </h1>

        <p class="text-lg sm:text-xl text-txt-2 max-w-3xl mx-auto mb-10 leading-relaxed">
          <template v-for="seg in segments(t.hero.subtitle)" :key="seg.k">
            <a
              v-if="seg.type === 'link'"
              :href="seg.href"
              target="_blank"
              rel="noopener noreferrer"
              class="text-neon hover:text-neon/80 underline underline-offset-2 decoration-neon/40"
            >{{ seg.value }}</a>
            <template v-else>{{ seg.value }}</template>
          </template>
        </p>

        <div class="flex flex-wrap items-center justify-center gap-4 mb-14">
          <a
            :href="REPO"
            target="_blank"
            rel="noopener noreferrer"
            class="flex items-center gap-2.5 px-6 py-3.5 rounded-xl bg-neon hover:brightness-110 text-obsidian font-bold text-sm shadow-xl shadow-neon/20 transition-all hover:scale-105 active:scale-95"
          >
            <Github class="w-4 h-4" />
            {{ t.hero.ctaPrimary }}
            <ArrowRight class="w-4 h-4" />
          </a>
          <a
            :href="`${REPO}/blob/main/PLAN.md`"
            target="_blank"
            rel="noopener noreferrer"
            class="flex items-center gap-2 px-6 py-3.5 rounded-xl bg-surface hover:bg-surface-2 text-txt font-semibold text-sm border border-line transition-all"
          >
            <FileText class="w-4 h-4 text-neon" />
            {{ t.hero.ctaSecondary }}
          </a>
        </div>

        <div class="flex flex-wrap justify-center items-center gap-3 text-xs text-txt-2">
          <span class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-surface border border-line">
            <Smartphone class="w-3.5 h-3.5 text-neon" /> {{ t.hero.badges.kotlin }}
          </span>
          <span class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-surface border border-line">
            <Watch class="w-3.5 h-3.5 text-neon" /> {{ t.hero.badges.wear }}
          </span>
          <span class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-surface border border-line">
            <Bluetooth class="w-3.5 h-3.5 text-electric" /> {{ t.hero.badges.ble }}
          </span>
          <span class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-surface border border-line">
            <HeartPulse class="w-3.5 h-3.5 text-crimson" /> {{ t.hero.badges.health }}
          </span>
          <span class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-surface border border-line">
            <Cpu class="w-3.5 h-3.5 text-gold" /> {{ t.hero.badges.mcp }}
          </span>
        </div>
      </section>

      <!-- Pillars -->
      <section class="max-w-6xl mx-auto px-4 sm:px-6 py-16">
        <div class="text-center mb-14">
          <h2 class="text-2xl sm:text-4xl font-bold tracking-tight text-txt mb-3">{{ t.pillars.title }}</h2>
          <p class="text-txt-2 text-sm sm:text-base max-w-2xl mx-auto">{{ t.pillars.subtitle }}</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div
            v-for="pillar in pillars"
            :key="pillar.title"
            class="p-7 rounded-2xl bg-surface border border-line hover:border-neon/30 transition-all group flex flex-col justify-between"
          >
            <div>
              <div class="flex items-center justify-between mb-5">
                <div class="w-12 h-12 rounded-xl bg-surface-2 border border-line flex items-center justify-center text-neon group-hover:scale-110 transition-transform">
                  <component :is="pillar.icon" class="w-6 h-6" />
                </div>
                <span class="text-xs font-semibold px-2.5 py-1 rounded-full border" :class="pillar.accent">
                  {{ pillar.badge }}
                </span>
              </div>

              <h3 class="text-xl font-bold text-txt mb-2">{{ pillar.title }}</h3>
              <p class="text-txt-2 text-sm mb-6 leading-relaxed">{{ pillar.description }}</p>
            </div>

            <ul class="space-y-2.5 pt-4 border-t border-line">
              <li
                v-for="feat in pillar.features"
                :key="feat"
                class="flex items-start gap-2.5 text-xs sm:text-sm text-txt-2"
              >
                <CheckCircle2 class="w-4 h-4 text-neon shrink-0 mt-0.5" />
                <span>{{ feat }}</span>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <!-- Design trade-offs -->
      <section class="max-w-5xl mx-auto px-4 sm:px-6 py-16">
        <div class="text-center mb-12">
          <span class="text-xs font-semibold px-3 py-1 rounded-full bg-neon/10 text-neon border border-neon/25 mb-3 inline-block">
            {{ t.comparison.badge }}
          </span>
          <h2 class="text-2xl sm:text-3xl font-bold text-txt mb-2">{{ t.comparison.title }}</h2>
          <p class="text-txt-2 text-sm max-w-xl mx-auto leading-relaxed">
            <template
              v-for="seg in segments(t.comparison.subtitle, { tradeoffs: t.comparison.tradeoffs })"
              :key="seg.k"
            >
              <strong v-if="seg.type === 'strong'" class="text-txt">{{ seg.value }}</strong>
              <template v-else>{{ seg.value }}</template>
            </template>
          </p>
        </div>

        <div class="overflow-x-auto rounded-2xl border border-line bg-surface">
          <table class="w-full text-left text-sm">
            <thead class="bg-surface-2 text-xs uppercase text-txt-2 border-b border-line">
              <tr>
                <th class="py-4 px-5">{{ t.comparison.colFeature }}</th>
                <th class="py-4 px-5">{{ t.comparison.colCommon }}</th>
                <th class="py-4 px-5 text-neon">BodyForger</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-line text-txt-2">
              <tr v-for="item in t.comparison.rows" :key="item.feature" class="hover:bg-surface-2/50">
                <td class="py-3.5 px-5 font-medium text-txt">{{ item.feature }}</td>
                <td class="py-3.5 px-5">{{ item.common }}</td>
                <td class="py-3.5 px-5 text-neon font-medium">
                  <span class="flex items-center gap-2">
                    <CheckCircle2 class="w-4 h-4 shrink-0" />
                    {{ item.bodyforger }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <p class="text-txt-2 text-sm max-w-2xl mx-auto text-center mt-8 leading-relaxed">
          <template v-for="seg in segments(t.comparison.note)" :key="seg.k">
            <a
              v-if="seg.type === 'link'"
              :href="seg.href"
              target="_blank"
              rel="noopener noreferrer"
              class="text-neon hover:text-neon/80 underline underline-offset-2 decoration-neon/40"
            >{{ seg.value }}</a>
            <template v-else>{{ seg.value }}</template>
          </template>
        </p>
      </section>

      <!-- Roadmap -->
      <section class="max-w-4xl mx-auto px-4 sm:px-6 py-16">
        <div class="text-center mb-12">
          <h2 class="text-2xl sm:text-3xl font-bold text-txt mb-2">{{ t.roadmap.title }}</h2>
          <p class="text-txt-2 text-sm">{{ t.roadmap.subtitle }}</p>
        </div>

        <div class="space-y-3">
          <div
            v-for="step in roadmap"
            :key="step.phase"
            class="p-4 rounded-xl border flex items-center justify-between gap-3"
            :class="[
              step.state === 'done' ? 'bg-surface border-line text-txt-2' :
              step.state === 'current' ? 'bg-neon/5 border-neon/40 text-txt' :
              'bg-obsidian border-line/60 text-txt-3'
            ]"
          >
            <div class="flex items-center gap-3 min-w-0">
              <span
                class="text-xs font-bold px-2 py-0.5 rounded shrink-0 border"
                :class="[
                  step.state === 'done' ? 'bg-surface-2 text-txt-2 border-line' :
                  step.state === 'current' ? 'bg-neon/15 text-neon border-neon/40 animate-pulse' :
                  'bg-surface text-txt-3 border-line/60'
                ]"
              >
                {{ step.phase }}
              </span>
              <span class="font-medium text-sm truncate">{{ step.title }}</span>
            </div>
            <span
              class="text-xs font-semibold shrink-0"
              :class="[
                step.state === 'done' ? 'text-txt-2' :
                step.state === 'current' ? 'text-neon' :
                'text-txt-3'
              ]"
            >
              {{ step.status }}
            </span>
          </div>
        </div>
      </section>
    </main>

    <!-- Fixed footer -->
    <footer class="fixed bottom-0 inset-x-0 z-40 border-t border-line bg-obsidian/95 backdrop-blur-md">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 py-3 flex flex-col sm:flex-row items-center justify-between gap-1.5 sm:gap-4 text-xs text-txt-3">
        <div class="flex items-center gap-2">
          <Zap class="w-3.5 h-3.5 text-neon shrink-0" />
          <span class="font-semibold text-txt-2">BodyForger</span>
          <span class="hidden sm:inline">— {{ t.footer.tagline }}</span>
        </div>
        <div class="flex items-center gap-3">
          <a
            href="https://github.com/DevOpsBenjamin/SimpleBodyGraph"
            target="_blank"
            rel="noopener noreferrer"
            class="hover:text-neon transition-colors"
          >SimpleBodyGraph</a>
          <span aria-hidden="true">•</span>
          <a
            href="https://gitlab.com/DuarteSantos8/opengym"
            target="_blank"
            rel="noopener noreferrer"
            class="hover:text-neon transition-colors"
          >openGym</a>
          <span aria-hidden="true">•</span>
          <a :href="REPO" target="_blank" rel="noopener noreferrer" class="hover:text-neon transition-colors">
            {{ t.footer.repo }}
          </a>
        </div>
      </div>
    </footer>
  </div>
</template>
