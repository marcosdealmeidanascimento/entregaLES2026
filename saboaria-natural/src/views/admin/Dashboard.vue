<template>
  <div class="space-y-8">
    <header class="flex justify-between items-end">
      <div>
        <h1 class="text-3xl font-serif font-bold text-stone-800">Visão Geral das Vendas</h1>
        <p class="text-stone-500">Acompanhe o desempenho da sua saboaria em tempo real.</p>
      </div>
      <div v-if="store.currentUser" class="text-right">
        <p class="text-xs text-stone-400 uppercase tracking-widest font-bold">Bem-vindo,</p>
        <p class="text-lg font-serif font-bold text-oliva">{{ store.currentUser.name }}</p>
      </div>
    </header>

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div v-for="stat in stats" :key="stat.label" class="bg-white p-6 rounded-3xl border border-stone-100 shadow-sm">
        <p class="text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-1">{{ stat.label }}</p>
        <p class="text-3xl font-serif font-bold text-stone-800">{{ stat.value }}</p>
        <p v-if="stat.trend !== null" class="text-xs mt-2" :class="stat.trend >= 0 ? 'text-green-600' : 'text-red-600'">
          {{ stat.trend >= 0 ? '+' : '' }}{{ stat.trend }}% em relação ao mês passado
        </p>
        <p v-else class="text-xs mt-2 text-stone-400">Sem dados do mês anterior</p>
      </div>
    </div>

    <!-- Chart Section -->
    <div class="bg-white p-8 rounded-[40px] border border-stone-100 shadow-sm">
      <div class="flex justify-between items-center mb-8">
        <h2 class="text-xl font-serif font-bold">Vendas vs. Tempo</h2>
        <div class="flex items-center gap-2">
          <button
            @click="carregarGrafico(7)"
            class="px-3 py-1 text-[10px] font-bold uppercase tracking-wider rounded transition-colors"
            :class="periodoAtivo === 7 && !customAtivo ? 'bg-oliva text-white' : 'bg-stone-100 text-stone-500 hover:bg-stone-200'"
          >7 Dias</button>
          <button
            @click="carregarGrafico(30)"
            class="px-3 py-1 text-[10px] font-bold uppercase tracking-wider rounded transition-colors"
            :class="periodoAtivo === 30 && !customAtivo ? 'bg-oliva text-white' : 'bg-stone-100 text-stone-500 hover:bg-stone-200'"
          >30 Dias</button>
          <div class="flex items-center gap-1 ml-1">
            <input
              v-model.number="diasCustom"
              type="number" min="1" max="365" placeholder="Dias"
              class="w-16 border border-areia rounded-lg px-2 py-1 text-xs text-center focus:outline-none focus:border-oliva"
              @keydown.enter="aplicarCustom"
            />
            <button
              @click="aplicarCustom"
              class="px-3 py-1 text-[10px] font-bold uppercase tracking-wider rounded transition-colors"
              :class="customAtivo ? 'bg-oliva text-white' : 'bg-stone-100 text-stone-500 hover:bg-stone-200'"
            >Aplicar</button>
          </div>
        </div>
      </div>
      <div v-if="semDadosGrafico" class="h-[400px] flex items-center justify-center text-stone-400 text-sm">
        Nenhuma venda no período selecionado.
      </div>
      <div v-else class="h-[400px]">
        <canvas ref="salesChart"></canvas>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import Chart from 'chart.js/auto';
import { store } from '../../store';
import apiClient from '@/plugins/axios';

const salesChart = ref<HTMLCanvasElement | null>(null);
let chart: Chart | null = null;

const periodoAtivo = ref(30);
const customAtivo = ref(false);
const diasCustom = ref<number | null>(null);
const semDadosGrafico = ref(false);

const stats = ref([
  { label: 'Receita Total', value: 'R$ —', trend: null as number | null },
  { label: 'Pedidos',       value: '—',    trend: null as number | null },
  { label: 'Ticket Médio',  value: 'R$ —', trend: null as number | null },
]);

const calcTrend = (atual: number, anterior: number): number | null => {
  if (anterior === 0) return null;
  return parseFloat(((atual - anterior) / anterior * 100).toFixed(1));
};

const formatBRL = (v: number) =>
  v.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const carregarStats = async () => {
  try {
    const res = await apiClient.get('/relatorios/dashboard');
    const d = res.data;
    stats.value = [
      {
        label: 'Receita Total',
        value: `R$ ${formatBRL(d.receitaAtual)}`,
        trend: calcTrend(d.receitaAtual, d.receitaAnterior)
      },
      {
        label: 'Pedidos',
        value: String(d.pedidosAtual),
        trend: calcTrend(d.pedidosAtual, d.pedidosAnterior)
      },
      {
        label: 'Ticket Médio',
        value: `R$ ${formatBRL(d.ticketAtual)}`,
        trend: calcTrend(d.ticketAtual, d.ticketAnterior)
      },
    ];
  } catch {
    // mantém estado vazio
  }
};

const aplicarCustom = () => {
  const d = diasCustom.value;
  if (!d || d < 1 || d > 365) return;
  customAtivo.value = true;
  carregarGrafico(d);
};

const carregarGrafico = async (dias: number) => {
  periodoAtivo.value = dias;
  if (dias !== diasCustom.value) customAtivo.value = false;
  try {
    const res = await apiClient.get('/relatorios/vendas-diarias', { params: { dias } });
    const { labels, dados } = res.data;

    semDadosGrafico.value = !labels || labels.length === 0;
    if (semDadosGrafico.value) {
      if (chart) { chart.destroy(); chart = null; }
      return;
    }

    await nextTick();

    if (chart) {
      chart.data.labels = labels;
      chart.data.datasets[0].data = dados;
      chart.update();
    } else if (salesChart.value) {
      chart = new Chart(salesChart.value, {
        type: 'line',
        data: {
          labels,
          datasets: [{
            label: 'Receita (R$)',
            data: dados,
            borderColor: '#7A8C6D',
            backgroundColor: '#7A8C6D22',
            tension: 0.4,
            pointRadius: 4,
            fill: true,
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: {
            y: {
              beginAtZero: true,
              grid: { display: false },
              ticks: { callback: (v: any) => 'R$ ' + Number(v).toLocaleString('pt-BR') }
            },
            x: { grid: { display: false } }
          }
        }
      });
    }
  } catch {
    semDadosGrafico.value = true;
  }
};

onMounted(async () => {
  await carregarStats();
  await carregarGrafico(30);
});
</script>
