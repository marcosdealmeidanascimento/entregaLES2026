import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { store } from '../store';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('../views/client/ClientLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('../views/client/Home.vue') },
      { path: 'produtos', name: 'products', component: () => import('../views/client/Products.vue') },
      { path: 'carrinho', name: 'cart', component: () => import('../views/client/Cart.vue') },
      { path: 'checkout', name: 'checkout', component: () => import('../views/client/Checkout.vue') },
      { path: 'perfil', name: 'profile', component: () => import('../views/client/Profile.vue') },
      { path: 'pedidos', name: 'client-orders', component: () => import('../views/client/Orders.vue') },
    ]
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/cadastro',
    name: 'register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    children: [
      { path: '', name: 'admin-dashboard', component: () => import('../views/admin/SalesAnalysis.vue') },
      { path: 'estoque', name: 'admin-inventory', component: () => import('../views/admin/Inventory.vue') },
      { path: 'entrada-estoque', name: 'admin-stock-entry', component: () => import('../views/admin/StockEntry.vue') },
      { path: 'clientes', name: 'admin-customers', component: () => import('../views/admin/Customers.vue') },
      { path: 'vendas', name: 'admin-orders', component: () => import('../views/admin/Orders.vue') },
      { path: 'analise', name: 'admin-analysis', component: () => import('../views/admin/SalesAnalysis.vue') },
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const user = store.currentUser;

  if (to.path.startsWith('/admin') && (!user || user.role !== 'ADMIN')) {
    next('/login');
  } else if ((to.name === 'profile' || to.name === 'client-orders' || to.name === 'checkout') && !user) {
    next('/login');
  } else {
    next();
  }
});

export default router;
