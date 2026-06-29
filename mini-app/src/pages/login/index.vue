<template>
  <view class="login-page">
    <view class="logo-section">
      <text class="logo-text">🐾 Petty</text>
      <text class="logo-sub">宠物上门喂养服务</text>
    </view>

    <view class="form-section">
      <view class="input-group">
        <text class="label">手机号</text>
        <input
          v-model="phone"
          type="number"
          placeholder="请输入手机号"
          maxlength="11"
          class="input"
        />
      </view>

      <view class="role-selector">
        <view
          :class="['role-btn', role === 'OWNER' ? 'active' : '']"
          @tap="role = 'OWNER'"
        >
          <text>🏠 宠物主人</text>
        </view>
        <view
          :class="['role-btn', role === 'SITTER' ? 'active' : '']"
          @tap="role = 'SITTER'"
        >
          <text>🐕 喂养师</text>
        </view>
      </view>

      <button class="login-btn" :disabled="!canLogin" @tap="handleLogin">
        登录
      </button>

      <view class="demo-hint">
        <text class="hint-title">演示账号</text>
        <text class="hint-text" @tap="fillDemo('owner')">主人: 13800001111</text>
        <text class="hint-text" @tap="fillDemo('sitter')">喂养师: 13900001111</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import { useUserStore } from '@/store/user'

const phone = ref('')
const role = ref<'OWNER' | 'SITTER'>('OWNER')
const userStore = useUserStore()

const canLogin = computed(() => phone.value.length === 11)

function fillDemo(type: 'owner' | 'sitter') {
  if (type === 'owner') {
    phone.value = '13800001111'
    role.value = 'OWNER'
  } else {
    phone.value = '13900001111'
    role.value = 'SITTER'
  }
}

async function handleLogin() {
  if (!canLogin.value) return
  try {
    uni.showLoading({ title: '登录中...' })
    const data = await authApi.login(phone.value, role.value)
    userStore.setLoginData(data, role.value)
    uni.hideLoading()
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 500)
  } catch {
    uni.hideLoading()
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5f0 0%, #ffe8d6 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 60rpx 0;
}
.logo-section {
  text-align: center;
  margin-bottom: 80rpx;
}
.logo-text {
  font-size: 72rpx;
  font-weight: bold;
  color: #ff6b35;
}
.logo-sub {
  display: block;
  font-size: 28rpx;
  color: #999;
  margin-top: 16rpx;
}
.form-section {
  width: 100%;
  background: #fff;
  border-radius: 24rpx;
  padding: 48rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}
.input-group {
  margin-bottom: 32rpx;
}
.label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
  display: block;
}
.input {
  width: 100%;
  height: 88rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 32rpx;
}
.role-selector {
  display: flex;
  gap: 24rpx;
  margin-bottom: 48rpx;
}
.role-btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #666;
}
.role-btn.active {
  border-color: #ff6b35;
  background: #fff5f0;
  color: #ff6b35;
  font-weight: bold;
}
.login-btn {
  width: 100%;
  height: 96rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 34rpx;
  border-radius: 48rpx;
  border: none;
  font-weight: bold;
}
.login-btn[disabled] {
  opacity: 0.5;
}
.demo-hint {
  margin-top: 40rpx;
  text-align: center;
}
.hint-title {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 12rpx;
}
.hint-text {
  font-size: 24rpx;
  color: #ff6b35;
  display: block;
  margin: 8rpx 0;
}
</style>
