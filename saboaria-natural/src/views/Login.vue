<template>
  <div class="min-h-screen flex items-center justify-center bg-linho px-4">
    <div class="max-w-md w-full bg-white p-10 rounded-[40px] shadow-xl shadow-oliva/5 border border-areia">
      <div class="text-center mb-10">
        <h1 class="text-4xl font-serif font-bold text-grafite">Bem-vindo</h1>
        <p class="text-stone-500 mt-2">Acesse sua conta na Saboaria Natural</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-6">
        <div>
          <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">E-mail</label>
          <input 
            v-model="email" 
            type="email" 
            required
            class="w-full border border-stone-200 rounded-2xl px-5 py-4 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva transition-all"
            placeholder="admin@saboaria.com ou cliente@email.com"
          />
        </div>

        <div>
          <label class="block text-[10px] uppercase tracking-widest text-stone-400 font-bold mb-2">Senha</label>
          <input 
            v-model="password" 
            type="password" 
            required
            class="w-full border border-stone-200 rounded-2xl px-5 py-4 focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva transition-all"
            placeholder="••••••••"
          />
        </div>

        <div class="flex items-center justify-between text-xs">
          <label class="flex items-center text-stone-500 cursor-pointer">
            <input type="checkbox" class="mr-2 rounded border-stone-300 text-oliva focus:ring-oliva" />
            Lembrar de mim
          </label>
          <a href="#" class="text-oliva font-medium hover:underline">Esqueceu a senha?</a>
        </div>

        <button 
          type="submit"
          class="w-full bg-oliva text-white py-5 rounded-2xl font-bold text-lg hover:bg-terra transition-all shadow-lg shadow-oliva/20"
        >
          Entrar
        </button>
      </form>

      <div class="mt-10 text-center text-sm text-stone-500">
        Não tem uma conta? 
        <router-link to="/cadastro" class="text-oliva font-bold hover:underline">Cadastre-se</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import apiClient from '@/plugins/axios';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../store';

const router = useRouter();
const email = ref('');
const password = ref('');

const handleLogin = () => {
  if (email.value.includes('admin')) {
    store.currentUser = { id: 0, name: 'Administrador', email: email.value, role: 'ADMIN' };
    router.push('/admin');
  } else {
    const response = apiClient.post("clientes/login", {
      "email": email.value,
      "senha": password.value
    });

    response.then(response => {
      store.currentUser = { cliente: response.data, role: 'CLIENT' };
      localStorage.setItem('cliente', JSON.stringify(response.data));
      router.push('/');
    })
    .catch(error => {
      console.error(error);
    });
  }
};
</script>
