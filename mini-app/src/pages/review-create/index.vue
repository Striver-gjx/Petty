<template>
  <view class="container">
    <view class="card">
      <text class="section-title">服务评分</text>

      <view v-for="dim in dimensions" :key="dim.key" class="rating-row">
        <text class="rating-label">{{ dim.label }}</text>
        <view class="stars">
          <text
            v-for="star in 5"
            :key="star"
            class="star"
            :class="{ active: (ratings[dim.key] || 0) >= star }"
            @tap="setRating(dim.key, star)"
          >★</text>
        </view>
        <text class="rating-value">{{ ratings[dim.key] || 0 }}</text>
      </view>

      <view class="overall-row">
        <text class="overall-label">综合评分</text>
        <text class="overall-value">{{ overallRating }}</text>
      </view>
    </view>

    <view class="card">
      <text class="section-title">评价内容</text>
      <textarea
        class="review-textarea"
        v-model="content"
        placeholder="分享您的服务体验..."
        maxlength="500"
      />
    </view>

    <view class="card">
      <text class="section-title">快捷标签</text>
      <view class="tags-wrap">
        <view
          v-for="tag in tagOptions"
          :key="tag"
          class="tag-item"
          :class="{ selected: selectedTags.includes(tag) }"
          @tap="toggleTag(tag)"
        >{{ tag }}</view>
      </view>
    </view>

    <view class="card anonymous-card">
      <view class="anonymous-row">
        <text class="anonymous-label">匿名评价</text>
        <switch :checked="anonymous" color="#FF6B35" @change="onAnonymousChange" />
      </view>
    </view>

    <view class="btn-primary submit-btn" @tap="onSubmit">提交评价</view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { reviewApi } from '@/api'

type DimensionKey = 'punctuality' | 'professionalism' | 'attitude' | 'petCare' | 'cleanliness'

interface Dimension {
  key: DimensionKey
  label: string
}

const dimensions: Dimension[] = [
  { key: 'punctuality', label: '准时性' },
  { key: 'professionalism', label: '专业度' },
  { key: 'attitude', label: '服务态度' },
  { key: 'petCare', label: '宠物照护' },
  { key: 'cleanliness', label: '环境整洁' },
]

const tagOptions = ['准时到达', '细心耐心', '宠物很开心', '专业负责', '值得推荐']

const orderId = ref(0)
const content = ref('')
const anonymous = ref(false)
const selectedTags = ref<string[]>([])
const submitting = ref(false)

const ratings = reactive<Record<DimensionKey, number>>({
  punctuality: 0,
  professionalism: 0,
  attitude: 0,
  petCare: 0,
  cleanliness: 0,
})

const overallRating = computed(() => {
  const values = Object.values(ratings) as number[]
  const sum = values.reduce((a, b) => a + b, 0)
  const filled = values.filter(v => v > 0).length
  if (filled === 0) return '0.0'
  return (sum / filled).toFixed(1)
})

function setRating(key: DimensionKey, value: number) {
  ratings[key] = value
}

function toggleTag(tag: string) {
  const idx = selectedTags.value.indexOf(tag)
  if (idx >= 0) {
    selectedTags.value.splice(idx, 1)
  } else {
    selectedTags.value.push(tag)
  }
}

function onAnonymousChange(e: { detail: { value: boolean } }) {
  anonymous.value = e.detail.value
}

function validate(): boolean {
  const allRated = Object.values(ratings).every(v => v > 0)
  if (!allRated) {
    uni.showToast({ title: '请为所有维度评分', icon: 'none' })
    return false
  }
  if (!content.value.trim()) {
    uni.showToast({ title: '请输入评价内容', icon: 'none' })
    return false
  }
  return true
}

async function onSubmit() {
  if (!validate() || submitting.value) return
  submitting.value = true

  try {
    await reviewApi.create({
      orderId: orderId.value,
      rating: parseFloat(overallRating.value),
      content: content.value.trim(),
      tags: selectedTags.value,
      anonymous: anonymous.value,
      punctualityRating: ratings.punctuality,
      professionalismRating: ratings.professionalism,
      attitudeRating: ratings.attitude,
      petCareRating: ratings.petCare,
      cleanlinessRating: ratings.cleanliness,
    })
    uni.showToast({ title: '评价成功', icon: 'success' })
    setTimeout(() => { uni.navigateBack() }, 800)
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onLoad((query) => {
  if (query?.orderId) {
    orderId.value = Number(query.orderId)
  }
})
</script>

<style scoped>
.rating-row {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.rating-row:last-of-type {
  border-bottom: none;
}

.rating-label {
  width: 160rpx;
  font-size: 28rpx;
  color: #333;
  flex-shrink: 0;
}

.stars {
  display: flex;
  gap: 12rpx;
  flex: 1;
}

.star {
  font-size: 44rpx;
  color: #e0e0e0;
  transition: color 0.15s;
}

.star.active {
  color: #FF6B35;
}

.rating-value {
  width: 48rpx;
  text-align: right;
  font-size: 26rpx;
  color: #999;
  flex-shrink: 0;
}

.overall-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0 8rpx;
  margin-top: 8rpx;
  border-top: 2rpx solid #f0f0f0;
}

.overall-label {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.overall-value {
  font-size: 40rpx;
  font-weight: 700;
  color: #FF6B35;
}

.review-textarea {
  width: 100%;
  height: 200rpx;
  font-size: 28rpx;
  color: #333;
  padding: 20rpx;
  background: #f9f9f9;
  border-radius: 12rpx;
  box-sizing: border-box;
}

.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.tag-item {
  padding: 12rpx 28rpx;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #666;
  background: #f5f5f5;
  transition: all 0.2s;
}

.tag-item.selected {
  background: #FFF3ED;
  color: #FF6B35;
  border: 1rpx solid #FF6B35;
}

.anonymous-card {
  padding: 16rpx 24rpx;
}

.anonymous-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.anonymous-label {
  font-size: 28rpx;
  color: #333;
}

.submit-btn {
  margin-top: 48rpx;
  margin-bottom: 48rpx;
}
</style>
