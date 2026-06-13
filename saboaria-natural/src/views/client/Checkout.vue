<template>
  <div class="py-16 max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
    <h1 class="text-4xl font-serif mb-12">Finalizar Compra</h1>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-16">
      <!-- Form -->
      <div class="space-y-10">
        <!-- Step 1: Address Selection -->
        <section>
          <h2 class="text-xl font-serif mb-6 flex items-center">
            <span class="w-8 h-8 rounded-full bg-areia/20 text-oliva flex items-center justify-center mr-3 text-sm font-bold">1</span>
            Endereço de Entrega
          </h2>
          <div class="grid grid-cols-1 gap-4">
            <div v-for="addr in enderecos" :key="addr.id" @click="selectAddress(addr)"
              class="border-2 p-6 rounded-2xl bg-white relative cursor-pointer transition-all"
              :class="selectedAddress?.id === addr.id ? 'border-oliva' : 'border-areia hover:border-oliva/50'">
              <div v-if="selectedAddress?.id === addr.id" class="absolute top-4 right-4 text-oliva">
                <CheckCircleIcon :size="20" />
              </div>
              <p class="font-medium">{{ addr.apelidoEndereco || addr.logradouro }}</p>
              <p class="text-stone-500 text-sm">{{ addr.logradouro }}, {{ addr.numero }} — {{ addr.cidade }}, {{ addr.estado }} - {{ addr.cep }}</p>
            </div>

            <!-- Formulário de novo endereço -->
            <div v-if="!showNewAddressForm"
              class="border-2 border-dashed border-areia rounded-2xl p-4 text-center cursor-pointer hover:border-oliva/50 transition-all"
              @click="showNewAddressForm = true">
              <p class="text-sm text-stone-400 font-medium">+ Novo endereço</p>
            </div>
            <div v-else class="border-2 border-oliva rounded-2xl p-6 bg-white space-y-4">
              <p class="font-serif text-lg font-bold text-grafite">Novo Endereço</p>
              <div class="flex gap-2">
                <input v-model="newAddress.cep" placeholder="CEP (00000-000)" maxlength="9"
                  @blur="buscarCEP"
                  class="flex-1 border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
                <button @click="buscarCEP" :disabled="buscandoCEP"
                  class="bg-grafite text-white px-4 py-2 rounded-xl text-xs font-bold hover:bg-black transition-colors disabled:opacity-50">
                  {{ buscandoCEP ? '...' : 'Buscar' }}
                </button>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <input v-model="newAddress.logradouro" placeholder="Logradouro"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva col-span-2" />
                <input v-model="newAddress.numero" placeholder="Número"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
                <input v-model="newAddress.complemento" placeholder="Complemento"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
                <input v-model="newAddress.bairro" placeholder="Bairro"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
                <input v-model="newAddress.cidade" placeholder="Cidade"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
                <input v-model="newAddress.estado" placeholder="Estado (UF)" maxlength="2"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
                <input v-model="newAddress.apelidoEndereco" placeholder="Apelido (ex: Casa)"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
              </div>
              <div class="flex gap-3">
                <button @click="saveNewAddress" :disabled="savingAddress"
                  class="flex-1 bg-oliva text-white py-3 rounded-xl font-bold text-sm hover:bg-terra transition-colors disabled:opacity-50">
                  {{ savingAddress ? 'Salvando...' : 'Salvar e Usar' }}
                </button>
                <button @click="showNewAddressForm = false"
                  class="px-6 py-3 border border-areia rounded-xl text-sm text-stone-500 hover:border-oliva/50 transition-colors">
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        </section>

        <!-- Step 2: Payment Method -->
        <section>
          <h2 class="text-xl font-serif mb-6 flex items-center">
            <span class="w-8 h-8 rounded-full bg-areia/20 text-oliva flex items-center justify-center mr-3 text-sm font-bold">2</span>
            Método de Pagamento
          </h2>

          <div class="space-y-3">
            <div
              v-for="card in cartoes"
              :key="card.id"
              class="border-2 rounded-2xl overflow-hidden transition-all"
              :class="isCardSelected(card.id) ? 'border-oliva' : 'border-areia hover:border-oliva/40'"
            >
              <div class="p-4 flex items-center justify-between cursor-pointer" @click="toggleCard(card.id)">
                <div class="flex items-center gap-3">
                  <div class="w-4 h-4 rounded-full border-2 shrink-0 transition-all"
                    :class="isCardSelected(card.id) ? 'border-oliva bg-oliva' : 'border-stone-300'" />
                  <div>
                    <p class="text-sm font-medium text-grafite">{{ card.apelidoCartao }}</p>
                    <p class="text-xs text-stone-400 uppercase">{{ card.bandeira }} •••• {{ formatCardNumber(card.numero) }}</p>
                  </div>
                </div>
                <span v-if="isCardSelected(card.id)" class="text-sm font-bold text-oliva ml-4 shrink-0">
                  R$ {{ getCardAmount(card.id).toFixed(2) }}
                </span>
                <span v-else class="text-xs text-stone-400 font-medium ml-4 shrink-0">+ incluir</span>
              </div>
              <div v-if="isCardSelected(card.id)" @click.stop class="px-4 pb-4 border-t border-areia/50">
                <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mt-3 mb-2">Valor neste cartão</label>
                <input :value="getCardAmount(card.id)" @input="setCardAmount(card.id, $event)"
                  type="number" step="0.01" min="10"
                  class="w-full border border-stone-200 rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
              </div>
            </div>

            <!-- Formulário de novo cartão -->
            <div v-if="!showNewCardForm"
              class="border-2 border-dashed border-areia rounded-2xl p-4 text-center cursor-pointer hover:border-oliva/50 transition-all"
              @click="showNewCardForm = true">
              <p class="text-sm text-stone-400 font-medium">+ Novo cartão</p>
            </div>
            <div v-else class="border-2 border-oliva rounded-2xl p-6 bg-white space-y-4">
              <p class="font-serif text-lg font-bold text-grafite">Novo Cartão</p>
              <div class="grid grid-cols-2 gap-3">
                <input v-model="newCard.numero" placeholder="Número do cartão" maxlength="16"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva col-span-2" />
                <input v-model="newCard.nomeImpresso" placeholder="Nome impresso"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva col-span-2" />
                <input v-model="newCard.cvv" placeholder="CVV" maxlength="4"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
                <input v-model="newCard.validade" placeholder="Validade (YYYY-MM-DD)"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
                <select v-model="newCard.bandeira"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva">
                  <option value="">Bandeira</option>
                  <option value="VISA">Visa</option>
                  <option value="MASTERCARD">Mastercard</option>
                  <option value="ELO">Elo</option>
                  <option value="AMEX">Amex</option>
                </select>
                <input v-model="newCard.apelidoCartao" placeholder="Apelido (ex: Nubank)"
                  class="border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
              </div>
              <div class="flex gap-3">
                <button @click="saveNewCard" :disabled="savingCard"
                  class="flex-1 bg-oliva text-white py-3 rounded-xl font-bold text-sm hover:bg-terra transition-colors disabled:opacity-50">
                  {{ savingCard ? 'Salvando...' : 'Salvar e Usar' }}
                </button>
                <button @click="showNewCardForm = false"
                  class="px-6 py-3 border border-areia rounded-xl text-sm text-stone-500 hover:border-oliva/50 transition-colors">
                  Cancelar
                </button>
              </div>
            </div>

            <p v-if="remainingAmount > 0.01" class="text-xs text-terra font-medium text-center py-1">
              Ainda falta distribuir R$ {{ remainingAmount.toFixed(2) }} — selecione outro cartão
            </p>
          </div>
        </section>

        <button @click="finishOrder" :disabled="!canFinish"
          class="w-full bg-oliva text-white py-5 rounded-2xl font-bold text-lg hover:bg-terra transition-all shadow-xl shadow-oliva/20 disabled:opacity-50 disabled:cursor-not-allowed">
          Confirmar e Pagar R$ {{ finalTotal.toFixed(2) }}
        </button>
      </div>

      <!-- Summary -->
      <aside class="bg-areia/10 p-10 rounded-[40px] h-fit sticky top-32 border border-areia">
        <h2 class="text-2xl font-serif mb-8">Resumo do Pedido</h2>

        <div class="space-y-6 mb-8">
          <div v-for="item in store.cart" :key="item.id" class="flex justify-between items-center">
            <div class="flex items-center">
              <div class="relative">
                <img :src="item.produto.foto" :alt="item.produto.descricao" class="w-12 h-12 rounded-lg object-cover" />
                <span class="absolute -top-2 -right-2 bg-stone-800 text-white text-[10px] w-5 h-5 flex items-center justify-center rounded-full">{{ item.quantity }}</span>
              </div>
              <span class="ml-4 text-sm text-stone-600">{{ item.produto.nome }}</span>
            </div>
            <span class="text-sm font-medium">R$ {{ (item.valorVenda * item.quantity).toFixed(2) }}</span>
          </div>
        </div>

        <!-- Coupons -->
        <div class="mb-6 space-y-4">
          <!-- Cupons disponíveis do cliente -->
          <div v-if="availableCoupons.length" class="space-y-2">
            <p class="text-[10px] uppercase tracking-widest text-stone-400 font-bold">Seus cupons</p>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="c in availableCoupons"
                :key="c.id"
                @click="applyAvailable(c)"
                :disabled="appliedCoupons.some(a => a.codigo === c.codigo)"
                class="px-3 py-1.5 rounded-full border text-xs font-bold transition-all"
                :class="appliedCoupons.some(a => a.codigo === c.codigo)
                  ? 'border-oliva bg-oliva/10 text-oliva opacity-50 cursor-not-allowed'
                  : 'border-areia text-stone-600 hover:border-oliva hover:text-oliva'"
              >
                {{ c.codigo }}
                <span class="ml-1 text-stone-400">
                  ({{ c.tipo === 'PERCENT' ? `-${c.valor}%` : `-R$ ${c.valor.toFixed(2)}` }})
                </span>
              </button>
            </div>
          </div>

          <div class="flex space-x-2">
            <input v-model="couponCode" type="text" placeholder="Código do cupom"
              class="flex-grow border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
            <button @click="applyCoupon"
              class="bg-grafite text-white px-4 py-2 rounded-xl text-xs font-bold hover:bg-black transition-colors">Aplicar</button>
          </div>
          <div v-for="(c, idx) in appliedCoupons" :key="idx"
            class="flex justify-between items-center bg-oliva/10 px-4 py-2 rounded-xl">
            <div>
              <span class="text-xs font-bold text-oliva">{{ c.codigo }}</span>
              <span class="text-xs text-stone-500 ml-2">
                ({{ c.tipo === 'PERCENT' ? `-${c.valor}%` : `-R$ ${c.valor.toFixed(2)}` }})
              </span>
            </div>
            <button @click="removeCoupon(idx)" class="text-oliva hover:text-terra">
              <XIcon :size="14" />
            </button>
          </div>
        </div>

        <!-- Shipping CEP -->
        <div class="mb-8 space-y-3">
          <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold">CEP para Entrega</label>
          <div class="flex space-x-2">
            <input v-model="freteCEP" type="text" placeholder="00000-000" maxlength="9"
              @input="onCEPInput"
              class="flex-grow border border-areia rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-oliva" />
            <button @click="calcularFrete" :disabled="freteCalculando"
              class="bg-grafite text-white px-4 py-2 rounded-xl text-xs font-bold hover:bg-black transition-colors disabled:opacity-50">
              {{ freteCalculando ? '...' : 'Calcular' }}
            </button>
          </div>
          <p v-if="freteStatus" :class="shippingCost === 0 ? 'text-oliva' : 'text-stone-500'" class="text-xs font-medium">
            {{ freteStatus }}
          </p>
        </div>

        <div class="border-t border-stone-200 pt-6 space-y-4">
          <div class="flex justify-between text-stone-500 text-sm">
            <span>Subtotal</span>
            <span>R$ {{ subtotal.toFixed(2) }}</span>
          </div>
          <div v-if="discount > 0" class="flex justify-between text-terra text-sm">
            <span>Desconto</span>
            <span>- R$ {{ discount.toFixed(2) }}</span>
          </div>
          <div class="flex justify-between text-stone-500 text-sm">
            <span>Frete ({{ freteCEP || 'Não informado' }})</span>
            <span :class="shippingCost === 0 && freteCEP ? 'text-oliva font-medium' : ''">
              {{ freteCEP && freteCalculado ? (shippingCost === 0 ? 'Grátis' : 'R$ ' + shippingCost.toFixed(2)) : 'R$ -' }}
            </span>
          </div>
          <div class="flex justify-between text-xl font-serif pt-4">
            <span>Total</span>
            <span class="text-oliva font-bold">R$ {{ finalTotal.toFixed(2) }}</span>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, reactive, onBeforeMount } from 'vue';
