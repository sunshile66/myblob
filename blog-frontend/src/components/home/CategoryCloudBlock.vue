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
  gap: 16px;
  height: 100%;
}

.category-cloud__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.category-cloud__kicker {
  font-size: 13px;
  font-weight: 800;
  color: var(--theme-text);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.category-cloud__more {
  font-size: 12px;
  font-weight: 600;
  color: var(--theme-primary);
  text-decoration: none;
}

.category-cloud__more:hover {
  text-decoration: underline;
}

.category-cloud__section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-cloud__subtitle {
  font-size: 11px;
  font-weight: 600;
  color: var(--theme-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.category-cloud__list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.category-cloud__chip {
  padding: 6px 14px;
  border: 1px solid var(--theme-border);
  border-radius: 999px;
  background: var(--theme-card);
  color: var(--theme-text);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.category-cloud__chip:hover {
  border-color: var(--theme-primary);
  background: var(--theme-primary-light);
  color: var(--theme-primary);
}

.category-cloud__chip--tag {
  background: var(--theme-hover);
  border-style: dashed;
}

.category-cloud__chip--empty {
  opacity: 0.4;
  cursor: default;
}
</style>
