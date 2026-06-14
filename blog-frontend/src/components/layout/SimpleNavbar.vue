<template>
  <div class="navbar">
    <div class="navbar-container">
      <div class="navbar-left">
        <NavLogo />
        <NavLinks />
        <NavSearch />
      </div>

      <div class="navbar-right">
        <ToolLauncher />
        <ThemeSwitcher />
        <UserMenu />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import ThemeSwitcher from "@/components/common/ThemeSwitcher.vue";
import NavLogo from "./navbar/NavLogo.vue";
import NavLinks from "./navbar/NavLinks.vue";
import NavSearch from "./navbar/NavSearch.vue";
import ToolLauncher from "./navbar/ToolLauncher.vue";
import UserMenu from "./navbar/UserMenu.vue";
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 28px;
  flex: 1;
  min-width: 0;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

@media (max-width: 960px) {
  .navbar-left {
    gap: 14px;
  }
}

@media (max-width: 768px) {
  .navbar-container {
    padding: 10px 12px;
  }
}
</style>
