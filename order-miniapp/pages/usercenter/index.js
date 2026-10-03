import { API_BASE, ensureLogin, currentUser } from '../../utils/auth'

Page({
  data: {
    orders: [],
    loading: true,
    error: '',
    totalOrders: 0,
    paidOrders: 0,
    browseHistory: [],
    nickname: '微信用户',
    loginHint: '正在登录...',
  },

  onShow() {
    this.getTabBar().init()
    const user = currentUser()
    this.setData({
      browseHistory: wx.getStorageSync('browseHistory') || [],
      nickname: user.nickname || '微信用户',
      loginHint: user.mode === 'wechat' ? `微信用户 #${user.userId || ''}` : `本地演示用户 #${user.userId || 1}`,
    })
    this.loadOrders()
    this.startCountdown()
  },

  onHide() {
    this.stopCountdown()
  },

  onUnload() {
    this.stopCountdown()
  },

  startCountdown() {
    this.stopCountdown()
    this.countdownTimer = setInterval(() => {
      const orders = this.data.orders.map((order) => ({ ...order, countdownText: this.countdownText(order) }))
      this.setData({ orders })
      if (orders.some((order) => order.orderStatus === 'PENDING_PAY' && !order.countdownText.includes('0分0秒'))) return
      if (orders.some((order) => order.orderStatus === 'PENDING_PAY')) this.loadOrders()
    }, 1000)
  },

  stopCountdown() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
      this.countdownTimer = null
    }
  },

  loadOrders() {
    this.setData({ loading: true, error: '' })
    ensureLogin().then((user) => {
      this.setData({
        nickname: user.nickname || '微信用户',
        loginHint: user.mode === 'wechat' ? `微信用户 #${user.userId}` : `本地演示用户 #${user.userId}`,
      })
      wx.request({
        url: `${API_BASE}/api/orders`,
        data: { userId: user.userId },
        success: (response) => {
          if (response.statusCode !== 200 || !Array.isArray(response.data)) {
            this.setData({ loading: false, error: response.data?.message || '订单加载失败' })
            return
          }
          const orders = response.data.map((order) => ({
            ...order,
            totalText: Number(order.totalAmount || 0).toFixed(2),
            statusText: this.statusText(order.orderStatus),
            canCancel: order.orderStatus === 'PENDING_PAY',
            countdownText: this.countdownText(order),
            itemSummary: (order.items || []).map((item) => `${item.productName} x${item.quantity}`).join('、'),
            timeText: (order.createTime || '').replace('T', ' '),
          }))
          this.setData({
            orders,
            totalOrders: orders.length,
            paidOrders: orders.filter((order) => order.payStatus === 1).length,
            loading: false,
          })
        },
        fail: (error) => this.setData({ loading: false, error: error.errMsg || '无法连接订货服务' }),
      })
    }).catch((error) => this.setData({ loading: false, error: error.message || '请先登录' }))
  },

  countdownText(order) {
    if (order.orderStatus !== 'PENDING_PAY') return ''
    const created = new Date((order.createTime || '').replace('T', ' ').replace(/-/g, '/')).getTime()
    const seconds = Math.max(0, Math.ceil((created + 10 * 60 * 1000 - Date.now()) / 1000))
    return `剩余支付时间约 ${Math.floor(seconds / 60)}分${seconds % 60}秒，超时自动取消`
  },

  statusText(status) {
    return {
      PENDING_PAY: '待支付',
      PAID: '已支付，待商家处理',
      TO_SHIP: '待发货',
      SHIPPED: '已发货',
      COMPLETED: '已完成',
      CANCELLED: '已取消',
    }[status] || status || '未知状态'
  },

  goShop() {
    wx.switchTab({ url: '/pages/home/home' })
  },

  goAddress() {
    wx.navigateTo({ url: '/pages/user/address/list/index' })
  },

  viewOrder(event) {
    const order = event.currentTarget.dataset.order
    wx.navigateTo({ url: `/pages/order/order-detail/index?id=${order.id}` })
  },

  confirmReceipt(event) {
    const order = event.currentTarget.dataset.order
    wx.showModal({
      title: '确认收货',
      content: `确认已收到订单 ${order.orderNo} 吗？`,
      success: (result) => {
        if (!result.confirm) return
        ensureLogin().then((user) => {
          wx.request({
            url: `${API_BASE}/api/orders/${order.id}/confirm-receipt?userId=${user.userId}`,
            method: 'POST',
            success: (response) => {
              if (response.statusCode === 200) {
                wx.showToast({ title: '已确认收货', icon: 'success' })
                this.loadOrders()
              } else wx.showToast({ title: response.data?.message || '确认失败', icon: 'none' })
            },
            fail: () => wx.showToast({ title: '无法连接订货服务', icon: 'none' }),
          })
        })
      },
    })
  },

  cancelOrder(event) {
    const order = event.currentTarget.dataset.order
    wx.showModal({
      title: '取消订单',
      content: `确定取消订单 ${order.orderNo} 吗？`,
      success: (result) => {
        if (!result.confirm) return
        ensureLogin().then((user) => {
          wx.request({
            url: `${API_BASE}/api/orders/${order.id}/cancel?userId=${user.userId}`,
            method: 'POST',
            success: (response) => {
              if (response.statusCode === 200) {
                wx.showToast({ title: '订单已取消', icon: 'success' })
                this.loadOrders()
              } else wx.showToast({ title: response.data?.message || '取消失败', icon: 'none' })
            },
            fail: () => wx.showToast({ title: '无法连接订货服务', icon: 'none' }),
          })
        })
      },
    })
  },

  openHistory(event) {
    wx.navigateTo({ url: `/pages/goods/details/index?id=${event.currentTarget.dataset.id}` })
  },

  refresh() {
    this.loadOrders()
  },
})
