export const API_BASE = 'http://localhost:8080'

function persist(data) {
  const app = getApp()
  if (app && app.globalData) {
    app.globalData.token = data.token
    app.globalData.userId = data.userId
    app.globalData.nickname = data.nickname
    app.globalData.loginMode = data.mode
  }
  wx.setStorageSync('loginToken', data.token)
  wx.setStorageSync('userId', data.userId)
  wx.setStorageSync('nickname', data.nickname || '')
  wx.setStorageSync('loginMode', data.mode || '')
}

export function currentUser() {
  const app = getApp()
  return {
    token: app?.globalData?.token || wx.getStorageSync('loginToken') || '',
    userId: app?.globalData?.userId || wx.getStorageSync('userId') || null,
    nickname: app?.globalData?.nickname || wx.getStorageSync('nickname') || '微信用户',
    mode: app?.globalData?.loginMode || wx.getStorageSync('loginMode') || '',
  }
}

function sendCode(code) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${API_BASE}/api/auth/wechat-login`,
      method: 'POST',
      header: { 'content-type': 'application/json' },
      data: { code },
      success: (response) => {
        if (response.statusCode !== 200 || !response.data?.userId) {
          reject(new Error(response.data?.message || '登录失败'))
          return
        }
        persist(response.data)
        resolve(currentUser())
      },
      fail: (error) => reject(new Error(error.errMsg || '无法连接订货服务')),
    })
  })
}

export function login() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (result) => {
        sendCode(result.code).then(resolve).catch(() => {
          sendCode('local-demo').then(resolve).catch(reject)
        })
      },
      fail: () => sendCode('local-demo').then(resolve).catch(reject),
    })
  })
}

export function ensureLogin() {
  const user = currentUser()
  if (user.userId) return Promise.resolve(user)
  return login()
}
