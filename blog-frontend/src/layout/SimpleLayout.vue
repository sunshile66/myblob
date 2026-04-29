<template>
  <div class="layout">
    <SimpleNavbar />
    <div class="container">
      <main class="main-content">
        <slot />
      </main>
    </div>
    <Footer />
    <AnnouncementModal />
  </div>
</template>

<script setup lang="ts">
import SimpleNavbar from "@/components/layout/SimpleNavbar.vue";
import Footer from "@/components/layout/Footer.vue";
import AnnouncementModal from "@/components/common/AnnouncementModal.vue";
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
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 20px;
}

.main-content {
  min-height: 400px;
}
</style>
