import { API_BASE, ensureLogin } from '../../utils/auth'

Page({
  data: {
    categories: [],
    selectedCategoryId: null,
    products: [],
    loading: true,
    error: '',
  },

  onShow() {
    this.getTabBar().init()
  },

  onLoad() {
    this.loadCategories()
  },

  request(url, data = {}) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${API_BASE}${url}`,
        data,
        success: (response) => {
          if (response.statusCode === 200) resolve(response.data)
          else reject(new Error(response.data?.message || `服务返回 ${response.statusCode}`))
        },
        fail: (error) => reject(new Error(error.errMsg || '无法连接订货服务')),
      })
    })
  },

  loadCategories() {
    this.setData({ loading: true, error: '' })
    Promise.all([this.request('/api/categories'), this.request('/api/products')])
      .then(([categories, products]) => {
        this.setData({ categories, products, selectedCategoryId: null, loading: false })
      })
      .catch((error) => this.setData({ error: error.message, loading: false }))
  },

  selectCategory(event) {
    const id = event.currentTarget.dataset.id
    this.setData({ selectedCategoryId: id, loading: true })
    this.request('/api/products', { categoryId: id })
      .then((products) => this.setData({ products, loading: false }))
      .catch((error) => this.setData({ error: error.message, loading: false }))
  },

  selectAll() {
    this.loadCategories()
  },

  openProduct(event) {
    wx.navigateTo({ url: `/pages/goods/details/index?id=${event.currentTarget.dataset.id}` })
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
          wx.showToast({
            title: response.statusCode === 200 ? '已加入购物车' : (response.data?.message || '加入失败'),
            icon: response.statusCode === 200 ? 'success' : 'none',
          })
        },
        fail: () => wx.showToast({ title: '无法连接订货服务', icon: 'none' }),
      })
    }).catch((error) => wx.showToast({ title: error.message || '请先登录', icon: 'none' }))
  },
})
