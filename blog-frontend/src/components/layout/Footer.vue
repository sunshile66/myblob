<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-brand">
        <span class="footer-badge">MyBlob</span>
        <p>把内容创作、知识沉淀和常用开发工具收拢到一个更轻的工作台里。</p>
      </div>

      <div class="footer-links">
        <router-link to="/">首页</router-link>
        <router-link to="/tools">工具中心</router-link>
        <router-link to="/privacy-policy">隐私政策</router-link>
        <router-link to="/user-agreement">用户协议</router-link>
      </div>

      <p class="footer-copy">
        © {{ currentYear }} MyBlob. Focus on making useful things easy.
      </p>
    </div>
  </footer>
</template>

<script setup lang="ts">
const currentYear = new Date().getFullYear();
</script>

<style scoped>
.footer {
  margin-top: 64px;
  padding: 48px 0 40px;
  border-top: 1px solid var(--theme-border);
  background: var(--theme-background);
}

.footer-content {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  gap: 24px;
}

.footer-brand {
  display: grid;
  gap: 12px;
}

.footer-badge {
  display: inline-flex;
  width: fit-content;
  padding: 8px 14px;
  border-radius: 10px;
  background: var(--theme-primary-light);
  color: var(--theme-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.footer-badge:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
}

.footer-brand p,
.footer-copy {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--theme-text-secondary);
  max-width: 400px;
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 24px;
}

.footer-links a {
  color: var(--theme-text);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color var(--transition-fast), transform var(--transition-fast);
  display: inline-block;
}

.footer-links a:hover {
  color: var(--theme-primary);
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .footer {
    padding: 36px 0 28px;
  }
  .footer-content {
    padding: 0 16px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .footer-badge,
  .footer-links a {
    transition: none;
  }
  .footer-badge:hover,
  .footer-links a:hover {
    transform: none;
  }
}
</style>
