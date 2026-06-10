<template>
  <div class="wscn-http404-container">
    <div class="wscn-http404">
      <div class="pic-404">
        <img class="pic-404__parent" src="../assets/404_images/404.png" alt="404" />
        <img class="pic-404__child left" src="../assets/404_images/404_cloud.png" alt="404" />
        <img class="pic-404__child mid" src="../assets/404_images/404_cloud.png" alt="404" />
        <img class="pic-404__child right" src="../assets/404_images/404_cloud.png" alt="404" />
      </div>

      <div class="bullshit">
        <div class="bullshit__oops">OOPS!</div>
        <div class="bullshit__headline">{{ message }}</div>
        <div class="bullshit__info">
          请检查您输入的网址是否正确，或点击下方按钮返回首页。
        </div>
        <button class="bullshit__return-home" @click="goHome">返回首页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

// 引入图片，Vite 会正确解析

const router = useRouter()

const goHome = () => {
  router.push('/login')
}

const message = '抱歉，您访问的页面不存在...'
</script>

<style scoped>
.wscn-http404-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f5f5;
}
.pic-404 {
  position: relative;
  text-align: center;
}
.pic-404__parent {
  width: 300px;
}
.pic-404__child {
  position: absolute;
  width: 100px;
  top: 50px;
}
.pic-404__child.left { left: -120px; }
.pic-404__child.mid { left: 100px; }
.pic-404__child.right { right: -120px; }

.bullshit {
  text-align: center;
  margin-top: 20px;
}
.bullshit__oops {
  font-size: 48px;
  font-weight: bold;
  color: #ff4d4f;
}
.bullshit__headline {
  font-size: 18px;
  margin: 10px 0;
}
.bullshit__info {
  font-size: 14px;
  color: #888;
}
.bullshit__return-home {
  margin-top: 15px;
  padding: 8px 16px;
  background-color: #20a0ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.bullshit__return-home:hover {
  background-color: #1b8cd8;
}
</style>
