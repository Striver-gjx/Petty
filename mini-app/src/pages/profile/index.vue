<template>
  <view class="container">
    <!-- 用户信息 -->
    <view class="card user-card">
      <view class="user-avatar">{{ displayName?.[0] || '?' }}</view>
      <view class="user-info">
        <text class="user-name">{{ displayName }}</text>
        <text class="user-level">{{ isSitter ? '喂养师' : levelLabel }}</text>
      </view>
    </view>

    <!-- 喂养师菜单 -->
    <view v-if="isSitter" class="card menu-card">
      <view class="menu-item" @tap="goSitterOrders">
        <text class="menu-icon">📋</text>
        <text class="menu-label">我的接单</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item">
        <text class="menu-icon">💰</text>
        <text class="menu-label">收入与提现</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item">
        <text class="menu-icon">⭐</text>
        <text class="menu-label">我的评价</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="handleLogout">
        <text class="menu-icon">🚪</text>
        <text class="menu-label">退出登录</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 主人菜单 -->
    <view v-else class="card menu-card">
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
      <view class="menu-item" @tap="handleLogout">
        <text class="menu-icon">🚪</text>
        <text class="menu-label">退出登录</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 统计 -->
    <view class="card stats-card">
      <view class="stat-item">
        <text class="stat-value">{{ isSitter ? '-' : (owner?.totalOrders || 0) }}</text>
        <text class="stat-label">总订单</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ isSitter ? '-' : pets.length }}</text>
        <text class="stat-label">宠物数</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ isSitter ? '-' : (owner?.memberLevel || '-') }}</text>
        <text class="stat-label">会员等级</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { petApi } from '@/api'
import { useUserStore } from '@/store/user'
import type { Owner, Pet } from '@/types'

const userStore = useUserStore()
const isSitter = computed(() => userStore.role === 'SITTER')
const displayName = computed(() => userStore.owner?.nickname || '用户')

const owner = ref<Owner | null>(null)
const pets = ref<Pet[]>([])

const levelLabel = computed(() => {
  const map: Record<string, string> = { NORMAL: '普通会员', VIP: 'VIP会员', SVIP: 'SVIP会员' }
  return map[owner.value?.memberLevel || ''] || '普通会员'
})

function goToPets() { uni.navigateTo({ url: '/pages/pet-manage/index' }) }
function goToOrders() { uni.switchTab({ url: '/pages/orders/index' }) }
function goSitterOrders() { uni.navigateTo({ url: '/pages/sitter-orders/index' }) }

function handleLogout() {
  uni.showModal({
    title: '确认退出',
    content: '退出后需要重新登录',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.reLaunch({ url: '/pages/login/index' })
      }
    }
  })
}

async function loadData() {
  if (isSitter.value) return
  try {
    pets.value = await petApi.list()
  } catch (e) { console.error(e) }
}

onShow(() => loadData())
onMounted(() => { owner.value = userStore.owner as Owner })
</script>

<style scoped>
.user-card { display: flex; align-items: center; padding: 32rpx; }
.user-avatar { width: 100rpx; height: 100rpx; border-radius: 50%; background: linear-gradient(135deg, #FF6B35, #FF8F5E); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 40rpx; font-weight: 600; }
.user-info { margin-left: 24rpx; }
.user-name { font-size: 34rpx; font-weight: 600; display: block; }
.user-level { font-size: 24rpx; color: #FF6B35; margin-top: 4rpx; display: block; }
.menu-card { padding: 0; }
.menu-item { display: flex; align-items: center; padding: 28rpx 32rpx; border-bottom: 1rpx solid #f5f5f5; }
.menu-item:last-child { border-bottom: none; }
.menu-icon { font-size: 36rpx; margin-right: 20rpx; }
.menu-label { flex: 1; font-size: 28rpx; color: #333; }
.menu-arrow { font-size: 32rpx; color: #ccc; }
.stats-card { display: flex; justify-content: space-around; padding: 32rpx; }
.stat-item { text-align: center; }
.stat-value { font-size: 36rpx; font-weight: 700; color: #333; display: block; }
.stat-label { font-size: 22rpx; color: #999; margin-top: 8rpx; display: block; }
</style>
