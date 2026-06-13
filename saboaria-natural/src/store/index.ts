import { reactive } from 'vue';
import apiClient from '@/plugins/axios';

const savedCliente = localStorage.getItem('cliente');

export const store = reactive({
  currentUser: savedCliente
    ? { cliente: JSON.parse(savedCliente), role: 'CLIENT' }
    : null as any,
  cart: [] as any[],
  user: {
    id: 1,
    name: 'Maria Silva',
    email: 'maria@exemplo.com',
    addresses: [
      { id: 1, street: 'Rua das Flores, 123', city: 'São Paulo', state: 'SP', zip: '01234-567', default: true }
    ],
    cards: [
      { id: 1, brand: 'Visa', last4: '4242', expiry: '12/26', default: true }
    ]
  },
  customers: [
    { id: 1, name: 'João Silva', email: 'joao@email.com', phone: '(11) 98888-7777', joinDate: '2024-01-15', ranking: 5 },
    { id: 2, name: 'Ana Oliveira', email: 'ana@email.com', phone: '(21) 97777-6666', joinDate: '2024-02-10', ranking: 3 },
  ],
  orders: [
    { 
      id: 'ORD-001', 
      customerId: 1, 
      items: [{ id: 1, name: 'Sabonete de Lavanda', price: 25.90, quantity: 2, image: 'https://picsum.photos/seed/lavender/400/400' }],
      total: 51.80,
      status: 'ENTREGUE',
      date: '2024-02-20',
      address: { street: 'Rua das Flores, 123', city: 'São Paulo', state: 'SP' }
    }
  ],
  reviews: [] as any[],
  coupons: [
    { code: 'TROCA50', value: 50, type: 'FIXED', active: true, isExchange: true, customerId: 1 },
    { code: 'PROMO10', value: 10, type: 'FIXED', active: true, isExchange: false, customerId: null },
  ],
  pricingGroups: [
    { id: 1, name: 'Linha Essencial', margin: 0.3 },
    { id: 2, name: 'Linha Rara', margin: 0.5 }
  ],
  products: [
    { 
      id: 1, 
      name: 'Sabonete de Lavanda', 
      price: 25.90, 
      cost: 15.00,
      pricingGroupId: 1,
      inventory: 50, 
      image: 'https://picsum.photos/seed/lavender/400/400', 
      description: 'Calmante e relaxante.',
      composition: 'Lavanda, Azeite de Oliva',
      olfactiveFamily: 'Floral',
      fabricationDate: '2024-01-01',
      cureTime: 30,
      botanicComposition: 'Lavandula angustifolia',
      properties: 'Relaxante, Antisséptico',
      weight: 100,
      dimensions: '7x5x3cm',
      batch: 'LAV-2024-001',
      gtin: '7891234567890',
      active: true
    },
    { 
      id: 2, 
      name: 'Sabonete de Alecrim', 
      price: 22.50, 
      cost: 12.00,
      pricingGroupId: 1,
      inventory: 35, 
      image: 'https://picsum.photos/seed/rosemary/400/400', 
      description: 'Energizante e refrescante.',
      composition: 'Alecrim, Manteiga de Karité',
      olfactiveFamily: 'Herbal',
      fabricationDate: '2024-01-05',
      cureTime: 30,
      botanicComposition: 'Rosmarinus officinalis',
      properties: 'Energizante, Circulatório',
      weight: 100,
      dimensions: '7x5x3cm',
      batch: 'ALE-2024-001',
      gtin: '7891234567891',
      active: true
    },
    { id: 3, name: 'Sabonete de Argila Verde', price: 28.00, inventory: 20, image: 'https://picsum.photos/seed/clay/400/400', description: 'Desintoxicante e purificante.' },
    { id: 4, name: 'Sabonete de Mel e Aveia', price: 24.00, inventory: 45, image: 'https://picsum.photos/seed/honey/400/400', description: 'Hidratante e esfoliante suave.' },
    { id: 5, name: 'Sabonete de Capim Limão', price: 21.00, inventory: 60, image: 'https://picsum.photos/seed/lemongrass/400/400', description: 'Cítrico e revigorante.' },
    { id: 6, name: 'Sabonete de Calêndula', price: 26.50, inventory: 15, image: 'https://picsum.photos/seed/calendula/400/400', description: 'Suave para peles sensíveis.' },
  ],
  salesData: {
    labels: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun'],
    datasets: [
      {
        label: 'Vendas (R$)',
        data: [1200, 1900, 1500, 2500, 2200, 3000],
        borderColor: '#7A8C6D',
        backgroundColor: 'rgba(122, 140, 109, 0.1)',
        tension: 0.4,
        fill: true
      }
    ]
  }
});

export const addToCart = async (product: any) => {
  const quantidadeEstoque = await apiClient.get(`/estoques/${product.id}`).then(response => response.data.quantidade);

  const existingProduct = store.cart.find(item => item.id === product.produto.id);

  if (existingProduct) {
    existingProduct.quantity++;
    return;
  }

  if (quantidadeEstoque > 0) {
    store.cart.push({ ...product, quantity: 1 });
  }
};

export const removeFromCart = (productId: number) => {
  store.cart = store.cart.filter(item => item.id !== productId);
};

export const updateInventory = (productId: number, newQuantity: number) => {
  const product = store.products.find(p => p.id === productId);
  if (product) {
    product.inventory = newQuantity;
  }
};

export const calculateSalePrice = (cost: number, pricingGroupId: number) => {
  const group = store.pricingGroups.find(g => g.id === pricingGroupId);
  if (group) {
    return cost * (1 + group.margin);
  }
  return cost;
};
