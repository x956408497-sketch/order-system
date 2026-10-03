import { API_BASE, ensureLogin } from '../../utils/auth'

Page({
  data: {
    items: [],
    total: '0.00',
    loading: true,
    error: '',
  },

  onShow() {
    this.getTabBar().init()
    this.loadCart()
  },

  loadCart() {
    this.setData({ loading: true, error: '' })
    ensureLogin().then((user) => {
      wx.request({
        url: `${API_BASE}/api/cart`,
        data: { userId: user.userId },
        success: (response) => {
          if (response.statusCode !== 200 || !Array.isArray(response.data)) {
            this.setData({ error: response.data?.message || '购物车加载失败', loading: false })
            return
          }
          const items = response.data.map((item) => ({
            ...item,
            firstChar: item.productName ? item.productName.slice(0, 1) : '商',
            priceText: Number(item.price || 0).toFixed(2),
            subtotal: (Number(item.price || 0) * item.quantity).toFixed(2),
          }))
          const total = items.reduce((sum, item) => sum + Number(item.subtotal), 0).toFixed(2)
          this.setData({ items, total, loading: false })
        },
        fail: (error) => this.setData({ error: error.errMsg || '无法连接订货服务', loading: false }),
      })
    }).catch((error) => this.setData({ error: error.message || '请先登录', loading: false }))
  },

  changeQuantity(event) {
    const item = event.currentTarget.dataset.item
    const step = Number(event.currentTarget.dataset.step)
    const quantity = item.quantity + step
    if (quantity < 1) return this.deleteItem(event)
    ensureLogin().then((user) => {
      wx.request({
        url: `${API_BASE}/api/cart/items/${item.id}?userId=${user.userId}`,
        method: 'PUT',
        data: { quantity },
        header: { 'content-type': 'application/json' },
        success: (response) => {
          if (response.statusCode === 200) this.loadCart()
          else wx.showToast({ title: response.data?.message || '修改失败', icon: 'none' })
        },
        fail: () => wx.showToast({ title: '修改失败', icon: 'none' }),
      })
    })
  },

  deleteItem(event) {
    const item = event.currentTarget.dataset.item
    wx.showModal({
      title: '删除商品',
      content: `确定删除${item.productName}吗？`,
      success: (result) => {
        if (!result.confirm) return
        ensureLogin().then((user) => {
          wx.request({
            url: `${API_BASE}/api/cart/items/${item.id}?userId=${user.userId}`,
            method: 'DELETE',
            success: () => this.loadCart(),
            fail: () => wx.showToast({ title: '删除失败', icon: 'none' }),
          })
        })
      },
    })
  },

  goHome() {
    wx.switchTab({ url: '/pages/home/home' })
  },

  goCheckout() {
    if (!this.data.items.length) {
      wx.showToast({ title: '购物车为空', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/order/order-confirm/index' })
  },
})
