<template>
    <div class="frontend-layout">
        <div class="navbar-container">
            <div class="brand-section">
                <el-image style="width: 50px; height: 50px" :src="iconUrl" alt="品牌logo" class="brand-logo" />
                <h1 class="brand-name">小药AI助手</h1>
            </div>
            <div class="nav-section">
                <router-link to="/" class="nav-link">首页</router-link>
                <router-link to="/consultation" class="nav-link" v-if="isLoggedIn">AI咨询</router-link>
                <router-link to="/emotion-diary" class="nav-link" v-if="isLoggedIn">情绪日记</router-link>
                <router-link to="/knowledge" class="nav-link">知识库</router-link>
                <router-link to="/questionnaire" class="nav-link" v-if="isLoggedIn">心理测评</router-link>
                <button class="dark-toggle-btn" @click="toggleDark" :title="isDark ? '切换亮色模式' : '切换暗色模式'">
                    <el-icon :size="20">
                        <Sunny v-if="isDark" />
                        <Moon v-else />
                    </el-icon>
                </button>
                <el-button v-if="isLoggedIn" class="logout-btn" @click="handleLogout">退出登录</el-button>
                <template v-else>
                    <router-link to="/auth/login" class="nav-link">登录</router-link>
                    <router-link to="/auth/register" class="nav-link">
                        <el-button type="primary">注册</el-button>
                    </router-link>
                </template>
            </div>
        </div>
        <div class="main-content">
            <router-view></router-view>
        </div>
        <div class="footer-container">
            <div class="footer-bottom">
                <p>&copy; 2026 小药AI助手. All rights reserved.</p>
            </div>
        </div>
    </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const iconUrl = new URL('@/assets/images/机器人.png', import.meta.url).href

const isLoggedIn = ref(false)

// Dark mode
const isDark = ref(false)

const toggleDark = () => {
    isDark.value = !isDark.value
    if (isDark.value) {
        document.documentElement.classList.add('dark')
        document.documentElement.setAttribute('data-theme', 'dark')
    } else {
        document.documentElement.classList.remove('dark')
        document.documentElement.removeAttribute('data-theme')
    }
    localStorage.setItem('darkMode', isDark.value)
}

// 登出
const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/auth/login')
}

onMounted(() => {
   isLoggedIn.value = localStorage.getItem('token') !== null
   // Restore dark mode from localStorage
   const savedDark = localStorage.getItem('darkMode')
   if (savedDark === 'true') {
       isDark.value = true
       document.documentElement.classList.add('dark')
       document.documentElement.setAttribute('data-theme', 'dark')
   }
})
</script>
<style scoped lang="scss">
.frontend-layout {
    background-color: #fff;

    .navbar-container {
        max-width: 1200px;
        height: 100%;
        margin: 0 auto;
        padding: 10px;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .brand-section {
            display: flex;
            align-items: center;

            .brand-name {
                margin-left: 10px;
                font-size: 24px;
                font-weight: 600;
                color: #333;
            }
        }

        .nav-section {
            display: flex;
            align-items: center;
            gap: 40px;

            .nav-link {
                color: #4b5563;
                font-size: 16px;
                font-weight: 500;

                &:hover {
                    color: #4A90E2;
                }
            }
        }
    }

    .footer-container {
        background: #1f2937;
        color: white;
        padding: 15px 0;
        margin-top: auto;

        .footer-bottom {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 10px;
            text-align: center;
        }
    }
}
</style>
