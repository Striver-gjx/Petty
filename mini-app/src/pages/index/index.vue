<template>
  <view class="container">
    <!-- Banner -->
    <view class="banner card">
      <text class="banner-title">宠物上门喂养</text>
      <text class="banner-desc">专业喂养师，让您安心出行</text>
    </view>

    <!-- 服务类型 -->
    <view class="section-title">热门服务</view>
    <view class="service-grid">
      <view
        v-for="item in serviceTypes"
        :key="item.id"
        class="service-item"
        @tap="goToOrderCreate(item)"
      >
        <view class="service-icon">{{ getIcon(item.code) }}</view>
        <text class="service-name">{{ item.name }}</text>
        <text class="service-price">¥{{ item.basePrice }}起</text>
      </view>
    </view>

    <!-- 附近喂养师 -->
    <view class="section-title">推荐喂养师</view>
    <view v-for="sitter in sitters" :key="sitter.id" class="card sitter-card">
      <view class="sitter-info">
        <view class="sitter-avatar">{{ sitter.name[0] }}</view>
        <view class="sitter-detail">
          <text class="sitter-name">{{ sitter.name }}</text>
          <text class="sitter-meta">{{ sitter.serviceArea }} · {{ sitter.experienceYears }}年经验</text>
        </view>
        <view class="sitter-rating">
          <text class="rating-star">★</text>
          <text class="rating-value">{{ sitter.rating }}</text>
        </view>
      </view>
      <text class="sitter-bio">{{ sitter.bio }}</text>
      <view class="sitter-footer">
        <text class="text-price">¥{{ sitter.basePrice }}/次</text>
        <text class="sitter-orders">已服务{{ sitter.totalOrders }}次</text>
      </view>
    </view>

    <view v-if="loading" class="loading">加载中...</view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { serviceTypeApi, sitterApi } from '@/api'
import type { ServiceType, Sitter } from '@/types'

const serviceTypes = ref<ServiceType[]>([])
const sitters = ref<Sitter[]>([])
const loading = ref(true)

const iconMap: Record<string, string> = {
  FEEDING: '🍲',
  DOG_WALKING: '🐕',
  PLAY_SESSION: '🎾',
  GROOMING: '✂️',
  MEDICATION: '💊',
  BOARDING: '🏠',
}

function getIcon(code: string): string {
  return iconMap[code] || '🐾'
}

function goToOrderCreate(item: ServiceType) {
  uni.navigateTo({ url: `/pages/order-create/index?serviceTypeId=${item.id}&name=${item.name}&price=${item.basePrice}` })
}

onMounted(async () => {
  try {
    const [types, sitterList] = await Promise.all([
      serviceTypeApi.list(),
      sitterApi.list('ACTIVE'),
    ])
    serviceTypes.value = types
    sitters.value = sitterList
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.banner {
  background: linear-gradient(135deg, #FF6B35, #FF8F5E);
  color: #fff;
  padding: 40rpx;
  border-radius: 20rpx;
}
.banner-title {
  font-size: 40rpx;
  font-weight: 700;
  display: block;
}
.banner-desc {
  font-size: 26rpx;
  opacity: 0.9;
  margin-top: 10rpx;
  display: block;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  margin-bottom: 32rpx;
}
.service-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx 16rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.service-icon {
  font-size: 48rpx;
  margin-bottom: 8rpx;
}
.service-name {
  font-size: 26rpx;
  color: #333;
  display: block;
}
.service-price {
  font-size: 22rpx;
  color: #FF6B35;
  margin-top: 4rpx;
  display: block;
}

.sitter-card { padding: 28rpx; }
.sitter-info {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}
.sitter-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #FFE8DE;
  color: #FF6B35;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
}
.sitter-detail {
  flex: 1;
  margin-left: 20rpx;
}
.sitter-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #222;
  display: block;
}
.sitter-meta {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}
.sitter-rating {
  display: flex;
  align-items: center;
}
.rating-star { color: #FFB800; font-size: 28rpx; }
.rating-value { color: #FFB800; font-size: 28rpx; font-weight: 600; margin-left: 4rpx; }
.sitter-bio {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sitter-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.sitter-orders { font-size: 22rpx; color: #999; }
.loading { text-align: center; padding: 40rpx; color: #999; }
</style>
