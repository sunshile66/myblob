<template>
  <BentoCard variant="border" :col-span="1" :row-span="2">
    <div class="category-cloud">
      <div class="category-cloud__head">
        <span class="category-cloud__kicker">发现</span>
        <router-link v-if="moreLink" :to="moreLink" class="category-cloud__more">全部</router-link>
      </div>

      <div class="category-cloud__section">
        <span class="category-cloud__subtitle">分类</span>
        <div class="category-cloud__list">
          <button
            v-for="cat in categories"
            :key="cat.id"
            class="category-cloud__chip"
            @click="$emit('select-category', cat.slug)"
          >
            {{ cat.name }}
          </button>
          <button
            v-if="categories.length === 0"
            class="category-cloud__chip category-cloud__chip--empty"
            disabled
          >
            暂无分类
          </button>
        </div>
      </div>

      <div class="category-cloud__section">
        <span class="category-cloud__subtitle">热门标签</span>
        <div class="category-cloud__list">
          <button
            v-for="tag in tags"
            :key="tag.id"
            class="category-cloud__chip category-cloud__chip--tag"
            @click="$emit('select-tag', tag.slug)"
          >
            # {{ tag.name }}
          </button>
          <button
            v-if="tags.length === 0"
            class="category-cloud__chip category-cloud__chip--empty"
            disabled
          >
            暂无标签
          </button>
        </div>
      </div>
    </div>
  </BentoCard>
</template>

<script setup lang="ts">
import BentoCard from '@/components/common/BentoCard.vue'
import type { Category, Tag } from '@/types'

defineProps<{
  categories: Category[]
  tags: Tag[]
  moreLink?: string
}>()

defineEmits<{
  'select-category': [slug: string]
  'select-tag': [slug: string]
}>()
</script>

<style scoped>
.category-cloud {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

.category-cloud__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.category-cloud__kicker {
  font-size: 13px;
  font-weight: 700;
  color: var(--theme-text);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.category-cloud__more {
  font-size: 12px;
  font-weight: 600;
  color: var(--theme-primary);
  text-decoration: none;
  transition: opacity 0.2s ease;
}

.category-cloud__more:hover {
  opacity: 0.8;
}

.category-cloud__section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.category-cloud__subtitle {
  font-size: 11px;
  font-weight: 600;
  color: var(--theme-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.category-cloud__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-cloud__chip {
  padding: 7px 16px;
  border: 1px solid var(--theme-border);
  border-radius: 10px;
  background: var(--theme-card);
  color: var(--theme-text);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  font-family: inherit;
  letter-spacing: -0.01em;
}

.category-cloud__chip:hover {
  border-color: var(--theme-primary);
  background: var(--theme-primary-light);
  color: var(--theme-primary);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.15);
}

.category-cloud__chip--tag {
  background: var(--theme-hover);
  border-style: dashed;
  border-color: var(--theme-border-strong);
}

.category-cloud__chip--tag:hover {
  border-style: solid;
}

.category-cloud__chip--empty {
  opacity: 0.4;
  cursor: default;
  pointer-events: none;
}

@media (prefers-reduced-motion: reduce) {
  .category-cloud__chip,
  .category-cloud__more {
    transition: none;
  }
  .category-cloud__chip:hover {
    transform: none;
  }
}
</style>
