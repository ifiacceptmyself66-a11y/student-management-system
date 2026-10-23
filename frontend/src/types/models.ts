import type { PageQuery } from './api'

export interface AdminInfo {
  id: number
  username: string
  nickname?: string
}

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  /** token 有效期, 单位秒 */
  expiresIn: number
  admin: AdminInfo
}

export interface ChangePasswordPayload {
  oldPassword: string
  newPassword: string
}

export interface ClassItem {
  id: number
  className: string
  grade: string
  headTeacher?: string
  remark?: string
  studentCount?: number
  createdAt?: string
}

export interface ClassQuery extends PageQuery {
  /** 班级名称模糊匹配 */
  keyword?: string
  /** 年级精确匹配 */
  grade?: string
}

export interface ClassPayload {
  className: string
  grade: string
  headTeacher?: string
  remark?: string
}

export type Gender = 0 | 1 | 2

export type StudentStatus = 0 | 1

export interface StudentItem {
  id: number
  studentNo: string
  name: string
  gender: Gender
  birthDate?: string
  /** 后端根据 birthDate 实时计算 */
  age?: number
  phone?: string
  email?: string
  guardianName?: string
  guardianPhone?: string
  classId: number
  className?: string
  grade?: string
  status: StudentStatus
  createdAt?: string
  updatedAt?: string
}

export interface StudentQuery extends PageQuery {
  /** 同时模糊匹配姓名与学号 */
  keyword?: string
  classId?: number
  gender?: Gender
  status?: StudentStatus
}

export interface StudentPayload {
  studentNo: string
  name: string
  gender: Gender
  birthDate?: string
  phone?: string
  email?: string
  guardianName?: string
  guardianPhone?: string
  classId: number
  status: StudentStatus
}
