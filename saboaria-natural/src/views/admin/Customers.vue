<template>
  <div class="space-y-8">
    <header class="flex justify-between items-end">
      <div>
        <h1 class="text-3xl font-serif font-bold text-grafite">Gerenciamento de Clientes</h1>
        <p class="text-stone-500">Visualize e cadastre novos clientes da sua saboaria.</p>
      </div>
      <button @click="openNewModal()"
        class="bg-oliva text-white px-6 py-3 rounded-xl text-sm font-medium hover:bg-terra transition-colors">
        + Novo Cliente
      </button>
    </header>

    <!-- Customers Table -->
    <div class="bg-white rounded-[40px] border border-areia shadow-sm overflow-hidden">
      <table class="w-full text-left">
        <thead>
          <tr class="bg-linho border-b border-areia">
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">Nome</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">E-mail</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold">CPF</th>
            <th class="px-8 py-4 text-[10px] uppercase tracking-widest text-stone-400 font-bold text-right">Ações</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-areia/30">
          <tr v-for="customer in customers" :key="customer.id" class="hover:bg-linho/50 transition-colors">
            <td class="px-8 py-6">
              <p class="font-serif font-bold text-grafite">{{ customer.nomeCompleto }}</p>
            </td>
            <td class="px-8 py-6 text-sm text-stone-600">{{ customer.email }}</td>
            <td class="px-8 py-6 text-sm text-stone-600">{{ customer.cpf }}</td>
            <td class="px-8 py-6 text-right">
              <button class="text-stone-300 hover:text-oliva mr-4" @click="editClient(customer.id)">
                <EditIcon :size="18" />
              </button>
              <button class="text-stone-300 hover:text-red-500" @click="deleteClient(customer.id)">
                <TrashIcon :size="18" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div class="flex items-center justify-between px-8 py-4 border-t border-areia/50">
        <span class="text-xs text-stone-400">Página {{ currentPage }}</span>
        <div class="flex gap-2">
          <button
            @click="prevPage"
            :disabled="currentPage === 1"
            class="px-4 py-2 text-sm rounded-xl border border-areia transition-colors"
            :class="currentPage === 1 ? 'text-stone-300 cursor-not-allowed' : 'hover:bg-linho text-grafite'">
            Anterior
          </button>
          <button
            @click="nextPage"
            :disabled="isLastPage"
            class="px-4 py-2 text-sm rounded-xl border border-areia transition-colors"
            :class="isLastPage ? 'text-stone-300 cursor-not-allowed' : 'hover:bg-linho text-grafite'">
            Próximo
          </button>
        </div>
      </div>
    </div>

    <!-- Modal for Registration -->
    <div v-if="showModal" class="fixed inset-0 z-[100] flex items-center justify-center px-4">
      <div class="absolute inset-0 bg-grafite/40 backdrop-blur-sm" @click="showModal = false"></div>
      <div
        class="relative bg-white w-full max-w-4xl p-10 rounded-[40px] shadow-2xl border border-areia overflow-y-auto max-h-[90vh]">
        <div class="flex justify-between items-center mb-8">
          <h2 class="text-3xl font-serif font-bold">{{ client.id ? 'Editar Cliente' : 'Novo Cliente' }}</h2>
          <button @click="showModal = false" class="text-stone-400 hover:text-grafite">
            <XIcon :size="24" />
          </button>
        </div>

        <CustomerForm :initial-data="client" @submit="handleAddCustomer" />
      </div>
    </div>
  </div>

  <ConfirmDialog unstyled :pt="{
    root: 'bg-white rounded-[40px] shadow-2xl border border-areia p-10 max-w-lg w-full fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-[110]',
    header: 'flex items-center justify-between mb-4',
    title: 'text-2xl font-serif font-bold text-grafite',
    content: 'text-stone-500 mb-8',
    icon: 'text-red-500 mr-3 text-2xl',
    footer: 'flex space-x-4 justify-end',
    mask: 'bg-grafite/40 backdrop-blur-sm fixed inset-0 z-[100]'
  }">
  </ConfirmDialog>
</template>

<script setup lang="ts">
import { onBeforeMount, ref, computed } from 'vue';
import { useConfirm } from "primevue/useconfirm";

