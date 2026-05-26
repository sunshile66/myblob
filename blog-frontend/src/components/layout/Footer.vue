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
  margin-top: 60px;
  padding: 42px 0 34px;
  border-top: 1px solid var(--theme-border);
  background: var(--theme-background);
}

.footer-content {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  gap: 18px;
}

.footer-brand {
  display: grid;
  gap: 10px;
}

.footer-badge {
  display: inline-flex;
  width: fit-content;
  padding: 8px 12px;
  border-radius: 999px;
  background: var(--theme-primary-light);
  color: var(--theme-primary);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.footer-brand p,
.footer-copy {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--theme-text-secondary);
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  gap: 14px 20px;
}

.footer-links a {
  color: var(--theme-text);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--theme-primary);
}

@media (max-width: 768px) {
  .footer {
    padding: 32px 0 26px;
  }
}
</style>
