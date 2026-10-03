<script setup>
import { nextTick, onMounted, ref } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'

const view = ref('dashboard')
const mode = ref(
  new URLSearchParams(window.location.search).get('mode') === 'shop' || window.location.pathname === '/shop'
    ? 'shop'
    : 'admin'
)
const shopProducts = ref([])
const shopCart = ref([])
const shopAddresses = ref([])
const shopOrders = ref([])
const shopKeyword = ref('')
const shopAddressId = ref(null)
const shopMessage = ref('')
const shopLoading = ref(false)
const addressForm = ref({ receiverName: '', phone: '', detailAddress: '' })
const showAddressForm = ref(false)
const isLoggedIn = ref(Boolean(localStorage.getItem('adminToken')))
const adminUsername = ref(localStorage.getItem('adminUsername') || '管理员')
const loginLoading = ref(false)
const loginError = ref('')
const loginForm = ref({ username: 'admin', password: '123456' })
const showAccountForm = ref(false)
const accountSaving = ref(false)
const accountError = ref('')
const accountForm = ref({ currentPassword: '', username: '', password: '' })
const loading = ref(true)
const sparkRefreshing = ref(false)
const sparkMessage = ref('')
const productLoading = ref(false)
const error = ref('')
const productError = ref('')
const overview = ref({ orderCount: 0, productCount: 0, salesAmount: 0, topProductName: '-' })
const trend = ref([])
const statisticsProducts = ref([])
const categories = ref([])
const allProducts = ref([])
const productCategories = ref([])
const showProductForm = ref(false)
const showCategoryForm = ref(false)
const categorySaving = ref(false)
const categoryForm = ref({ id: null, name: '', sort: 0 })
const productSaving = ref(false)
const productForm = ref(emptyProductForm())
const orders = ref([])
const keyword = ref('')
const categoryFilter = ref('')
const orderStatusFilter = ref('')
const trendChart = ref(null)
const productChart = ref(null)
const categoryChart = ref(null)

const statisticsApi = axios.create({ baseURL: '/api/admin/statistics' })
const productApi = axios.create({ baseURL: '/api' })
const orderApi = axios.create({ baseURL: '/api/admin/orders' })
const shopApi = axios.create({ baseURL: '/api' })
const userId = 1

function emptyProductForm() {
  return {
    id: null,
    categoryId: '',
    name: '',
    price: '',
    stock: 0,
    description: '',
    imageUrl: '',
    status: 1,
  }
}

function applyAdminToken() {
  const token = localStorage.getItem('adminToken')
  const clients = [statisticsApi, productApi, orderApi]
  clients.forEach((client) => {
    if (token) {
      client.defaults.headers.common['X-Admin-Token'] = token
    } else {
      delete client.defaults.headers.common['X-Admin-Token']
    }
  })
}

function handleUnauthorized(error) {
  if (error.response?.status === 401 && isLoggedIn.value) {
    logout()
  }
  return Promise.reject(error)
}

;[statisticsApi, productApi, orderApi].forEach((client) => {
  client.interceptors.response.use((response) => response, handleUnauthorized)
})

async function login() {
  loginLoading.value = true
  loginError.value = ''
  try {
    const response = await axios.post('/api/admin/login', loginForm.value)
    localStorage.setItem('adminToken', response.data.token)
    localStorage.setItem('adminUsername', response.data.username)
    adminUsername.value = response.data.username
    applyAdminToken()
    isLoggedIn.value = true
    await loadStatistics()
  } catch (err) {
    loginError.value = err.response?.data?.message || '登录失败，请检查账号和密码。'
  } finally {
    loginLoading.value = false
  }
}

function openAccountForm() {
  accountError.value = ''
  accountForm.value = { currentPassword: '', username: adminUsername.value, password: '' }
  showAccountForm.value = true
}

async function saveAccount() {
  accountSaving.value = true
  accountError.value = ''
  try {
    await axios.post('/api/admin/account', accountForm.value, { headers: { 'X-Admin-Token': localStorage.getItem('adminToken') } })
    showAccountForm.value = false
    logout()
    loginForm.value = { username: accountForm.value.username, password: '' }
    loginError.value = '账号密码已修改，请使用新密码重新登录。'
  } catch (err) {
    accountError.value = err.response?.data?.message || '账号密码修改失败。'
  } finally {
    accountSaving.value = false
  }
}

function logout() {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminUsername')
  adminUsername.value = '管理员'
  isLoggedIn.value = false
  view.value = 'dashboard'
}

applyAdminToken()

