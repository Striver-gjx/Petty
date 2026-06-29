import request from './request';

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
};

export const orderApi = {
  list: (params?: Record<string, unknown>) => request.get('/orders', { params }),
  get: (id: number) => request.get(`/orders/${id}`),
  create: (data: Record<string, unknown>) => request.post('/orders', data),
  updateStatus: (id: number, status: string) => request.put(`/orders/${id}/status`, { status }),
};

export const serviceTypeApi = {
  list: () => request.get('/service-types'),
};
