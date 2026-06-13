<template>
  <div class="flex min-h-screen bg-stone-50">
    <!-- Sidebar -->
    <aside class="w-64 bg-grafite text-white flex flex-col shadow-2xl">
      <div class="p-6">
        <h1 class="text-xl font-serif font-bold text-white tracking-wide">ADM Panel</h1>
        <p class="text-[10px] uppercase tracking-widest text-stone-500 mt-1">Saboaria Natural</p>
      </div>
      
      <nav class="flex-grow mt-6">
        <router-link 
          to="/admin/analise" 
          class="flex items-center px-6 py-4 text-sm font-medium transition-colors border-l-4"
          :class="$route.name === 'admin-analysis' ? 'bg-white/5 border-oliva text-white' : 'border-transparent text-stone-400 hover:text-white hover:bg-white/5'"
        >
          <LayoutDashboardIcon :size="18" class="mr-3" />
          Dashboard
        </router-link>
        <router-link 
          to="/admin/vendas" 
          class="flex items-center px-6 py-4 text-sm font-medium transition-colors border-l-4"
          :class="$route.name === 'admin-orders' ? 'bg-white/5 border-oliva text-white' : 'border-transparent text-stone-400 hover:text-white hover:bg-white/5'"
        >
          <ShoppingBagIcon :size="18" class="mr-3" />
          Vendas
        </router-link>
        <router-link 
          to="/admin/clientes" 
          class="flex items-center px-6 py-4 text-sm font-medium transition-colors border-l-4"
          :class="$route.name === 'admin-customers' ? 'bg-white/5 border-oliva text-white' : 'border-transparent text-stone-400 hover:text-white hover:bg-white/5'"
        >
          <UsersIcon :size="18" class="mr-3" />
          Clientes
        </router-link>
        <router-link 
          to="/admin/estoque" 
          class="flex items-center px-6 py-4 text-sm font-medium transition-colors border-l-4"
          :class="$route.name === 'admin-inventory' ? 'bg-white/5 border-oliva text-white' : 'border-transparent text-stone-400 hover:text-white hover:bg-white/5'"
        >
          <PackageIcon :size="18" class="mr-3" />
          Produtos
        </router-link>
        <router-link 
          to="/admin/entrada-estoque" 
          class="flex items-center px-6 py-4 text-sm font-medium transition-colors border-l-4"
          :class="$route.name === 'admin-stock-entry' ? 'bg-white/5 border-oliva text-white' : 'border-transparent text-stone-400 hover:text-white hover:bg-white/5'"
        >
          <PlusCircleIcon :size="18" class="mr-3" />
          Entrada Estoque
        </router-link>
        <router-link 
          to="/admin/analise" 
          class="flex items-center px-6 py-4 text-sm font-medium transition-colors border-l-4"
          :class="$route.name === 'admin-analysis' ? 'bg-white/5 border-oliva text-white' : 'border-transparent text-stone-400 hover:text-white hover:bg-white/5'"
        >
          <BarChartIcon :size="18" class="mr-3" />
          Análise
        </router-link>
      </nav>

      <div class="p-6 border-t border-white/10">
        <div v-if="store.currentUser" class="mb-4 px-2">
          <p class="text-xs text-stone-400">Logado como:</p>
          <p class="text-sm font-medium text-white truncate">{{ store.currentUser.name }}</p>
          <button @click="handleLogout" class="mt-2 text-[10px] uppercase tracking-widest text-red-400 hover:text-red-300 font-bold">Sair do Painel</button>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-grow p-8 overflow-y-auto">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { store } from '../../store';
import { 
  LayoutDashboard as LayoutDashboardIcon, 
  Package as PackageIcon, 
  ArrowLeft as ArrowLeftIcon, 
  Users as UsersIcon,
  ShoppingBag as ShoppingBagIcon,
  PlusCircle as PlusCircleIcon,
  BarChart3 as BarChartIcon
} from 'lucide-vue-next';

const router = useRouter();

const handleLogout = () => {
  store.currentUser = null;
  router.push('/login');
};
</script>
