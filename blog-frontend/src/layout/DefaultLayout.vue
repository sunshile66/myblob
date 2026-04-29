<template>
  <div class="layout">
    <Navbar />
    <div class="container">
      <main class="main-content">
        <slot />
      </main>
    </div>
    <Footer />
  </div>
</template>

<script setup lang="ts">
import Navbar from "@/components/layout/Navbar.vue";
import Footer from "@/components/layout/Footer.vue";
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.container {
  flex: 1;
  width: 100%;
  margin: 0 auto;
}

.main-content {
  min-height: 400px;
}
</style>
