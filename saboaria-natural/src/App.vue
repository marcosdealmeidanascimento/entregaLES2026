<template>
  <div class="min-h-screen bg-linho text-grafite font-sans">
    <router-view />
    <ChatBot />
  </div>
</template>

<script setup lang="ts">
import ChatBot from './components/ChatBot.vue';
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,300;0,400;0,500;0,600;0,700;1,300;1,400;1,500;1,600;1,700&family=Inter:wght@100;200;300;400;500;600;700;800;900&display=swap');

:root {
  --font-serif: 'Cormorant Garamond', serif;
  --font-sans: 'Inter', sans-serif;
}

body {
  font-family: var(--font-sans);
}

h1, h2, h3, .serif {
  font-family: var(--font-serif);
}
</style>
