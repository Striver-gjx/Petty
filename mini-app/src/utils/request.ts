const BASE_URL = '/api/v1'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, unknown>
  params?: Record<string, unknown>
}

interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

function buildUrl(url: string, params?: Record<string, unknown>): string {
  if (!params) return url
  const query = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null)
    .map(([k, v]) => `${k}=${encodeURIComponent(String(v))}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

export function request<T = unknown>(options: RequestOptions): Promise<T> {
  const { url, method = 'GET', data, params } = options
  const fullUrl = buildUrl(`${BASE_URL}${url}`, params)

  return new Promise((resolve, reject) => {
    uni.request({
      url: fullUrl,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
      },
      success: (res) => {
        const body = res.data as ApiResponse<T>
        if (body.code === 200) {
          resolve(body.data)
        } else {
          uni.showToast({ title: body.message || '请求失败', icon: 'none' })
          reject(new Error(body.message))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      },
    })
  })
}
