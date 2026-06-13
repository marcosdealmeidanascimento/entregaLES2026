<template>
  <div class="space-y-8">
    <!-- Tabs/Steps Indicator -->
    <div class="flex border-b border-areia">
      <button v-for="step in steps" :key="step.id" @click="activeStep = step.id"
        class="px-6 py-4 text-sm font-medium transition-all border-b-2"
        :class="activeStep === step.id ? 'border-oliva text-oliva' : 'border-transparent text-stone-400'">
        {{ step.label }}
      </button>
    </div>

    <!-- Step 1: Personal Data -->
    <div v-if="activeStep === 1" class="space-y-6">
      <h2 class="text-2xl font-serif">Dados Pessoais</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="md:col-span-2">
          <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Nome
            Completo</label>
          <input v-model="formData.nomeCompleto" type="text" required
            class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
        </div>
        <div>
          <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">E-mail</label>
          <input v-model="formData.email" type="text" required
            class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
        </div>
        <div>
          <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Senha</label>
          <input v-model="formData.senha" type="password" required
            class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
        </div>
        <div>
          <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">CPF</label>
          <InputMask v-model="formData.cpf" type="text" required mask="999.999.999-99"
            class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
        </div>
        <div>
          <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Data de
            Nascimento</label>
          <input v-model="formData.dataNascimento" type="date" required
            class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
        </div>
        <div>
          <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Gênero</label>
          <select v-model="formData.genero" required
            class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva">
            <option value="Masculino">Masculino</option>
            <option value="Feminino">Feminino</option>
            <option value="Outro">Outro</option>
          </select>
        </div>
        <div class="grid grid-cols-3 gap-2">
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">DDD</label>
            <InputMask v-model="formData.telefoneDDD" type="text" required mask="(99)"
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div class="col-span-2">
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Número</label>
            <InputMask v-model="formData.telefone" type="text" required mask="99999-9999"
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Tipo de
              Telefone</label>
            <select v-model="formData.tipoTelefone" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva">
              <option value="Residencial">Residencial</option>
              <option value="Comercial">Comercial</option>
              <option value="Outro">Outro</option>
            </select>
          </div>
        </div>
      </div>
      <div class="flex justify-end">
        <button type="button" @click="activeStep = 2"
          class="bg-oliva text-white px-8 py-4 rounded-2xl text-sm font-medium hover:bg-terra transition-all">Próximo:
          Endereços</button>
      </div>
    </div>

    <!-- Step 2: Addresses -->
    <div v-if="activeStep === 2" class="space-y-6">
      <div class="flex justify-between items-center">
        <h2 class="text-2xl font-serif">Endereços</h2>
        <button type="button" @click="addAddress" class="text-oliva text-sm font-medium">+ Adicionar Endereço</button>
      </div>

      <div v-for="(addr, index) in formData.addresses" :key="index"
        class="p-6 border border-areia rounded-3xl space-y-4 relative">
        <button v-if="formData.addresses.length > 1" @click="removeAddress(index)"
          class="absolute top-4 right-4 text-red-400 hover:text-red-600">Remover</button>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="md:col-span-3 flex space-x-4">
            <label class="flex items-center text-xs text-stone-500">
              <input type="checkbox" v-model="addr.favorito" class="mr-2" /> Favorito
            </label>
            <label class="flex items-center text-xs text-stone-500">
              <input type="checkbox" v-model="addr.cobranca" class="mr-2" /> Cobrança
            </label>
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">CEP</label>
            <InputMask v-model="addr.cep" type="text" required @change="getCEPInfo(addr.cep, index)" mask="99999-999"
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div class="md:col-span-2">
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Logradouro</label>
            <input v-model="addr.logradouro" type="text" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Tipo de
              Logradouro</label>
            <select v-model="addr.tipoLogradouro" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva">
              <option value="rua">Rua</option>
              <option value="avenida">Avenida</option>
              <option value="travessa">Travessa</option>
            </select>
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Número</label>
            <input v-model="addr.numero" type="text" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Bairro</label>
            <input v-model="addr.bairro" type="text" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Cidade</label>
            <input v-model="addr.cidade" type="text" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Complemento</label>
            <input v-model="addr.complemento" type="text" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Estado</label>
            <input v-model="addr.estado" type="text" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">País</label>
            <input v-model="addr.pais" type="text" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Tipo de
              Residência</label>
            <select v-model="addr.tipoResidencia" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva">
              <option value="Casa">Casa</option>
              <option value="Apartamento">Apartamento</option>
              <option value="Terreno">Terreno</option>
            </select>
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Observações</label>
            <input v-model="addr.observacao" type="text" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Apelido</label>
            <input v-model="addr.apelidoEndereco" type="text" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
        </div>
      </div>

      <div class="flex justify-between">
        <button type="button" @click="activeStep = 1" class="text-stone-400 hover:text-grafite">Voltar</button>
        <button type="button" @click="activeStep = 3"
          class="bg-oliva text-white px-8 py-4 rounded-2xl text-sm font-medium hover:bg-terra transition-all">Próximo:
          Cartões</button>
      </div>
    </div>

    <!-- Step 3: Cards -->
    <div v-if="activeStep === 3" class="space-y-6">
      <div class="flex justify-between items-center">
        <h2 class="text-2xl font-serif">Cartões de Crédito</h2>
        <button type="button" @click="addCard" class="text-oliva text-sm font-medium">+ Adicionar Cartão</button>
      </div>

      <div v-for="(card, index) in formData.cards" :key="index"
        class="p-6 border border-areia rounded-3xl space-y-4 relative">
        <button v-if="formData.cards.length > 1" @click="removeCard(index)"
          class="absolute top-4 right-4 text-red-400 hover:text-red-600">Remover</button>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="md:col-span-2">
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Número do
              Cartão</label>
            <InputMask v-model="card.numero" type="text" required mask="9999 9999 9999 9999"
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Nome
              Impresso</label>
            <input v-model="card.nomeImpresso" type="text" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Bandeira</label>
              <select v-model="card.bandeira" required
                class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva">
                <option value="visa">Visa</option>
                <option value="mastercard">Mastercard</option>
                <option value="elo">Elo</option>
              </select>
            </div>
            <div>
              <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">CVV</label>
              <InputMask v-model="card.cvv" type="text" required mask="999"
                class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
            </div>
            <div>
              <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Validade</label>
              <input v-model="card.validade" type="month" required
                class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
            </div>
          </div>
          <div>
            <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Apelido do
              Cartão</label>
            <input v-model="card.apelidoCartao" type="text" required
              class="w-full border border-stone-200 rounded-2xl px-5 py-3 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva" />
          </div>
        </div>
      </div>

      <div class="flex justify-between">
        <button type="button" @click="activeStep = 2" class="text-stone-400 hover:text-grafite">Voltar</button>
        <button v-if="isAdminMode" @click="handleSubmit"
          class="bg-oliva text-white px-12 py-4 rounded-2xl font-bold hover:bg-terra transition-all shadow-lg shadow-oliva/20">
          {{ initialData?.id ? 'Salvar Alterações' : 'Finalizar Cadastro' }}
        </button>
        <button v-else-if="!store.currentUser?.cliente?.id" @click="handleSubmit"
          class="bg-oliva text-white px-12 py-4 rounded-2xl font-bold hover:bg-terra transition-all shadow-lg shadow-oliva/20">Finalizar
          Cadastro</button>
        <button v-else @click="handleUpdate"
          class="bg-oliva text-white px-12 py-4 rounded-2xl font-bold hover:bg-terra transition-all shadow-lg shadow-oliva/20">Atualizar
          Cadastro</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import axios from 'axios';
