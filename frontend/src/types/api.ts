/** 后端统一响应体 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

/** 分页响应体 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

/** 分页请求参数 */
export interface PageQuery {
  page: number
  size: number
}
