const API_BASE = 'http://localhost:8080'

function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      ...options,
      success: resolve,
      fail: reject,
    })
  })
}

function adaptProduct(product) {
  return {
    ...product,
    spuId: product.id,
    title: product.name,
    price: Number(product.price || 0).toFixed(2),
    thumb: product.imageUrl || '',
    tags: product.stock <= 0 ? ['暂时缺货'] : ['现货'],
    originPrice: '',
  }
}

export async function fetchGoodsList() {
  const response = await request({
    url: `${API_BASE}/api/products`,
    method: 'GET',
  })
  if (response.statusCode !== 200 || !Array.isArray(response.data)) {
    throw new Error('商品接口返回格式不正确')
  }
  return response.data.map(adaptProduct)
}