import InputMask from 'primevue/inputmask';
import { store } from '../store';
import { ref, reactive, computed, watch, onBeforeMount } from 'vue';
import apiClient from '@/plugins/axios';

const clienteId = store.currentUser?.cliente?.id;

const props = defineProps<{
  initialData?: any;
}>();

const emit = defineEmits(['submit']);


const deletedAddressIds = ref<number[]>([]);
const deletedCardIds = ref<number[]>([]);

// Admin mode: when initialData prop is provided, we're editing a customer from admin panel
const isAdminMode = computed(() => props.initialData !== undefined);

const activeStep = ref(1);
const steps = [
  { id: 1, label: 'Pessoal' },
  { id: 2, label: 'Endereços' },
  { id: 3, label: 'Cartões' }
];

const formData = reactive({
  nomeCompleto: "",
  genero: "",
  cpf: "",
  email: "",
  telefone: "",
  tipoTelefone: "",
  telefoneDDD: "",
  dataNascimento: "",
  senha: "",
  addresses: [
    {
      cep: "",
      tipoResidencia: "",
      logradouro: "",
      tipoLogradouro: "",
      numero: "",
      bairro: "",
      cidade: "",
      estado: "",
      pais: "",
      complemento: "",
      apelidoEndereco: "",
      observacao: "",
      favorito: true,
      cobranca: true,
    }
  ],
  cards: [
    {
      numero: "",
      nomeImpresso: "",
      cvv: "",
      bandeira: "",
      validade: "",
      apelidoCartao: "",
      favorito: false,
    }
  ]
});

