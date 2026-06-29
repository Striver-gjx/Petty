import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Owner, Pet } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(uni.getStorageSync('token') || '')
  const owner = ref<Owner | null>(null)
  const pets = ref<Pet[]>([])
  const isLoggedIn = ref(!!token.value)
  const role = ref<string>(uni.getStorageSync('role') || 'OWNER')

  function setLoginData(data: { token: string; userId: number; name: string }, userRole: string) {
    token.value = data.token
    role.value = userRole
    isLoggedIn.value = true
    uni.setStorageSync('token', data.token)
    uni.setStorageSync('role', userRole)
    owner.value = { id: data.userId, nickname: data.name } as Owner
  }

  function setOwner(data: Owner) {
    owner.value = data
  }

  function setPets(data: Pet[]) {
    pets.value = data
  }

  function logout() {
    token.value = ''
    owner.value = null
    pets.value = []
    isLoggedIn.value = false
    role.value = 'OWNER'
    uni.removeStorageSync('token')
    uni.removeStorageSync('role')
  }

  return { token, owner, pets, isLoggedIn, role, setLoginData, setOwner, setPets, logout }
})