import { useRouter } from 'vue-router';
import { CheckCircle as CheckCircleIcon, X as XIcon } from 'lucide-vue-next';
import { store } from '../../store';
import apiClient from '@/plugins/axios';

const router = useRouter();

const clienteId = store.currentUser?.cliente?.id;

// Endereços e cartões locais (iniciados do store, atualizados ao adicionar novo)
const enderecos = ref<any[]>([...(store.currentUser?.cliente?.enderecos ?? [])]);
const cartoes = ref<any[]>([...(store.currentUser?.cliente?.cartoes ?? [])]);

const selectedAddress = ref<any>(enderecos.value[0] ?? null);
const couponCode = ref('');
const appliedCoupons = ref<any[]>([]);
const availableCoupons = ref<any[]>([]);
const payments = ref([{ cardId: cartoes.value[0]?.id ?? null, amount: 0 }]);

const freteCEP = ref(selectedAddress.value?.cep ?? '');
const freteCalculando = ref(false);
const freteCalculado = ref(!!selectedAddress.value?.cep);
const freteStatus = ref('');
const dadosViaCEP = ref<any>(null);

// Novo endereço
const showNewAddressForm = ref(false);
const savingAddress = ref(false);
const buscandoCEP = ref(false);
const newAddress = reactive({
  cep: '', logradouro: '', tipoLogradouro: 'Rua', numero: '', bairro: '',
  cidade: '', estado: '', pais: 'Brasil', complemento: '',
  apelidoEndereco: 'Entrega', tipoResidencia: 'CASA', observacao: '',
  favorito: false, cobranca: false
});

