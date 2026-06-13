<template>
  <div class="space-y-8">
    <header>
      <h1 class="text-3xl font-serif font-bold text-grafite">Gerenciamento de Vendas</h1>
      <p class="text-stone-500">Acompanhe o status dos pedidos e gerencie trocas.</p>
    </header>

    <!-- Top-level tabs: Vendas / Devoluções -->
    <div class="flex space-x-8 border-b border-areia mb-2">
      <button @click="mainTab = 'vendas'" class="pb-4 text-sm font-medium transition-all border-b-2"
        :class="mainTab === 'vendas' ? 'border-oliva text-oliva' : 'border-transparent text-stone-400'">
        Vendas
      </button>
      <button @click="mainTab = 'devolucoes'; loadDevolucoes()" class="pb-4 text-sm font-medium transition-all border-b-2"
        :class="mainTab === 'devolucoes' ? 'border-oliva text-oliva' : 'border-transparent text-stone-400'">
        Devoluções
      </button>
    </div>

    <!-- Tabs for filtering by status (only for vendas) -->
    <div v-if="mainTab === 'vendas'" class="flex space-x-4 border-b border-areia overflow-x-auto">
      <button
        v-for="status in statuses"
        :key="status"
        @click="activeStatus = status"
        class="px-4 py-3 text-sm font-medium transition-all border-b-2 whitespace-nowrap"
        :class="activeStatus === status ? 'border-oliva text-oliva' : 'border-transparent text-stone-400'"
      >
        {{ statusLabel(status) }}
      </button>
    </div>

    <!-- Devoluções Table -->
    <div v-if="mainTab === 'devolucoes'" class="bg-white rounded-[40px] border border-areia shadow-sm overflow-hidden">
      <table class="w-full text-left">
        <thead>
          <tr class="bg-linho border-b border-areia">
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">ID</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Pedido</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Data</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Itens</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Status</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold text-right">Ações</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-areia/30">
          <tr v-if="devolucoes.length === 0">
            <td colspan="6" class="px-8 py-12 text-center text-stone-400 text-sm">Nenhuma devolução encontrada.</td>
          </tr>
          <tr v-for="dev in devolucoes" :key="dev.id" class="hover:bg-linho/50 transition-colors">
            <td class="px-8 py-6 font-mono text-xs">#{{ dev.id }}</td>
            <td class="px-8 py-6 text-sm text-stone-600">Pedido #{{ dev.venda?.id }}</td>
            <td class="px-8 py-6 text-sm text-stone-600">{{ dev.dataSolicitacao }}</td>
            <td class="px-8 py-6 text-sm text-stone-600">
              <span v-for="item in dev.itens" :key="item.id" class="block text-xs">
                {{ item.itemVenda?.produto?.nome }} ({{ item.quantidade }}x)
              </span>
            </td>
            <td class="px-8 py-6">
              <span class="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest"
                :class="devStatusClasses[dev.status]">{{ dev.status }}</span>
            </td>
            <td class="px-8 py-6 text-right space-x-2">
              <button v-if="dev.status === 'SOLICITADA'" @click="autorizarDevolucao(dev.id)"
                class="text-xs bg-blue-500 text-white px-3 py-1 rounded-lg hover:bg-blue-600 transition-colors">
                Autorizar
              </button>
              <button v-if="dev.status === 'AUTORIZADA'" @click="concluirDevolucao(dev.id)"
                class="text-xs bg-grafite text-white px-3 py-1 rounded-lg hover:bg-black transition-colors">
                Concluir e Devolver Estoque
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Orders Table -->
    <div v-if="mainTab === 'vendas'" class="bg-white rounded-[40px] border border-areia shadow-sm overflow-hidden">
      <table class="w-full text-left">
        <thead>
          <tr class="bg-linho border-b border-areia">
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">ID</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Cliente</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Data</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Total</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Status</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold text-right">Ações</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-areia/30">
          <tr v-for="order in filteredOrders" :key="order.id" class="hover:bg-linho/50 transition-colors">
            <td class="px-8 py-6 font-mono text-xs">{{ order.id }}</td>
            <td class="px-8 py-6 text-sm text-stone-600">{{ order.cliente?.nomeCompleto ?? `ID ${order.cliente?.id}` }}</td>
            <td class="px-8 py-6 text-sm text-stone-600">{{ order.dataVenda }}</td>
            <td class="px-8 py-6 text-sm font-bold text-grafite">R$ {{ Number(order.valorTotal).toFixed(2) }}</td>
            <td class="px-8 py-6">
              <span
                class="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest"
                :class="statusClasses[order.status]"
              >
                {{ statusLabel(order.status) }}
              </span>
            </td>
            <td class="px-8 py-6 text-right space-x-2">
              <button
                @click="viewOrderDetails(order)"
                class="text-xs bg-linho text-stone-600 px-3 py-1 rounded-lg hover:bg-areia transition-colors"
              >
                Detalhes
              </button>
              <button
                v-if="order.status === 'EM_PROCESSAMENTO'"
                @click="aprovar(order.id)"
                class="text-xs bg-green-500 text-white px-3 py-1 rounded-lg hover:bg-green-600 transition-colors"
              >
                Aprovar
              </button>
              <button
                v-if="order.status === 'EM_PROCESSAMENTO'"
                @click="reprovar(order.id)"
                class="text-xs bg-red-500 text-white px-3 py-1 rounded-lg hover:bg-red-600 transition-colors"
              >
                Reprovar
              </button>
              <button
                v-if="order.status === 'APROVADA'"
                @click="despachar(order.id)"
                class="text-xs bg-oliva text-white px-3 py-1 rounded-lg hover:bg-terra transition-colors"
              >
                Despachar
              </button>
              <button
                v-if="order.status === 'EM_TRANSPORTE'"
                @click="entregar(order.id)"
                class="text-xs bg-oliva text-white px-3 py-1 rounded-lg hover:bg-terra transition-colors"
              >
                Confirmar Entrega
              </button>
              <button
                v-if="order.status === 'EM_TROCA'"
                @click="autorizarTroca(order.id)"
                class="text-xs bg-terra text-white px-3 py-1 rounded-lg hover:bg-grafite transition-colors"
              >
                Autorizar Troca
              </button>
              <button
                v-if="order.status === 'TROCA_AUTORIZADA'"
                @click="confirmarTroca(order.id)"
                class="text-xs bg-grafite text-white px-3 py-1 rounded-lg hover:bg-black transition-colors"
              >
                Receber Itens
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Order Details Modal -->
    <div v-if="selectedOrder" class="fixed inset-0 z-[100] flex items-center justify-center px-4">
      <div class="absolute inset-0 bg-grafite/40 backdrop-blur-sm" @click="selectedOrder = null"></div>
      <div class="relative bg-white w-full max-w-2xl p-10 rounded-[40px] shadow-2xl border border-areia max-h-[90vh] overflow-y-auto">
        <div class="flex justify-between items-start mb-8">
          <div>
            <h2 class="text-3xl font-serif font-bold text-grafite">Pedido #{{ selectedOrder.id }}</h2>
            <p class="text-stone-500">{{ selectedOrder.dataVenda }}</p>
          </div>
          <span
            class="px-4 py-1.5 rounded-full text-[10px] font-bold uppercase tracking-widest"
            :class="statusClasses[selectedOrder.status]"
          >
            {{ statusLabel(selectedOrder.status) }}
          </span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-10 mb-10">
          <div>
            <h3 class="text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-4">Itens do Pedido</h3>
            <div class="space-y-4">
              <div v-for="item in selectedOrder.itens" :key="item.id" class="flex items-center space-x-4">
                <img v-if="item.produto?.foto" :src="item.produto.foto" class="w-12 h-12 rounded-lg object-cover border border-areia" />
                <div class="w-12 h-12 rounded-lg border border-areia bg-linho flex items-center justify-center text-xs text-stone-400" v-else>—</div>
                <div>
                  <p class="text-sm font-bold text-grafite">{{ item.produto?.nome ?? `Produto #${item.produto?.id}` }}</p>
                  <p class="text-xs text-stone-500">{{ item.quantidade }}x R$ {{ Number(item.valorUnitario).toFixed(2) }}</p>
                </div>
              </div>
            </div>
            <div class="mt-6 pt-6 border-t border-areia flex justify-between items-center">
              <span class="text-sm font-bold text-grafite">Total</span>
              <span class="text-lg font-bold text-oliva">R$ {{ Number(selectedOrder.valorTotal).toFixed(2) }}</span>
            </div>
          </div>

          <div>
            <h3 class="text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-4">Endereço de Entrega</h3>
            <div class="bg-linho/30 p-6 rounded-3xl border border-areia">
              <template v-if="selectedOrder.endereco">
                <p class="text-sm font-medium text-grafite">{{ selectedOrder.endereco.logradouro }}, {{ selectedOrder.endereco.numero }}</p>
                <p class="text-sm text-stone-600">{{ selectedOrder.endereco.cidade }}, {{ selectedOrder.endereco.estado }}</p>
                <p class="text-xs text-stone-400 mt-2">{{ selectedOrder.endereco.cep }}</p>
              </template>
              <p v-else class="text-sm text-stone-400">Não informado</p>
            </div>

            <h3 class="text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-4 mt-8">Cliente</h3>
            <p class="text-sm font-medium text-grafite">{{ selectedOrder.cliente?.nomeCompleto ?? `ID ${selectedOrder.cliente?.id}` }}</p>
          </div>
        </div>

        <div class="flex justify-end space-x-4">
          <button
            @click="selectedOrder = null"
            class="px-8 py-3 rounded-xl text-sm font-medium text-stone-500 hover:bg-linho transition-colors"
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onBeforeMount } from 'vue';
import apiClient from '@/plugins/axios';

