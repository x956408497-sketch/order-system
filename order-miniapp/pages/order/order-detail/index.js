import { API_BASE, ensureLogin } from '../../../utils/auth'

Page({
  data: { order: null, loading: true, error: '', totalText: '' },
  onLoad(options) {
    const id = Number(options.id)
    if (!Number.isInteger(id) || id <= 0) {
      this.setData({ loading: false, error: '订单编号无效' })
      return
    }
    this.orderId = id
    this.loadDetail()
  },
  loadDetail() {
    ensureLogin().then((user) => {
      wx.request({
        url: `${API_BASE}/api/orders/${this.orderId}?userId=${user.userId}`,
        success: (response) => {
          if (response.statusCode !== 200 || !response.data?.id) {
            this.setData({ loading: false, error: response.data?.message || '订单不存在' })
            return
          }
          const order = response.data
          order.statusText = this.statusText(order.orderStatus)
          order.timeText = (order.createTime || '').replace('T', ' ')
          order.items = (order.items || []).map((item) => ({ ...item, subtotalText: Number(item.subtotal || 0).toFixed(2) }))
          this.setData({ order, totalText: Number(order.totalAmount || 0).toFixed(2), loading: false })
        },
        fail: (error) => this.setData({ loading: false, error: error.errMsg || '无法连接订货服务' }),
      })
    }).catch((error) => this.setData({ loading: false, error: error.message || '请先登录' }))
  },
  statusText(status) {
    return { PENDING_PAY: '待支付', PAID: '已支付，待商家处理', TO_SHIP: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }[status] || status
  },
  confirmReceipt() {
    const order = this.data.order
    if (!order || order.orderStatus !== 'SHIPPED') return
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
                this.loadDetail()
              } else wx.showToast({ title: response.data?.message || '确认失败', icon: 'none' })
            },
            fail: () => wx.showToast({ title: '无法连接订货服务', icon: 'none' }),
          })
        })
      },
    })
  },
  backToOrders() { wx.navigateBack() },
})
