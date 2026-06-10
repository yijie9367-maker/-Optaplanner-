/**
 * 全局常量和配置
 */

// ==================== 表格配置 ====================
export const TABLE_CONFIG = {
  DEFAULT_PAGE_SIZE: 15,
  PAGE_SIZES: [10, 15, 20, 30, 50],
  DEFAULT_LOADING: false,
  STRIPE: true, // 斑马纹
  BORDER: true, // 边框
  HIGHLIGHT_CURRENT_ROW: false, // 高亮当前行
  MAX_HEIGHT: null // 最大高度
}

// ==================== 表单配置 ====================
export const FORM_CONFIG = {
  DEFAULT_LABEL_WIDTH: '100px',
  DEFAULT_INPUT_SIZE: 'default',
  SIZE_OPTIONS: ['large', 'default', 'small'],
  TRIGGER_EVENT: 'blur' // 验证触发事件
}

// ==================== 分页配置 ====================
export const PAGINATION_CONFIG = {
  PAGE_SIZES: [10, 15, 20, 30, 50],
  LAYOUT: 'total, sizes, prev, pager, next, jumper',
  BACKGROUND: true // 分页按钮是否有背景色
}

// ==================== 对话框配置 ====================
export const DIALOG_CONFIG = {
  DEFAULT_WIDTH: '50%',
  CLOSE_ON_CLICK_MODAL: false,
  CLOSE_ON_PRESS_ESCAPE: true,
  DESTROY_ON_CLOSE: true,
  SHOW_CLOSE: true
}

// ==================== 消息提示配置 ====================
export const MESSAGE_CONFIG = {
  SUCCESS_DURATION: 2000,
  ERROR_DURATION: 3000,
  WARNING_DURATION: 3000,
  INFO_DURATION: 2000,
  POSITION: 'top-right'
}

// ==================== 颜色映射 ====================
export const COLOR_MAP = {
  // 状态颜色
  SUCCESS: 'success',
  WARNING: 'warning',
  DANGER: 'danger',
  INFO: 'info',
  PRIMARY: 'primary',
  
  // 性别
  MALE: '#409EFF',
  FEMALE: '#E78684',
  
  // 状态
  ACTIVE: 'success',
  INACTIVE: 'danger',
  PENDING: 'warning'
}

// ==================== 性别选项 ====================
export const GENDER_OPTIONS = [
  { label: '男', value: 'M' },
  { label: '女', value: 'F' }
]

// ==================== 通用字段验证规则 ====================
export const VALIDATION_RULES = {
  // 姓名
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  
  // 邮箱
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  
  // 电话
  phone: [
    { required: true, message: '请输入电话号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的电话号码', trigger: 'blur' }
  ],
  
  // 身份证
  idCard: [
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  
  // URL
  url: [
    { type: 'url', message: '请输入正确的URL地址', trigger: 'blur' }
  ],
  
  // 数字
  number: [
    { type: 'number', message: '请输入数字', trigger: 'blur' }
  ],
  
  // 年龄
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 0, max: 150, message: '年龄范围为 0-150', trigger: 'blur' }
  ]
}

// ==================== HTTP状态码映射 ====================
export const HTTP_STATUS = {
  SUCCESS: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503
}

// ==================== 时间常量 ====================
export const TIME_CONSTANTS = {
  SECOND: 1000,
  MINUTE: 60 * 1000,
  HOUR: 60 * 60 * 1000,
  DAY: 24 * 60 * 60 * 1000
}

// ==================== 星期映射 ====================
export const WEEK_DAYS = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 0 }
]

export const WEEK_DAY_NAMES = ['日', '一', '二', '三', '四', '五', '六']

// ==================== 学位映射 ====================
export const DEGREE_OPTIONS = [
  { label: '高中', value: 'HS' },
  { label: '本科', value: 'BA' },
  { label: '硕士', value: 'MA' },
  { label: '博士', value: 'PHD' }
]

// ==================== 职称映射 ====================
export const TITLE_OPTIONS = [
  { label: '讲师', value: 'lecturer' },
  { label: '副教授', value: 'associate_professor' },
  { label: '教授', value: 'professor' },
  { label: '助教', value: 'teaching_assistant' }
]

// ==================== 课程类型 ====================
export const COURSE_TYPE_OPTIONS = [
  { label: '理论', value: 'theory' },
  { label: '实践', value: 'practice' },
  { label: '混合', value: 'hybrid' }
]

// ==================== 排课状态映射 ====================
export const SCHEDULE_STATUS = {
  UNSCHEDULED: 'warning',
  SCHEDULED: 'success',
  CONFLICT: 'danger'
}

export const SCHEDULE_STATUS_LABEL = {
  UNSCHEDULED: '未安排',
  SCHEDULED: '已安排',
  CONFLICT: '冲突'
}