const orders = ref<any[]>([]);
const activeStatus = ref('TODOS');
const selectedOrder = ref<any>(null);
const mainTab = ref('vendas');
const devolucoes = ref<any[]>([]);

const statuses = ['TODOS', 'EM_PROCESSAMENTO', 'APROVADA', 'REPROVADA', 'EM_TRANSPORTE', 'ENTREGUE', 'EM_TROCA', 'TROCA_AUTORIZADA', 'TROCADO'];

const statusLabels: Record<string, string> = {
  'TODOS': 'Todos',
  'EM_PROCESSAMENTO': 'Em Processamento',
  'APROVADA': 'Aprovada',
  'REPROVADA': 'Reprovada',
  'EM_TRANSPORTE': 'Em Transporte',
  'ENTREGUE': 'Entregue',
  'EM_TROCA': 'Em Troca',
  'TROCA_AUTORIZADA': 'Troca Autorizada',
  'TROCADO': 'Trocado',
};

const statusLabel = (s: string) => statusLabels[s] ?? s;

const devStatusClasses: Record<string, string> = {
  'SOLICITADA': 'bg-yellow-100 text-yellow-700',
  'AUTORIZADA': 'bg-blue-100 text-blue-700',
  'CONCLUIDA': 'bg-emerald-100 text-emerald-700'
};

