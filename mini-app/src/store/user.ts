import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Owner, Pet } from '@/types'

export const useUserStore = defineStore('user', () => {
  const owner = ref<Owner | null>(null)
  const pets = ref<Pet[]>([])
  const isLoggedIn = ref(false)

  function setOwner(data: Owner) {
    owner.value = data
    isLoggedIn.value = true
  }

  function setPets(data: Pet[]) {
    pets.value = data
  }

  function logout() {
    owner.value = null
    pets.value = []
    isLoggedIn.value = false
  }

  return { owner, pets, isLoggedIn, setOwner, setPets, logout }
})
