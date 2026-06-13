<template>
  <div class="py-16 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <header class="mb-12">
      <h1 class="text-5xl font-serif">Nossa Coleção</h1>
      <p class="text-stone-500 mt-2">Encontre o sabonete perfeito para o seu ritual de autocuidado.</p>
    </header>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
      <div v-for="stock in stocks" :key="stock.id" class="bg-white border border-stone-100 rounded-2xl overflow-hidden hover:shadow-xl transition-shadow group">
        <div class="aspect-square overflow-hidden relative">
          <img 
            :src="stock.produto.foto" 
            class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
            referrerPolicy="no-referrer"
          />
          <div v-if="stock.quantidade < 10" class="absolute top-4 left-4 bg-orange-100 text-orange-700 text-[10px] font-bold px-2 py-1 rounded uppercase tracking-wider">
            Poucas unidades
          </div>
        </div>
        <div class="p-6">
          <div class="flex items-center justify-between mb-2">
            <h3 class="text-lg font-serif font-medium">{{ stock.produto.nome }}</h3>
            <!-- <div v-if="getProductRating(stock.produto.id)" class="flex items-center space-x-1">
              <StarIcon :size="12" class="text-terra fill-terra" />
              <span class="text-[10px] font-bold text-stone-500">{{ getProductRating(product.id).toFixed(1) }}</span>
            </div> -->
          </div>
          <p class="text-stone-400 text-xs mt-1 line-clamp-2">{{ stock.produto.descricao }}</p>
          <div class="mt-4 flex items-center justify-between">
            <span class="text-oliva font-bold">R$ {{ stock.valorVenda.toFixed(2) }}</span>
            <button 
              @click="addToCart(stock)"
              class="bg-oliva text-white p-2 rounded-lg hover:bg-terra transition-colors"
            >
              <ShoppingCartIcon :size="18" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ShoppingCart as ShoppingCartIcon, Star as StarIcon } from 'lucide-vue-next';
import { store, addToCart } from '../../store';
import { onBeforeMount, ref } from 'vue';
import apiClient from '@/plugins/axios';

const getProductRating = (productId: number) => {
  const reviews = store.reviews.filter(r => r.productId === productId);
  if (reviews.length === 0) return null;
  return reviews.reduce((acc, r) => acc + r.rating, 0) / reviews.length;
};

const stocks = ref([]);

const getStocks = async () => {
  const response = await apiClient.get('/estoques');
  stocks.value = response.data;
}

onBeforeMount(() => {
  getStocks();
});

</script>
