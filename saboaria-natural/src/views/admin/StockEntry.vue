<template>
  <div class="space-y-8">
    <header>
      <h1 class="text-3xl font-serif font-bold text-grafite">Entrada em Estoque</h1>
      <p class="text-stone-500">Registre a entrada de novos lotes de sabonetes.</p>
    </header>

    <div class="bg-white rounded-[40px] border border-areia shadow-sm p-10 max-w-2xl">
      <form @submit.prevent="handleStockEntry" class="space-y-6">
        <div>
          <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Produto</label>
          <select v-model="entry.productId" @change="onProductChange" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva">
            <option v-for="produto in produtos" :key="produto.id" :value="produto.id">
              {{ produto.nome }}
            </option>
          </select>
          <p v-if="estoqueAtual !== null" class="text-xs text-stone-400 mt-1">Estoque atual: {{ estoqueAtual }} unidades</p>
        </div>

        <div class="grid grid-cols-2 gap-6">
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Quantidade</label>
            <input v-model.number="entry.quantity" type="number" min="1" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Valor de Custo (Un.)</label>
            <input v-model.number="entry.cost" type="number" step="0.01" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
        </div>

        <div class="grid grid-cols-2 gap-6">
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Fornecedor</label>
            <input v-model="entry.supplier" type="text" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Data de Entrada</label>
            <input v-model="entry.date" type="date" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
        </div>

        <div class="pt-6">
          <button type="submit" :disabled="loading" class="w-full bg-oliva text-white px-8 py-4 rounded-2xl font-bold hover:bg-terra transition-all shadow-lg shadow-oliva/20 disabled:opacity-50">
            {{ loading ? 'Registrando...' : 'Registrar Entrada' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import apiClient from '@/plugins/axios';

const produtos = ref<any[]>([]);
const estoqueAtual = ref<number | null>(null);
const loading = ref(false);

const entry = reactive({
  productId: null as number | null,
  quantity: 0,
  cost: 0,
  supplier: '',
  date: (() => {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  })()
});

const fetchProdutos = async () => {
  try {
    const res = await apiClient.get('/produtos', { params: { limit: 100, offset: 0 } });
    produtos.value = res.data;
  } catch (e) {
    console.error('Erro ao carregar produtos:', e);
  }
};

const onProductChange = async () => {
  if (!entry.productId) { estoqueAtual.value = null; return; }
  try {
    const res = await apiClient.get('/estoques/disponivel', { params: { produtoId: entry.productId } });
    estoqueAtual.value = res.data;
  } catch { estoqueAtual.value = null; }
};

const handleStockEntry = async () => {
  loading.value = true;
  try {
    const res = await apiClient.post('/estoques', {
      produto: { id: entry.productId },
      quantidade: entry.quantity,
      valorCusto: entry.cost,
      fornecedor: entry.supplier,
      dataEntrada: entry.date
    });
    const novoPreco = res.data?.valorVenda?.toFixed(2) ?? '—';
    alert(`Entrada registrada! Novo preço de venda: R$ ${novoPreco}`);
    entry.productId = null;
    entry.quantity = 0;
    entry.cost = 0;
    entry.supplier = '';
    estoqueAtual.value = null;
  } catch (e) {
    console.error('Erro ao registrar entrada:', e);
    alert('Erro ao registrar entrada. Tente novamente.');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchProdutos);
</script>