import apiClient from '@/plugins/axios';
import { Edit2 as EditIcon, Trash2 as TrashIcon, Star as StarIcon, X as XIcon } from 'lucide-vue-next';
import { store } from '../../store';
import CustomerForm from '@/components/CustomerForm.vue';
import ConfirmDialog from 'primevue/confirmdialog';

const showModal = ref(false);

const confirm = useConfirm();
const customers = ref([]);
const client = ref({} as any);

const PAGE_SIZE = 10;
const currentPage = ref(1);
const isLastPage = computed(() => customers.value.length < PAGE_SIZE);

const openNewModal = () => {
  client.value = {};
  showModal.value = true;
};


const editClient = (client_id) => {
  showModal.value = true;

  apiClient.get('/clientes/' + client_id)
    .then(response => {
      client.value = response.data;
    })
    .catch(error => {
      console.error(error);
    });
}

const deleteClient = (client_id) => {
  confirm.require({
    message: 'Você tem certeza que deseja excluir esse cliente?',
    header: 'Danger Zone',
    icon: 'pi pi-info-circle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Deletar',
    rejectClass: 'p-button-secondary p-button-outlined',
    acceptClass: 'p-button-danger',
    accept: () => {
      apiClient.delete('/clientes/' + client_id)
        .then(() => {
          if (customers.value.length === 1 && currentPage.value > 1) currentPage.value--;
          getCustomers();
        })
        .catch(error => {
          console.error(error);
        });
    }
  });
};

const getCustomers = () => {
  const offset = (currentPage.value - 1) * PAGE_SIZE;
  apiClient.get('/clientes', { params: { limit: PAGE_SIZE, offset } })
    .then(response => {
      customers.value = response.data;
    })
    .catch(error => {
      console.error(error);
    });
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    getCustomers();
  }
}

const nextPage = () => {
  if (!isLastPage.value) {
    currentPage.value++;
    getCustomers();
  }
}

const addAddress = (client_id, addresses) => {
  addresses.forEach(addr => {
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
        getCustomers();
      })
      .catch(error => {
        console.error(error);
      });
  });
}


const addCard = (client_id, cards) => {
  cards.forEach(card => {
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
        getCustomers();
      })
      .catch(error => {
        console.error(error);
      });
  });
}

const closeModal = () => {
  showModal.value = false;
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

  try {
    if (data.id) {
      await apiClient.put('/clientes/' + data.id, payloadClient);

      for (const addr of data.addresses) {
        const payload = {
          cep: addr.cep, tipoResidencia: addr.tipoResidencia,
          logradouro: addr.logradouro, tipoLogradouro: addr.tipoLogradouro,
          numero: addr.numero, bairro: addr.bairro, cidade: addr.cidade,
          estado: addr.estado, pais: addr.pais, complemento: addr.complemento,
          apelidoEndereco: addr.apelidoEndereco, observacao: addr.observacao,
          favorito: addr.favorito, cobranca: addr.cobranca,
          cliente: { id: data.id }
        };
        addr.id
          ? await apiClient.put('/enderecos/' + addr.id, payload)
          : await apiClient.post('/enderecos', payload);
      }
      for (const id of (data.deletedAddressIds ?? [])) {
        await apiClient.delete('/enderecos/' + id);
      }

      for (const card of data.cards) {
        const validade = card.validade.length === 7 ? card.validade + '-01' : card.validade;
        const payload = {
          numero: card.numero, nomeImpresso: card.nomeImpresso,
          cvv: card.cvv, bandeira: card.bandeira, validade,
          apelidoCartao: card.apelidoCartao, favorito: card.favorito,
          cliente: { id: data.id }
        };
        card.id
          ? await apiClient.put('/cartoes/' + card.id, payload)
          : await apiClient.post('/cartoes', payload);
      }
      for (const id of (data.deletedCardIds ?? [])) {
        await apiClient.delete('/cartoes/' + id);
      }

      getCustomers();
      closeModal();
    } else {
      const response = await apiClient.post('/clientes', payloadClient);
      const customer = response.data;
      addAddress(customer.id, data.addresses);
      addCard(customer.id, data.cards);
      getCustomers();
      closeModal();
    }
  } catch (error) {
    console.error(error);
  }
};

onBeforeMount(() => {
  getCustomers();
});

</script>
