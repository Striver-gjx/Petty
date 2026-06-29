<template>
  <view class="container" v-if="order">
    <!-- Status bar -->
    <view class="status-bar card">
      <text class="status-label">{{ order.statusLabel }}</text>
      <text class="order-no">{{ order.orderNo }}</text>
    </view>

    <!-- Service info -->
    <view class="card">
      <text class="section-title">服务信息</text>
      <view class="info-row">
        <text class="info-label">服务类型</text>
        <text class="info-value">{{ order.serviceTypeName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">预约时间</text>
        <text class="info-value">{{ order.scheduledDate }} {{ formatTime(order.scheduledStartTime) }}-{{ formatTime(order.scheduledEndTime) }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">服务地址</text>
        <text class="info-value">{{ order.serviceAddress }}</text>
      </view>
      <view class="info-row" v-if="order.sitterName">
        <text class="info-label">喂养师</text>
        <text class="info-value">{{ order.sitterName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">订单金额</text>
        <text class="info-value text-price">¥{{ order.totalAmount }}</text>
      </view>
      <view class="info-row" v-if="order.remark">
        <text class="info-label">备注</text>
        <text class="info-value remark-text">{{ order.remark }}</text>
      </view>
    </view>

    <!-- Pet list -->
    <view class="card" v-if="order.pets && order.pets.length > 0">
      <text class="section-title">宠物信息</text>
      <view v-for="pet in order.pets" :key="pet.petId" class="pet-item">
        <view class="pet-avatar-sm">{{ getSpeciesIcon(pet.species) }}</view>
        <view class="pet-item-info">
          <text class="pet-item-name">{{ pet.petName }}</text>
          <text class="pet-item-species">{{ getSpeciesLabel(pet.species) }}</text>
        </view>
        <text v-if="pet.specialNotes" class="pet-notes">{{ pet.specialNotes }}</text>
      </view>
    </view>

    <!-- Service logs timeline -->
    <view class="card" v-if="order.serviceLogs && order.serviceLogs.length > 0">
      <text class="section-title">服务记录</text>
      <view class="timeline">
        <view
          v-for="(log, idx) in order.serviceLogs"
          :key="log.id"
          class="timeline-item"
          :class="{ 'timeline-last': idx === order.serviceLogs.length - 1 }"
        >
          <view class="timeline-line">
            <view class="timeline-dot" :class="getLogDotClass(log.logType)"></view>
            <view v-if="idx < order.serviceLogs.length - 1" class="timeline-connector"></view>
          </view>
          <view class="timeline-content">
            <view class="timeline-header">
              <text class="timeline-type">{{ getLogLabel(log.logType) }}</text>
              <text class="timeline-time">{{ formatDateTime(log.createdAt) }}</text>
            </view>
            <text class="timeline-desc" v-if="log.description">{{ log.description }}</text>
            <text class="timeline-pet-status" v-if="log.petStatus">宠物状态：{{ log.petStatus }}</text>
            <view class="timeline-photos" v-if="log.photoUrls && log.photoUrls.length > 0">
              <image
                v-for="(url, pIdx) in log.photoUrls"
                :key="pIdx"
                :src="url"
                class="timeline-photo"
                mode="aspectFill"
                @tap="previewImage(url, log.photoUrls)"
              />
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- Bottom actions -->
    <view class="bottom-actions" v-if="showActions">
      <view v-if="order.status === 'SERVICE_COMPLETED'" class="btn-primary" @tap="confirmOrder">
        确认完成
      </view>
      <view v-if="order.status === 'OWNER_CONFIRMED'" class="btn-primary" @tap="goReview">
        去评价
      </view>
      <view v-if="canCancel" class="btn-outline" @tap="showCancelDialog">
        取消订单
      </view>
    </view>

    <!-- Spacer to prevent content behind fixed bottom bar -->
    <view v-if="showActions" class="bottom-spacer"></view>

    <!-- Cancel reason dialog -->
    <view v-if="cancelDialogVisible" class="dialog-mask" @tap="cancelDialogVisible = false">
      <view class="dialog-body" @tap.stop>
        <text class="dialog-title">取消订单</text>
        <text class="dialog-subtitle">请输入取消原因</text>
        <textarea
          class="dialog-textarea"
          v-model="cancelReason"
          placeholder="请简要说明取消原因"
          maxlength="200"
        />
        <view class="dialog-actions">
          <view class="dialog-btn dialog-btn-cancel" @tap="cancelDialogVisible = false">返回</view>
          <view class="dialog-btn dialog-btn-confirm" @tap="doCancel">确认取消</view>
        </view>
      </view>
    </view>
  </view>
  <view v-else class="loading">加载中...</view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { orderApi } from '@/api'
import type { OrderDetailVO } from '@/types'

const orderId = ref(0)
const order = ref<OrderDetailVO | null>(null)
const cancelDialogVisible = ref(false)
const cancelReason = ref('')

const cancellableStatuses = ['PENDING_MATCH', 'PENDING_ACCEPT', 'ACCEPTED']

const canCancel = computed(() => {
  const s = order.value?.status
  return s !== undefined && cancellableStatuses.includes(s)
})

const showActions = computed(() => {
  const s = order.value?.status
  return s === 'SERVICE_COMPLETED' || s === 'OWNER_CONFIRMED' || canCancel.value
})

const speciesMap: Record<string, string> = {
  CAT: '猫', DOG: '狗', BIRD: '鸟', FISH: '鱼',
  REPTILE: '爬行动物', SMALL_ANIMAL: '小动物', OTHER: '其他',
}

const speciesIcons: Record<string, string> = {
  CAT: '🐱', DOG: '🐶', BIRD: '🐦', FISH: '🐟',
  REPTILE: '🦎', SMALL_ANIMAL: '🐹', OTHER: '🐾',
}

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

function getSpeciesIcon(species: string) { return speciesIcons[species] || '🐾' }
function getSpeciesLabel(species: string) { return speciesMap[species] || species }
function getLogLabel(type: string) { return logLabelMap[type] || type }

function getLogDotClass(type: string): string {
  if (type === 'CHECK_IN' || type === 'CHECK_OUT') return 'dot-primary'
  if (type === 'ABNORMAL') return 'dot-warning'
  return ''
}

function formatTime(t: string | null | undefined): string {
  if (!t) return ''
  return t.slice(0, 5)
}

function formatDateTime(dt: string | null | undefined): string {
  if (!dt) return ''
  return dt.slice(5, 16).replace('T', ' ')
}

function previewImage(current: string, urls: string[]) {
  uni.previewImage({ current, urls })
}

async function confirmOrder() {
  uni.showModal({
    title: '确认完成',
    content: '确认服务已完成？确认后可进行评价。',
    success: async (res) => {
      if (res.confirm) {
        try {
          await orderApi.confirm(orderId.value)
          uni.showToast({ title: '已确认', icon: 'success' })
          loadDetail()
        } catch (e) { console.error(e) }
      }
    },
  })
}

function goReview() {
  uni.navigateTo({ url: `/pages/review-create/index?orderId=${orderId.value}` })
}

function showCancelDialog() {
  cancelReason.value = ''
  cancelDialogVisible.value = true
}

async function doCancel() {
  const reason = cancelReason.value.trim()
  if (!reason) {
    uni.showToast({ title: '请输入取消原因', icon: 'none' })
    return
  }
  try {
    await orderApi.cancel(orderId.value, reason)
    cancelDialogVisible.value = false
    uni.showToast({ title: '已取消', icon: 'success' })
    loadDetail()
  } catch (e) { console.error(e) }
}

async function loadDetail() {
  try {
    order.value = await orderApi.get(orderId.value)
  } catch (e) { console.error(e) }
}

onLoad((query) => {
  if (query?.id) orderId.value = Number(query.id)
})

onShow(() => {
  if (orderId.value > 0) loadDetail()
})
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
.info-label { color: #999; font-size: 26rpx; flex-shrink: 0; margin-right: 24rpx; }
.info-value { color: #333; font-size: 26rpx; text-align: right; }
.remark-text { max-width: 400rpx; word-break: break-all; }

/* Pet list */
.pet-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.pet-item:last-child { border-bottom: none; }

.pet-avatar-sm {
  width: 64rpx;
  height: 64rpx;
  border-radius: 12rpx;
  background: #FFF3ED;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  flex-shrink: 0;
}

.pet-item-info {
  margin-left: 16rpx;
  flex: 1;
}
.pet-item-name { font-size: 28rpx; font-weight: 500; display: block; }
.pet-item-species { font-size: 24rpx; color: #999; margin-top: 4rpx; display: block; }
.pet-notes { font-size: 22rpx; color: #FF6B35; flex-shrink: 0; max-width: 240rpx; text-align: right; }

/* Timeline */
.timeline {
  padding-left: 8rpx;
}

.timeline-item {
  display: flex;
  min-height: 100rpx;
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 32rpx;
  flex-shrink: 0;
}

.timeline-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #ddd;
  margin-top: 6rpx;
  flex-shrink: 0;
}
.timeline-dot.dot-primary { background: #FF6B35; }
.timeline-dot.dot-warning { background: #E74C3C; }

.timeline-connector {
  width: 2rpx;
  flex: 1;
  background: #e8e8e8;
  margin: 4rpx 0;
}

.timeline-content {
  flex: 1;
  margin-left: 16rpx;
  padding-bottom: 28rpx;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.timeline-type { font-size: 26rpx; font-weight: 500; color: #333; }
.timeline-time { font-size: 22rpx; color: #999; }
.timeline-desc { font-size: 24rpx; color: #666; margin-top: 8rpx; display: block; }
.timeline-pet-status { font-size: 22rpx; color: #FF6B35; margin-top: 6rpx; display: block; }

.timeline-photos {
  display: flex;
  gap: 12rpx;
  margin-top: 12rpx;
  flex-wrap: wrap;
}
.timeline-photo {
  width: 140rpx;
  height: 140rpx;
  border-radius: 8rpx;
}

/* Bottom actions */
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 32rpx;
  background: #fff;
  box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.06);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  gap: 20rpx;
}

.btn-outline {
  flex: 1;
  text-align: center;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  color: #999;
  border: 1rpx solid #ddd;
}

.bottom-actions .btn-primary {
  flex: 1;
}

.bottom-spacer {
  height: 160rpx;
}

/* Cancel dialog */
.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.dialog-body {
  width: 600rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.dialog-title {
  font-size: 32rpx;
  font-weight: 600;
  display: block;
  text-align: center;
}

.dialog-subtitle {
  font-size: 26rpx;
  color: #999;
  display: block;
  text-align: center;
  margin-top: 12rpx;
  margin-bottom: 24rpx;
}

.dialog-textarea {
  width: 100%;
  height: 160rpx;
  font-size: 28rpx;
  padding: 16rpx;
  background: #f9f9f9;
  border-radius: 12rpx;
  box-sizing: border-box;
}

.dialog-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 32rpx;
}

.dialog-btn {
  flex: 1;
  text-align: center;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
}

.dialog-btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.dialog-btn-confirm {
  background: linear-gradient(135deg, #FF6B35, #FF8F5E);
  color: #fff;
}

.loading { text-align: center; padding: 80rpx; color: #999; }
</style>
