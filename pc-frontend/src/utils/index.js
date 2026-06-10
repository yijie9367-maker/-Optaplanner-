/**
 * 通用工具库索引
 * 集中导出所有可复用的工具和Composable
 */

// ==================== Composables ====================
export { useTableOperation } from '@/composables/useTableOperation'
export { useFormDialog, useCrudOperation } from '@/composables/useFormDialog'
export { useDataLoading } from '@/composables/useDataLoading'

// ==================== Formatters ====================
export {
  formatDateTime,
  formatDate,
  formatTime
} from '@/utils/formatters'

// ==================== API Factory ====================
export {
  createCrudApi,
  createSearchableApi,
  createPaginatedApi,
  withCache,
  withRetry
} from '@/utils/apiFactory'

// ==================== Constants ====================
export {
  TABLE_CONFIG,
  FORM_CONFIG,
  PAGINATION_CONFIG,
  DIALOG_CONFIG,
  MESSAGE_CONFIG,
  COLOR_MAP,
  GENDER_OPTIONS,
  VALIDATION_RULES,
  HTTP_STATUS,
  TIME_CONSTANTS,
  WEEK_DAYS,
  WEEK_DAY_NAMES,
  DEGREE_OPTIONS,
  TITLE_OPTIONS,
  COURSE_TYPE_OPTIONS,
  SCHEDULE_STATUS,
  SCHEDULE_STATUS_LABEL
} from '@/constants/config'
