<template>
  <view class="container">
    <view v-if="!order" class="loading"><text class="text-muted">加载中...</text></view>

    <template v-else>
      <!-- 订单信息 -->
      <view class="card">
        <view class="info-row"><text class="label">订单号</text><text>{{ order.orderNo }}</text></view>
        <view class="info-row"><text class="label">主人</text><text>{{ order.ownerNickname }}</text></view>
        <view class="info-row"><text class="label">地址</text><text>{{ order.serviceAddress }}</text></view>
        <view class="info-row"><text class="label">时间</text><text>{{ order.scheduledDate }} {{ order.scheduledStartTime?.slice(0,5) }}-{{ order.scheduledEndTime?.slice(0,5) }}</text></view>
        <view class="info-row"><text class="label">费用</text><text class="text-price">¥{{ order.totalAmount }}</text></view>
        <view class="info-row"><text class="label">状态</text><text class="status-tag">{{ order.statusLabel }}</text></view>
      </view>

      <!-- 宠物列表 -->
      <view v-if="order.pets && order.pets.length" class="card">
        <text class="section-title">服务宠物</text>
        <view v-for="pet in order.pets" :key="pet.petId" class="pet-row">
          <text class="pet-name">{{ pet.petName }}</text>
          <text class="text-muted">{{ pet.species }}</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="card actions">
        <!-- ACCEPTED → 到达打卡 -->
        <view v-if="order.status === 'ACCEPTED' || order.status === 'SITTER_EN_ROUTE'"
          class="btn-primary" @tap="checkIn">
          📍 到达打卡
        </view>

        <!-- IN_SERVICE → 添加记录 / 完成打卡 -->
        <template v-if="order.status === 'IN_SERVICE'">
          <view class="btn-secondary" @tap="showLogForm = true">📝 添加服务记录</view>
          <view class="btn-primary" @tap="checkOut" style="margin-top: 20rpx;">✅ 服务完成打卡</view>
        </template>

        <!-- SERVICE_COMPLETED -->
        <view v-if="order.status === 'SERVICE_COMPLETED'" class="done-tip">
          <text>等待主人确认中...</text>
        </view>
      </view>

      <!-- 服务记录列表 -->
      <view v-if="logs.length" class="card">
        <text class="section-title">服务记录</text>
        <view v-for="log in logs" :key="log.id" class="log-item">
          <text class="log-type">{{ log.logType }}</text>
          <text class="log-desc">{{ log.description }}</text>
          <text class="text-muted">{{ log.createdAt?.slice(11, 16) }}</text>
        </view>
      </view>

      <!-- 添加记录弹窗 -->
      <view v-if="showLogForm" class="modal-mask" @tap="showLogForm = false">
        <view class="modal-content" @tap.stop>
          <text class="section-title">添加服务记录</text>
          <view class="form-item">
            <text class="label">类型</text>
            <picker :range="logTypes" @change="onLogTypeChange">
              <view class="picker-row"><text>{{ logForm.logType || '请选择' }}</text></view>
            </picker>
          </view>
          <view class="form-item">
            <text class="label">描述</text>
            <textarea class="textarea" v-model="logForm.description" placeholder="记录服务内容..." />
          </view>
          <view class="form-item">
            <text class="label">宠物状态</text>
            <input class="input" v-model="logForm.petStatus" placeholder="宠物当前状态" />
          </view>
          <view class="btn-primary" @tap="submitLog">提交记录</view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { orderApi } from '@/api'
import type { OrderDetailVO, ServiceLogVO } from '@/types'

const orderId = ref(0)
const order = ref<OrderDetailVO | null>(null)
const logs = ref<ServiceLogVO[]>([])
const showLogForm = ref(false)
const logTypes = ['FEEDING', 'WALKING', 'PLAYING', 'MEDICATION', 'CLEANING', 'OTHER']
const logForm = ref({ logType: '', description: '', petStatus: '' })

function onLogTypeChange(e: { detail: { value: number } }) {
  logForm.value.logType = logTypes[e.detail.value]
}

async function loadOrder() {
  try {
    order.value = await orderApi.get(orderId.value)
    logs.value = await orderApi.getLogs(orderId.value)
  } catch (e) { console.error(e) }
}

async function checkIn() {
  try {
    await orderApi.checkIn(orderId.value, {
      latitude: 39.9087,
      longitude: 116.4716,
      photoUrl: 'https://placeholder.com/checkin.jpg',
    })
    uni.showToast({ title: '打卡成功', icon: 'success' })
    loadOrder()
  } catch (e) { console.error(e) }
}

async function checkOut() {
  uni.showModal({
    title: '确认完成服务',
    content: '请确保已完成所有服务项目',
    success: async (res) => {
      if (res.confirm) {
        try {
          await orderApi.checkOut(orderId.value, {
            latitude: 39.9087,
            longitude: 116.4716,
            photoUrl: 'https://placeholder.com/checkout.jpg',
            serviceReport: '服务已完成，宠物状态良好',
          })
          uni.showToast({ title: '服务完成', icon: 'success' })
          loadOrder()
        } catch (e) { console.error(e) }
      }
    }
  })
}

async function submitLog() {
  if (!logForm.value.logType || !logForm.value.description) {
    uni.showToast({ title: '请填写完整', icon: 'none' }); return
  }
  try {
    await orderApi.addLog(orderId.value, {
      logType: logForm.value.logType,
      description: logForm.value.description,
      petStatus: logForm.value.petStatus,
      latitude: 39.9087,
      longitude: 116.4716,
    })
    uni.showToast({ title: '记录已添加', icon: 'success' })
    showLogForm.value = false
    logForm.value = { logType: '', description: '', petStatus: '' }
    logs.value = await orderApi.getLogs(orderId.value)
  } catch (e) { console.error(e) }
}

onLoad((query) => {
  if (query?.orderId) orderId.value = Number(query.orderId)
})
onMounted(() => { if (orderId.value) loadOrder() })
</script>

<style scoped>
.info-row { display: flex; justify-content: space-between; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; font-size: 28rpx; }
.label { color: #999; }
.status-tag { color: #FF6B35; font-weight: 500; }
.pet-row { display: flex; gap: 16rpx; padding: 12rpx 0; align-items: center; }
.pet-name { font-size: 28rpx; font-weight: 500; }
.actions { padding: 32rpx 24rpx; }
.btn-secondary { text-align: center; padding: 24rpx; border: 2rpx solid #FF6B35; color: #FF6B35; border-radius: 44rpx; font-size: 30rpx; }
.done-tip { text-align: center; color: #999; padding: 32rpx; }
.log-item { padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.log-type { font-size: 24rpx; color: #FF6B35; background: #FFF3ED; padding: 4rpx 12rpx; border-radius: 8rpx; margin-right: 12rpx; }
.log-desc { font-size: 26rpx; color: #333; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal-content { background: #fff; border-radius: 24rpx; padding: 40rpx; width: 85%; max-height: 80vh; }
.form-item { margin: 24rpx 0; }
.input { border: 1rpx solid #eee; border-radius: 12rpx; padding: 16rpx 20rpx; font-size: 28rpx; width: 100%; }
.textarea { border: 1rpx solid #eee; border-radius: 12rpx; padding: 16rpx 20rpx; font-size: 28rpx; width: 100%; height: 160rpx; }
.picker-row { border: 1rpx solid #eee; border-radius: 12rpx; padding: 16rpx 20rpx; font-size: 28rpx; }
.loading { text-align: center; padding: 120rpx 0; }
</style>