// Novo cartão
const showNewCardForm = ref(false);
const savingCard = ref(false);
const newCard = reactive({
  numero: '', nomeImpresso: '', cvv: '', bandeira: '', validade: '', apelidoCartao: '', favorito: false
});

const isMogi = (cep: string) => cep.replace(/\D/g, '').startsWith('087');

const subtotal = computed(() => store.cart.reduce((acc, item) => acc + (item.valorVenda * item.quantity), 0));

const shippingCost = computed(() => {
  if (!freteCEP.value || !freteCalculado.value) return 0;
  return isMogi(freteCEP.value) ? 0 : 15;
});

const discount = computed(() => {
  return appliedCoupons.value.reduce((acc, c) => {
    if (c.tipo === 'PERCENT') return acc + (subtotal.value * (c.valor / 100));
    return acc + c.valor;
  }, 0);
});

const finalTotal = computed(() => Math.max(0, subtotal.value - discount.value + shippingCost.value));

const remainingAmount = computed(() => {
  const paid = payments.value.reduce((acc, p) => acc + (p.amount || 0), 0);
  return Math.max(0, finalTotal.value - paid);
});

const canFinish = computed(() => {
  const paid = payments.value.reduce((acc, p) => acc + (p.amount || 0), 0);
  const allCardsOk = payments.value.every(p => p.cardId && p.amount >= 10);
  return freteCalculado.value && Math.abs(paid - finalTotal.value) < 0.01 && allCardsOk;
});

