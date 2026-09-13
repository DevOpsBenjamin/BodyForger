<script setup>
import { computed } from 'vue'
import { ShieldCheck, Database, HeartPulse, Bluetooth, Cloud, UserX, Mail, ArrowLeft, Lock } from 'lucide-vue-next'
import { t } from './i18n.js'

const emit = defineEmits(['navigate'])

const sections = computed(() => [
  {
    icon: Database,
    title: t.value.privacy.sections.localFirst.title,
    content: t.value.privacy.sections.localFirst.content
  },
  {
    icon: HeartPulse,
    title: t.value.privacy.sections.healthData.title,
    content: t.value.privacy.sections.healthData.content
  },
  {
    icon: Bluetooth,
    title: t.value.privacy.sections.bleSensors.title,
    content: t.value.privacy.sections.bleSensors.content
  },
  {
    icon: Cloud,
    title: t.value.privacy.sections.cloudSync.title,
    content: t.value.privacy.sections.cloudSync.content
  },
  {
    icon: Lock,
    title: t.value.privacy.sections.permissions.title,
    content: t.value.privacy.sections.permissions.content
  },
  {
    icon: UserX,
    title: t.value.privacy.sections.rights.title,
    content: t.value.privacy.sections.rights.content
  },
  {
    icon: Mail,
    title: t.value.privacy.sections.contact.title,
    content: t.value.privacy.sections.contact.content
  }
])
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 pt-12 pb-24">
    <!-- Back button -->
    <button
      type="button"
      @click="emit('navigate', '/')"
      class="inline-flex items-center gap-2 text-sm font-medium text-txt-2 hover:text-neon transition-colors mb-8 cursor-pointer group"
    >
      <ArrowLeft class="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
      {{ t.privacy.backHome }}
    </button>

    <!-- Header -->
    <div class="mb-12">
      <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-neon/10 border border-neon/25 text-neon text-xs font-semibold mb-4">
        <ShieldCheck class="w-4 h-4" />
        {{ t.privacy.badge }}
      </div>
      <h1 class="text-3xl sm:text-5xl font-extrabold tracking-tight text-txt mb-4">
        {{ t.privacy.title }}
      </h1>
      <p class="text-sm text-txt-3">
        {{ t.privacy.lastUpdated }}
      </p>
      <p class="text-base sm:text-lg text-txt-2 mt-4 leading-relaxed">
        {{ t.privacy.intro }}
      </p>
    </div>

    <!-- Sections -->
    <div class="space-y-8">
      <section
        v-for="(sec, idx) in sections"
        :key="idx"
        class="p-6 sm:p-8 rounded-2xl bg-surface border border-line"
      >
        <div class="flex items-center gap-3.5 mb-4">
          <div class="w-10 h-10 rounded-xl bg-surface-2 border border-line flex items-center justify-center text-neon">
            <component :is="sec.icon" class="w-5 h-5" />
          </div>
          <h2 class="text-lg sm:text-xl font-bold text-txt">
            {{ sec.title }}
          </h2>
        </div>

        <div class="text-sm sm:text-base text-txt-2 leading-relaxed whitespace-pre-line space-y-3">
          {{ sec.content }}
        </div>
      </section>
    </div>
  </div>
</template>