const emptyAddress = () => ({
  cep: '', tipoResidencia: '', logradouro: '', tipoLogradouro: '',
  numero: '', bairro: '', cidade: '', estado: '', pais: '',
  complemento: '', apelidoEndereco: '', observacao: '', favorito: true, cobranca: true
});

const emptyCard = () => ({
  numero: '', nomeImpresso: '', cvv: '', bandeira: '',
  validade: '', apelidoCartao: '', favorito: false
});

watch(() => props.initialData, (data) => {
  if (!isAdminMode.value || data === undefined) return;
  formData.nomeCompleto = data.nomeCompleto ?? '';
  formData.genero = data.genero ?? '';
  formData.cpf = data.cpf ?? '';
  formData.email = data.email ?? '';
  formData.telefone = data.telefone ?? '';
  formData.tipoTelefone = data.tipoTelefone ?? '';
  formData.telefoneDDD = data.telefoneDDD ?? '';
  formData.dataNascimento = data.dataNascimento ?? '';
  formData.senha = data.senha ?? '';
  formData.addresses = data.enderecos?.length ? data.enderecos : [emptyAddress()];
  formData.cards = data.cartoes?.length
    ? data.cartoes.map((c: any) => ({ ...c, validade: c.validade ? c.validade.substring(0, 7) : '' }))
    : [emptyCard()];
  deletedAddressIds.value = [];
  deletedCardIds.value = [];
  activeStep.value = 1;
}, { immediate: true });

const addAddress = () => {
  formData.addresses.push({
    cep: '',
    tipoResidencia: '',
    logradouro: '',
    tipoLogradouro: '',
    numero: '',
    bairro: '',
    cidade: '',
    estado: '',
    pais: '',
    complemento: '',
    apelidoEndereco: '',
    observacao: '',
    favorito: false,
    cobranca: false
  });
};

const removeAddress = (index: number) => {
  const addr = formData.addresses[index];
  if ((addr as any).id) deletedAddressIds.value.push((addr as any).id);
  formData.addresses.splice(index, 1);
};

const getCEPInfo = (cep, index) => {
  if (cep.length < 8) return;

  setTimeout(() => {
    axios.get(`https://brasilapi.com.br/api/cep/v1/${cep}`).then((response) => {
      formData.addresses[index].logradouro = response.data.street;
      formData.addresses[index].bairro = response.data.neighborhood;
      formData.addresses[index].cidade = response.data.city;
      formData.addresses[index].estado = response.data.state;
      formData.addresses[index].pais = "Brasil";
    });
  }, 2000);
}

