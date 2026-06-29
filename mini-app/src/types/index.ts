export interface ServiceType {
  id: number
  name: string
  code: string
  description: string
  iconUrl: string | null
  baseDurationMin: number
  basePrice: number
  extraPetPrice: number
  applicableSpecies: string
  checklistTemplate: string
  sortOrder: number
}

export interface Owner {
  id: number
  nickname: string
  phone: string
  avatarUrl: string | null
  address: string
  memberLevel: string
  totalOrders: number
}

export interface Pet {
  id: number
  ownerId: number
  name: string
  species: string
  breed: string
  gender: string
  weight: number
  personality: string
  dietInfo: string
  avatarUrl: string | null
}

export interface Sitter {
  id: number
  name: string
  phone: string
  avatarUrl: string | null
  bio: string
  experienceYears: number
  serviceArea: string
  rating: number
  totalOrders: number
  basePrice: number
  status: string
  acceptedSpecies: string
}

export interface OrderVO {
  id: number
  orderNo: string
  serviceTypeName: string
  scheduledDate: string
  scheduledStartTime: string
  scheduledEndTime: string
  totalAmount: number
  status: string
  statusLabel: string
  sitterName: string
  sitterAvatarUrl: string | null
  ownerNickname: string
  petCount: number
  createdAt: string
}

export interface OrderDetailVO extends OrderVO {
  serviceAddress: string
  pets: OrderPetVO[]
  sitterPhone: string
  remark: string
  actualStartTime: string | null
  actualEndTime: string | null
  serviceLogs: ServiceLogVO[]
}

export interface OrderPetVO {
  petId: number
  petName: string
  species: string
  avatarUrl: string | null
  specialNotes: string
}

export interface ServiceLogVO {
  id: number
  logType: string
  description: string
  photoUrls: string[]
  videoUrl: string | null
  petStatus: string
  createdAt: string
}

export interface ReviewVO {
  id: number
  orderId: number
  reviewerType: string
  reviewerNickname: string
  reviewerAvatar: string | null
  rating: number
  content: string
  photoUrls: string[]
  tags: string[]
  createdAt: string
}
