import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import PrimeVue from 'primevue/config';
import './index.css';

// PrimeVue Styles (Legacy/Standard for v3)
import 'primevue/resources/themes/lara-light-indigo/theme.css';
import 'primevue/resources/primevue.min.css';
import 'primeicons/primeicons.css';
import ConfirmationService from 'primevue/confirmationservice';

const app = createApp(App);

app.use(router);
app.use(PrimeVue, { ripple: true, unstyled: true });
app.use(ConfirmationService);

app.mount('#root');
