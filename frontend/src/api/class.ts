import { del, get, post, put } from '@/api/request'
import type { PageResult } from '@/types/api'
import type { ClassItem, ClassPayload, ClassQuery } from '@/types/models'

export function fetchClassPage(query: ClassQuery): Promise<PageResult<ClassItem>> {
  return get<PageResult<ClassItem>>('/classes', query)
}

export function fetchClassOptions(keyword?: string): Promise<ClassItem[]> {
  return get<ClassItem[]>('/classes/options', { keyword })
}

export function fetchClassDetail(id: number): Promise<ClassItem> {
  return get<ClassItem>(`/classes/${id}`)
}

export function createClass(payload: ClassPayload): Promise<number> {
  return post<number>('/classes', payload)
}

export function updateClass(id: number, payload: ClassPayload): Promise<void> {
  return put<void>(`/classes/${id}`, payload)
}

export function deleteClass(id: number): Promise<void> {
  return del<void>(`/classes/${id}`)
}
