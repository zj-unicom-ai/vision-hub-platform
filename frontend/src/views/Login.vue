<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>⊙ 算法平台</h1>
        <p>视觉模型处理平台</p>
      </div>
      <a-form
        :model="formState"
        :rules="rules"
        @finish="handleLogin"
        layout="vertical"
      >
        <a-form-item name="username" label="用户名">
          <a-input
            v-model:value="formState.username"
            placeholder="请输入用户名"
            size="large"
            :prefix="h(UserOutlined)"
          />
        </a-form-item>
        <a-form-item name="password" label="密码">
          <a-input-password
            v-model:value="formState.password"
            placeholder="请输入密码"
            size="large"
            :prefix="h(LockOutlined)"
          />
        </a-form-item>
        <a-form-item name="captchaCode" label="验证码">
          <div class="captcha-row">
            <a-input
              v-model:value="formState.captchaCode"
              placeholder="请输入验证码"
              size="large"
              class="captcha-input"
            />
            <button
              type="button"
              class="captcha-image-button"
              :disabled="captchaLoading"
              title="点击刷新验证码"
              @click="refreshCaptcha"
            >
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <span v-else>{{ captchaLoading ? '加载中' : '点击刷新' }}</span>
            </button>
          </div>
        </a-form-item>
        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
          >
            登 录
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/store/user'
import { getCaptchaApi, loginApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const captchaLoading = ref(false)
const captchaId = ref('')
const captchaImage = ref('')

const formState = reactive({
  username: '',
  password: '',
  captchaCode: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }],
  captchaCode: [{ required: true, message: '请输入验证码' }],
}

async function refreshCaptcha() {
  captchaLoading.value = true
  try {
    const res = await getCaptchaApi()
    captchaId.value = res.data.captchaId
    captchaImage.value = res.data.captchaImage
  } catch {
    captchaId.value = ''
    captchaImage.value = ''
  } finally {
    captchaLoading.value = false
  }
}

async function handleLogin() {
  if (!captchaId.value) {
    message.warning('请先获取验证码')
    await refreshCaptcha()
    return
  }

  loading.value = true
  try {
    const res = await loginApi({
      username: formState.username,
      password: formState.password,
      captchaId: captchaId.value,
      captchaCode: formState.captchaCode,
    })
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      const name = res.data.user?.username || formState.username
      userStore.setUsername(name)
      message.success('登录成功')
      router.push('/')
    }
  } catch {
    formState.captchaCode = ''
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped lang="less">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a3a8a 0%, #1e50c0 50%, #2764e0 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  h1 {
    font-size: 24px;
    color: #1a3a8a;
    margin-bottom: 8px;
  }

  p {
    color: #999;
    font-size: 14px;
  }
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.captcha-input {
  flex: 1;
  min-width: 0;
}

.captcha-image-button {
  width: 128px;
  height: 40px;
  padding: 0;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #f7f8fa;
  color: #8c8c8c;
  font-size: 13px;
  line-height: 38px;
  cursor: pointer;
  overflow: hidden;

  &:hover:not(:disabled) {
    border-color: #2764e0;
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.7;
  }

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
</style>
