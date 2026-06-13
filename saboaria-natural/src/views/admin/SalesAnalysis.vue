<template>
  <div class="space-y-8">
    <header>
      <h1 class="text-3xl font-serif font-bold text-grafite">Análise de Vendas</h1>
      <p class="text-stone-500">Volume de sabonetes vendidos por categoria ao longo do tempo.</p>
    </header>

    <!-- Filtros de período -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="bg-white p-8 rounded-[40px] border border-areia shadow-sm">
        <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Data Início</label>
        <input v-model="filters.start" type="date" class="w-full border-none focus:ring-0 text-sm font-medium" />
      </div>
      <div class="bg-white p-8 rounded-[40px] border border-areia shadow-sm">
        <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Data Fim</label>
        <input v-model="filters.end" type="date" class="w-full border-none focus:ring-0 text-sm font-medium" />
      </div>
      <div class="bg-white p-8 rounded-[40px] border border-areia shadow-sm flex items-end">
        <button @click="generateReport" :disabled="loading" class="w-full bg-oliva text-white px-6 py-3 rounded-xl text-sm font-medium hover:bg-terra transition-colors disabled:opacity-50">
          {{ loading ? 'Carregando...' : 'Gerar Relatório' }}
        </button>
      </div>
    </div>

    <!-- Filtro de categorias (multi-seleção) -->
    <div class="bg-white p-8 rounded-[40px] border border-areia shadow-sm">
      <div class="flex items-center justify-between mb-4">
        <label class="text-[10px] uppercase tracking-widest text-stone-400 font-bold">Categorias</label>
        <div class="flex gap-2">
          <button @click="selectAll" class="text-[10px] uppercase tracking-widest text-oliva font-bold hover:text-terra transition-colors">Todos</button>
          <span class="text-stone-300">|</span>
          <button @click="clearAll" class="text-[10px] uppercase tracking-widest text-stone-400 font-bold hover:text-red-400 transition-colors">Limpar</button>
        </div>
      </div>
      <div class="flex flex-wrap gap-3">
        <button
          v-for="(cat, idx) in categories"
          :key="cat.id"
          @click="toggleCategory(cat.nome)"
          class="px-4 py-2 rounded-xl text-sm font-medium transition-all"
          :style="selectedCategories.includes(cat.nome) ? { backgroundColor: CORES[idx % CORES.length], color: '#fff' } : {}"
          :class="selectedCategories.includes(cat.nome) ? '' : 'bg-areia/30 text-stone-500 hover:bg-areia/60'"
        >
          {{ cat.nome }}
        </button>
      </div>
    </div>

    <!-- Gráfico -->
    <div class="bg-white p-10 rounded-[40px] border border-areia shadow-sm">
      <div v-if="noData" class="h-[400px] flex items-center justify-center text-stone-400 text-sm">
        Nenhuma venda no período selecionado.
      </div>
      <div v-else-if="selectedCategories.length === 0" class="h-[400px] flex items-center justify-center text-stone-400 text-sm">
        Selecione ao menos uma categoria para visualizar o gráfico.
      </div>
      <div v-else class="h-[400px]">
        <canvas ref="chartCanvas"></canvas>
      </div>
    </div>

    <!-- KPIs -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="bg-white p-8 rounded-[40px] border border-areia shadow-sm">
        <p class="text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Total de Receita</p>
        <p class="text-3xl font-serif font-bold text-grafite">R$ {{ totais.totalReceita.toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}</p>
      </div>
      <div class="bg-white p-8 rounded-[40px] border border-areia shadow-sm">
        <p class="text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Ticket Médio</p>
        <p class="text-3xl font-serif font-bold text-grafite">R$ {{ totais.ticketMedio.toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}</p>
      </div>
      <div class="bg-white p-8 rounded-[40px] border border-areia shadow-sm">
        <p class="text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Unidades Vendidas</p>
        <p class="text-3xl font-serif font-bold text-grafite">{{ totais.produtosVendidos }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, nextTick } from 'vue';
import Chart from 'chart.js/auto';
import apiClient from '@/plugins/axios';

const chartCanvas = ref<HTMLCanvasElement | null>(null);
let chart: Chart | null = null;

const loading = ref(false);
const noData = ref(false);

const filters = reactive({
  start: new Date(new Date().getFullYear() - 1, new Date().getMonth(), 1).toISOString().split('T')[0],
  end: new Date().toISOString().split('T')[0]
});

const categories = ref<any[]>([]);
const selectedCategories = ref<string[]>([]);
const allSeries = ref<any[]>([]);
const allLabels = ref<string[]>([]);

const totais = reactive({
  totalReceita: 0,
  ticketMedio: 0,
  produtosVendidos: 0
});

const CORES = [
  '#7A8C6D', '#8B6B4F', '#C4956A', '#3F3A37', '#A0B090',
  '#5C7A5C', '#9E7B5A', '#B0C4A0', '#7A6050', '#D8CFC4'
];

const fetchCategories = async () => {
  try {
    const res = await apiClient.get('/categorias');
    categories.value = res.data.filter((c: any) => c.nome !== 'Neutros');
  } catch (e) {
    console.error('Erro ao carregar categorias:', e);
  }
};

const toggleCategory = (nome: string) => {
  const idx = selectedCategories.value.indexOf(nome);
  if (idx === -1) {
    selectedCategories.value = [...selectedCategories.value, nome];
  } else {
    selectedCategories.value = selectedCategories.value.filter(n => n !== nome);
  }
  applyFilter();
};

const selectAll = () => {
  selectedCategories.value = allSeries.value.map((s: any) => s.nome);
  applyFilter();
};

const clearAll = () => {
  selectedCategories.value = [];
  applyFilter();
};

const renderChart = async (labels: string[], series: any[]) => {
  if (!labels.length || !series.length) {
    if (chart) { chart.destroy(); chart = null; }
    return;
  }

  const datasets = series.map((s: any, idx: number) => {
    const catIdx = categories.value.findIndex((c: any) => c.nome === s.nome);
    const color = CORES[(catIdx >= 0 ? catIdx : idx) % CORES.length];
    return {
      label: s.nome,
      data: s.dadosQtd,
      borderColor: color,
      backgroundColor: color + '22',
      tension: 0.4,
      pointRadius: 4,
      fill: false
    };
  });

  await nextTick();

  if (chart) {
    chart.data.labels = labels;
    chart.data.datasets = datasets;
    chart.update();
  } else if (chartCanvas.value) {
    chart = new Chart(chartCanvas.value, {
      type: 'line',
      data: { labels, datasets },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: true,
            position: 'bottom',
            labels: { boxWidth: 12, font: { size: 11 } }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: { display: false },
            ticks: { callback: (v: any) => v + ' un.' }
          },
          x: { grid: { display: false } }
        }
      }
    });
  }
};