watch(finalTotal, (newTotal) => {
  if (payments.value.length === 1) payments.value[0].amount = newTotal;
}, { immediate: true });

const selectAddress = (addr: any) => {
  selectedAddress.value = addr;
  freteCEP.value = addr.cep ?? '';
  dadosViaCEP.value = null;
  if (addr.cep) {
    freteCalculado.value = true;
    freteStatus.value = isMogi(addr.cep) ? 'Frete Grátis para Mogi das Cruzes!' : 'Frete: R$ 15,00';
  } else {
    freteCalculado.value = false;
    freteStatus.value = '';
  }
};

const onCEPInput = () => {
  freteCalculado.value = false;
  freteStatus.value = '';
  dadosViaCEP.value = null;
};

const calcularFrete = async () => {
  const cepNorm = freteCEP.value.replace(/\D/g, '');
  if (cepNorm.length !== 8) { freteStatus.value = 'CEP inválido. Digite 8 dígitos.'; return; }
  freteCalculando.value = true;
  freteStatus.value = '';
  dadosViaCEP.value = null;
  try {
    const res = await fetch(`https://viacep.com.br/ws/${cepNorm}/json/`);
    const data = await res.json();
    if (data.erro) { freteStatus.value = 'CEP não encontrado.'; freteCalculado.value = false; return; }
    dadosViaCEP.value = data;
    freteCalculado.value = true;
    freteStatus.value = isMogi(cepNorm) ? 'Frete Grátis para Mogi das Cruzes!' : 'Frete: R$ 15,00';
  } catch {
    freteStatus.value = 'Erro ao consultar CEP. Tente novamente.';
    freteCalculado.value = false;
  } finally {
    freteCalculando.value = false;
  }
};

