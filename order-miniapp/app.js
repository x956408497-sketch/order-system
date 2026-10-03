import updateManager from './common/updateManager'
import { login } from './utils/auth'

App({
  globalData: {
    token: '',
    userId: null,
    nickname: '',
    loginMode: '',
  },

  onLaunch() {
    this.globalData.token = wx.getStorageSync('loginToken') || ''
    this.globalData.userId = wx.getStorageSync('userId') || null
    this.globalData.nickname = wx.getStorageSync('nickname') || ''
    this.globalData.loginMode = wx.getStorageSync('loginMode') || ''
    login().catch(() => {})
  },

  onShow() {
    updateManager()
  },
})
