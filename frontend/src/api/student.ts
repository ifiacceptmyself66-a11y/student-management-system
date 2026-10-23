import { del, get, post, put } from '@/api/request'
import type { PageResult } from '@/types/api'
import type { StudentItem, StudentPayload, StudentQuery } from '@/types/models'

export function fetchStudentPage(query: StudentQuery): Promise<PageResult<StudentItem>> {
  return get<PageResult<StudentItem>>('/students', query)
}

export function fetchStudentDetail(id: number): Promise<StudentItem> {
  return get<StudentItem>(`/students/${id}`)
}

export function createStudent(payload: StudentPayload): Promise<number> {
  return post<number>('/students', payload)
}

export function updateStudent(id: number, payload: StudentPayload): Promise<void> {
  return put<void>(`/students/${id}`, payload)
}

export function deleteStudent(id: number): Promise<void> {
  return del<void>(`/students/${id}`)
}