async function loadShop({ clearMessage = false } = {}) {
  shopLoading.value = true
  if (clearMessage) shopMessage.value = ''
  try {
    const [productsResponse, cartResponse, addressesResponse, ordersResponse] = await Promise.all([
      shopApi.get('/products', { params: { keyword: shopKeyword.value || undefined } }),
      shopApi.get('/cart', { params: { userId } }),
      shopApi.get('/addresses', { params: { userId } }),
      shopApi.get('/orders', { params: { userId } }),
    ])
    shopProducts.value = productsResponse.data
    shopCart.value = cartResponse.data
    shopAddresses.value = addressesResponse.data
    shopOrders.value = ordersResponse.data
    if (!shopAddresses.value.some((address) => address.id === shopAddressId.value)) {
      shopAddressId.value = shopAddresses.value.find((address) => address.isDefault === 1)?.id || shopAddresses.value[0]?.id || null
    }
  } catch (err) {
    const status = err.response?.status
    if (!err.response) {
      shopMessage.value = '无法连接后端。请在 IDEA 中启动 OrderSystemApplication，并访问 http://localhost:8080/?mode=shop'
    } else if (status === 401) {
      shopMessage.value = '当前接口需要管理员登录，请确认访问的是用户端地址。'
    } else {
      shopMessage.value = err.response?.data?.message || `用户端数据加载失败（${status || '未知错误'}）。`
    }
  } finally {
    shopLoading.value = false
  }
}

async function addToCart(product) {
  try {
    await shopApi.post('/cart/items', { productId: product.id, quantity: 1 }, { params: { userId } })
    shopMessage.value = `已加入购物车：${product.name}`
    await loadShop()
  } catch (err) {
    shopMessage.value = err.response?.data?.message || '加入购物车失败。'
  }
}

async function changeCart(item, amount) {
  const quantity = item.quantity + amount
  if (quantity < 1) return removeCart(item)
  try {
    await shopApi.put(`/cart/items/${item.id}`, { quantity }, { params: { userId } })
    await loadShop()
  } catch (err) {
    shopMessage.value = err.response?.data?.message || '购物车更新失败。'
  }
}

async function removeCart(item) {
  try {
    await shopApi.delete(`/cart/items/${item.id}`, { params: { userId } })
    await loadShop()
  } catch (err) {
    shopMessage.value = err.response?.data?.message || '移除商品失败。'
  }
}

async function saveAddress() {
  if (!addressForm.value.receiverName.trim() || !addressForm.value.phone.trim() || !addressForm.value.detailAddress.trim()) {
    shopMessage.value = '请填写完整的收货地址。'
    return
  }
  try {
    const response = await shopApi.post('/addresses', { ...addressForm.value, isDefault: shopAddresses.value.length ? 0 : 1 }, { params: { userId } })
    showAddressForm.value = false
    addressForm.value = { receiverName: '', phone: '', detailAddress: '' }
    await loadShop()
    shopAddressId.value = response.data.id
    shopMessage.value = '收货地址已保存。'
  } catch (err) {
    shopMessage.value = err.response?.data?.message || '保存地址失败。'
  }
}

async function submitShopOrder() {
  if (!shopAddressId.value) { shopMessage.value = '请先选择收货地址。'; return }
  if (!shopCart.value.length) { shopMessage.value = '购物车为空，请先选择商品。'; return }
  try {
    const response = await shopApi.post('/orders', { addressId: shopAddressId.value, remark: '用户端测试订单' }, { params: { userId } })
    await shopApi.post(`/orders/${response.data.id}/pay`, null, { params: { userId } })
    shopMessage.value = `订单 ${response.data.id} 已创建并完成模拟支付。`
    await loadShop()
  } catch (err) {
    shopMessage.value = err.response?.data?.message || '订单创建失败。'
  }
}

function shopStatus(status) {
  return { PENDING_PAY: '待支付', PAID: '已支付', TO_SHIP: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }[status] || status
}

function switchView(nextView) {
  view.value = nextView
  if (nextView === 'products' && !allProducts.value.length) loadProducts()
  if (nextView === 'categories') loadCategories()
  if (nextView === 'orders' && !orders.value.length) loadOrders()
  nextTick(() => {
    if (nextView === 'dashboard') renderCharts()
  })
}

