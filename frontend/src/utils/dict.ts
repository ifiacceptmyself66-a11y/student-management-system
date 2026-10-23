import type { Gender, StudentStatus } from '@/types/models'

/** 每页条数可选项, 与后端一致 */
export const PAGE_SIZES = [10, 20, 50]

export const GENDER_OPTIONS: Array<{ value: Gender; label: string }> = [
  { value: 1, label: '男' },
  { value: 2, label: '女' },
  { value: 0, label: '未知' }
]

export const STUDENT_STATUS_OPTIONS: Array<{ value: StudentStatus; label: string }> = [
  { value: 1, label: '在读' },
  { value: 0, label: '离校' }
]

/** 年级为自由文本, 这里只提供常用候选, 表单中允许自行输入 */
export const GRADE_OPTIONS = ['2023级', '2024级', '2025级', '2026级', '2027级']

export function genderLabel(value?: number): string {
  return GENDER_OPTIONS.find((item) => item.value === value)?.label ?? '未知'
}

export function studentStatusLabel(value?: number): string {
  return value === 1 ? '在读' : '离校'
}
