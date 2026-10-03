import { API_BASE, ensureLogin } from '../../../utils/auth'

Page({
  data: { product: null, priceText: '', loading: true, error: '' },
  onLoad(options) {
    const id = Number(options.id || options.spuId)
    if (!Number.isInteger(id) || id <= 0) {
      this.setData({ loading: false, error: '商品编号无效' })
      return
    }
    wx.request({
      url: `${API_BASE}/api/products/${id}`,
      success: (response) => {
        if (response.statusCode !== 200 || !response.data?.id) {
          this.setData({ loading: false, error: response.data?.message || '商品不存在或已下架' })
          return
        }
        const product = response.data
        this.setData({ product, priceText: Number(product.price).toFixed(2), loading: false })
        const history = wx.getStorageSync('browseHistory') || []
        wx.setStorageSync('browseHistory', [product, ...history.filter((item) => item.id !== product.id)].slice(0, 10))
      },
      fail: (error) => this.setData({ loading: false, error: error.errMsg || '无法连接订货服务' }),
    })
  },
  addToCart() {
    const product = this.data.product
    if (!product || product.stock <= 0) return
    ensureLogin().then((user) => {
      wx.request({
        url: `${API_BASE}/api/cart/items?userId=${user.userId}`,
        method: 'POST',
        header: { 'content-type': 'application/json' },
        data: { productId: product.id, quantity: 1 },
        success: (response) => wx.showToast({
          title: response.statusCode === 200 ? '已加入购物车' : (response.data?.message || '加入失败'),
          icon: response.statusCode === 200 ? 'success' : 'none',
        }),
        fail: () => wx.showToast({ title: '无法连接订货服务', icon: 'none' }),
      })
    }).catch((error) => wx.showToast({ title: error.message || '请先登录', icon: 'none' }))
  },
  goCart() { wx.switchTab({ url: '/pages/cart/index' }) },
})
