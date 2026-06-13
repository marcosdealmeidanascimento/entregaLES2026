<template>
  <div>
    <!-- Hero Section -->
    <section class="relative h-[70vh] flex items-center justify-center overflow-hidden bg-areia/20">
      <div class="absolute inset-0 z-0">
        <img 
          src="https://picsum.photos/seed/soap-hero/1920/1080?blur=2" 
          class="w-full h-full object-cover opacity-40"
          referrerPolicy="no-referrer"
        />
      </div>
      <div class="relative z-10 text-center px-4 max-w-3xl">
        <div v-if="store.currentUser" class="mb-6 inline-block bg-white/50 backdrop-blur px-6 py-2 rounded-full border border-white/20">
          <p class="text-xs font-bold text-oliva uppercase tracking-widest">Bem-vinda de volta, {{ store.currentUser.name }}</p>
        </div>
        <h1 class="text-6xl md:text-8xl font-serif font-light text-grafite leading-tight">
          Pureza em cada <br/>
          <span class="italic font-normal">detalhe.</span>
        </h1>
        <p class="mt-6 text-lg text-stone-600 font-light tracking-wide">
          Sabonetes artesanais feitos com óleos essenciais e ingredientes 100% naturais.
        </p>
        <div class="mt-10">
          <router-link 
            to="/produtos" 
            class="inline-block bg-oliva text-white px-10 py-4 rounded-full text-sm font-medium hover:bg-terra transition-all transform hover:scale-105"
          >
            Explorar Coleção
          </router-link>
        </div>
      </div>
    </section>

    <!-- Featured Products -->
    <section class="py-24 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-end mb-12">
        <div>
          <h2 class="text-4xl font-serif">Nossos Favoritos</h2>
          <p class="text-stone-500 mt-2">Os mais amados da nossa comunidade.</p>
        </div>
        <router-link to="/produtos" class="text-sm font-medium text-oliva underline underline-offset-8">Ver todos</router-link>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-10">
        <div v-for="stock in stocks" :key="stock.id" class="group">
          <div class="aspect-square overflow-hidden rounded-3xl bg-stone-100 mb-6 relative">
            <img 
              :src="stock.produto.foto" 
              class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
              referrerPolicy="no-referrer"
            />
            <button 
              @click="addToCart(stock)"
              class="absolute bottom-4 right-4 bg-white/90 backdrop-blur p-4 rounded-full shadow-lg opacity-0 group-hover:opacity-100 transition-all transform translate-y-4 group-hover:translate-y-0"
            >
              <PlusIcon :size="20" class="text-oliva" />
            </button>
          </div>
          <h3 class="text-xl font-serif">{{ stock.produto.nome }}</h3>
          <p class="text-stone-400 text-sm mt-1">{{ stock.produto.descricao }}</p>
          <p class="text-oliva font-medium mt-2">R$ {{ stock.valorVenda.toFixed(2) }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { Plus as PlusIcon } from 'lucide-vue-next';
import { store, addToCart } from '../../store';
import apiClient from '@/plugins/axios';
import { ref, onBeforeMount } from 'vue';

const stocks = ref([]);

const getStocks = async () => {
  const response = await apiClient.get('/estoques?limit=3');
  stocks.value = response.data;
}

onBeforeMount(() => {
  getStocks();
});

</script>
