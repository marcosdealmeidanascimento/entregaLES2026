<template>
  <div class="py-16 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
    <h1 class="text-4xl font-serif mb-12">Seu Carrinho</h1>

    <div v-if="store.cart.length === 0" class="text-center py-20 bg-stone-50 rounded-3xl border border-dashed border-stone-200">
      <ShoppingCartIcon :size="48" class="mx-auto text-stone-300 mb-4" />
      <p class="text-stone-500">Seu carrinho está vazio.</p>
      <router-link to="/produtos" class="mt-6 inline-block text-oliva font-medium underline underline-offset-4">Continuar comprando</router-link>
    </div>

    <div v-else>
      <div class="space-y-6">
        <div v-for="item in store.cart" :key="item.id" class="flex items-center bg-white p-4 rounded-2xl border border-stone-100">
          <img :src="item.produto.foto" class="w-20 h-20 rounded-xl object-cover" />
          <div class="ml-6 flex-grow">
            <h3 class="font-serif text-lg">{{ item.produto.nome }}</h3>
            <p class="text-stone-400 text-sm">R$ {{ item.valorVenda.toFixed(2) }}</p>
          </div>
          <div class="flex items-center space-x-4">
            <div class="flex items-center border border-stone-200 rounded-lg">
              <button @click="item.quantity > 1 ? item.quantity-- : null" class="px-3 py-1 hover:bg-stone-50">-</button>
              <span class="px-3 py-1 text-sm">{{ item.quantity }}</span>
              <button @click="increaseQuantity(item)" class="px-3 py-1 hover:bg-stone-50">+</button>
            </div>
            <button @click="removeFromCart(item.id)" class="text-stone-300 hover:text-red-500 transition-colors">
              <TrashIcon :size="18" />
            </button>
          </div>
        </div>
      </div>

      <div class="mt-12 bg-areia/10 p-8 rounded-3xl border border-areia">
        <div class="flex justify-between items-center mb-4">
          <span class="text-stone-500">Subtotal</span>
          <span class="font-medium">R$ {{ subtotal.toFixed(2) }}</span>
        </div>
        <div class="flex justify-between items-center mb-6">
          <span class="text-stone-500">Frete</span>
          <span class="text-green-600 font-medium">Grátis</span>
        </div>
        <div class="border-t border-stone-200 pt-6 flex justify-between items-center mb-8">
          <span class="text-xl font-serif">Total</span>
          <span class="text-2xl font-bold text-oliva">R$ {{ subtotal.toFixed(2) }}</span>
        </div>
        <router-link 
          to="/checkout" 
          class="block w-full bg-oliva text-white text-center py-4 rounded-xl font-medium hover:bg-terra transition-colors"
        >
          Finalizar Compra
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ShoppingCart as ShoppingCartIcon, Trash as TrashIcon } from 'lucide-vue-next';
import { store, removeFromCart } from '../../store';

const subtotal = computed(() => store.cart.reduce((acc, item) => acc + (item.valorVenda * item.quantity), 0));

const increaseQuantity = (item: any) => {
  const product = store.products.find(p => p.id === item.id);
  if (product && item.quantity < product.inventory) {
    item.quantity++;
  } else {
    alert('Quantidade máxima em estoque atingida.');
  }
};
</script>
