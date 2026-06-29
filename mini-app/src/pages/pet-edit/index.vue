<template>
  <view class="container">
    <view class="card form-card">
      <view class="form-item">
        <text class="form-label">宠物名称</text>
        <input
          class="form-input"
          v-model="form.name"
          placeholder="请输入宠物名称"
          maxlength="20"
        />
      </view>

      <view class="form-item">
        <text class="form-label">物种</text>
        <picker :range="speciesOptions" :range-key="'label'" :value="speciesIndex" @change="onSpeciesChange">
          <view class="form-picker">
            <text :class="{ placeholder: speciesIndex < 0 }">
              {{ speciesIndex >= 0 ? speciesOptions[speciesIndex].label : '请选择物种' }}
            </text>
            <text class="picker-arrow">▸</text>
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">品种</text>
        <input
          class="form-input"
          v-model="form.breed"
          placeholder="请输入品种"
          maxlength="30"
        />
      </view>

      <view class="form-item">
        <text class="form-label">性别</text>
        <picker :range="genderOptions" :range-key="'label'" :value="genderIndex" @change="onGenderChange">
          <view class="form-picker">
            <text :class="{ placeholder: genderIndex < 0 }">
              {{ genderIndex >= 0 ? genderOptions[genderIndex].label : '请选择性别' }}
            </text>
            <text class="picker-arrow">▸</text>
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">体重 (kg)</text>
        <input
          class="form-input"
          v-model="weightStr"
          placeholder="请输入体重"
          type="digit"
        />
      </view>

      <view class="form-item">
        <text class="form-label">性格特征</text>
        <input
          class="form-input"
          v-model="form.personality"
          placeholder="如：温顺、活泼、胆小"
          maxlength="50"
        />
      </view>

      <view class="form-item">
        <text class="form-label">饮食信息</text>
        <textarea
          class="form-textarea"
          v-model="form.dietInfo"
          placeholder="如：每日喂食2次，不可吃巧克力"
          maxlength="200"
        />
      </view>
    </view>

    <view class="btn-primary submit-btn" @tap="onSubmit">
      {{ isEdit ? '保存修改' : '添加宠物' }}
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { petApi } from '@/api'

interface PetForm {
  name: string
  species: string
  breed: string
  gender: string
  personality: string
  dietInfo: string
}

const speciesOptions = [
  { label: '猫', value: 'CAT' },
  { label: '狗', value: 'DOG' },
  { label: '兔', value: 'SMALL_ANIMAL' },
  { label: '其他', value: 'OTHER' },
]

const genderOptions = [
  { label: '公', value: 'MALE' },
  { label: '母', value: 'FEMALE' },
]

const petId = ref(0)
const isEdit = computed(() => petId.value > 0)
const submitting = ref(false)
const weightStr = ref('')

const form = ref<PetForm>({
  name: '',
  species: '',
  breed: '',
  gender: '',
  personality: '',
  dietInfo: '',
})

const speciesIndex = computed(() =>
  speciesOptions.findIndex(o => o.value === form.value.species)
)
const genderIndex = computed(() =>
  genderOptions.findIndex(o => o.value === form.value.gender)
)

function onSpeciesChange(e: { detail: { value: number } }) {
  form.value.species = speciesOptions[e.detail.value].value
}

function onGenderChange(e: { detail: { value: number } }) {
  form.value.gender = genderOptions[e.detail.value].value
}

function validate(): boolean {
  if (!form.value.name.trim()) {
    uni.showToast({ title: '请输入宠物名称', icon: 'none' })
    return false
  }
  if (!form.value.species) {
    uni.showToast({ title: '请选择物种', icon: 'none' })
    return false
  }
  if (!form.value.breed.trim()) {
    uni.showToast({ title: '请输入品种', icon: 'none' })
    return false
  }
  if (!form.value.gender) {
    uni.showToast({ title: '请选择性别', icon: 'none' })
    return false
  }
  const w = parseFloat(weightStr.value)
  if (!weightStr.value || isNaN(w) || w <= 0) {
    uni.showToast({ title: '请输入有效体重', icon: 'none' })
    return false
  }
  return true
}

async function onSubmit() {
  if (!validate() || submitting.value) return
  submitting.value = true

  const weight = parseFloat(weightStr.value)
  const data = { ...form.value, weight }

  try {
    if (isEdit.value) {
      await petApi.update(petId.value, data)
      uni.showToast({ title: '修改成功', icon: 'success' })
    } else {
      await petApi.create(data)
      uni.showToast({ title: '添加成功', icon: 'success' })
    }
    setTimeout(() => { uni.navigateBack() }, 800)
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

async function loadPet(id: number) {
  try {
    const pet = await petApi.get(id)
    form.value = {
      name: pet.name,
      species: pet.species,
      breed: pet.breed,
      gender: pet.gender,
      personality: pet.personality || '',
      dietInfo: pet.dietInfo || '',
    }
    weightStr.value = String(pet.weight)
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

onLoad((query) => {
  if (query?.id) {
    petId.value = Number(query.id)
    uni.setNavigationBarTitle({ title: '编辑宠物' })
    loadPet(petId.value)
  } else {
    uni.setNavigationBarTitle({ title: '添加宠物' })
  }
})
</script>

<style scoped>
.form-card {
  padding: 8rpx 24rpx;
}

.form-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f2f2f2;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 16rpx;
  display: block;
}

.form-input {
  font-size: 28rpx;
  color: #333;
  padding: 16rpx 20rpx;
  background: #f9f9f9;
  border-radius: 12rpx;
}

.form-textarea {
  font-size: 28rpx;
  color: #333;
  padding: 16rpx 20rpx;
  background: #f9f9f9;
  border-radius: 12rpx;
  width: 100%;
  height: 160rpx;
  box-sizing: border-box;
}

.form-picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 20rpx;
  background: #f9f9f9;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.placeholder {
  color: #c0c0c0;
}

.picker-arrow {
  color: #ccc;
  font-size: 24rpx;
}

.submit-btn {
  margin-top: 48rpx;
}
</style>
