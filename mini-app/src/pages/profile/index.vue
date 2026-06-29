<template>
  <view class="container">
    <!-- 用户信息 -->
    <view class="card user-card">
      <view class="user-avatar">{{ owner?.nickname?.[0] || '?' }}</view>
      <view class="user-info">
        <text class="user-name">{{ owner?.nickname || '未登录' }}</text>
        <text class="user-level">{{ levelLabel }}</text>
      </view>
    </view>

    <!-- 菜单 -->
    <view class="card menu-card">
      <view class="menu-item" @tap="goToPets">
        <text class="menu-icon">🐾</text>
        <text class="menu-label">我的宠物</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goToOrders">
        <text class="menu-icon">📋</text>
        <text class="menu-label">我的订单</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item">
        <text class="menu-icon">📍</text>
        <text class="menu-label">地址管理</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item">
        <text class="menu-icon">⭐</text>
        <text class="menu-label">我的评价</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item">
        <text class="menu-icon">⚙️</text>
        <text class="menu-label">设置</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 统计 -->
    <view class="card stats-card">
      <view class="stat-item">
        <text class="stat-value">{{ owner?.totalOrders || 0 }}</text>
        <text class="stat-label">总订单</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ pets.length }}</text>
        <text class="stat-label">宠物数</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ owner?.memberLevel || '-' }}</text>
        <text class="stat-label">会员等级</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { petApi } from '@/api'
import type { Owner, Pet } from '@/types'

const owner = ref<Owner>({ id: 1, nickname: '张小花', phone: '13800001111', avatarUrl: null, address: '北京市朝阳区建国路88号', memberLevel: 'VIP', totalOrders: 0 })
const pets = ref<Pet[]>([])

const levelLabel = computed(() => {
  const map: Record<string, string> = { NORMAL: '普通会员', VIP: 'VIP会员', SVIP: 'SVIP会员' }
  return map[owner.value?.memberLevel || ''] || '普通会员'
})

function goToPets() { uni.navigateTo({ url: '/pages/pet-manage/index' }) }
function goToOrders() { uni.switchTab({ url: '/pages/orders/index' }) }

onMounted(async () => {
  try {
    pets.value = await petApi.list(1)
  } catch (e) { console.error(e) }
})
</script>

<style scoped>
.user-card {
  display: flex;
  align-items: center;
  padding: 32rpx;
}
.user-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #FF8F5E);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: 600;
}
.user-info { margin-left: 24rpx; }
.user-name { font-size: 34rpx; font-weight: 600; display: block; }
.user-level { font-size: 24rpx; color: #FF6B35; margin-top: 4rpx; display: block; }

.menu-card { padding: 0; }
.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #f5f5f5;
}
.menu-item:last-child { border-bottom: none; }
.menu-icon { font-size: 36rpx; margin-right: 20rpx; }
.menu-label { flex: 1; font-size: 28rpx; color: #333; }
.menu-arrow { font-size: 32rpx; color: #ccc; }

.stats-card {
  display: flex;
  justify-content: space-around;
  padding: 32rpx;
}
.stat-item { text-align: center; }
.stat-value { font-size: 36rpx; font-weight: 700; color: #333; display: block; }
.stat-label { font-size: 22rpx; color: #999; margin-top: 8rpx; display: block; }
</style>
