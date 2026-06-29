<template>
  <view class="container">
    <view class="tabs">
      <view v-for="tab in tabs" :key="tab.value"
        :class="['tab', currentTab === tab.value && 'tab-active']"
        @tap="currentTab = tab.value">
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <view v-if="loading" class="loading">
      <text class="text-muted">加载中...</text>
    </view>

    <view v-else-if="orders.length === 0" class="empty">
      <text class="text-muted">暂无订单</text>
    </view>

    <view v-else class="order-list">
      <view v-for="order in orders" :key="order.id" class="card order-card"
        @tap="goDetail(order)">
        <view class="order-header">
          <text class="order-no">{{ order.orderNo }}</text>
          <text :class="['status-tag', `status-${order.status}`]">{{ order.statusLabel }}</text>
        </view>
        <view class="order-body">
          <text class="owner-name">主人: {{ order.ownerNickname }}</text>
          <text class="pet-count">{{ order.petCount }}只宠物</text>
          <text class="schedule">{{ order.scheduledDate }} {{ order.scheduledStartTime?.slice(0,5) }}</text>
        </view>
        <view class="order-footer">
          <text class="text-price">¥{{ order.totalAmount }}</text>
          <view v-if="order.status === 'PENDING_ACCEPT'" class="action-btns">
            <view class="btn-reject" @tap.stop="rejectOrder(order.id)">拒单</view>
            <view class="btn-accept" @tap.stop="acceptOrder(order.id)">接单</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { orderApi } from '@/api'
import type { OrderVO } from '@/types'

const tabs = [
  { label: '待接单', value: 'PENDING_ACCEPT' },
  { label: '已接单', value: 'ACCEPTED' },
  { label: '服务中', value: 'IN_SERVICE' },
  { label: '已完成', value: 'completed' },
]
const currentTab = ref('PENDING_ACCEPT')
const orders = ref<OrderVO[]>([])
const loading = ref(false)

async function loadOrders() {
  loading.value = true
  try {
    const status = currentTab.value === 'completed' ? 'SERVICE_COMPLETED,OWNER_CONFIRMED' : currentTab.value
    orders.value = await orderApi.list({ status })
  } catch (e) { console.error(e) }
  loading.value = false
}

function goDetail(order: OrderVO) {
  uni.navigateTo({ url: `/pages/sitter-work/index?orderId=${order.id}` })
}

async function acceptOrder(id: number) {
  try {
    await orderApi.accept(id)
    uni.showToast({ title: '接单成功', icon: 'success' })
    loadOrders()
  } catch (e) { console.error(e) }
}

async function rejectOrder(id: number) {
  uni.showModal({
    title: '确认拒单',
    content: '拒单后订单将重新匹配其他喂养师',
    success: async (res) => {
      if (res.confirm) {
        try {
          await orderApi.reject(id, '时间冲突')
          uni.showToast({ title: '已拒单', icon: 'none' })
          loadOrders()
        } catch (e) { console.error(e) }
      }
    }
  })
}

watch(currentTab, () => loadOrders())
onShow(() => loadOrders())
onMounted(() => loadOrders())
</script>

<style scoped>
.tabs { display: flex; background: #fff; border-radius: 12rpx; margin-bottom: 24rpx; overflow: hidden; }
.tab { flex: 1; text-align: center; padding: 24rpx 0; font-size: 26rpx; color: #666; }
.tab-active { color: #FF6B35; font-weight: bold; border-bottom: 4rpx solid #FF6B35; }
.order-card { margin-bottom: 20rpx; }
.order-header { display: flex; justify-content: space-between; margin-bottom: 16rpx; }
.order-no { font-size: 24rpx; color: #999; }
.status-tag { font-size: 24rpx; padding: 4rpx 16rpx; border-radius: 20rpx; background: #f0f0f0; }
.status-PENDING_ACCEPT { background: #FFF3E0; color: #E65100; }
.status-ACCEPTED { background: #E3F2FD; color: #1565C0; }
.status-IN_SERVICE { background: #E8F5E9; color: #2E7D32; }
.order-body { margin-bottom: 16rpx; }
.order-body text { display: block; font-size: 26rpx; color: #666; margin: 4rpx 0; }
.owner-name { font-size: 28rpx !important; color: #333 !important; font-weight: 500; }
.order-footer { display: flex; justify-content: space-between; align-items: center; }
.action-btns { display: flex; gap: 16rpx; }
.btn-reject { padding: 12rpx 28rpx; border: 2rpx solid #ddd; border-radius: 32rpx; font-size: 26rpx; color: #666; }
.btn-accept { padding: 12rpx 28rpx; background: #FF6B35; border-radius: 32rpx; font-size: 26rpx; color: #fff; }
.loading, .empty { text-align: center; padding: 120rpx 0; }
</style>
