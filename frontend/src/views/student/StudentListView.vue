<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { fetchClassOptions } from '@/api/class'
import { deleteStudent, fetchStudentPage } from '@/api/student'
import { usePagination } from '@/composables/usePagination'
import {
  GENDER_OPTIONS,
  PAGE_SIZES,
  STUDENT_STATUS_OPTIONS,
  genderLabel,
  studentStatusLabel
} from '@/utils/dict'
import type { ClassItem, Gender, StudentItem, StudentQuery, StudentStatus } from '@/types/models'
import StudentFormDialog from './StudentFormDialog.vue'

const loading = ref(false)
const records = ref<StudentItem[]>([])
const classOptions = ref<ClassItem[]>([])
const { page, size, total, resetPage, changeSize, changePage } = usePagination()

const filters = reactive<{
  keyword: string
  classId: number | undefined
  gender: Gender | undefined
  status: StudentStatus | undefined
}>({
  keyword: '',
  classId: undefined,
  gender: undefined,
  status: undefined
})

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

function buildQuery(): StudentQuery {
  return {
    page: page.value,
    size: size.value,
    keyword: filters.keyword.trim() || undefined,
    classId: filters.classId,
    gender: filters.gender,
    status: filters.status
  }
}

async function loadData(): Promise<void> {
  loading.value = true
  try {
    const result = await fetchStudentPage(buildQuery())
    records.value = result.records
    total.value = result.total
  } catch {
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadClassOptions(): Promise<void> {
  try {
    classOptions.value = await fetchClassOptions()
  } catch {
    classOptions.value = []
  }
}

function handleSearch(): void {
  resetPage()
  void loadData()
}

function handleReset(): void {
  filters.keyword = ''
  filters.classId = undefined
  filters.gender = undefined
  filters.status = undefined
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

function handleEdit(row: StudentItem): void {
  editingId.value = row.id
  dialogVisible.value = true
}

async function handleDelete(row: StudentItem): Promise<void> {
  try {
    await ElMessageBox.confirm(`确定删除学生「${row.name}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    await deleteStudent(row.id)
    ElMessage.success('删除成功')
    if (records.value.length === 1 && page.value > 1) {
      changePage(page.value - 1)
    }
    await loadData()
  } catch {
    // 请求层已提示错误
  }
}

function handleSaved(): void {
  void loadData()
}

onMounted(() => {
  void loadClassOptions()
  void loadData()
})
</script>

<template>
  <div class="page">
    <header class="page-head">
      <div>
        <h2 class="page-title">学生管理</h2>
        <p class="page-desc">统一维护学生基本信息，支持按姓名或学号搜索。</p>
      </div>
      <el-button type="primary" @click="handleCreate">新增学生</el-button>
    </header>

    <section class="panel">
      <div class="toolbar">
        <el-input
          v-model="filters.keyword"
          class="toolbar-input"
          placeholder="搜索姓名 / 学号"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select
          v-model="filters.classId"
          class="toolbar-select"
          placeholder="全部班级"
          clearable
          filterable
          @change="handleSearch"
        >
          <el-option
            v-for="item in classOptions"
            :key="item.id"
            :label="item.grade ? `${item.grade} ${item.className}` : item.className"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="filters.gender"
          class="toolbar-select-sm"
          placeholder="性别"
          clearable
          @change="handleSearch"
        >
          <el-option
            v-for="item in GENDER_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select
          v-model="filters.status"
          class="toolbar-select-sm"
          placeholder="状态"
          clearable
          @change="handleSearch"
        >
          <el-option
            v-for="item in STUDENT_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="name" label="姓名" width="110" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ genderLabel(row.gender) }}</template>
        </el-table-column>
        <el-table-column label="年龄" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.age !== undefined && row.age !== null">{{ row.age }}</span>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="班级" min-width="140">
          <template #default="{ row }">
            <span v-if="row.className">{{ row.className }}</span>
            <span v-else class="muted">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="家长" width="110">
          <template #default="{ row }">{{ row.guardianName || '—' }}</template>
        </el-table-column>
        <el-table-column label="家长电话" width="140">
          <template #default="{ row }">{{ row.guardianPhone || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
              {{ studentStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无学生数据" />
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

    <StudentFormDialog v-model="dialogVisible" :student-id="editingId" @saved="handleSaved" />
  </div>
</template>
