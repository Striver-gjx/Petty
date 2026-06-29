<template>
  <view class="container">
    <!-- Tab 筛选 -->
    <scroll-view scroll-x class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        :class="['tab', activeTab === tab.value && 'tab-active']"
        @tap="activeTab = tab.value"
      >
        {{ tab.label }}
      </view>
    </scroll-view>

    <!-- 订单列表 -->
    <view v-for="order in filteredOrders" :key="order.id" class="card order-card" @tap="goDetail(order.id)">
      <view class="order-header">
        <text class="order-type">{{ order.serviceTypeName }}</text>
        <text :class="['order-status', `status-${order.status}`]">{{ order.statusLabel }}</text>
      </view>
      <view class="order-body">
        <text class="order-date">{{ order.scheduledDate }} {{ order.scheduledStartTime?.slice(0,5) }}</text>
        <text class="order-sitter" v-if="order.sitterName">喂养师：{{ order.sitterName }}</text>
      </view>
      <view class="order-footer">
        <text class="text-price">¥{{ order.totalAmount }}</text>
        <text class="order-no">{{ order.orderNo }}</text>
      </view>
    </view>

    <view v-if="!loading && filteredOrders.length === 0" class="empty">暂无订单</view>
    <view v-if="loading" class="loading">加载中...</view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { orderApi } from '@/api'
import type { OrderVO } from '@/types'

const orders = ref<OrderVO[]>([])
const loading = ref(true)
const activeTab = ref('ALL')

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '待接单', value: 'PENDING_ACCEPT' },
  { label: '服务中', value: 'IN_SERVICE' },
  { label: '已完成', value: 'OWNER_CONFIRMED' },
  { label: '已取消', value: 'CANCELLED' },
]

const filteredOrders = computed(() => {
  if (activeTab.value === 'ALL') return orders.value
  return orders.value.filter(o => o.status === activeTab.value)
})

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/order-detail/index?id=${id}` })
}

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await orderApi.list({ ownerId: 1 })
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(loadOrders)
onShow(loadOrders)
</script>

<style scoped>
.tabs {
  display: flex;
  white-space: nowrap;
  margin-bottom: 24rpx;
  padding-bottom: 8rpx;
}
.tab {
  display: inline-block;
  padding: 12rpx 28rpx;
  font-size: 26rpx;
  color: #666;
  border-radius: 30rpx;
  margin-right: 16rpx;
  background: #fff;
}
.tab-active {
  background: #FF6B35;
  color: #fff;
}
.order-card { padding: 28rpx; }
.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}
.order-type { font-size: 30rpx; font-weight: 600; }
.order-status { font-size: 24rpx; padding: 4rpx 16rpx; border-radius: 20rpx; }
.status-PENDING_ACCEPT, .status-PENDING_MATCH { background: #FFF3E0; color: #FF9800; }
.status-IN_SERVICE, .status-ACCEPTED, .status-SITTER_EN_ROUTE { background: #E8F5E9; color: #4CAF50; }
.status-OWNER_CONFIRMED, .status-SERVICE_COMPLETED { background: #E3F2FD; color: #2196F3; }
.status-CANCELLED { background: #FFEBEE; color: #F44336; }
.order-body { margin-bottom: 16rpx; }
.order-date { font-size: 26rpx; color: #333; display: block; }
.order-sitter { font-size: 24rpx; color: #666; margin-top: 8rpx; display: block; }
.order-footer { display: flex; justify-content: space-between; align-items: center; }
.order-no { font-size: 22rpx; color: #999; }
.empty { text-align: center; padding: 80rpx; color: #999; }
.loading { text-align: center; padding: 40rpx; color: #999; }
</style>
