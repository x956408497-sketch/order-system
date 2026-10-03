import { API_BASE, ensureLogin } from '../../../utils/auth'

Page({
  data: {
    items: [],
    addresses: [],
    addressId: null,
    selectedAddress: null,
    total: '0.00',
    loading: true,
    submitting: false,
    error: '',
    showAddressForm: false,
    addressForm: { receiverName: '', phone: '', detailAddress: '' },
  },

  onLoad() {
    this.loadData()
  },

  loadData() {
    this.setData({ loading: true, error: '' })
    ensureLogin().then((user) => Promise.all([
      this.request('/api/cart', { userId: user.userId }),
      this.request('/api/addresses', { userId: user.userId }),
    ]).then(([cart, addresses]) => {
      const items = cart.map((item) => ({
        ...item,
        priceText: Number(item.price || 0).toFixed(2),
        subtotal: (Number(item.price || 0) * item.quantity).toFixed(2),
      }))
      const total = items.reduce((sum, item) => sum + Number(item.subtotal), 0).toFixed(2)
      const defaultAddress = addresses.find((address) => address.isDefault === 1) || addresses[0]
      this.setData({
        items,
        addresses,
        addressId: defaultAddress ? defaultAddress.id : null,
        selectedAddress: defaultAddress || null,
        total,
        loading: false,
      })
    })).catch((error) => this.setData({ error: error.message || '订单信息加载失败', loading: false }))
  },

  request(path, data = {}, method = 'GET', body) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${API_BASE}${path}`,
        data: body || data,
        method,
        header: { 'content-type': 'application/json' },
        success: (response) => {
          if (response.statusCode >= 200 && response.statusCode < 300) resolve(response.data)
          else reject(new Error(response.data?.message || `服务返回 ${response.statusCode}`))
        },
        fail: (error) => reject(new Error(error.errMsg || '无法连接订货服务')),
      })
    })
  },

  toggleAddressForm() {
    this.setData({ showAddressForm: !this.data.showAddressForm })
  },

  selectAddress(event) {
    const address = this.data.addresses[Number(event.detail.value)]
    this.setData({ addressId: address.id, selectedAddress: address })
  },

  inputAddress(event) {
    const field = event.currentTarget.dataset.field
    this.setData({ [`addressForm.${field}`]: event.detail.value })
  },

  saveAddress() {
    const form = this.data.addressForm
    if (!form.receiverName.trim() || !form.phone.trim() || !form.detailAddress.trim()) {
      wx.showToast({ title: '请填写完整地址', icon: 'none' })
      return
    }
    ensureLogin().then((user) => this.request(`/api/addresses?userId=${user.userId}`, {}, 'POST', {
      ...form,
      isDefault: this.data.addresses.length ? 0 : 1,
    })).then((address) => {
      this.setData({
        addresses: this.data.addresses.concat(address),
        addressId: address.id,
        selectedAddress: address,
        showAddressForm: false,
        addressForm: { receiverName: '', phone: '', detailAddress: '' },
      })
      wx.showToast({ title: '地址已保存', icon: 'success' })
    }).catch((error) => wx.showToast({ title: error.message, icon: 'none' }))
  },

  submitOrder() {
    if (!this.data.items.length) {
      wx.showToast({ title: '购物车为空', icon: 'none' })
      return
    }
    if (!this.data.addressId) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    ensureLogin().then((user) => this.request(`/api/orders?userId=${user.userId}`, {}, 'POST', {
      addressId: this.data.addressId,
      remark: '微信小程序订单',
    })).then((order) => {
      this.setData({ submitting: false })
      wx.showModal({
        title: '订单已创建',
        content: `订单 ${order.orderNo} 待支付，库存已暂时锁定。是否立即模拟支付？`,
        confirmText: '立即支付',
        cancelText: '稍后支付',
        success: (result) => {
          if (result.confirm) this.payOrder(order)
          else wx.switchTab({ url: '/pages/usercenter/index' })
        },
      })
    }).catch((error) => {
      this.setData({ submitting: false })
      wx.showToast({ title: error.message || '下单失败', icon: 'none' })
    })
  },

  payOrder(order) {
    this.setData({ submitting: true })
    ensureLogin().then((user) => this.request(`/api/orders/${order.id}/pay?userId=${user.userId}`, {}, 'POST')).then(() => {
      this.setData({ submitting: false })
      wx.showModal({
        title: '支付成功',
        content: `订单 ${order.orderNo} 已完成模拟支付`,
        showCancel: false,
        success: () => wx.switchTab({ url: '/pages/usercenter/index' }),
      })
    }).catch((error) => {
      this.setData({ submitting: false })
      wx.showToast({ title: error.message || '支付失败', icon: 'none' })
    })
  },
})
