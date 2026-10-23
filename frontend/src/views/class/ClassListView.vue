<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { deleteClass, fetchClassPage } from '@/api/class'
import { usePagination } from '@/composables/usePagination'
import { GRADE_OPTIONS, PAGE_SIZES } from '@/utils/dict'
import type { ClassItem, ClassQuery } from '@/types/models'
import ClassFormDialog from './ClassFormDialog.vue'

const loading = ref(false)
const records = ref<ClassItem[]>([])
const { page, size, total, resetPage, changeSize, changePage } = usePagination()

const filters = reactive({ keyword: '', grade: '' })

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

function buildQuery(): ClassQuery {
  return {
    page: page.value,
    size: size.value,
    keyword: filters.keyword.trim() || undefined,
    grade: filters.grade || undefined
  }
}

async function loadData(): Promise<void> {
  loading.value = true
  try {
    const result = await fetchClassPage(buildQuery())
    records.value = result.records
    total.value = result.total
  } catch {
    // 请求层已提示错误
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch(): void {
  resetPage()
  void loadData()
}

function handleReset(): void {
  filters.keyword = ''
  filters.grade = ''
  resetPage()
  void loadData()
}

function handleSizeChange(value: number): void {
  changeSize(value)
  void loadData()
}

function handlePageChange(value: number): void {
  changePage(value)
  void loadData()
}

function handleCreate(): void {
  editingId.value = null
  dialogVisible.value = true
}

function handleEdit(row: ClassItem): void {
  editingId.value = row.id
  dialogVisible.value = true
}

async function handleDelete(row: ClassItem): Promise<void> {
  try {
    await ElMessageBox.confirm(`确定删除班级「${row.className}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    await deleteClass(row.id)
    ElMessage.success('删除成功')
    // 删掉当前页最后一条时回退一页, 避免停在空页
    if (records.value.length === 1 && page.value > 1) {
      changePage(page.value - 1)
    }
    await loadData()
  } catch {
    // 请求层已提示错误(如班级下仍有学生)
  }
}

function handleSaved(): void {
  void loadData()
}

onMounted(() => {
  void loadData()
})
</script>

<template>
  <div class="page">
    <header class="page-head">
      <div>
        <h2 class="page-title">班级管理</h2>
        <p class="page-desc">维护年级与班级信息，班级下仍有学生时不允许删除。</p>
      </div>
      <el-button type="primary" @click="handleCreate">新增班级</el-button>
    </header>

    <section class="panel">
      <div class="toolbar">
        <el-input
          v-model="filters.keyword"
          class="toolbar-input"
          placeholder="搜索班级名称"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select
          v-model="filters.grade"
          class="toolbar-select"
          placeholder="全部年级"
          clearable
          filterable
          allow-create
          @change="handleSearch"
        >
          <el-option v-for="item in GRADE_OPTIONS" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="className" label="班级名称" min-width="160" />
        <el-table-column prop="grade" label="年级" width="110" />
        <el-table-column label="班主任" width="120">
          <template #default="{ row }">{{ row.headTeacher || '—' }}</template>
        </el-table-column>
        <el-table-column label="学生数" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.studentCount" type="info" effect="plain">{{ row.studentCount }}</el-tag>
            <span v-else class="muted">0</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无班级数据" />
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="PAGE_SIZES"
          :current-page="page"
          :page-size="size"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </section>

    <ClassFormDialog v-model="dialogVisible" :class-id="editingId" @saved="handleSaved" />
  </div>
</template>
