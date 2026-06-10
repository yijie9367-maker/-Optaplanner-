import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import Dashboard from '../views/admin/Dashboard.vue'
import Students from '../views/admin/Students.vue'
import Teachers from '../views/admin/Teachers.vue'
import Courses from '../views/admin/Courses.vue'
import TeacherCourses from '../views/admin/TeacherCourses.vue'
import Scheduling from '../views/admin/Scheduling.vue'
import Classroom from '../views/admin/Classroom.vue'
import ClassGroup from '../views/admin/ClassGroup.vue'
import Admins from '../views/admin/Admins.vue'
import ExamAdmin from '../views/admin/ExamAdmin.vue'
import Messages from '../views/admin/Messages.vue'
import TeacherMessages from '../views/admin/TeacherMessages.vue'
import StudentMessages from '../views/admin/StudentMessages.vue'
import ForumAdmin from '../views/admin/ForumAdmin.vue'
import SensitiveWords from '../views/admin/SensitiveWords.vue'
import NotFound from '../views/404.vue'
import AdminLogin from '../views/login/AdminLogin.vue'
import StudentLogin from '../views/login/StudentLogin.vue'
import StudentDashboard from '../views/student/StudentDashboard.vue'
import TeacherDashboard from '../views/teacher/TeacherDashboard.vue'
import VideoIndex from '../views/video/VideoIndex.vue'
import VideoShow from '../views/video/VideoShow.vue'
import VideoPlay from '../views/video/VideoPlay.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'StudentLogin',
    component: StudentLogin
  },
  {
    path: '/admin-login',
    name: 'AdminLogin',
    component: AdminLogin
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true, role: 'admin' }, // 需要admin登录权限
    children: [
      { path: 'admins', name: 'Admins', component: Admins, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'students', name: 'Students', component: Students, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'teachers', name: 'Teachers', component: Teachers, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'courses', name: 'Courses', component: Courses, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'teacher-courses', name: 'TeacherCourses', component: TeacherCourses, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'scheduling', name: 'Scheduling', component: Scheduling, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'classroom', name: 'Classroom', component: Classroom, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'class-group', name: 'ClassGroup', component: ClassGroup, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'exams', name: 'ExamAdmin', component: ExamAdmin, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'messages-admin', name: 'MessagesAdmin', component: Messages, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'messages-teacher', name: 'MessagesTeacher', component: TeacherMessages, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'messages-student', name: 'MessagesStudent', component: StudentMessages, meta: { requiresAuth: true, role: 'admin' } }
      ,{ path: 'forum', name: 'Forum', component: ForumAdmin, meta: { requiresAuth: true, role: 'admin' } },
      { path: 'sensitive-words', name: 'SensitiveWords', component: SensitiveWords, meta: { requiresAuth: true, role: 'admin' } }
    ]
  },
  {
    path: '/student-dashboard',
    name: 'StudentDashboard',
    component: StudentDashboard,
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/teacher-dashboard',
    name: 'TeacherDashboard',
    component: TeacherDashboard,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFound },
  {
    path: '/video',
    name: 'VideoIndex',
    component: VideoIndex
  },
  {
    path: '/video/:showId',
    name: 'VideoShow',
    component: VideoShow
  },
  {
    path: '/video/:showId/play/:epId',
    name: 'VideoPlay',
    component: VideoPlay
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth) {
    if (userStore.token) {
      // 检查角色权限
      if (to.meta.role && userStore.user?.role !== to.meta.role) {
        // 角色不匹配：跳转到当前用户对应的登录页
        if (userStore.user?.role === 'admin') {
          next('/admin-login')
        } else {
          next('/login')
        }
        return
      }
      // 已登录，允许访问
      next()
    } else {
      // 未登录，根据所需角色跳转到不同的登录页
      if (to.meta.role === 'admin') {
        next('/admin-login')
      } else {
        next('/login')
      }
    }
  } else {
    next()
  }
})

export default router
