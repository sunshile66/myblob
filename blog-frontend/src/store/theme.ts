import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export type ThemeType = 'light' | 'dark' | 'system' | 'pink' | 'blue' | 'purple' | 'cyan' | 'orange'

export interface ThemeColors {
  primary: string
  primaryLight: string
  secondary: string
  background: string
  card: string
  text: string
  textSecondary: string
  border: string
  hover: string
  glassBg: string
  glassBorder: string
  glassShadow: string
  gradientPrimary: string
}

export interface ThemeConfig {
  name: string
  description: string
  icon: string
  colors: ThemeColors
}

export const useThemeStore = defineStore('theme', () => {
  const currentTheme = ref<ThemeType>('system')
  const effectiveTheme = ref<ThemeType>('light')
  const brightness = ref(100)
  const contrast = ref(100)

  const themeConfigs: Record<ThemeType, ThemeConfig> = {
    light: {
      name: '浅色主题',
      description: '极简现代蓝，明亮清爽',
      icon: '☀️',
      colors: {
        primary: '#4F46E5',
        primaryLight: 'rgba(79, 70, 229, 0.10)',
        secondary: '#6366F1',
        background: '#FAFAFB',
        card: '#FFFFFF',
        text: '#0F172A',
        textSecondary: '#64748B',
        border: '#E5E7EB',
        hover: '#F1F5F9',
        glassBg: 'rgba(255, 255, 255, 0.72)',
        glassBorder: 'rgba(15, 23, 42, 0.06)',
        glassShadow: '0 8px 24px rgba(15, 23, 42, 0.06)',
        gradientPrimary: 'linear-gradient(135deg, #4F46E5 0%, #6366F1 100%)',
      },
    },
    dark: {
      name: '深色主题',
      description: '护眼深色模式，适合夜间使用',
      icon: '🌙',
      colors: {
        primary: '#818CF8',
        primaryLight: 'rgba(129, 140, 248, 0.16)',
        secondary: '#A5B4FC',
        background: '#0B0F19',
        card: '#111827',
        text: '#F1F5F9',
        textSecondary: '#94A3B8',
        border: '#1F2937',
        hover: '#1A2233',
        glassBg: 'rgba(17, 24, 39, 0.72)',
        glassBorder: 'rgba(255, 255, 255, 0.06)',
        glassShadow: '0 8px 24px rgba(0, 0, 0, 0.32)',
        gradientPrimary: 'linear-gradient(135deg, #818CF8 0%, #A5B4FC 100%)',
      },
    },
    system: {
      name: '跟随系统',
      description: '自动跟随操作系统主题',
      icon: '🖥️',
      colors: {
        primary: '#4F46E5',
        primaryLight: 'rgba(79, 70, 229, 0.10)',
        secondary: '#6366F1',
        background: '#FAFAFB',
        card: '#FFFFFF',
        text: '#0F172A',
        textSecondary: '#64748B',
        border: '#E5E7EB',
        hover: '#F1F5F9',
        glassBg: 'rgba(255, 255, 255, 0.72)',
        glassBorder: 'rgba(15, 23, 42, 0.06)',
        glassShadow: '0 8px 24px rgba(15, 23, 42, 0.06)',
        gradientPrimary: 'linear-gradient(135deg, #4F46E5 0%, #6366F1 100%)',
      },
    },
    pink: {
      name: '粉色主题',
      description: '温柔甜美的粉色调',
      icon: '🌸',
      colors: {
        primary: '#ec4899',
        primaryLight: 'rgba(236, 72, 153, 0.15)',
        secondary: '#f472b6',
        background: '#fdf2f8',
        card: '#ffffff',
        text: '#831843',
        textSecondary: '#be185d',
        border: '#fbcfe8',
        hover: '#fce7f3',
        glassBg: 'rgba(255, 255, 255, 0.75)',
        glassBorder: 'rgba(236, 72, 153, 0.2)',
        glassShadow: '0 8px 32px rgba(236, 72, 153, 0.15)',
        gradientPrimary: 'linear-gradient(135deg, #ec4899 0%, #f472b6 100%)',
      },
    },
    blue: {
      name: '蓝色主题',
      description: '清新冷静的蓝色调',
      icon: '💎',
      colors: {
        primary: '#2563eb',
        primaryLight: 'rgba(37, 99, 235, 0.15)',
        secondary: '#60a5fa',
        background: '#eff6ff',
        card: '#ffffff',
        text: '#1e3a8a',
        textSecondary: '#3b82f6',
        border: '#bfdbfe',
        hover: '#dbeafe',
        glassBg: 'rgba(255, 255, 255, 0.75)',
        glassBorder: 'rgba(37, 99, 235, 0.2)',
        glassShadow: '0 8px 32px rgba(37, 99, 235, 0.15)',
        gradientPrimary: 'linear-gradient(135deg, #2563eb 0%, #60a5fa 100%)',
      },
    },
    purple: {
      name: '紫色主题',
      description: '神秘优雅的紫色调',
      icon: '💜',
      colors: {
        primary: '#7c3aed',
        primaryLight: 'rgba(124, 58, 237, 0.15)',
        secondary: '#a78bfa',
        background: '#f5f3ff',
        card: '#ffffff',
        text: '#4c1d95',
        textSecondary: '#7c3aed',
        border: '#ddd6fe',
        hover: '#ede9fe',
        glassBg: 'rgba(255, 255, 255, 0.75)',
        glassBorder: 'rgba(124, 58, 237, 0.2)',
        glassShadow: '0 8px 32px rgba(124, 58, 237, 0.15)',
        gradientPrimary: 'linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%)',
      },
    },
    cyan: {
      name: '青色主题',
      description: '清新自然的青色调',
      icon: '🌊',
      colors: {
        primary: '#06b6d4',
        primaryLight: 'rgba(6, 182, 212, 0.15)',
        secondary: '#22d3ee',
        background: '#ecfeff',
        card: '#ffffff',
        text: '#0e7490',
        textSecondary: '#06b6d4',
        border: '#67e8f9',
        hover: '#cffafe',
        glassBg: 'rgba(255, 255, 255, 0.75)',
        glassBorder: 'rgba(6, 182, 212, 0.2)',
        glassShadow: '0 8px 32px rgba(6, 182, 212, 0.15)',
        gradientPrimary: 'linear-gradient(135deg, #06b6d4 0%, #22d3ee 100%)',
      },
    },
    orange: {
      name: '橙色主题',
      description: '温暖活力的橙色调',
      icon: '🍊',
      colors: {
        primary: '#f97316',
        primaryLight: 'rgba(249, 115, 22, 0.15)',
        secondary: '#fb923c',
        background: '#fff7ed',
        card: '#ffffff',
        text: '#c2410c',
        textSecondary: '#f97316',
        border: '#fed7aa',
        hover: '#ffedd5',
        glassBg: 'rgba(255, 255, 255, 0.75)',
        glassBorder: 'rgba(249, 115, 22, 0.2)',
        glassShadow: '0 8px 32px rgba(249, 115, 22, 0.15)',
        gradientPrimary: 'linear-gradient(135deg, #f97316 0%, #fb923c 100%)',
      },
    },
  }

  const currentThemeConfig = computed(() => {
    return themeConfigs[effectiveTheme.value]
  })

  const availableThemes = computed(() => {
    return Object.entries(themeConfigs).map(([key, config]) => ({
      id: key as ThemeType,
      ...config,
    }))
  })

  const isDark = computed(() => {
    return effectiveTheme.value === 'dark'
  })

  const detectSystemTheme = (): 'light' | 'dark' => {
    if (typeof window !== 'undefined' && window.matchMedia) {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    return 'light'
  }

  const resolveEffectiveTheme = (theme: ThemeType): ThemeType => {
    if (theme === 'system') {
      return detectSystemTheme()
    }
    return theme
  }

  const applyTheme = (theme: ThemeType) => {
    const resolvedTheme = resolveEffectiveTheme(theme)
    effectiveTheme.value = resolvedTheme
    const colors = themeConfigs[resolvedTheme].colors
    const root = document.documentElement

    root.style.setProperty('--theme-primary', colors.primary)
    root.style.setProperty('--theme-primary-light', colors.primaryLight)
    root.style.setProperty('--theme-secondary', colors.secondary)
    root.style.setProperty('--theme-background', colors.background)
    root.style.setProperty('--theme-card', colors.card)
    root.style.setProperty('--theme-text', colors.text)
    root.style.setProperty('--theme-text-secondary', colors.textSecondary)
    root.style.setProperty('--theme-border', colors.border)
    root.style.setProperty('--theme-hover', colors.hover)
    root.style.setProperty('--glass-bg', colors.glassBg)
    root.style.setProperty('--glass-border', colors.glassBorder)
    root.style.setProperty('--glass-shadow', colors.glassShadow)
    root.style.setProperty('--gradient-primary', colors.gradientPrimary)

    root.style.setProperty('--brightness', `${brightness.value}%`)
    root.style.setProperty('--contrast', `${contrast.value}%`)

    if (resolvedTheme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }

    const event = new CustomEvent('themeChange', { detail: { theme, resolvedTheme } })
    window.dispatchEvent(event)
  }

  const setTheme = (theme: ThemeType) => {
    currentTheme.value = theme
    applyTheme(theme)
    localStorage.setItem('theme', theme)
    localStorage.setItem('theme-brightness', brightness.value.toString())
    localStorage.setItem('theme-contrast', contrast.value.toString())
  }

  const setBrightness = (value: number) => {
    brightness.value = Math.max(50, Math.min(150, value))
    document.documentElement.style.setProperty('--brightness', `${brightness.value}%`)
    localStorage.setItem('theme-brightness', brightness.value.toString())
  }

  const setContrast = (value: number) => {
    contrast.value = Math.max(50, Math.min(150, value))
    document.documentElement.style.setProperty('--contrast', `${contrast.value}%`)
    localStorage.setItem('theme-contrast', contrast.value.toString())
  }

  const initTheme = () => {
    const savedTheme = localStorage.getItem('theme') as ThemeType
    const savedBrightness = localStorage.getItem('theme-brightness')
    const savedContrast = localStorage.getItem('theme-contrast')

    if (savedTheme && Object.keys(themeConfigs).includes(savedTheme)) {
      currentTheme.value = savedTheme
    }

    if (savedBrightness) {
      brightness.value = parseInt(savedBrightness, 10)
    }

    if (savedContrast) {
      contrast.value = parseInt(savedContrast, 10)
    }

    applyTheme(currentTheme.value)

    if (typeof window !== 'undefined' && window.matchMedia) {
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
        if (currentTheme.value === 'system') {
          applyTheme('system')
        }
      })
    }
  }

  watch(currentTheme, (newTheme) => {
    applyTheme(newTheme)
  })

  return {
    currentTheme,
    effectiveTheme,
    brightness,
    contrast,
    themeConfigs,
    currentThemeConfig,
    availableThemes,
    isDark,
    setTheme,
    setBrightness,
    setContrast,
    initTheme,
    detectSystemTheme,
    resolveEffectiveTheme,
  }
})
