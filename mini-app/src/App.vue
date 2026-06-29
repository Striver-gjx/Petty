<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.reLaunch({ url: '/pages/login/index' })
  }
})
</script>

<style>
page {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 28rpx;
  color: #333;
  background-color: #f5f5f5;
}

.container {
  padding: 24rpx;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.btn-primary {
  background: linear-gradient(135deg, #FF6B35, #FF8F5E);
  color: #fff;
  border-radius: 44rpx;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: 500;
}

.btn-primary:active {
  opacity: 0.85;
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #222;
  margin-bottom: 20rpx;
}

.text-muted {
  color: #999;
  font-size: 24rpx;
}

.text-price {
  color: #FF6B35;
  font-weight: 600;
}
</style>
