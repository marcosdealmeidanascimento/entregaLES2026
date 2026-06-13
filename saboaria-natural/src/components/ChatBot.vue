<template>
  <div class="fixed bottom-6 right-6 z-[9999]">
    <!-- Chat Window -->
    <transition name="slide-up">
      <div v-if="isOpen" class="mb-4 w-80 md:w-96 bg-white rounded-[32px] shadow-2xl border border-areia overflow-hidden flex flex-col h-[500px]">
        <!-- Header -->
        <div class="bg-oliva p-6 text-white flex justify-between items-center">
          <div class="flex items-center">
            <div class="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center mr-3">
              <BotIcon :size="24" />
            </div>
            <div>
              <h3 class="font-serif font-bold text-lg leading-none">Assistente Natural</h3>
              <p class="text-[10px] opacity-80 uppercase tracking-widest mt-1">Sempre online</p>
            </div>
          </div>
          <button @click="isOpen = false" class="hover:bg-white/10 p-2 rounded-full transition-colors">
            <XIcon :size="20" />
          </button>
        </div>

        <!-- Messages -->
        <div ref="messageContainer" class="flex-grow overflow-y-auto p-6 space-y-4 bg-linho/30">
          <div v-for="(msg, idx) in messages" :key="idx" :class="['flex', msg.role === 'user' ? 'justify-end' : 'justify-start']">
            <div 
              :class="[
                'max-w-[80%] p-4 rounded-2xl text-sm leading-relaxed',
                msg.role === 'user' ? 'bg-oliva text-white rounded-tr-none' : 'bg-white text-grafite border border-areia rounded-tl-none shadow-sm'
              ]"
            >
              <p v-if="!msg.isTyping" v-html="renderText(msg.text)"></p>
              <div v-else class="flex space-x-1 py-1">
                <div class="w-1.5 h-1.5 bg-stone-400 rounded-full animate-bounce"></div>
                <div class="w-1.5 h-1.5 bg-stone-400 rounded-full animate-bounce [animation-delay:0.2s]"></div>
                <div class="w-1.5 h-1.5 bg-stone-400 rounded-full animate-bounce [animation-delay:0.4s]"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="p-4 bg-white border-t border-areia">
          <form @submit.prevent="sendMessage" class="flex items-center space-x-2">
            <input 
              v-model="userInput" 
              type="text" 
              placeholder="Como posso ajudar?"
              class="flex-grow border border-stone-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-oliva/20 focus:border-oliva"
              :disabled="isLoading"
            />
            <button
              type="submit"
              class="bg-oliva text-white p-3 rounded-xl hover:bg-terra transition-colors disabled:opacity-50 min-w-[44px] flex items-center justify-center"
              :disabled="!userInput.trim() || isLoading || cooldown > 0"
            >
              <span v-if="cooldown > 0" class="text-xs font-bold leading-none">{{ cooldown }}s</span>
              <SendIcon v-else :size="18" />
            </button>
          </form>
        </div>
      </div>
    </transition>

    <!-- Toggle Button -->
    <button 
      @click="isOpen = !isOpen"
      class="w-16 h-16 bg-oliva text-white rounded-full shadow-xl shadow-oliva/30 flex items-center justify-center hover:scale-110 transition-all active:scale-95 group"
    >
      <BotIcon v-if="!isOpen" :size="32" class="group-hover:rotate-12 transition-transform" />
      <XIcon v-else :size="32" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue';
import { Bot as BotIcon, X as XIcon, Send as SendIcon } from 'lucide-vue-next';
import apiClient from '@/plugins/axios';
import { store } from '@/store';

const COOLDOWN_SECONDS = 15;

const isOpen = ref(false);
const userInput = ref('');
const isLoading = ref(false);
const cooldown = ref(0);
const messageContainer = ref<HTMLElement | null>(null);
const historico = ref<{ role: string; content: string }[]>([]);

const renderText = (text: string) =>
  text.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');

const startCooldown = () => {
  cooldown.value = COOLDOWN_SECONDS;
  const interval = setInterval(() => {
    cooldown.value--;
    if (cooldown.value <= 0) clearInterval(interval);
  }, 1000);
};

const messages = ref([
  { role: 'bot', text: 'Olá! Sou seu assistente da Saboaria Natural. Como posso ajudar você hoje?' }
]);

const scrollToBottom = async () => {
  await nextTick();
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
};

watch(messages, scrollToBottom, { deep: true });

const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value || cooldown.value > 0) return;

  const text = userInput.value;
  userInput.value = '';
  messages.value.push({ role: 'user', text });

  isLoading.value = true;
  messages.value.push({ role: 'bot', text: '', isTyping: true });

  const clienteId = store.currentUser?.cliente?.id ?? null;

  try {
    const response = await apiClient.post('/chat', {
      clienteId,
      mensagem: text,
      historico: historico.value,
    });
    const resposta = response.data?.resposta || 'Desculpe, tive um pequeno problema. Pode repetir?';
    messages.value.pop();
    messages.value.push({ role: 'bot', text: resposta });
    historico.value.push({ role: 'user', content: text });
    historico.value.push({ role: 'model', content: resposta });
  } catch (error) {
    console.error('Chat error:', error);
    messages.value.pop();
    messages.value.push({ role: 'bot', text: 'Sinto muito, minhas pétalas murcharam um pouco. Tente novamente em breve.' });
  } finally {
    isLoading.value = false;
    startCooldown();
  }
};
</script>

<style scoped>
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
</style>
