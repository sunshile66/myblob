import request from '@/utils/request'
import type { PaginatedResponse } from '@/types'

// ==================== 英语词汇 ====================

export interface VocabularyMeaning {
  pos: string
  def: string
  examples: { en: string; cn: string }[]
}

export interface VocabularyPhrase {
  phrase: string
  translation: string
}

export interface VocabularyItem {
  id: number
  word: string
  phonetic: string
  ukPhonetic: string | null
  partOfSpeech: string
  definition: string
  exampleSentence: string
  exampleTranslation: string
  difficulty: number
  category: string
  audioUrl: string | null
  meanings: VocabularyMeaning[]
  phrases: VocabularyPhrase[]
  createdAt: string
}

export const getVocabularyList = (params?: {
  page?: number; size?: number; category?: string; difficulty?: number; search?: string
}) => request.get<PaginatedResponse<VocabularyItem>>('/knowledge/vocabulary', { params })

export const getRandomVocabulary = (count = 10) =>
  request.get<VocabularyItem[]>(`/knowledge/vocabulary/random?count=${count}`)

export const getVocabularyDetail = (id: number) =>
  request.get<VocabularyItem>(`/knowledge/vocabulary/${id}`)

export const getVocabularyCategories = () =>
  request.get<string[]>('/knowledge/vocabulary/categories')

// ==================== 知识卡片 ====================

export interface KnowledgeItem {
  id: number
  title: string
  content: string
  category: string
  tags: string
  source: string
  difficulty: number
  viewCount: number
  summary: string
  createdAt: string
  updatedAt: string
}

export interface KnowledgeCategory {
  key: string
  name: string
  icon: string
}

export const getKnowledgeItems = (params?: {
  page?: number; size?: number; category?: string; search?: string
}) => request.get<PaginatedResponse<KnowledgeItem>>('/knowledge/items', { params })

export const getKnowledgeDetail = (id: number) =>
  request.get<KnowledgeItem>(`/knowledge/items/${id}`)

export const getKnowledgeCategories = () =>
  request.get<KnowledgeCategory[]>('/knowledge/items/categories')
