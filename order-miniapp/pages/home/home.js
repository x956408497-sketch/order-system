import { API_BASE, ensureLogin } from '../../utils/auth'

function mapProduct(product) {
  return {
    ...product,
    firstChar: product.name ? product.name.slice(0, 1) : '商',
    priceText: Number(product.price || 0).toFixed(2),
  }
}

function filterProducts(products, keyword) {
  const text = (keyword || '').trim()
  if (!text) return products
  return products.filter((product) => product.name.includes(text))
}

Page({
  data: {
    products: [],
    visibleProducts: [],
    loading: true,
    error: '',
    keyword: '',
  },

  onLoad() {
    this.loadProducts()
  },

  onPullDownRefresh() {
    this.loadProducts().finally(() => wx.stopPullDownRefresh())
  },

  loadProducts() {
    this.setData({ loading: true, error: '' })
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${API_BASE}/api/products`,
        method: 'GET',
        success: (response) => {
          if (response.statusCode !== 200 || !Array.isArray(response.data)) {
            const message = response.data?.message || `服务返回 ${response.statusCode}`
            this.setData({ error: message, loading: false })
            reject(new Error(message))
            return
          }
          const products = response.data.map(mapProduct)
          this.setData({ products, visibleProducts: products, loading: false })
          resolve(products)
        },
        fail: (error) => {
          this.setData({
            error: `${error.errMsg || '请求失败'}。请确认 Spring Boot 已运行在 8080 端口。`,
            loading: false,
          })
          reject(error)
        },
      })
    })
  },

  onSearchInput(event) {
    const keyword = event.detail.value || ''
    this.setData({ keyword, visibleProducts: filterProducts(this.data.products, keyword) })
  },

  addToCart(event) {
    const product = event.currentTarget.dataset.product
    ensureLogin().then((user) => {
      wx.request({
        url: `${API_BASE}/api/cart/items?userId=${user.userId}`,
        method: 'POST',
        data: { productId: product.id, quantity: 1 },
        header: { 'content-type': 'application/json' },
        success: (response) => {
          if (response.statusCode === 200) wx.showToast({ title: `${product.name} 已加入`, icon: 'success' })
          else wx.showToast({ title: response.data?.message || '加入失败', icon: 'none' })
        },
        fail: () => wx.showToast({ title: '无法连接订货服务', icon: 'none' }),
      })
    }).catch((error) => wx.showToast({ title: error.message || '请先登录', icon: 'none' }))
  },

  openProduct(event) {
    const product = event.currentTarget.dataset.product
    const history = wx.getStorageSync('browseHistory') || []
    const nextHistory = [product, ...history.filter((item) => item.id !== product.id)].slice(0, 10)
    wx.setStorageSync('browseHistory', nextHistory)
    wx.navigateTo({ url: `/pages/goods/details/index?id=${product.id}` })
  },
})
