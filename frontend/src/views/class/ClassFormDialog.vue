<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

import { createClass, fetchClassDetail, updateClass } from '@/api/class'
import type { ClassPayload } from '@/types/models'
import { GRADE_OPTIONS } from '@/utils/dict'

const props = defineProps<{
  modelValue: boolean
  /** null 表示新增, 否则为编辑的班级 id */
  classId: number | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const isEdit = computed(() => props.classId !== null)

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

const form = reactive<ClassPayload>({
  className: '',
  grade: '',
  headTeacher: '',
  remark: ''
})

const rules: FormRules<ClassPayload> = {
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择或输入年级', trigger: 'change' }]
}

function resetForm(): void {
  form.className = ''
  form.grade = ''
  form.headTeacher = ''
  form.remark = ''
  formRef.value?.clearValidate()
}

async function loadDetail(): Promise<void> {
  if (props.classId === null) {
    resetForm()
    return
  }
  loading.value = true
  try {
    const detail = await fetchClassDetail(props.classId)
    form.className = detail.className
    form.grade = detail.grade
    form.headTeacher = detail.headTeacher ?? ''
    form.remark = detail.remark ?? ''
  } catch {
    // 请求层已提示, 关闭弹窗避免提交脏数据
    visible.value = false
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      void loadDetail()
    }
  }
)

async function handleSubmit(): Promise<void> {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  const payload: ClassPayload = {
    className: form.className.trim(),
    grade: form.grade.trim(),
    headTeacher: form.headTeacher?.trim() || undefined,
    remark: form.remark?.trim() || undefined
  }

  submitting.value = true
  try {
    if (props.classId === null) {
      await createClass(payload)
    } else {
      await updateClass(props.classId, payload)
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
    :title="isEdit ? '编辑班级' : '新增班级'"
    width="480px"
    :close-on-click-modal="false"
    @closed="resetForm"
  >
    <el-form ref="formRef" v-loading="loading" :model="form" :rules="rules" label-width="88px">
      <el-form-item label="班级名称" prop="className">
        <el-input v-model="form.className" maxlength="50" show-word-limit placeholder="如：高一(1)班" />
      </el-form-item>

      <el-form-item label="年级" prop="grade">
        <el-select
          v-model="form.grade"
          class="full"
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入年级"
        >
          <el-option v-for="item in GRADE_OPTIONS" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>

      <el-form-item label="班主任" prop="headTeacher">
        <el-input v-model="form.headTeacher" maxlength="50" placeholder="选填" />
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="3"
          maxlength="255"
          show-word-limit
          placeholder="选填"
        />
      </el-form-item>
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
