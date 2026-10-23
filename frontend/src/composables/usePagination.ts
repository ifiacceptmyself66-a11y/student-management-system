import { ref } from 'vue'

/**
 * 列表页分页状态。页码/每页条数变更后由调用方负责重新请求。
 */
export function usePagination(initialSize = 10) {
  const page = ref(1)
  const size = ref(initialSize)
  const total = ref(0)

  function resetPage(): void {
    page.value = 1
  }

  function changeSize(value: number): void {
    size.value = value
    page.value = 1
  }

  function changePage(value: number): void {
    page.value = value
  }

  return { page, size, total, resetPage, changeSize, changePage }
}
