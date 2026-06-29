import { request } from '@/utils/request'
import type { ServiceType, Sitter, Pet, OrderVO, OrderDetailVO, ServiceLogVO, ReviewVO } from '@/types'

export const authApi = {
  login: (phone: string, role: string) =>
    request<{ token: string; userId: number; name: string }>({
      url: '/auth/login',
      method: 'POST',
      data: { phone, role },
      noAuth: true,
    }),
  me: () => request<{ userId: number; role: string }>({ url: '/auth/me' }),
}

export const serviceTypeApi = {
  list: () => request<ServiceType[]>({ url: '/service-types' }),
}

export const sitterApi = {
  list: (status?: string) => request<Sitter[]>({ url: '/sitters', params: status ? { status } : undefined }),
  get: (id: number) => request<Sitter>({ url: `/sitters/${id}` }),
}

export const petApi = {
  list: () => request<Pet[]>({ url: '/pets' }),
  get: (id: number) => request<Pet>({ url: `/pets/${id}` }),
  create: (data: Partial<Pet>) => request<Pet>({ url: '/pets', method: 'POST', data: data as Record<string, unknown> }),
  update: (id: number, data: Partial<Pet>) => request<void>({ url: `/pets/${id}`, method: 'PUT', data: data as Record<string, unknown> }),
  delete: (id: number) => request<void>({ url: `/pets/${id}`, method: 'DELETE' }),
}

export const orderApi = {
  list: (params?: Record<string, unknown>) => request<OrderVO[]>({ url: '/orders', params }),
  get: (id: number) => request<OrderDetailVO>({ url: `/orders/${id}` }),
  create: (data: Record<string, unknown>) =>
    request<OrderVO>({ url: '/orders', method: 'POST', data }),
  confirm: (id: number) =>
    request<void>({ url: `/orders/${id}/confirm`, method: 'POST' }),
  cancel: (id: number, reason: string) =>
    request<void>({ url: `/orders/${id}/cancel`, method: 'POST', data: { reason } }),
  accept: (id: number) =>
    request<void>({ url: `/orders/${id}/accept`, method: 'POST' }),
  reject: (id: number, reason?: string) =>
    request<void>({ url: `/orders/${id}/reject`, method: 'POST', data: reason ? { reason } : {} }),
  checkIn: (id: number, data: Record<string, unknown>) =>
    request<void>({ url: `/orders/${id}/check-in`, method: 'POST', data }),
  checkOut: (id: number, data: Record<string, unknown>) =>
    request<void>({ url: `/orders/${id}/check-out`, method: 'POST', data }),
  addLog: (id: number, data: Record<string, unknown>) =>
    request<void>({ url: `/orders/${id}/logs`, method: 'POST', data }),
  getLogs: (id: number) => request<ServiceLogVO[]>({ url: `/orders/${id}/logs` }),
}

export const reviewApi = {
  create: (data: Record<string, unknown>) =>
    request<void>({ url: '/reviews', method: 'POST', data }),
  listBySitter: (sitterId: number) => request<ReviewVO[]>({ url: `/reviews/sitter/${sitterId}` }),
}

export const paymentApi = {
  pay: (orderId: number, paymentMethod: string) =>
    request<{ prepayId: string; timeStamp: string }>({
      url: '/payments/pay',
      method: 'POST',
      data: { orderId, paymentMethod },
    }),
  getByOrder: (orderId: number) => request<Record<string, unknown>>({ url: `/payments/order/${orderId}` }),
}
