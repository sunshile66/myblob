<template>
  <router-link to="/" class="logo">
    <span class="logo-badge">M</span>
    <div class="logo-copy">
      <span class="logo-text">MyBlob</span>
      <span class="logo-subtitle">创作与效率工作台</span>
    </div>
  </router-link>
</template>

<script setup lang="ts">
</script>

<style scoped>
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-badge {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: var(--gradient-primary);
  color: white;
  font-size: 18px;
  font-weight: 800;
  box-shadow: 0 12px 22px rgba(79, 70, 229, 0.22);
}

.logo-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  color: var(--theme-text);
  letter-spacing: -0.02em;
}

.logo-subtitle {
  font-size: 11px;
  color: var(--theme-text-secondary);
  letter-spacing: 0.02em;
}

@media (max-width: 768px) {
  .logo-text {
    font-size: 18px;
  }

  .logo-subtitle {
    display: none;
  }
}
</style>
