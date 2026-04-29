import { TOOL_CATALOG, findToolBySlug, type ToolItem } from '@/data/toolCatalog'

const RECENT_TOOLS_KEY = 'myblob_recent_tools'
const MAX_RECENT_TOOLS = 6

const isToolSlug = (slug: string) =>
  TOOL_CATALOG.some((tool) => tool.slug === slug)

export const saveRecentTool = (slug: string) => {
  if (!slug || !isToolSlug(slug)) {
    return
  }

  try {
    const existing = getRecentToolSlugs()
    const next = [slug, ...existing.filter((item) => item !== slug)].slice(
      0,
      MAX_RECENT_TOOLS,
    )
    window.localStorage.setItem(RECENT_TOOLS_KEY, JSON.stringify(next))
  } catch (error) {
    console.warn('Failed to save recent tool:', error)
  }
}

export const getRecentToolSlugs = (): string[] => {
  try {
    const raw = window.localStorage.getItem(RECENT_TOOLS_KEY)
    if (!raw) {
      return []
    }

    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return []
    }

    return parsed.filter(
      (item): item is string => typeof item === 'string' && isToolSlug(item),
    )
  } catch (error) {
    console.warn('Failed to read recent tools:', error)
    return []
  }
}

export const getRecentTools = (): ToolItem[] =>
  getRecentToolSlugs()
    .map((slug) => findToolBySlug(slug))
    .filter((tool): tool is ToolItem => Boolean(tool))
