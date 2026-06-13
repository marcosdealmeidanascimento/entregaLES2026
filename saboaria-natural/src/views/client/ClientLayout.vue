<template>
  <div class="flex flex-col min-h-screen">
    <header class="bg-white border-b border-stone-200 sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-20">
          <div class="flex items-center">
            <router-link to="/" class="text-3xl font-serif font-bold text-oliva tracking-tight">
              Saboaria Natural
            </router-link>
          </div>
          
          <nav class="hidden md:flex space-x-8">
            <router-link to="/" class="text-sm font-medium hover:text-oliva transition-colors">Home</router-link>
            <router-link to="/produtos" class="text-sm font-medium hover:text-oliva transition-colors">Produtos</router-link>
            <router-link v-if="store.currentUser?.role === 'ADMIN'" to="/admin" class="text-sm font-medium text-stone-400 hover:text-oliva transition-colors">Painel ADM</router-link>
          </nav>

          <div class="flex items-center space-x-6">
            <div v-if="store.currentUser" class="flex items-center space-x-4">
              <router-link to="/pedidos" class="text-sm font-medium text-stone-600 hover:text-oliva">Meus Pedidos</router-link>
              <router-link to="/perfil" class="p-2 hover:bg-stone-100 rounded-full transition-colors">
                <UserIcon :size="20" />
              </router-link>
              <button @click="handleLogout" class="text-sm font-medium text-stone-400 hover:text-red-500">Sair</button>
            </div>
            <div v-else>
              <router-link to="/login" class="text-sm font-medium text-oliva hover:underline">Entrar</router-link>
            </div>
            <router-link to="/carrinho" class="p-2 hover:bg-stone-100 rounded-full transition-colors relative">
              <ShoppingCartIcon :size="20" />
              <span v-if="cartCount > 0" class="absolute top-0 right-0 bg-oliva text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">
                {{ cartCount }}
              </span>
            </router-link>
          </div>
        </div>
      </div>
    </header>

    <main class="flex-grow">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="bg-linho border-t border-areia py-12">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <p class="text-stone-500 text-sm font-serif italic">Feito com amor e ingredientes naturais.</p>
        <p class="text-stone-400 text-xs mt-2">© {{ new Date().getFullYear()}} Saboaria Natural. Todos os direitos reservados.</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { User as UserIcon, ShoppingCart as ShoppingCartIcon } from 'lucide-vue-next';
import { store } from '../../store';

const router = useRouter();
const cartCount = computed(() => store.cart.reduce((acc, item) => acc + item.quantity, 0));

const handleLogout = () => {
  store.currentUser = null;
  router.push('/login');
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