const applyFilter = () => {
  const filtered = allSeries.value.filter((s: any) =>
    selectedCategories.value.includes(s.nome)
  );

  totais.produtosVendidos = filtered.reduce((sum: number, s: any) =>
    sum + s.dadosQtd.reduce((a: number, v: number) => a + v, 0), 0);

  totais.totalReceita = Math.round(
    filtered.reduce((sum: number, s: any) =>
      sum + s.dados.reduce((a: number, v: number) => a + v, 0), 0) * 100) / 100;

  renderChart(allLabels.value, filtered);
};

const generateReport = async () => {
  loading.value = true;
  noData.value = false;
  try {
    const [sy, sm] = filters.start.split('-').map(Number);
    const dataInicio = `${sy}-${String(sm).padStart(2, '0')}-01`;

    const [ey, em] = filters.end.split('-').map(Number);
    const lastDay = new Date(ey, em, 0).getDate();
    const dataFim = `${ey}-${String(em).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;

    const res = await apiClient.get('/relatorios/vendas', {
      params: { dataInicio, dataFim, agruparPor: 'CATEGORIA' }
    });

    const { labels, series, totais: t } = res.data;

    allLabels.value = labels ?? [];
    allSeries.value = series ?? [];
    totais.ticketMedio = t.ticketMedio;

    if (!allLabels.value.length) {
      noData.value = true;
      if (chart) { chart.destroy(); chart = null; }
      return;
    }

    if (selectedCategories.value.length === 0) {
      selectedCategories.value = allSeries.value.map((s: any) => s.nome);
    }

    applyFilter();
  } catch (e) {
    console.error('Erro ao gerar relatório:', e);
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  await fetchCategories();
  generateReport();
});
</script>
