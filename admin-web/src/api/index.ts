import request from './request';

export const authApi = {
  login: (phone: string, role: string) => request.post('/auth/login', { phone, role }),
  me: () => request.get('/auth/me'),
};

export const ownerApi = {
  list: () => request.get('/owners'),
  get: (id: number) => request.get(`/owners/${id}`),
  create: (data: Record<string, unknown>) => request.post('/owners', data),
  update: (id: number, data: Record<string, unknown>) => request.put(`/owners/${id}`, data),
  delete: (id: number) => request.delete(`/owners/${id}`),
};

export const petApi = {
  list: (ownerId?: number) => request.get('/pets', { params: ownerId ? { ownerId } : undefined }),
  get: (id: number) => request.get(`/pets/${id}`),
  create: (data: Record<string, unknown>) => request.post('/pets', data),
  update: (id: number, data: Record<string, unknown>) => request.put(`/pets/${id}`, data),
  delete: (id: number) => request.delete(`/pets/${id}`),
};

export const sitterApi = {
  list: (status?: string) => request.get('/sitters', { params: status ? { status } : undefined }),
  get: (id: number) => request.get(`/sitters/${id}`),
  create: (data: Record<string, unknown>) => request.post('/sitters', data),
  update: (id: number, data: Record<string, unknown>) => request.put(`/sitters/${id}`, data),
  approve: (id: number) => request.post(`/sitters/${id}/approve`),
  reject: (id: number, reason?: string) => request.post(`/sitters/${id}/reject`, { reason }),
  suspend: (id: number) => request.put(`/sitters/${id}`, { status: 'SUSPENDED' }),
  activate: (id: number) => request.put(`/sitters/${id}`, { status: 'ACTIVE' }),
};

export const orderApi = {
  listAll: (params?: Record<string, unknown>) => request.get('/orders/all', { params }),
  get: (id: number) => request.get(`/orders/${id}`),
};

export const serviceTypeApi = {
  list: () => request.get('/service-types'),
  create: (data: Record<string, unknown>) => request.post('/service-types', data),
  update: (id: number, data: Record<string, unknown>) => request.put(`/service-types/${id}`, data),
  delete: (id: number) => request.delete(`/service-types/${id}`),
};
