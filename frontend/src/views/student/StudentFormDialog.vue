<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

import { fetchClassOptions } from '@/api/class'
import { createStudent, fetchStudentDetail, updateStudent } from '@/api/student'
import { GENDER_OPTIONS, STUDENT_STATUS_OPTIONS } from '@/utils/dict'
import type { ClassItem, Gender, StudentPayload, StudentStatus } from '@/types/models'

interface StudentForm {
  studentNo: string
  name: string
  gender: Gender
  birthDate?: string
  phone: string
  email: string
  guardianName: string
  guardianPhone: string
  classId?: number
  status: StudentStatus
}

const props = defineProps<{
  modelValue: boolean
  /** null 表示新增, 否则为编辑的学生 id */
  studentId: number | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const isEdit = computed(() => props.studentId !== null)

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const classOptions = ref<ClassItem[]>([])

const form = reactive<StudentForm>({
  studentNo: '',
  name: '',
  gender: 1,
  birthDate: undefined,
  phone: '',
  email: '',
  guardianName: '',
  guardianPhone: '',
  classId: undefined,
  status: 1
})

const rules: FormRules<StudentForm> = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { max: 20, message: '学号长度不能超过 20', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 50, message: '姓名长度不能超过 50', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  classId: [{ required: true, message: '请选择所属班级', trigger: 'change' }],
  // 选填字段, 空字符串视为未填写
  phone: [{ pattern: /^$|^\d{6,20}$/, message: '手机号应为 6-20 位数字', trigger: 'blur' }],
  guardianPhone: [{ pattern: /^$|^\d{6,20}$/, message: '联系电话应为 6-20 位数字', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

function resetForm(): void {
  form.studentNo = ''
  form.name = ''
  form.gender = 1
  form.birthDate = undefined
  form.phone = ''
  form.email = ''
  form.guardianName = ''
  form.guardianPhone = ''
  form.classId = undefined
  form.status = 1
  formRef.value?.clearValidate()
}

async function loadOptions(): Promise<void> {
  try {
    classOptions.value = await fetchClassOptions()
  } catch {
    classOptions.value = []
  }
}

async function loadDetail(): Promise<void> {
  if (props.studentId === null) {
    resetForm()
    return
  }
  loading.value = true
  try {
    const detail = await fetchStudentDetail(props.studentId)
    form.studentNo = detail.studentNo
    form.name = detail.name
    form.gender = detail.gender
    form.birthDate = detail.birthDate
    form.phone = detail.phone ?? ''
    form.email = detail.email ?? ''
    form.guardianName = detail.guardianName ?? ''
    form.guardianPhone = detail.guardianPhone ?? ''
    form.classId = detail.classId
    form.status = detail.status
  } catch {
    visible.value = false
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (!open) {
      return
    }
    void loadOptions()
    void loadDetail()
  }
)

function classLabel(item: ClassItem): string {
  return item.grade ? `${item.grade} ${item.className}` : item.className
}

function disabledDate(date: Date): boolean {
  return date.getTime() > Date.now()
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || form.classId === undefined) {
    return
  }

  const payload: StudentPayload = {
    studentNo: form.studentNo.trim(),
    name: form.name.trim(),
    gender: form.gender,
    birthDate: form.birthDate || undefined,
    phone: form.phone.trim() || undefined,
    email: form.email.trim() || undefined,
    guardianName: form.guardianName.trim() || undefined,
    guardianPhone: form.guardianPhone.trim() || undefined,
    classId: form.classId,
    status: form.status
  }

  submitting.value = true
  try {
    if (props.studentId === null) {
      await createStudent(payload)
    } else {
      await updateStudent(props.studentId, payload)
    }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    visible.value = false
    emit('saved')
  } catch {
    // 请求层已提示错误, 保留弹窗便于修改
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑学生' : '新增学生'"
    width="680px"
    :close-on-click-modal="false"
    @closed="resetForm"
  >
    <el-form ref="formRef" v-loading="loading" :model="form" :rules="rules" label-width="92px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="form.studentNo" maxlength="20" placeholder="手工录入, 需唯一" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" maxlength="50" placeholder="学生姓名" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio v-for="item in GENDER_OPTIONS" :key="item.value" :value="item.value">
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出生日期" prop="birthDate">
            <el-date-picker
              v-model="form.birthDate"
              class="full"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="用于计算年龄"
              :disabled-date="disabledDate"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="所属班级" prop="classId">
            <el-select v-model="form.classId" class="full" filterable placeholder="请选择班级">
              <el-option
                v-for="item in classOptions"
                :key="item.id"
                :label="classLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio v-for="item in STUDENT_STATUS_OPTIONS" :key="item.value" :value="item.value">
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" maxlength="20" placeholder="选填" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" maxlength="100" placeholder="选填" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="家长姓名" prop="guardianName">
            <el-input v-model="form.guardianName" maxlength="50" placeholder="选填" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="家长电话" prop="guardianPhone">
            <el-input v-model="form.guardianPhone" maxlength="20" placeholder="选填" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.full {
  width: 100%;
}
</style>