function renderCharts() {
  if (trendChart.value) {
    const chart = echarts.init(trendChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 42, right: 20, top: 26, bottom: 34 },
      xAxis: { type: 'category', data: trend.value.map((item) => item.statDate) },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#edf0f4' } } },
      series: [{ name: '销售额', type: 'line', smooth: true, data: trend.value.map((item) => Number(item.salesAmount)), itemStyle: { color: '#176b87' }, areaStyle: { color: 'rgba(23,107,135,.12)' } }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
  if (productChart.value) {
    const chart = echarts.init(productChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 90, right: 24, top: 18, bottom: 28 },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: statisticsProducts.value.slice().reverse().map((item) => item.productName) },
      series: [{ type: 'bar', data: statisticsProducts.value.slice().reverse().map((item) => item.salesCount), itemStyle: { color: '#d8894a' }, barMaxWidth: 20 }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
  if (categoryChart.value) {
    const chart = echarts.init(categoryChart.value)
    chart.setOption({
      tooltip: { trigger: 'item', valueFormatter: (value) => `${value} 元` },
      legend: { bottom: 0, left: 'center' },
      series: [{ type: 'pie', radius: ['42%', '70%'], center: ['50%', '44%'], itemStyle: { borderColor: '#fff', borderWidth: 3 }, data: categories.value.map((item) => ({ name: item.categoryName, value: Number(item.salesAmount) })) }],
    })
    window.addEventListener('resize', () => chart.resize())
  }
}

async function refreshStatistics() {
  if (sparkRefreshing.value) return
  sparkRefreshing.value = true
  sparkMessage.value = '正在提交分析任务...'
  error.value = ''
  try {
    const response = await statisticsApi.post('/refresh')
    let task = response.data
    while (task.status === 'RUNNING') {
      sparkMessage.value = task.message || 'Spark 分析中，图表将在完成后自动更新...'
      await new Promise((resolve) => setTimeout(resolve, 2000))
      task = (await statisticsApi.get(`/refresh/${task.taskId}`)).data
    }
    if (task.status !== 'COMPLETED') throw new Error(task.message || 'Spark 分析失败')
    await loadStatistics()
    sparkMessage.value = task.message
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Spark 分析失败，请检查分析工程和 MySQL。'
    sparkMessage.value = ''
  } finally {
    sparkRefreshing.value = false
  }
}

async function loadStatistics() {
  loading.value = true
  error.value = ''
  try {
    const [overviewResponse, trendResponse, productsResponse, categoriesResponse] = await Promise.all([
      statisticsApi.get('/overview'),
      statisticsApi.get('/sales-trend'),
      statisticsApi.get('/top-products'),
      statisticsApi.get('/categories'),
    ])
    overview.value = overviewResponse.data
    trend.value = trendResponse.data
    statisticsProducts.value = productsResponse.data
    categories.value = categoriesResponse.data
  } catch (err) {
    if (err.response?.status === 401) {
      error.value = '登录已失效，请重新登录。'
    } else {
      error.value = '统计数据加载失败，请确认 Spring Boot 服务运行在 8080 端口。'
    }
  } finally {
    loading.value = false
    setTimeout(renderCharts, 0)
  }
}

async function loadCategories() {
  productLoading.value = true
  productError.value = ''
  try {
    const response = await productApi.get('/categories')
    productCategories.value = response.data
  } catch {
    productError.value = '分类数据加载失败，请确认后端服务正在运行。'
  } finally {
    productLoading.value = false
  }
}

function openCreateCategory() {
  categoryForm.value = { id: null, name: '', sort: 0 }
  showCategoryForm.value = true
}

function editCategory(category) {
  categoryForm.value = { id: category.id, name: category.name, sort: category.sort }
  showCategoryForm.value = true
}

async function saveCategory() {
  const form = categoryForm.value
  if (!form.name.trim() || Number(form.sort) < 0) {
    productError.value = '请输入分类名称，排序不能为负数。'
    return
  }
  categorySaving.value = true
  productError.value = ''
  const payload = { name: form.name.trim(), sort: Number(form.sort) }
  try {
    if (form.id) await productApi.put(`/admin/categories/${form.id}`, payload)
    else await productApi.post('/admin/categories', payload)
    showCategoryForm.value = false
    await loadCategories()
  } catch (err) {
    productError.value = err.response?.data?.message || '分类保存失败。'
  } finally {
    categorySaving.value = false
  }
}

async function deleteCategory(category) {
  if (!window.confirm(`确定删除分类“${category.name}”吗？`)) return
  try {
    await productApi.delete(`/admin/categories/${category.id}`)
    await loadCategories()
  } catch (err) {
    productError.value = err.response?.data?.message || '分类删除失败，该分类可能仍有商品。'
  }
}

async function loadProducts() {
  productLoading.value = true
  productError.value = ''
  try {
    const [productsResponse, categoriesResponse] = await Promise.all([
      productApi.get('/admin/products'),
      productApi.get('/categories'),
    ])
    allProducts.value = productsResponse.data
    productCategories.value = categoriesResponse.data
  } catch {
    productError.value = '商品数据加载失败，请确认后端服务正在运行。'
  } finally {
    productLoading.value = false
  }
}

function openCreateProduct() {
  productForm.value = emptyProductForm()
  showProductForm.value = true
}

function editProduct(product) {
  productForm.value = {
    id: product.id,
    categoryId: product.categoryId,
    name: product.name,
    price: Number(product.price),
    stock: product.stock,
    description: product.description || '',
    imageUrl: product.imageUrl || '',
    status: product.status,
  }
  showProductForm.value = true
}

async function onProductImage(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    productError.value = '请选择图片文件。'
    return
  }
  if (file.size > 1024 * 1024) {
    productError.value = '图片请控制在 1MB 以内。'
    return
  }
  const formData = new FormData()
  formData.append('file', file)
  try {
    const response = await productApi.post('/admin/products/images', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    productForm.value.imageUrl = response.data.imageUrl
    productError.value = ''
  } catch (err) {
    productError.value = err.response?.data?.message || '图片上传失败。'
  }
}

async function saveProduct() {
  const form = productForm.value
  const price = Number(form.price)
  const stock = Number(form.stock)
  if (!form.categoryId || !form.name.trim() || !Number.isFinite(price) || price < 0 || !Number.isInteger(stock) || stock < 0) {
    productError.value = '请完整填写分类、名称、价格和库存。'
    return
  }
  productSaving.value = true
  productError.value = ''
  const payload = {
    categoryId: Number(form.categoryId),
    name: form.name.trim(),
    price,
    stock,
    description: form.description.trim(),
    imageUrl: form.imageUrl,
    status: Number(form.status),
  }
  try {
    if (form.id) await productApi.put(`/admin/products/${form.id}`, payload)
    else await productApi.post('/admin/products', payload)
    showProductForm.value = false
    await loadProducts()
  } catch (err) {
    productError.value = err.response?.data?.message || '商品保存失败。'
  } finally {
    productSaving.value = false
  }
}

async function loadOrders() {
  productLoading.value = true
  productError.value = ''
  try {
    const response = await orderApi.get('')
    orders.value = response.data
  } catch {
    productError.value = '订单数据加载失败，请确认后端服务正在运行。'
  } finally {
    productLoading.value = false
  }
}

const filteredOrders = () => orders.value.filter((order) => {
  const matchesKeyword = !keyword.value.trim() || String(order.orderNo).includes(keyword.value.trim()) || String(order.receiverName).includes(keyword.value.trim())
  const matchesStatus = !orderStatusFilter.value || order.orderStatus === orderStatusFilter.value
  return matchesKeyword && matchesStatus
})

function statusText(status) {
  return { PENDING_PAY: '待支付', PAID: '已支付', TO_SHIP: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }[status] || status
}

async function orderAction(id, action) {
  try {
    await orderApi.post(`/${id}/${action}`)
    await loadOrders()
  } catch (err) {
    productError.value = err.response?.data?.message || '订单操作失败，请检查订单状态。'
  }
}

const filteredProducts = () => allProducts.value.filter((product) => {
  const matchesKeyword = !keyword.value.trim() || product.name.includes(keyword.value.trim())
  const matchesCategory = !categoryFilter.value || String(product.categoryId) === String(categoryFilter.value)
  return matchesKeyword && matchesCategory
})

async function toggleProduct(product) {
  await productApi.put(`/admin/products/${product.id}/status`, null, { params: { status: product.status === 1 ? 0 : 1 } })
  await loadProducts()
}

async function updateStock(product) {
  const stock = Number(product.stock)
  if (!Number.isInteger(stock) || stock < 0) {
    productError.value = '库存必须是大于等于0的整数。'
    await loadProducts()
    return
  }
  await productApi.put(`/admin/products/${product.id}/stock`, null, { params: { stock } })
  await loadProducts()
}

async function deleteProduct(product) {
  if (!window.confirm(`确定删除商品“${product.name}”吗？`)) return
  try {
    await productApi.delete(`/admin/products/${product.id}`)
    await loadProducts()
  } catch (err) {
    productError.value = err.response?.data?.message || '商品删除失败。'
  }
}

onMounted(() => {
  if (mode.value === 'shop') loadShop()
  else if (isLoggedIn.value) loadStatistics()
})
</script>

<template>
  <main v-if="mode === 'shop'" class="shop-page">
    <header class="shop-header"><div class="shop-brand"><span class="brand-mark">OS</span><div><strong>在线订货</strong><small>ORDER SYSTEM</small></div></div><div class="shop-user">测试用户 #1 <a href="/">进入商家后台</a></div></header>
    <section class="shop-content"><div class="shop-title"><div><p class="eyebrow">用户端 / 商品商城</p><h1>选购商品</h1></div><button class="refresh" @click="loadShop({ clearMessage: true })">↻ 刷新</button></div>
      <div v-if="shopMessage" class="alert">{{ shopMessage }}</div>
      <div class="shop-layout"><section><div class="shop-search"><input v-model="shopKeyword" @keyup.enter="loadShop" placeholder="搜索商品名称" /><button @click="loadShop">搜索</button></div><div v-if="shopLoading" class="empty">正在加载商品...</div><div v-else class="shop-products"><article v-for="product in shopProducts" :key="product.id" class="shop-product"><div class="shop-product-icon">{{ product.name.slice(0, 1) }}</div><div class="shop-product-body"><span class="shop-category">{{ product.categoryName }}</span><h2>{{ product.name }}</h2><p>{{ product.description || '品质商品，放心选购' }}</p><div class="shop-product-foot"><strong>¥ {{ Number(product.price).toFixed(2) }}</strong><button @click="addToCart(product)">加入购物车</button></div></div></article></div></section>
      <aside class="shop-side"><section class="shop-box"><div class="shop-box-title"><h2>购物车</h2><span>{{ shopCart.length }} 件</span></div><div v-if="!shopCart.length" class="empty">购物车为空</div><div v-for="item in shopCart" :key="item.id" class="cart-row"><div><strong>{{ item.productName }}</strong><small>¥ {{ Number(item.price).toFixed(2) }}</small></div><div class="stepper"><button @click="changeCart(item, -1)">−</button><span>{{ item.quantity }}</span><button @click="changeCart(item, 1)">+</button></div></div><div v-if="shopCart.length" class="cart-total">合计 <strong>¥ {{ shopCart.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0).toFixed(2) }}</strong></div></section><section class="shop-box"><div class="shop-box-title"><h2>收货地址</h2><button class="text-button" @click="showAddressForm = !showAddressForm">{{ showAddressForm ? '取消' : '新增地址' }}</button></div><select v-model="shopAddressId" class="address-select"><option :value="null">请选择地址</option><option v-for="address in shopAddresses" :key="address.id" :value="address.id">{{ address.receiverName }} · {{ address.phone }} · {{ address.detailAddress }}</option></select><form v-if="showAddressForm" class="address-form" @submit.prevent="saveAddress"><input v-model="addressForm.receiverName" placeholder="收货人" /><input v-model="addressForm.phone" placeholder="联系电话" /><input v-model="addressForm.detailAddress" placeholder="详细地址" /><button type="submit">保存地址</button></form><button class="checkout" @click="submitShopOrder">提交订单并模拟支付</button></section><section class="shop-box"><div class="shop-box-title"><h2>我的订单</h2></div><div v-if="!shopOrders.length" class="empty">暂无订单</div><div v-for="order in shopOrders.slice(0, 5)" :key="order.id" class="shop-order"><div><strong>#{{ order.orderNo }}</strong><small>{{ order.createTime?.replace('T', ' ') }}</small></div><span>{{ shopStatus(order.orderStatus) }}</span><b>¥ {{ Number(order.totalAmount).toFixed(2) }}</b></div></section></aside></div>
    </section>
  </main>
  <main v-else-if="!isLoggedIn" class="login-page">
    <section class="login-card">
      <div class="login-mark">OS</div>
      <p class="eyebrow">ORDER SYSTEM / ADMIN</p>
      <h1>商家后台登录</h1>
      <p class="login-subtitle">登录后管理商品、订单与销售数据</p>
      <form @submit.prevent="login">
        <label>管理员账号<input v-model="loginForm.username" autocomplete="username" placeholder="请输入账号" /></label>
        <label>登录密码<input v-model="loginForm.password" type="password" autocomplete="current-password" placeholder="请输入密码" /></label>
        <div v-if="loginError" class="login-error">{{ loginError }}</div>
        <button class="login-button" type="submit" :disabled="loginLoading">{{ loginLoading ? '登录中...' : '登录后台' }}</button>
      </form>
      <p class="login-hint">测试账号：admin　密码：123456</p>
    </section>
  </main>
  <div v-else class="shell">
    <aside class="sidebar">
      <div class="brand"><span class="brand-mark">OS</span><div><strong>订货管理</strong><small>ORDER SYSTEM</small></div></div>
      <nav>
        <button class="nav-item" :class="{ active: view === 'dashboard' }" @click="switchView('dashboard')"><span>▦</span> 数据总览</button>
        <button class="nav-item" :class="{ active: view === 'products' }" @click="switchView('products')"><span>▤</span> 商品管理</button>
        <button class="nav-item" :class="{ active: view === 'categories' }" @click="switchView('categories')"><span>⌗</span> 分类管理</button>
        <button class="nav-item" :class="{ active: view === 'orders' }" @click="switchView('orders')"><span>⌁</span> 订单管理</button>
      </nav>
      <div class="sidebar-foot"><span class="status-dot"></span> 本地服务已连接</div>
    </aside>

    <main v-if="view === 'dashboard'" class="content" id="dashboard">
      <header class="topbar"><div><p class="eyebrow">商家工作台 / 数据中心</p><h1>销售数据总览</h1></div><div class="top-actions"><span class="admin-user">{{ adminUsername }}</span><button class="text-button" @click="openAccountForm">账号设置</button><button class="logout" @click="logout">退出登录</button><button class="refresh" @click="refreshStatistics" :disabled="sparkRefreshing" title="运行 Spark 并刷新统计">↻ <span>{{ sparkRefreshing ? 'Spark 分析中...' : '刷新数据' }}</span></button></div></header>
      <div v-if="error" class="alert">{{ error }}</div>
      <div v-if="sparkMessage" class="spark-progress">{{ sparkMessage }}</div>
      <section class="metrics" aria-label="核心指标">
        <article class="metric"><span class="metric-label">累计销售额</span><strong>¥ {{ Number(overview.salesAmount || 0).toFixed(2) }}</strong><span class="metric-note">有效订单汇总</span></article>
        <article class="metric"><span class="metric-label">有效订单</span><strong>{{ overview.orderCount || 0 }}</strong><span class="metric-note">已支付及已完成订单</span></article>
        <article class="metric"><span class="metric-label">商品销量</span><strong>{{ overview.productCount || 0 }}</strong><span class="metric-note">累计售出商品数量</span></article>
        <article class="metric featured"><span class="metric-label">热销商品</span><strong>{{ overview.topProductName || '-' }}</strong><span class="metric-note">按累计销量排序</span></article>
      </section>
      <section class="dashboard-grid">
        <article class="panel wide"><div class="panel-head"><div><h2>销售趋势</h2><p>按订单日期统计销售额</p></div><span class="panel-tag">Spark SQL</span></div><div ref="trendChart" class="chart trend-chart"></div></article>
        <article class="panel"><div class="panel-head"><div><h2>分类销售</h2><p>各分类销售额占比</p></div></div><div ref="categoryChart" class="chart pie-chart"></div></article>
        <article class="panel wide"><div class="panel-head"><div><h2>商品销量排行</h2><p>当前统计周期内的商品销售表现</p></div><span class="panel-tag warm">TOP 20</span></div><div ref="productChart" class="chart product-chart"></div></article>
        <article class="panel table-panel"><div class="panel-head"><div><h2>商品明细</h2><p>销量与销售额</p></div></div><div v-if="loading" class="empty">正在加载...</div><table v-else><thead><tr><th>商品</th><th>销量</th><th>销售额</th></tr></thead><tbody><tr v-for="item in statisticsProducts" :key="item.productId"><td>{{ item.productName }}</td><td>{{ item.salesCount }}</td><td>¥ {{ Number(item.salesAmount).toFixed(2) }}</td></tr><tr v-if="!statisticsProducts.length"><td colspan="3" class="empty">暂无统计数据</td></tr></tbody></table></article>
      </section>
      <footer>数据由 Apache Spark SQL 批量分析生成 · {{ trend.length ? `最近统计日期 ${trend[trend.length - 1].statDate}` : '等待数据' }}</footer>
    </main>

    <main v-else-if="view === 'categories'" class="content">
      <header class="topbar"><div><p class="eyebrow">商家工作台 / 商品中心</p><h1>分类管理</h1></div><div class="top-actions"><span class="admin-user">{{ adminUsername }}</span><button class="logout" @click="logout">退出登录</button><button class="refresh" @click="openCreateCategory">＋ <span>新增分类</span></button><button class="refresh" @click="loadCategories">↻ <span>刷新分类</span></button></div></header>
      <div v-if="productError" class="alert">{{ productError }}</div>
      <form v-if="showCategoryForm" class="panel category-form" @submit.prevent="saveCategory"><div class="panel-head"><div><h2>{{ categoryForm.id ? '编辑分类' : '新增分类' }}</h2><p>分类会同步到小程序分类页</p></div><button type="button" class="text-button" @click="showCategoryForm = false">关闭</button></div><div class="category-fields"><label>分类名称<input v-model="categoryForm.name" placeholder="例如：饮料" /></label><label>排序<input v-model="categoryForm.sort" type="number" min="0" /></label><button class="login-button" type="submit" :disabled="categorySaving">{{ categorySaving ? '保存中...' : '保存分类' }}</button></div></form>
      <section class="panel product-table-panel"><div v-if="productLoading" class="empty">正在加载分类...</div><table v-else><thead><tr><th>分类名称</th><th>排序</th><th>分类 ID</th><th>操作</th></tr></thead><tbody><tr v-for="category in productCategories" :key="category.id"><td><strong>{{ category.name }}</strong></td><td>{{ category.sort }}</td><td>{{ category.id }}</td><td><div class="row-actions"><button class="text-button" @click="editCategory(category)">编辑</button><button class="text-button danger" @click="deleteCategory(category)">删除</button></div></td></tr><tr v-if="!productCategories.length"><td colspan="4" class="empty">暂无分类</td></tr></tbody></table></section>
      <footer>删除分类前请先处理该分类下的商品</footer>
    </main>

    <main v-else-if="view === 'products'" class="content">
      <header class="topbar"><div><p class="eyebrow">商家工作台 / 商品中心</p><h1>商品管理</h1></div><div class="top-actions"><span class="admin-user">{{ adminUsername }}</span><button class="text-button" @click="openAccountForm">账号设置</button><button class="logout" @click="logout">退出登录</button><button class="refresh" @click="openCreateProduct">＋ <span>新增商品</span></button><button class="refresh" @click="loadProducts">↻ <span>刷新商品</span></button></div></header>
      <div v-if="productError" class="alert">{{ productError }}</div>
      <section class="toolbar panel"><div class="search-box"><span>⌕</span><input v-model="keyword" placeholder="搜索商品名称" /></div><select v-model="categoryFilter"><option value="">全部分类</option><option v-for="category in productCategories" :key="category.id" :value="category.id">{{ category.name }}</option></select><span class="result-count">共 {{ filteredProducts().length }} 件商品</span></section>
      <form v-if="showProductForm" class="panel product-form" @submit.prevent="saveProduct"><div class="panel-head"><div><h2>{{ productForm.id ? '编辑商品' : '新增商品' }}</h2><p>修改名称、价格、分类、描述和图片</p></div><button type="button" class="text-button" @click="showProductForm = false">关闭</button></div><div class="form-grid"><label>商品名称<input v-model="productForm.name" placeholder="请输入商品名称" /></label><label>商品分类<select v-model="productForm.categoryId"><option value="">请选择分类</option><option v-for="category in productCategories" :key="category.id" :value="category.id">{{ category.name }}</option></select></label><label>价格<input v-model="productForm.price" type="number" min="0" step="0.01" /></label><label>库存<input v-model="productForm.stock" type="number" min="0" step="1" /></label><label class="wide">商品描述<textarea v-model="productForm.description" rows="3" placeholder="请输入商品描述"></textarea></label><label class="wide">商品图片<input type="file" accept="image/*" @change="onProductImage" /><input v-model="productForm.imageUrl" placeholder="或输入图片地址" /></label><label>状态<select v-model="productForm.status"><option :value="1">上架</option><option :value="0">下架</option></select></label><img v-if="productForm.imageUrl" class="product-preview" :src="productForm.imageUrl" alt="商品图片预览" /></div><button class="login-button product-save" type="submit" :disabled="productSaving">{{ productSaving ? '保存中...' : '保存商品' }}</button></form>
      <section class="panel product-table-panel"><div v-if="productLoading" class="empty">正在加载商品...</div><table v-else><thead><tr><th>商品</th><th>分类</th><th>价格</th><th>库存</th><th>状态</th><th>操作</th></tr></thead><tbody><tr v-for="product in filteredProducts()" :key="product.id"><td><div class="product-name"><img v-if="product.imageUrl" class="product-thumb-image" :src="product.imageUrl" alt="" /><span v-else class="product-thumb">{{ product.name.slice(0, 1) }}</span><div><strong>{{ product.name }}</strong><small>{{ product.description || '暂无商品描述' }}</small></div></div></td><td>{{ product.categoryName }}</td><td class="price">¥ {{ Number(product.price).toFixed(2) }}</td><td><input class="stock-input" type="number" min="0" v-model.number="product.stock" @change="updateStock(product)" /></td><td><span class="status" :class="product.status === 1 ? 'on' : 'off'">{{ product.status === 1 ? '已上架' : '已下架' }}</span></td><td><div class="row-actions"><button class="text-button" @click="editProduct(product)">编辑</button><button class="text-button" @click="toggleProduct(product)">{{ product.status === 1 ? '下架' : '上架' }}</button><button class="text-button danger" @click="deleteProduct(product)">删除</button></div></td></tr><tr v-if="!filteredProducts().length"><td colspan="6" class="empty">没有匹配的商品</td></tr></tbody></table></section>
      <footer>商品修改会直接同步到用户端商品列表</footer>
    </main>

    <main v-else class="content">
      <header class="topbar"><div><p class="eyebrow">商家工作台 / 订单中心</p><h1>订单管理</h1></div><div class="top-actions"><span class="admin-user">{{ adminUsername }}</span><button class="text-button" @click="openAccountForm">账号设置</button><button class="logout" @click="logout">退出登录</button><button class="refresh" @click="loadOrders">↻ <span>刷新订单</span></button></div></header>
      <div v-if="productError" class="alert">{{ productError }}</div>
      <section class="toolbar panel"><div class="search-box"><span>⌕</span><input v-model="keyword" placeholder="搜索订单号或收货人" /></div><select v-model="orderStatusFilter"><option value="">全部状态</option><option value="PENDING_PAY">待支付</option><option value="PAID">已支付</option><option value="TO_SHIP">待发货</option><option value="SHIPPED">已发货</option><option value="COMPLETED">已完成</option><option value="CANCELLED">已取消</option></select><span class="result-count">共 {{ filteredOrders().length }} 笔订单</span></section>
      <section class="panel product-table-panel"><div v-if="productLoading" class="empty">正在加载订单...</div><table v-else><thead><tr><th>订单信息</th><th>收货信息</th><th>金额</th><th>状态</th><th>下单时间</th><th>操作</th></tr></thead><tbody><tr v-for="order in filteredOrders()" :key="order.id"><td><div class="order-info"><strong>#{{ order.orderNo }}</strong><small>{{ order.items?.length || 0 }} 种商品 · {{ order.remark || '无备注' }}</small></div></td><td><div class="order-info"><strong>{{ order.receiverName }}</strong><small>{{ order.phone }} · {{ order.detailAddress }}</small></div></td><td class="price">¥ {{ Number(order.totalAmount).toFixed(2) }}</td><td><span class="status" :class="order.orderStatus === 'COMPLETED' ? 'on' : order.orderStatus === 'CANCELLED' ? 'off' : 'pending'">{{ statusText(order.orderStatus) }}</span></td><td class="time">{{ order.createTime?.replace('T', ' ') }}</td><td><div class="row-actions"><button v-if="order.orderStatus === 'PAID'" class="text-button" @click="orderAction(order.id, 'to-ship')">确认订单</button><button v-if="order.orderStatus === 'TO_SHIP'" class="text-button" @click="orderAction(order.id, 'ship')">发货</button><button v-if="order.orderStatus === 'SHIPPED'" class="text-button" @click="orderAction(order.id, 'complete')">完成</button><button v-if="['PENDING_PAY', 'PAID'].includes(order.orderStatus)" class="text-button danger" @click="orderAction(order.id, 'cancel')">取消</button><span v-if="['COMPLETED', 'CANCELLED', 'PENDING_PAY'].includes(order.orderStatus)" class="muted-action">—</span></div></td></tr><tr v-if="!filteredOrders().length"><td colspan="6" class="empty">暂无匹配订单</td></tr></tbody></table></section>
      <footer>订单状态按业务流程流转：已支付 → 待发货 → 已发货 → 已完成</footer>
    </main>
  </div>
  <div v-if="showAccountForm" class="account-mask"><form class="account-dialog" @submit.prevent="saveAccount"><h2>修改账号密码</h2><p>修改成功后需要重新登录</p><label>当前密码<input v-model="accountForm.currentPassword" type="password" /></label><label>新账号<input v-model="accountForm.username" /></label><label>新密码<input v-model="accountForm.password" type="password" /></label><div v-if="accountError" class="login-error">{{ accountError }}</div><div class="account-actions"><button type="button" class="refresh" @click="showAccountForm = false">取消</button><button class="login-button" type="submit" :disabled="accountSaving">{{ accountSaving ? '保存中...' : '保存并重新登录' }}</button></div></form></div>
</template>
