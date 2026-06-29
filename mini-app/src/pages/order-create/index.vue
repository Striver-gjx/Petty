<template>
  <view class="container">
    <!-- 服务类型 -->
    <view class="card">
      <text class="section-title">{{ serviceName }}</text>
      <text class="text-muted">基础价格 ¥{{ servicePrice }}/次</text>
    </view>

    <!-- 选择宠物 -->
    <view class="card">
      <text class="section-title">选择宠物</text>
      <view class="pet-list">
        <view
          v-for="pet in pets"
          :key="pet.id"
          :class="['pet-item', selectedPets.includes(pet.id) && 'pet-selected']"
          @tap="togglePet(pet.id)"
        >
          <text class="pet-name">{{ pet.name }}</text>
          <text class="pet-breed">{{ pet.breed }}</text>
        </view>
      </view>
      <text v-if="pets.length === 0" class="text-muted">暂无宠物，请先添加</text>
    </view>

    <!-- 预约时间 -->
    <view class="card">
      <text class="section-title">预约时间</text>
      <picker mode="date" :start="today" @change="onDateChange">
        <view class="picker-row">
          <text>日期</text>
          <text class="picker-value">{{ form.scheduledDate || '请选择' }}</text>
        </view>
      </picker>
      <picker mode="time" @change="onStartTimeChange">
        <view class="picker-row">
          <text>开始时间</text>
          <text class="picker-value">{{ form.scheduledStartTime || '请选择' }}</text>
        </view>
      </picker>
      <picker mode="time" @change="onEndTimeChange">
        <view class="picker-row">
          <text>结束时间</text>
          <text class="picker-value">{{ form.scheduledEndTime || '请选择' }}</text>
        </view>
      </picker>
    </view>

    <!-- 服务地址 -->
    <view class="card">
      <text class="section-title">服务地址</text>
      <input class="input" v-model="form.serviceAddress" placeholder="请输入服务地址" />
    </view>

    <!-- 备注 -->
    <view class="card">
      <text class="section-title">备注</text>
      <textarea class="textarea" v-model="form.remark" placeholder="如有特殊要求请备注" />
    </view>

    <!-- 价格 + 提交 -->
    <view class="bottom-bar">
      <view class="price-info">
        <text class="text-muted">预估费用</text>
        <text class="total-price">¥{{ totalAmount }}</text>
      </view>
      <view class="btn-primary submit-btn" @tap="submitOrder">立即预约</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { petApi, orderApi } from '@/api'
import type { Pet } from '@/types'

const serviceTypeId = ref(0)
const serviceName = ref('')
const servicePrice = ref(0)
const pets = ref<Pet[]>([])
const selectedPets = ref<number[]>([])

const form = ref({
  scheduledDate: '',
  scheduledStartTime: '',
  scheduledEndTime: '',
  serviceAddress: '北京市朝阳区建国路88号',
  remark: '',
})

const today = new Date().toISOString().slice(0, 10)

const totalAmount = computed(() => {
  const extra = Math.max(0, selectedPets.value.length - 1) * 20
  return selectedPets.value.length > 0 ? servicePrice.value + extra : 0
})

function togglePet(id: number) {
  const idx = selectedPets.value.indexOf(id)
  if (idx >= 0) selectedPets.value.splice(idx, 1)
  else selectedPets.value.push(id)
}

function onDateChange(e: { detail: { value: string } }) { form.value.scheduledDate = e.detail.value }
function onStartTimeChange(e: { detail: { value: string } }) { form.value.scheduledStartTime = e.detail.value }
function onEndTimeChange(e: { detail: { value: string } }) { form.value.scheduledEndTime = e.detail.value }

async function submitOrder() {
  if (selectedPets.value.length === 0) {
    uni.showToast({ title: '请选择宠物', icon: 'none' }); return
  }
  if (!form.value.scheduledDate || !form.value.scheduledStartTime || !form.value.scheduledEndTime) {
    uni.showToast({ title: '请选择预约时间', icon: 'none' }); return
  }

  try {
    const data = {
      serviceTypeId: serviceTypeId.value,
      petIds: selectedPets.value,
      scheduledDate: form.value.scheduledDate,
      scheduledStartTime: form.value.scheduledStartTime,
      scheduledEndTime: form.value.scheduledEndTime,
      serviceAddress: form.value.serviceAddress,
      latitude: 39.9087,
      longitude: 116.4716,
      remark: form.value.remark || undefined,
    }
    await orderApi.create(1, data)
    uni.showToast({ title: '预约成功', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/orders/index' }), 1500)
  } catch (e) {
    console.error(e)
  }
}

onLoad((query) => {
  if (query?.serviceTypeId) serviceTypeId.value = Number(query.serviceTypeId)
  if (query?.name) serviceName.value = decodeURIComponent(query.name)
  if (query?.price) servicePrice.value = Number(query.price)
})

onMounted(async () => {
  try {
    pets.value = await petApi.list(1)
  } catch (e) { console.error(e) }
})
</script>

<style scoped>
.pet-list { display: flex; flex-wrap: wrap; gap: 16rpx; }
.pet-item {
  padding: 16rpx 24rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  text-align: center;
}
.pet-selected { border-color: #FF6B35; background: #FFF3ED; }
.pet-name { font-size: 28rpx; font-weight: 500; display: block; }
.pet-breed { font-size: 22rpx; color: #999; display: block; }

.picker-row {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.picker-value { color: #666; }

.input {
  border: 1rpx solid #eee;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  font-size: 28rpx;
}
.textarea {
  border: 1rpx solid #eee;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  font-size: 28rpx;
  width: 100%;
  height: 160rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 32rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.06);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}
.price-info { flex: 1; }
.total-price { font-size: 40rpx; font-weight: 700; color: #FF6B35; display: block; }
.submit-btn { width: 260rpx; height: 80rpx; line-height: 80rpx; font-size: 30rpx; }
</style>
