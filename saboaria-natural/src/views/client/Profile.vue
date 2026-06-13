<template>
  <div class="py-16 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
    <h1 class="text-4xl font-serif mb-12">Seu Perfil</h1>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-12">
      <!-- Sidebar -->
      <nav class="space-y-2">
        <button v-for="tab in tabs" :key="tab.id" @click="activeTab = tab.id"
          class="w-full text-left px-6 py-3 rounded-xl text-sm font-medium transition-all"
          :class="activeTab === tab.id ? 'bg-oliva text-white shadow-lg' : 'text-stone-500 hover:bg-stone-100'">
          {{ tab.label }}
        </button>
        <button @click="handleLogout"
          class="w-full text-left px-6 py-3 rounded-xl text-sm font-medium text-red-400 hover:bg-red-50 transition-all">
          Sair da Conta
        </button>
      </nav>

      <!-- Content -->
      <div class="md:col-span-2 bg-white p-8 rounded-3xl border border-stone-100 shadow-sm">
        <!-- Dados Cadastrais -->
        <div v-if="activeTab === 'data'" class="space-y-6">
          <h2 class="text-2xl font-serif mb-6">Dados Pessoais</h2>
          <CustomerForm @submit="handleAddCustomer" />
        </div>

        <!-- Cupons -->
        <div v-if="activeTab === 'coupons'" class="space-y-6">
          <h2 class="text-2xl font-serif mb-6">Meus Cupons</h2>
          <div v-if="userCoupons.length === 0" class="text-center py-12 text-stone-400">
            Você não possui cupons ativos no momento.
          </div>
          <div v-for="coupon in userCoupons" :key="coupon.code"
            class="p-6 rounded-2xl border-2 border-dashed border-stone-200 bg-stone-50 flex justify-between items-center">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="text-lg font-mono font-bold text-grafite">{{ coupon.code }}</span>
                <span v-if="coupon.isExchange"
                  class="text-[10px] bg-oliva/10 text-oliva px-2 py-0.5 rounded uppercase font-bold">Troca</span>
              </div>
              <p class="text-stone-500 text-sm">
                Desconto de <span class="font-bold text-terra">{{ coupon.type === 'PERCENT' ? coupon.value + '%' : 'R$ '
                  + coupon.value.toFixed(2) }}</span>
              </p>
            </div>
            <button @click="copyToClipboard(coupon.code)"
              class="text-oliva hover:text-terra text-sm font-medium flex items-center gap-1">
              <CopyIcon :size="14" /> Copiar
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Edit2 as EditIcon, Trash2 as TrashIcon, Copy as CopyIcon } from 'lucide-vue-next';
import { store } from '../../store';
import CustomerForm from '@/components/CustomerForm.vue';
import apiClient from '@/plugins/axios';

const router = useRouter();
const activeTab = ref('data');

const userCoupons = computed(() => {
  return store.coupons.filter(c => c.customerId === store.user.id || c.customerId === null);
});


const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text);
  alert('Cupom copiado para a área de transferência!');
};

const handleLogout = () => {
  store.currentUser = null;
  router.push('/login');
};
const tabs = [
  { id: 'data', label: 'Dados Cadastrais' },
  { id: 'coupons', label: 'Cupons' },
];

const addAddress = (client_id, addresses) => {
  addresses.foreach(addr => {
    const payloadAddress = {
      "cep": addr.cep,
      "tipoResidencia": addr.tipoResidencia,
      "logradouro": addr.logradouro,
      "tipoLogradouro": addr.tipoLogradouro,
      "numero": addr.numero,
      "bairro": addr.bairro,
      "cidade": addr.cidade,
      "estado": addr.estado,
      "pais": addr.pais,
      "complemento": addr.complemento,
      "apelidoEndereco": addr.apelidoEndereco,
      "observacao": addr.observacao,
      "favorito": addr.favorito,
      "cobranca": addr.cobranca,
      "cliente": {
        "id": client_id
      }
    }
    apiClient.post('/enderecos', payloadAddress)
      .then(response => {
      })
      .catch(error => {
        console.error(error);
      });
  });
}


const addCard = (client_id, cards) => {
  cards.array.forEach(card => {
    let validade = card.validade + '-01';
    const payloadCard = {
      "numero": card.numero,
      "nomeImpresso": card.nomeImpresso,
      "cvv": card.cvv,
      "bandeira": card.bandeira,
      "validade": validade,
      "apelidoCartao": card.apelidoCartao,
      "favorito": card.favorito,
      "cliente": {
        "id": client_id
      }
    }

    apiClient.post('/cartoes', payloadCard)
      .then(response => {
      })
      .catch(error => {
        console.error(error);
      });
  });
}

const handleAddCustomer = async (data: any) => {
  const payloadClient = {
    "nomeCompleto": data.nomeCompleto,
    "genero": data.genero,
    "cpf": data.cpf,
    "email": data.email,
    "telefone": data.telefone,
    "tipoTelefone": data.tipoTelefone,
    "telefoneDDD": data.telefoneDDD,
    "dataNascimento": data.dataNascimento,
    "senha": data.senha
  }

  apiClient.post('/clientes', payloadClient)
    .then(response => {
      const customer = response.data;
      addAddress(customer.id, data.addresses);
      addCard(customer.id, data.cards);
    })
    .catch(error => {
      console.error(error);
    });
};

</script>
