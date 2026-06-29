import { request } from '@/utils/request'
import type { ServiceType, Sitter, Pet, OrderVO, OrderDetailVO, ServiceLogVO, ReviewVO } from '@/types'

export const serviceTypeApi = {
  list: () => request<ServiceType[]>({ url: '/service-types' }),
}

export const sitterApi = {
  list: (status?: string) => request<Sitter[]>({ url: '/sitters', params: status ? { status } : undefined }),
  get: (id: number) => request<Sitter>({ url: `/sitters/${id}` }),
}

export const petApi = {
  list: (ownerId?: number) => request<Pet[]>({ url: '/pets', params: ownerId ? { ownerId } : undefined }),
  get: (id: number) => request<Pet>({ url: `/pets/${id}` }),
  create: (data: Partial<Pet>) => request<Pet>({ url: '/pets', method: 'POST', data: data as Record<string, unknown> }),
  update: (id: number, data: Partial<Pet>) => request<void>({ url: `/pets/${id}`, method: 'PUT', data: data as Record<string, unknown> }),
  delete: (id: number) => request<void>({ url: `/pets/${id}`, method: 'DELETE' }),
}

export const orderApi = {
  list: (params?: Record<string, unknown>) => request<OrderVO[]>({ url: '/orders', params }),
  get: (id: number) => request<OrderDetailVO>({ url: `/orders/${id}` }),
  create: (ownerId: number, data: Record<string, unknown>) =>
    request<OrderVO>({ url: '/orders', method: 'POST', params: { ownerId }, data }),
  accept: (id: number, sitterId: number) =>
    request<void>({ url: `/orders/${id}/accept`, method: 'POST', params: { sitterId } }),
  confirm: (id: number, ownerId: number) =>
    request<void>({ url: `/orders/${id}/confirm`, method: 'POST', params: { ownerId } }),
  cancel: (id: number, userId: number, reason: string) =>
    request<void>({ url: `/orders/${id}/cancel`, method: 'POST', params: { userId }, data: { reason } }),
  getLogs: (id: number) => request<ServiceLogVO[]>({ url: `/orders/${id}/logs` }),
}

export const reviewApi = {
  create: (reviewerId: number, data: Record<string, unknown>) =>
    request<void>({ url: '/reviews', method: 'POST', params: { reviewerId, reviewerType: 'OWNER' }, data }),
  listBySitter: (sitterId: number) => request<ReviewVO[]>({ url: `/reviews/sitter/${sitterId}` }),
}

export const paymentApi = {
  pay: (ownerId: number, orderId: number, method: string) =>
    request<Record<string, string>>({ url: '/payments/pay', method: 'POST', params: { ownerId }, data: { orderId, paymentMethod: method } }),
  getByOrder: (orderId: number) => request<Record<string, unknown>>({ url: `/payments/order/${orderId}` }),
}
