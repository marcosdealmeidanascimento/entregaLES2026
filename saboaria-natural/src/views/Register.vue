<template>
  <div class="min-h-screen bg-linho flex flex-col items-center justify-center p-6">
    <div class="w-full max-w-4xl bg-white rounded-[40px] shadow-2xl border border-areia overflow-hidden">
      <div class="grid grid-cols-1 md:grid-cols-3">
        <!-- Left Side: Branding -->
        <div class="bg-oliva p-12 text-white flex flex-col justify-between">
          <div>
            <h1 class="text-4xl font-serif font-bold mb-4">Saboaria Natural</h1>
            <p class="text-white/80 text-sm leading-relaxed">
              Junte-se à nossa comunidade e descubra o poder dos cuidados naturais.
            </p>
          </div>
          <div class="space-y-4">
            <div class="flex items-center space-x-3">
              <div class="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center">
                <CheckIcon :size="16" />
              </div>
              <span class="text-xs">Produtos 100% Artesanais</span>
            </div>
            <div class="flex items-center space-x-3">
              <div class="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center">
                <CheckIcon :size="16" />
              </div>
              <span class="text-xs">Ingredientes Botânicos</span>
            </div>
          </div>
        </div>

        <!-- Right Side: Form -->
        <div class="md:col-span-2 p-12">
          <div class="mb-8">
            <h2 class="text-3xl font-serif font-bold text-grafite">Criar Conta</h2>
            <p class="text-stone-500 text-sm">Preencha seus dados para começar.</p>
          </div>

          <CustomerForm @submit="handleRegister" />

          <div class="mt-8 pt-8 border-t border-areia text-center">
            <p class="text-sm text-stone-500">
              Já tem uma conta? 
              <router-link to="/login" class="text-oliva font-bold hover:underline">Fazer Login</router-link>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Check as CheckIcon } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import CustomerForm from '../components/CustomerForm.vue';
import apiClient from '@/plugins/axios';

const router = useRouter();

const handleRegister = async (data: any) => {
  try {
    await apiClient.post('/clientes', {
      nomeCompleto: data.nomeCompleto,
      genero: data.genero,
      cpf: data.cpf,
      email: data.email,
      telefone: data.telefone,
      tipoTelefone: data.tipoTelefone,
      telefoneDDD: data.telefoneDDD,
      dataNascimento: data.dataNascimento,
      senha: data.senha,
      enderecos: data.addresses,
      cartoes: data.cards.map((c: any) => ({
        ...c,
        validade: c.validade.length === 7 ? c.validade + '-01' : c.validade,
      })),
    });
    alert('Cadastro realizado com sucesso! Agora você pode fazer login.');
    router.push('/login');
  } catch (error) {
    console.error(error);
    alert('Erro ao realizar cadastro.');
  }
};
</script>
