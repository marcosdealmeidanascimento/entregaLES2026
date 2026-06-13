<template>
  <div class="max-w-7xl mx-auto px-6 py-12">
    <header class="mb-12">
      <h1 class="text-4xl font-serif font-bold text-grafite">Meus Pedidos</h1>
      <p class="text-stone-500">Acompanhe o histórico e status das suas compras.</p>
    </header>

    <!-- Tabs -->
    <div class="flex space-x-8 border-b border-areia mb-12">
      <button @click="activeTab = 'orders'" class="pb-4 text-sm font-medium transition-all border-b-2"
        :class="activeTab === 'orders' ? 'border-oliva text-oliva' : 'border-transparent text-stone-400'">
        Histórico de Pedidos
      </button>
      <button @click="activeTab = 'products'" class="pb-4 text-sm font-medium transition-all border-b-2"
        :class="activeTab === 'products' ? 'border-oliva text-oliva' : 'border-transparent text-stone-400'">
        Produtos Comprados
      </button>
    </div>

    <!-- Orders History -->
    <div v-if="activeTab === 'orders'">
      <div v-if="orders.length === 0" class="text-center py-20 bg-white rounded-[40px] border border-areia">
        <ShoppingBagIcon :size="48" class="mx-auto text-stone-200 mb-4" />
        <p class="text-stone-500">Você ainda não realizou nenhum pedido.</p>
        <router-link to="/produtos" class="mt-6 inline-block text-oliva font-bold hover:underline">Ir para a Loja</router-link>
      </div>

      <div v-else class="space-y-6">
        <div v-for="order in orders" :key="order.id" class="bg-white rounded-[40px] border border-areia shadow-sm overflow-hidden">
          <div class="p-8 border-b border-areia flex flex-wrap justify-between items-center gap-4">
            <div class="flex space-x-8">
              <div>
                <p class="text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-1">Pedido</p>
                <p class="font-mono text-sm font-bold text-grafite">#{{ order.id }}</p>
              </div>
              <div>
                <p class="text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-1">Data</p>
                <p class="text-sm text-stone-600">{{ order.dataVenda }}</p>
              </div>
              <div>
                <p class="text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-1">Total</p>
                <p class="text-sm font-bold text-oliva">R$ {{ order.valorTotal.toFixed(2) }}</p>
              </div>
            </div>
            <div class="flex items-center space-x-3 flex-wrap gap-2">
              <span class="px-4 py-1.5 rounded-full text-[10px] font-bold uppercase tracking-widest"
                :class="statusClasses[order.status]">{{ order.status }}</span>
              <button v-if="order.status === 'ENTREGUE'" @click="requestExchange(order.id)"
                class="text-xs border border-terra text-terra px-4 py-1.5 rounded-full hover:bg-terra hover:text-white transition-all">
                Solicitar Troca
              </button>
              <button v-if="order.status === 'ENTREGUE'" @click="openDevolucaoModal(order)"
                class="text-xs border border-oliva text-oliva px-4 py-1.5 rounded-full hover:bg-oliva hover:text-white transition-all">
                Devolver Item
              </button>
            </div>
          </div>

          <div class="p-8 bg-linho/30">
            <div class="space-y-4">
              <div v-for="item in order.itens" :key="item.id" class="flex justify-between items-center">
                <div class="flex items-center space-x-4">
                  <div class="w-12 h-12 bg-white rounded-xl border border-areia overflow-hidden">
                    <img :src="item.produto.foto || 'https://picsum.photos/seed/soap/100/100'" class="w-full h-full object-cover" />
                  </div>
                  <div>
                    <p class="text-sm font-bold text-grafite">{{ item.produto.nome }}</p>
                    <p class="text-xs text-stone-500">{{ item.quantidade }}x R$ {{ item.valorUnitario.toFixed(2) }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Devoluções deste pedido -->
            <div v-if="devolucoesByPedido[order.id]?.length" class="mt-6 border-t border-areia/50 pt-4">
              <p class="text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-3">Devoluções</p>
              <div v-for="dev in devolucoesByPedido[order.id]" :key="dev.id"
                class="flex justify-between items-center text-xs py-2 border-b border-areia/30 last:border-0">
                <span class="text-stone-600">Devolução #{{ dev.id }} — {{ dev.itens.length }} item(s)</span>
                <span class="px-3 py-1 rounded-full font-bold uppercase tracking-widest text-[10px]"
                  :class="devStatusClasses[dev.status]">{{ dev.status }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Purchased Products & Reviews -->
    <div v-else-if="activeTab === 'products'">
      <div v-if="entregues.length === 0" class="text-center py-20 bg-white rounded-[40px] border border-areia">
        <PackageIcon :size="48" class="mx-auto text-stone-200 mb-4" />
        <p class="text-stone-500">Nenhum produto entregue ainda.</p>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div v-for="order in entregues" :key="order.id">
          <div v-for="item in order.itens" :key="item.id">
            <div class="bg-white p-8 rounded-[40px] border border-areia shadow-sm flex items-center space-x-6">
              <img :src="item.produto.foto" class="w-24 h-24 rounded-3xl object-cover border border-areia" />
              <div class="flex-grow">
                <h3 class="font-serif font-bold text-xl text-grafite">{{ item.produto.nome }}</h3>
                <div v-if="getReview(item.produto.id)" class="mt-4">
                  <div class="flex items-center space-x-1 mb-2">
                    <StarIcon v-for="i in 5" :key="i" :size="14"
                      :class="i <= getReview(item.produto.id).rating ? 'text-terra fill-terra' : 'text-stone-200'" />
                  </div>
                  <p class="text-xs text-stone-500 italic">"{{ getReview(item.produto.id).comment }}"</p>
                </div>
                <button v-else @click="openReviewModal(item.produto)"
                  class="mt-4 text-xs bg-oliva text-white px-4 py-2 rounded-xl hover:bg-terra transition-colors">
                  Avaliar Produto
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Devolução -->
    <div v-if="showDevolucaoModal" class="fixed inset-0 z-[100] flex items-center justify-center px-4">
      <div class="absolute inset-0 bg-grafite/40 backdrop-blur-sm" @click="showDevolucaoModal = false"></div>
      <div class="relative bg-white w-full max-w-lg p-10 rounded-[40px] shadow-2xl border border-areia">
        <h2 class="text-3xl font-serif font-bold mb-2">Solicitar Devolução</h2>
        <p class="text-stone-500 text-sm mb-6">Selecione os itens que deseja devolver e a quantidade.</p>

        <div class="space-y-4 mb-8">
          <div v-for="item in devolucaoOrder?.itens" :key="item.id"
            class="border border-areia rounded-2xl p-4 flex items-center gap-4">
            <input type="checkbox" :value="item.id" v-model="selectedItems"
              class="w-4 h-4 accent-oliva rounded" />
            <img :src="item.produto.foto || 'https://picsum.photos/seed/soap/100/100'"
              class="w-12 h-12 rounded-xl object-cover border border-areia" />
            <div class="flex-1">
              <p class="text-sm font-bold text-grafite">{{ item.produto.nome }}</p>
              <p class="text-xs text-stone-500">Comprado: {{ item.quantidade }}x</p>
            </div>
            <input v-if="selectedItems.includes(item.id)"
              v-model.number="devQuantidades[item.id]"
              type="number" min="1" :max="item.quantidade"
              class="w-16 border border-areia rounded-xl px-3 py-1 text-sm text-center focus:outline-none focus:border-oliva" />
          </div>
        </div>

        <div class="flex gap-3">
          <button @click="submitDevolucao" :disabled="selectedItems.length === 0 || submittingDev"
            class="flex-1 bg-oliva text-white py-4 rounded-2xl font-bold hover:bg-terra transition-all shadow-lg shadow-oliva/20 disabled:opacity-50">
            {{ submittingDev ? 'Enviando...' : 'Solicitar Devolução' }}
          </button>
          <button @click="showDevolucaoModal = false"
            class="px-6 py-4 border border-areia rounded-2xl text-sm text-stone-500 hover:border-oliva/50">
            Cancelar
          </button>
        </div>
      </div>
    </div>

    <!-- Modal: Avaliação -->
    <div v-if="showReviewModal" class="fixed inset-0 z-[100] flex items-center justify-center px-4">
      <div class="absolute inset-0 bg-grafite/40 backdrop-blur-sm" @click="showReviewModal = false"></div>
      <div class="relative bg-white w-full max-w-lg p-10 rounded-[40px] shadow-2xl border border-areia">
        <h2 class="text-3xl font-serif font-bold mb-2">Avaliar Produto</h2>
        <p class="text-stone-500 text-sm mb-8">{{ selectedProduct?.nome }}</p>
        <div class="space-y-6">
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Sua Nota</label>
            <div class="flex space-x-2">
              <button v-for="i in 5" :key="i" @click="newReview.rating = i" class="p-1 transition-all">
                <StarIcon :size="24" :class="i <= newReview.rating ? 'text-terra fill-terra' : 'text-stone-200 hover:text-terra/50'" />
              </button>
            </div>
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Seu Comentário</label>
            <textarea v-model="newReview.comment"
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva h-32"
              placeholder="O que você achou deste sabonete?"></textarea>
          </div>
          <button @click="submitReview"
            class="w-full bg-oliva text-white py-4 rounded-2xl font-bold hover:bg-terra transition-all shadow-lg shadow-oliva/20">
            Enviar Avaliação
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onBeforeMount } from 'vue';
import { ShoppingBag as ShoppingBagIcon, Package as PackageIcon, Star as StarIcon } from 'lucide-vue-next';
import { store } from '../../store';
import apiClient from '@/plugins/axios';

const orders = ref<any[]>([]);
const activeTab = ref('orders');
const showReviewModal = ref(false);
const selectedProduct = ref<any>(null);
const newReview = reactive({ rating: 5, comment: '' });

// Devolução
const showDevolucaoModal = ref(false);
const devolucaoOrder = ref<any>(null);
const selectedItems = ref<number[]>([]);
const devQuantidades = reactive<Record<number, number>>({});
const submittingDev = ref(false);
const devolucoesByPedido = ref<Record<number, any[]>>({});

const entregues = computed(() => orders.value.filter(o => o.status === 'ENTREGUE'));

const statusClasses: Record<string, string> = {
  'EM_PROCESSAMENTO': 'bg-blue-100 text-blue-600',
  'APROVADA': 'bg-green-100 text-green-600',
  'REPROVADA': 'bg-red-100 text-red-600',
  'EM_TRANSPORTE': 'bg-yellow-100 text-yellow-600',
  'ENTREGUE': 'bg-stone-100 text-stone-600',
  'EM_TROCA': 'bg-orange-100 text-orange-600',
  'TROCA_AUTORIZADA': 'bg-purple-100 text-purple-600',
  'TROCADO': 'bg-emerald-100 text-emerald-600'
};

const devStatusClasses: Record<string, string> = {
  'SOLICITADA': 'bg-yellow-100 text-yellow-700',
  'AUTORIZADA': 'bg-blue-100 text-blue-700',
  'CONCLUIDA': 'bg-emerald-100 text-emerald-700'
};

const requestExchange = async (orderId: number) => {
  await apiClient.put(`/vendas/${orderId}/solicitar-troca`);
  const response = await apiClient.get(`/vendas/${orderId}`);
  const idx = orders.value.findIndex(o => o.id === orderId);
  if (idx !== -1) orders.value[idx] = response.data;
  alert('Solicitação de troca enviada! Aguarde a autorização do administrador.');
};

const openDevolucaoModal = (order: any) => {
  devolucaoOrder.value = order;
  selectedItems.value = [];
  order.itens.forEach((item: any) => {
    devQuantidades[item.id] = 1;
  });
  showDevolucaoModal.value = true;
};

const submitDevolucao = async () => {
  if (!devolucaoOrder.value || selectedItems.value.length === 0) return;
  submittingDev.value = true;
  try {
    const payload = {
      venda: { id: devolucaoOrder.value.id },
      itens: selectedItems.value.map(itemId => ({
        itemVenda: { id: itemId },
        quantidade: devQuantidades[itemId] ?? 1
      }))
    };
    const res = await apiClient.post('/devolucoes', payload);
    const devId = res.data.id;

    if (!devolucoesByPedido.value[devolucaoOrder.value.id]) {
      devolucoesByPedido.value[devolucaoOrder.value.id] = [];
    }
    devolucoesByPedido.value[devolucaoOrder.value.id].push(res.data);

    showDevolucaoModal.value = false;
    alert('Devolução solicitada! Aguarde a autorização do administrador.');
  } catch {
    alert('Erro ao solicitar devolução. Tente novamente.');
  } finally {
    submittingDev.value = false;
  }
};

const openReviewModal = (product: any) => {
  selectedProduct.value = product;
  newReview.rating = 5;
  newReview.comment = '';
  showReviewModal.value = true;
};

const submitReview = () => {
  if (!newReview.comment) { alert('Por favor, escreva um comentário.'); return; }
  store.reviews.push({
    productId: selectedProduct.value.id,
    rating: newReview.rating,
    comment: newReview.comment,
    date: new Date().toISOString().split('T')[0]
  });
  showReviewModal.value = false;
  alert('Obrigado pela sua avaliação!');
};

const getReview = (productId: number) => store.reviews.find(r => r.productId === productId);

const getOrders = async () => {
  const response = await apiClient.get(`/vendas/cliente/${store.currentUser.cliente.id}`);
  orders.value = response.data;

  // Carrega devoluções de cada pedido entregue
  for (const order of orders.value) {
    if (order.status === 'ENTREGUE') {
      try {
        const devRes = await apiClient.get(`/devolucoes/venda/${order.id}`);
        if (devRes.data?.length) {
          devolucoesByPedido.value[order.id] = devRes.data;
        }
      } catch { /* sem devoluções */ }
    }
  }
};

onBeforeMount(() => { getOrders(); });
</script>
