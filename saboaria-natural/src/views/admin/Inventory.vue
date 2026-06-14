<template>
  <div class="space-y-8">
    <header class="flex justify-between items-end">
      <div>
        <h1 class="text-3xl font-serif font-bold text-grafite">Gerenciamento de Produtos</h1>
        <p class="text-stone-500">Controle o catálogo e a disponibilidade dos seus sabonetes.</p>
      </div>
      <button @click="showModal = true" class="bg-oliva text-white px-6 py-3 rounded-xl text-sm font-medium hover:bg-terra transition-colors">
        + Novo Produto
      </button>
    </header>

    <!-- Inventory Table -->
    <div class="bg-white rounded-[40px] border border-areia shadow-sm overflow-hidden">
      <table class="w-full text-left">
        <thead>
          <tr class="bg-linho border-b border-areia">
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Produto</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Categoria</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Preço</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Estoque</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Status</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold text-right">Ações</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-areia/30">
          <tr v-for="product in products" :key="product.id" class="hover:bg-linho/50 transition-colors">
            <td class="px-8 py-6">
              <div class="flex items-center">
                <img :src="product.foto || 'https://picsum.photos/seed/soap/400/400'" class="w-12 h-12 rounded-xl object-cover mr-4 border border-areia" />
                <div>
                  <p class="font-serif font-bold text-grafite">{{ product.nome }}</p>
                  <p class="text-xs text-stone-400">{{ product.descricao }}</p>
                </div>
              </div>
            </td>
            <td class="px-8 py-6">
              <span v-if="product.categoria" class="text-xs font-medium text-stone-600 bg-areia/40 px-3 py-1 rounded-full">
                {{ product.categoria.nome }}
              </span>
              <span v-else class="text-xs text-stone-400">—</span>
            </td>
            <td class="px-8 py-6 text-sm text-stone-600">
              {{ produtoStats[product.id]?.preco != null ? `R$ ${produtoStats[product.id].preco.toFixed(2)}` : '—' }}
            </td>
            <td class="px-8 py-6">
              <span class="text-sm font-bold text-grafite">{{ produtoStats[product.id]?.estoque ?? '—' }}</span>
            </td>
            <td class="px-8 py-6">
              <span class="text-[10px] font-bold uppercase tracking-widest text-oliva">Ativo</span>
            </td>
            <td class="px-8 py-6 text-right">
              <button @click="openEdit(product)" class="text-stone-300 hover:text-oliva mr-4"><EditIcon :size="18" /></button>
              <button class="text-stone-300 hover:text-red-500"><TrashIcon :size="18" /></button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal for Registration -->
    <div v-if="showModal" class="fixed inset-0 z-[100] flex items-center justify-center px-4">
      <div class="absolute inset-0 bg-grafite/40 backdrop-blur-sm" @click="showModal = false"></div>
      <div class="relative bg-white w-full max-w-4xl p-10 rounded-[40px] shadow-2xl border border-areia overflow-y-auto max-h-[90vh]">
        <div class="flex justify-between items-center mb-8">
          <h2 class="text-3xl font-serif font-bold">Novo Produto</h2>
          <button @click="showModal = false" class="text-stone-400 hover:text-grafite"><XIcon :size="24" /></button>
        </div>

        <form @submit.prevent="handleAddProduct" class="space-y-8">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <!-- Basic Info -->
            <div class="space-y-6">
              <h3 class="text-xl font-serif">Informações Básicas</h3>
              <div>
                <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Nome</label>
                <input v-model="newProduct.nome" type="text" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
              </div>
              <div>
                <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Descrição</label>
                <textarea v-model="newProduct.descricao" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva h-24"></textarea>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Grupo de Precificação</label>
                  <select v-model="newProduct.grupoPrecificacaoId" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva">
                    <option v-for="group in store.pricingGroups" :key="group.id" :value="group.id">{{ group.name }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Categoria</label>
                  <select v-model="newProduct.categoriaId" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva">
                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.nome }}</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Technical Details -->
            <div class="space-y-6">
              <h3 class="text-xl font-serif">Detalhes Técnicos</h3>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Peso (g)</label>
                  <input v-model.number="newProduct.peso" type="number" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
                </div>
                <div>
                  <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Dimensões</label>
                  <input v-model="newProduct.dimensoes" type="text" placeholder="7x5x3cm" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
                </div>
              </div>
              <div>
                <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Ingredientes</label>
                <input v-model="newProduct.ingredientes" type="text" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
              </div>
              <div>
                <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Aroma</label>
                <input v-model="newProduct.aroma" type="text" class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
              </div>
            </div>
          </div>

          <div class="flex justify-end space-x-4 pt-8 border-t border-areia">
            <button type="button" @click="showModal = false" class="px-8 py-4 rounded-2xl text-sm font-medium text-stone-500 hover:bg-stone-50 transition-colors">Cancelar</button>
            <button type="submit" :disabled="saving" class="bg-oliva text-white px-12 py-4 rounded-2xl font-bold hover:bg-terra transition-all shadow-lg shadow-oliva/20 disabled:opacity-50">
              {{ saving ? 'Salvando...' : 'Salvar Produto' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal for Editing -->
    <div v-if="showEditModal" class="fixed inset-0 z-[100] flex items-center justify-center px-4">
      <div class="absolute inset-0 bg-grafite/40 backdrop-blur-sm" @click="showEditModal = false"></div>
      <div class="relative bg-white w-full max-w-4xl p-10 rounded-[40px] shadow-2xl border border-areia overflow-y-auto max-h-[90vh]">
        <div class="flex justify-between items-center mb-8">
          <h2 class="text-3xl font-serif font-bold">Editar Produto</h2>
          <button @click="showEditModal = false" class="text-stone-400 hover:text-grafite"><XIcon :size="24" /></button>
        </div>

        <form @submit.prevent="handleEditProduct" class="space-y-8">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <!-- Basic Info -->
            <div class="space-y-6">
              <h3 class="text-xl font-serif">Informações Básicas</h3>
              <div>
                <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Nome</label>
                <input v-model="editProduct.nome" type="text" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
              </div>
              <div>
                <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Descrição</label>
                <textarea v-model="editProduct.descricao" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva h-24"></textarea>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Grupo de Precificação</label>
                  <select v-model="editProduct.grupoPrecificacaoId" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva">
                    <option v-for="group in store.pricingGroups" :key="group.id" :value="group.id">{{ group.name }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Categoria</label>
                  <select v-model="editProduct.categoriaId" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva">
                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.nome }}</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Technical Details -->
            <div class="space-y-6">
              <h3 class="text-xl font-serif">Detalhes Técnicos</h3>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Peso (g)</label>
                  <input v-model.number="editProduct.peso" type="number" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
                </div>
                <div>
                  <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Dimensões</label>
                  <input v-model="editProduct.dimensoes" type="text" placeholder="7x5x3cm" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
                </div>
              </div>
              <div>
                <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Ingredientes</label>
                <input v-model="editProduct.ingredientes" type="text" required class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
              </div>
              <div>
                <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Aroma</label>
                <input v-model="editProduct.aroma" type="text" class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
              </div>
            </div>
          </div>

          <div class="flex justify-end space-x-4 pt-8 border-t border-areia">
            <button type="button" @click="showEditModal = false" class="px-8 py-4 rounded-2xl text-sm font-medium text-stone-500 hover:bg-stone-50 transition-colors">Cancelar</button>
            <button type="submit" :disabled="saving" class="bg-oliva text-white px-12 py-4 rounded-2xl font-bold hover:bg-terra transition-all shadow-lg shadow-oliva/20 disabled:opacity-50">
              {{ saving ? 'Salvando...' : 'Salvar Alterações' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { Edit2 as EditIcon, Trash2 as TrashIcon, X as XIcon } from 'lucide-vue-next';
import { store } from '../../store';
import apiClient from '@/plugins/axios';

const showModal = ref(false);
const saving = ref(false);
const products = ref<any[]>([]);
const categories = ref<any[]>([]);
const produtoStats = ref<Record<number, { preco: number | null; estoque: number | null }>>({});

const showEditModal = ref(false);
const editingProductId = ref<number | null>(null);
const editProduct = reactive({
  nome: '',
  descricao: '',
  peso: 100,
  dimensoes: '',
  ingredientes: '',
  aroma: '',
  grupoPrecificacaoId: null as number | null,
  categoriaId: null as number | null,
});

const openEdit = (product: any) => {
  editingProductId.value = product.id;
  Object.assign(editProduct, {
    nome: product.nome,
    descricao: product.descricao,
    peso: product.peso ?? 100,
    dimensoes: product.dimensoes ?? '',
    ingredientes: product.ingredientes ?? '',
    aroma: product.aroma ?? '',
    grupoPrecificacaoId: product.grupoPrecificacao?.id ?? null,
    categoriaId: product.categoria?.id ?? null,
  });
  showEditModal.value = true;
};

const handleEditProduct = async () => {
  if (!editingProductId.value) return;
  saving.value = true;
  try {
    await apiClient.put(`/produtos/${editingProductId.value}`, {
      nome: editProduct.nome,
      descricao: editProduct.descricao,
      peso: editProduct.peso,
      dimensoes: editProduct.dimensoes,
      ingredientes: editProduct.ingredientes,
      aroma: editProduct.aroma,
      grupoPrecificacao: editProduct.grupoPrecificacaoId ? { id: editProduct.grupoPrecificacaoId } : null,
      categoria: editProduct.categoriaId ? { id: editProduct.categoriaId } : null,
    });
    showEditModal.value = false;
    await fetchProducts();
    fetchEstoqueInfo();
  } catch (e) {
    console.error('Erro ao editar produto:', e);
    alert('Erro ao salvar alterações. Verifique os dados e tente novamente.');
  } finally {
    saving.value = false;
  }
};

const newProduct = reactive({
  nome: '',
  descricao: '',
  peso: 100,
  dimensoes: '',
  ingredientes: '',
  aroma: '',
  grupoPrecificacaoId: null as number | null,
  categoriaId: null as number | null
});

const fetchProducts = async () => {
  try {
    const res = await apiClient.get('/produtos', { params: { limit: 100, offset: 0 } });
    products.value = res.data;
  } catch (e) {
    console.error('Erro ao carregar produtos:', e);
  }
};

const fetchEstoqueInfo = async () => {
  const prods = products.value;
  if (!prods.length) return;
  try {
    const [estoqueRes, disponivelList] = await Promise.all([
      apiClient.get('/estoques', { params: { limit: 1000, offset: 0 } }),
      Promise.all(
        prods.map((p: any) =>
          apiClient.get('/estoques/disponivel', { params: { produtoId: p.id } })
            .then(r => ({ id: p.id as number, qtd: r.data as number }))
            .catch(() => ({ id: p.id as number, qtd: null as number | null }))
        )
      )
    ]);

    const stats: Record<number, { preco: number | null; estoque: number | null }> = {};
    for (const entry of estoqueRes.data as any[]) {
      const pid = entry.produto?.id;
      if (pid != null) stats[pid] = { preco: entry.valorVenda ?? null, estoque: null };
    }
    for (const { id, qtd } of disponivelList) {
      if (!stats[id]) stats[id] = { preco: null, estoque: null };
      stats[id].estoque = qtd;
    }
    produtoStats.value = stats;
  } catch (e) {
    console.error('Erro ao carregar informações de estoque:', e);
  }
};

const fetchCategories = async () => {
  try {
    const res = await apiClient.get('/categorias');
    categories.value = res.data;
  } catch (e) {
    console.error('Erro ao carregar categorias:', e);
  }
};

const handleAddProduct = async () => {
  saving.value = true;
  try {
    await apiClient.post('/produtos', {
      nome: newProduct.nome,
      descricao: newProduct.descricao,
      peso: newProduct.peso,
      dimensoes: newProduct.dimensoes,
      ingredientes: newProduct.ingredientes,
      aroma: newProduct.aroma,
      grupoPrecificacao: newProduct.grupoPrecificacaoId ? { id: newProduct.grupoPrecificacaoId } : null,
      categoria: newProduct.categoriaId ? { id: newProduct.categoriaId } : null
    });
    showModal.value = false;
    await fetchProducts();
    Object.assign(newProduct, {
      nome: '', descricao: '', peso: 100, dimensoes: '',
      ingredientes: '', aroma: '', grupoPrecificacaoId: null, categoriaId: null
    });
  } catch (e) {
    console.error('Erro ao salvar produto:', e);
    alert('Erro ao salvar produto. Verifique os dados e tente novamente.');
  } finally {
    saving.value = false;
  }
};

onMounted(async () => {
  await fetchProducts();
  fetchCategories();
  fetchEstoqueInfo();
});
</script>