const addCard = () => {
  formData.cards.push({
    numero: '',
    nomeImpresso: '',
    cvv: '',
    bandeira: '',
    validade: '',
    apelidoCartao: '',
    favorito: false
  });
};

const removeCard = (index: number) => {
  const card = formData.cards[index];
  if ((card as any).id) deletedCardIds.value.push((card as any).id);
  formData.cards.splice(index, 1);
};

const getCliente = async () => {
  if (!clienteId) return;
  const response = await apiClient.get(`/clientes/${clienteId}`);
  formData.nomeCompleto = response.data.nomeCompleto;
  formData.genero = response.data.genero;
  formData.cpf = response.data.cpf;
  formData.email = response.data.email;
  formData.telefone = response.data.telefone;
  formData.tipoTelefone = response.data.tipoTelefone;
  formData.telefoneDDD = response.data.telefoneDDD;
  formData.dataNascimento = response.data.dataNascimento;
  formData.senha = response.data.senha;

  formData.addresses = response.data.enderecos;
  formData.cards = response.data.cartoes.map((c: any) => ({
    ...c,
    validade: c.validade ? c.validade.substring(0, 7) : ''
  }));

  activeStep.value = 1;
};

const handleSubmit = () => {
  emit('submit', {
    ...formData,
    id: props.initialData?.id,
    deletedAddressIds: deletedAddressIds.value,
    deletedCardIds: deletedCardIds.value,
  });
};

const handleUpdate = async () => {
  try {
    await apiClient.put(`/clientes/${clienteId}`, {
      nomeCompleto: formData.nomeCompleto,
      genero: formData.genero,
      cpf: formData.cpf,
      email: formData.email,
      telefone: formData.telefone,
      tipoTelefone: formData.tipoTelefone,
      telefoneDDD: formData.telefoneDDD,
      dataNascimento: formData.dataNascimento,
      senha: formData.senha,
    });

    for (const addr of formData.addresses) {
      const payload = {
        cep: addr.cep, tipoResidencia: addr.tipoResidencia,
        logradouro: addr.logradouro, tipoLogradouro: addr.tipoLogradouro,
        numero: addr.numero, bairro: addr.bairro, cidade: addr.cidade,
        estado: addr.estado, pais: addr.pais, complemento: addr.complemento,
        apelidoEndereco: addr.apelidoEndereco, observacao: addr.observacao,
        favorito: addr.favorito, cobranca: addr.cobranca,
        cliente: { id: clienteId },
      };
      (addr as any).id
        ? await apiClient.put(`/enderecos/${(addr as any).id}`, payload)
        : await apiClient.post('/enderecos', payload);
    }

    for (const id of deletedAddressIds.value) {
      await apiClient.delete(`/enderecos/${id}`);
    }

    for (const card of formData.cards) {
      const validade = card.validade.length === 7 ? card.validade + '-01' : card.validade;
      const payload = {
        numero: card.numero, nomeImpresso: card.nomeImpresso,
        cvv: card.cvv, bandeira: card.bandeira, validade,
        apelidoCartao: card.apelidoCartao, favorito: card.favorito,
        cliente: { id: clienteId },
      };
      (card as any).id
        ? await apiClient.put(`/cartoes/${(card as any).id}`, payload)
        : await apiClient.post('/cartoes', payload);
    }

    for (const id of deletedCardIds.value) {
      await apiClient.delete(`/cartoes/${id}`);
    }

    deletedAddressIds.value = [];
    deletedCardIds.value = [];
    await getCliente();
    alert('Cadastro atualizado com sucesso!');
  } catch (error) {
    console.error(error);
    alert('Erro ao atualizar cadastro.');
  }
};

onBeforeMount(() => {
  getCliente();
});
</script>
