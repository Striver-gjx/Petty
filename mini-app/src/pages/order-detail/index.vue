<template>
  <view class="container" v-if="order">
    <!-- 状态栏 -->
    <view class="status-bar card">
      <text class="status-label">{{ order.statusLabel }}</text>
      <text class="order-no">{{ order.orderNo }}</text>
    </view>

    <!-- 服务信息 -->
    <view class="card">
      <text class="section-title">服务信息</text>
      <view class="info-row">
        <text class="info-label">服务类型</text>
        <text class="info-value">{{ order.serviceTypeName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">预约时间</text>
        <text class="info-value">{{ order.scheduledDate }} {{ order.scheduledStartTime?.slice(0,5) }}-{{ order.scheduledEndTime?.slice(0,5) }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">服务地址</text>
        <text class="info-value">{{ order.serviceAddress }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">宠物数量</text>
        <text class="info-value">{{ order.petCount }}只</text>
      </view>
      <view class="info-row" v-if="order.sitterName">
        <text class="info-label">喂养师</text>
        <text class="info-value">{{ order.sitterName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">订单金额</text>
        <text class="info-value text-price">{{ order.totalAmount }}</text>
      </view>
    </view>

    <!-- 服务记录 -->
    <view class="card" v-if="order.serviceLogs && order.serviceLogs.length > 0">
      <text class="section-title">服务记录</text>
      <view v-for="log in order.serviceLogs" :key="log.id" class="log-item">
        <view class="log-dot"></view>
        <view class="log-content">
          <text class="log-type">{{ getLogLabel(log.logType) }}</text>
          <text class="log-desc">{{ log.description }}</text>
          <text class="log-time">{{ log.createdAt?.slice(11, 16) }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="bottom-actions" v-if="showActions">
      <view class="btn-primary" v-if="order.status === 'SERVICE_COMPLETED'" @tap="confirmOrder">确认完成</view>
      <view class="btn-cancel" v-if="canCancel" @tap="cancelOrder">取消订单</view>
    </view>
  </view>
  <view v-else class="loading">加载中...</view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { orderApi } from '@/api'
import type { OrderDetailVO } from '@/types'

const orderId = ref(0)
const order = ref<OrderDetailVO | null>(null)

const canCancel = computed(() => {
  const s = order.value?.status
  return s === 'PENDING_MATCH' || s === 'PENDING_ACCEPT' || s === 'ACCEPTED'
})

const showActions = computed(() => {
  return order.value?.status === 'SERVICE_COMPLETED' || canCancel.value
})

const logLabelMap: Record<string, string> = {
  CHECK_IN: '到达打卡',
  CHECK_OUT: '服务完成',
  FEEDING: '喂食',
  PLAYING: '互动',
  CLEANING: '清洁',
  WALKING: '遛狗',
  MEDICATION: '喂药',
  ABNORMAL: '异常上报',
  OTHER: '其他',
}

function getLogLabel(type: string) { return logLabelMap[type] || type }

async function confirmOrder() {
  try {
    await orderApi.confirm(orderId.value, 1)
    uni.showToast({ title: '已确认', icon: 'success' })
    loadDetail()
  } catch (e) { console.error(e) }
}

async function cancelOrder() {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消此订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await orderApi.cancel(orderId.value, 1, '用户主动取消')
          uni.showToast({ title: '已取消', icon: 'success' })
          loadDetail()
        } catch (e) { console.error(e) }
      }
    }
  })
}

async function loadDetail() {
  try {
    order.value = await orderApi.get(orderId.value)
  } catch (e) { console.error(e) }
}

onLoad((query) => {
  if (query?.id) orderId.value = Number(query.id)
})
onMounted(loadDetail)
</script>

<style scoped>
.status-bar {
  background: linear-gradient(135deg, #FF6B35, #FF8F5E);
  color: #fff;
  padding: 32rpx;
}
.status-label { font-size: 36rpx; font-weight: 700; display: block; }
.order-no { font-size: 22rpx; opacity: 0.8; margin-top: 8rpx; display: block; }

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f9f9f9;
}
.info-row:last-child { border-bottom: none; }
.info-label { color: #999; font-size: 26rpx; }
.info-value { color: #333; font-size: 26rpx; }

.log-item {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f9f9f9;
}
.log-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #FF6B35;
  margin-top: 10rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}
.log-content { flex: 1; }
.log-type { font-size: 26rpx; font-weight: 500; display: block; }
.log-desc { font-size: 24rpx; color: #666; margin-top: 4rpx; display: block; }
.log-time { font-size: 22rpx; color: #999; margin-top: 4rpx; display: block; }

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 32rpx;
  background: #fff;
  box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.06);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}
.btn-cancel {
  text-align: center;
  padding: 20rpx;
  color: #999;
  font-size: 28rpx;
  margin-top: 12rpx;
}
.loading { text-align: center; padding: 80rpx; color: #999; }
</style>