const buscarCEP = async () => {
  const cepNorm = newAddress.cep.replace(/\D/g, '');
  if (cepNorm.length !== 8) return;
  buscandoCEP.value = true;
  try {
    const res = await fetch(`https://viacep.com.br/ws/${cepNorm}/json/`);
    const data = await res.json();
    if (!data.erro) {
      newAddress.logradouro = data.logradouro || '';
      newAddress.tipoLogradouro = data.logradouro?.split(' ')?.[0] || 'Rua';
      newAddress.bairro = data.bairro || '';
      newAddress.cidade = data.localidade || '';
      newAddress.estado = data.uf || '';
    }
  } catch { /* silently ignore */ } finally {
    buscandoCEP.value = false;
  }
};

const saveNewAddress = async () => {
  if (!newAddress.cep || !newAddress.logradouro || !newAddress.numero) {
    alert('Preencha CEP, logradouro e número.');
    return;
  }
  savingAddress.value = true;
  try {
    const res = await apiClient.post('/enderecos', {
      ...newAddress,
      cliente: { id: clienteId }
    });
    const saved = res.data;
    enderecos.value.push(saved);
    store.currentUser.cliente.enderecos.push(saved);
    selectAddress(saved);
    showNewAddressForm.value = false;
    Object.assign(newAddress, {
      cep: '', logradouro: '', numero: '', bairro: '', cidade: '', estado: '',
      complemento: '', apelidoEndereco: 'Entrega'
    });
  } catch {
    alert('Erro ao salvar endereço. Tente novamente.');
  } finally {
    savingAddress.value = false;
  }
};

