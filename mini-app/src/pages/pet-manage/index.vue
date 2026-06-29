<template>
  <view class="container">
    <view class="section-title">我的宠物</view>

    <view v-for="pet in pets" :key="pet.id" class="card pet-card" @tap="editPet(pet.id)">
      <view class="pet-header">
        <view class="pet-avatar">{{ getSpeciesIcon(pet.species) }}</view>
        <view class="pet-info">
          <text class="pet-name">{{ pet.name }}</text>
          <text class="pet-meta">{{ pet.breed }} · {{ pet.weight }}kg</text>
        </view>
        <view class="pet-edit-hint">编辑</view>
      </view>
      <view class="pet-details" v-if="pet.personality">
        <text class="pet-detail-label">性格：</text>
        <text>{{ pet.personality }}</text>
      </view>
      <view class="pet-details" v-if="pet.dietInfo">
        <text class="pet-detail-label">饮食：</text>
        <text>{{ pet.dietInfo }}</text>
      </view>
    </view>

    <view v-if="!loading && pets.length === 0" class="empty">
      <text>暂无宠物</text>
      <view class="btn-primary add-btn" @tap="addPet">添加宠物</view>
    </view>

    <view class="btn-primary add-btn" v-if="pets.length > 0" @tap="addPet">添加宠物</view>
    <view v-if="loading" class="loading">加载中...</view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { petApi } from '@/api'
import type { Pet } from '@/types'

const pets = ref<Pet[]>([])
const loading = ref(true)

const speciesIcons: Record<string, string> = {
  CAT: '🐱', DOG: '🐶', BIRD: '🐦', FISH: '🐟',
  REPTILE: '🦎', SMALL_ANIMAL: '🐹', OTHER: '🐾',
}
function getSpeciesIcon(species: string) { return speciesIcons[species] || '🐾' }

function addPet() {
  uni.navigateTo({ url: '/pages/pet-edit/index' })
}

function editPet(id: number) {
  uni.navigateTo({ url: `/pages/pet-edit/index?id=${id}` })
}

async function loadPets() {
  loading.value = true
  try {
    pets.value = await petApi.list()
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onShow(() => { loadPets() })
</script>

<style scoped>
.pet-card { padding: 28rpx; }
.pet-header { display: flex; align-items: center; margin-bottom: 16rpx; }
.pet-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  background: #FFF3ED;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
}
.pet-info { margin-left: 20rpx; flex: 1; }
.pet-edit-hint { font-size: 24rpx; color: #FF6B35; }
.pet-name { font-size: 30rpx; font-weight: 600; display: block; }
.pet-meta { font-size: 24rpx; color: #999; margin-top: 4rpx; display: block; }
.pet-details { font-size: 26rpx; color: #666; margin-top: 8rpx; }
.pet-detail-label { color: #999; }
.empty { text-align: center; padding: 80rpx 0; color: #999; }
.add-btn { margin-top: 32rpx; }
.loading { text-align: center; padding: 40rpx; color: #999; }
</style>
