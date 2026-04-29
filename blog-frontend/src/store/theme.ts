import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ThemeType = 'light' | 'dark' | 'pink' | 'blue' | 'purple'

export const useThemeStore = defineStore('theme', () => {
  const currentTheme = ref<ThemeType>('light')

  const themeColors = {
    light: {
      primary: '#ff2442',
      primaryLight: 'rgba(255, 36, 66, 0.12)',
      secondary: '#7c3aed',
      background: '#fafafa',
      card: '#ffffff',
      text: '#1a1a1a',
      textSecondary: '#6b7280',
      border: '#e5e7eb',
      hover: '#f3f4f6',
      glassBg: 'rgba(255, 255, 255, 0.7)',
      glassBorder: 'rgba(255, 255, 255, 0.3)',
      glassShadow: '0 8px 32px rgba(0, 0, 0, 0.1)',
      gradientPrimary: 'linear-gradient(135deg, #ff2442 0%, #ff6b9d 100%)',
    },
    dark: {
      primary: '#ff4d6f',
      primaryLight: 'rgba(255, 77, 111, 0.18)',
      secondary: '#a78bfa',
      background: '#0f0f0f',
      card: '#1e1e1e',
      text: '#f5f5f5',
      textSecondary: '#a0a0a0',
      border: '#333333',
      hover: '#2a2a2a',
      glassBg: 'rgba(30, 30, 30, 0.7)',
      glassBorder: 'rgba(255, 255, 255, 0.1)',
      glassShadow: '0 8px 32px rgba(0, 0, 0, 0.3)',
      gradientPrimary: 'linear-gradient(135deg, #ff4d6f 0%, #ff8fab 100%)',
    },
    pink: {
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
    blue: {
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
    purple: {
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
  }

  const setTheme = (theme: ThemeType) => {
    currentTheme.value = theme
    applyTheme(theme)
    localStorage.setItem('theme', theme)
  }

  const applyTheme = (theme: ThemeType) => {
    const colors = themeColors[theme]
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

    if (theme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  const initTheme = () => {
    const savedTheme = localStorage.getItem('theme') as ThemeType
    if (savedTheme && Object.keys(themeColors).includes(savedTheme)) {
      currentTheme.value = savedTheme
    }
    applyTheme(currentTheme.value)
  }

  return {
    currentTheme,
    themeColors,
    setTheme,
    initTheme,
  }
})
