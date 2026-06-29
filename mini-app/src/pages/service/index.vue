<template>
  <view class="container">
    <view class="section-title">全部服务</view>
    <view v-for="item in serviceTypes" :key="item.id" class="card service-card" @tap="goToOrder(item)">
      <view class="service-header">
        <text class="service-icon-large">{{ getIcon(item.code) }}</text>
        <view class="service-info">
          <text class="service-name">{{ item.name }}</text>
          <text class="service-desc">{{ item.description }}</text>
        </view>
        <text class="text-price">¥{{ item.basePrice }}</text>
      </view>
      <view class="service-tags">
        <text class="tag">{{ item.baseDurationMin }}分钟</text>
        <text class="tag" v-if="item.extraPetPrice > 0">多宠+¥{{ item.extraPetPrice }}/只</text>
      </view>
    </view>
    <view v-if="loading" class="loading">加载中...</view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { serviceTypeApi } from '@/api'
import type { ServiceType } from '@/types'

const serviceTypes = ref<ServiceType[]>([])
const loading = ref(true)

const iconMap: Record<string, string> = {
  FEEDING: '🍲', DOG_WALKING: '🐕', PLAY_SESSION: '🎾',
  GROOMING: '✂️', MEDICATION: '💊', BOARDING: '🏠',
}
function getIcon(code: string) { return iconMap[code] || '🐾' }

function goToOrder(item: ServiceType) {
  uni.navigateTo({ url: `/pages/order-create/index?serviceTypeId=${item.id}&name=${item.name}&price=${item.basePrice}` })
}

onMounted(async () => {
  try {
    serviceTypes.value = await serviceTypeApi.list()
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})
</script>

<style scoped>
.service-card { padding: 28rpx; }
.service-header { display: flex; align-items: center; margin-bottom: 16rpx; }
.service-icon-large { font-size: 56rpx; margin-right: 20rpx; }
.service-info { flex: 1; }
.service-name { font-size: 30rpx; font-weight: 600; display: block; }
.service-desc { font-size: 24rpx; color: #666; margin-top: 4rpx; display: block; }
.service-tags { display: flex; gap: 12rpx; }
.tag {
  background: #FFF3ED;
  color: #FF6B35;
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}
.loading { text-align: center; padding: 40rpx; color: #999; }
</style>