const saveNewCard = async () => {
  if (!newCard.numero || !newCard.nomeImpresso || !newCard.cvv || !newCard.validade || !newCard.bandeira) {
    alert('Preencha todos os campos do cartão.');
    return;
  }
  savingCard.value = true;
  try {
    const res = await apiClient.post('/cartoes', {
      ...newCard,
      cliente: { id: clienteId }
    });
    const saved = res.data;
    cartoes.value.push(saved);
    store.currentUser.cliente.cartoes.push(saved);
    // Seleciona o novo cartão automaticamente
    payments.value.push({ cardId: saved.id, amount: remainingAmount.value });
    showNewCardForm.value = false;
    Object.assign(newCard, { numero: '', nomeImpresso: '', cvv: '', bandeira: '', validade: '', apelidoCartao: '' });
  } catch {
    alert('Erro ao salvar cartão. Tente novamente.');
  } finally {
    savingCard.value = false;
  }
};

const applyAvailable = (cupom: any) => {
  if (appliedCoupons.value.some(a => a.codigo === cupom.codigo)) return;
  const promoCount = appliedCoupons.value.filter(c => !c.ehTroca).length;
  if (!cupom.ehTroca && promoCount >= 1) {
    alert('Apenas um cupom promocional é permitido por compra.');
    return;
  }
  appliedCoupons.value.push(cupom);
};

const applyCoupon = async () => {
  const code = couponCode.value.trim().toUpperCase();
  if (!code) return;

  if (appliedCoupons.value.some(c => c.codigo === code)) {
    alert('Este cupom já foi aplicado.');
    return;
  }

  try {
    const res = await apiClient.get(`/cupons/codigo/${code}`);
    const cupom = res.data;

    if (cupom.clienteId && cupom.clienteId !== clienteId) {
      alert('Este cupom não pertence a você.');
      return;
    }

    const promoCount = appliedCoupons.value.filter(c => !c.ehTroca).length;
    if (!cupom.ehTroca && promoCount >= 1) {
      alert('Apenas um cupom promocional é permitido por compra.');
      return;
    }

    appliedCoupons.value.push(cupom);
    couponCode.value = '';
  } catch {
    alert('Cupom inválido ou expirado.');
  }
};

const removeCoupon = (idx: number) => {
  appliedCoupons.value.splice(idx, 1);
};

const formatCardNumber = (n: string) => n?.slice(-4) ?? '****';

const isCardSelected = (cardId: number) => payments.value.some(p => p.cardId === cardId);
const getCardAmount = (cardId: number) => payments.value.find(p => p.cardId === cardId)?.amount ?? 0;

const setCardAmount = (cardId: number, event: Event) => {
  const p = payments.value.find(p => p.cardId === cardId);
  if (p) p.amount = parseFloat((event.target as HTMLInputElement).value) || 0;
};

const toggleCard = (cardId: number) => {
  const idx = payments.value.findIndex(p => p.cardId === cardId);
  if (idx >= 0) {
    if (payments.value.length > 1) payments.value.splice(idx, 1);
  } else {
    payments.value.push({ cardId, amount: remainingAmount.value });
  }
};

onBeforeMount(async () => {
  try {
    const res = await apiClient.get(`/cupons?clienteId=${clienteId}`);
    availableCoupons.value = res.data;
  } catch { /* sem cupons disponíveis */ }
});

const finishOrder = async () => {
  let enderecoId: number | null = selectedAddress.value?.id ?? null;

  const payload: any = {
    cliente: { id: clienteId },
    itens: store.cart.map(item => ({ produto: { id: item.produto.id }, quantidade: item.quantity })),
    frete: shippingCost.value,
    pagamentos: payments.value.map(p => ({ cartao: { id: p.cardId }, valor: p.amount })),
    cupons: appliedCoupons.value.map(c => ({ codigo: c.codigo }))
  };

  if (enderecoId) payload.endereco = { id: enderecoId };

  await apiClient.post('/vendas', payload);
  store.cart = [];
  router.push('/pedidos');
};
</script>