const statusClasses: Record<string, string> = {
  'EM_PROCESSAMENTO': 'bg-blue-100 text-blue-600',
  'APROVADA': 'bg-green-100 text-green-600',
  'REPROVADA': 'bg-red-100 text-red-600',
  'EM_TRANSPORTE': 'bg-yellow-100 text-yellow-600',
  'ENTREGUE': 'bg-stone-100 text-stone-600',
  'EM_TROCA': 'bg-orange-100 text-orange-600',
  'TROCA_AUTORIZADA': 'bg-purple-100 text-purple-600',
  'TROCADO': 'bg-emerald-100 text-emerald-600',
};

const filteredOrders = computed(() => {
  if (activeStatus.value === 'TODOS') return orders.value;
  return orders.value.filter(o => o.status === activeStatus.value);
});

const getOrders = async () => {
  const response = await apiClient.get('/vendas');
  orders.value = response.data;
};

const refreshOrder = async (id: number) => {
  const response = await apiClient.get(`/vendas/${id}`);
  const idx = orders.value.findIndex(o => o.id === id);
  if (idx !== -1) orders.value[idx] = response.data;
  if (selectedOrder.value?.id === id) selectedOrder.value = response.data;
};

const aprovar = async (id: number) => {
  await apiClient.put(`/vendas/${id}/aprovar`);
  await refreshOrder(id);
};

const reprovar = async (id: number) => {
  await apiClient.put(`/vendas/${id}/reprovar`);
  await refreshOrder(id);
};

const despachar = async (id: number) => {
  await apiClient.put(`/vendas/${id}/despachar`);
  await refreshOrder(id);
};

const entregar = async (id: number) => {
  await apiClient.put(`/vendas/${id}/entregar`);
  await refreshOrder(id);
};

const autorizarTroca = async (id: number) => {
  await apiClient.put(`/vendas/${id}/autorizar-troca`);
  await refreshOrder(id);
  alert('Troca autorizada com sucesso! O cliente foi notificado.');
};

const confirmarTroca = async (id: number) => {
  if (!confirm('Os itens recebidos devem retornar ao estoque?')) return;
  await apiClient.put(`/vendas/${id}/confirmar-troca`);
  await refreshOrder(id);
};

const viewOrderDetails = (order: any) => {
  selectedOrder.value = order;
};

const loadDevolucoes = async () => {
  const res = await apiClient.get('/devolucoes');
  devolucoes.value = res.data;
};

const autorizarDevolucao = async (id: number) => {
  await apiClient.put(`/devolucoes/${id}/autorizar`);
  await loadDevolucoes();
};

const concluirDevolucao = async (id: number) => {
  if (!confirm('Confirma a conclusão? Os itens serão devolvidos ao estoque.')) return;
  await apiClient.put(`/devolucoes/${id}/concluir`);
  await loadDevolucoes();
};

onBeforeMount(() => {
  getOrders();
});
</script>
