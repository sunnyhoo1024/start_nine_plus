<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增 Banner</el-button>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="120" />
      <el-table-column prop="imageUrl" label="图片" min-width="180" show-overflow-tooltip />
      <el-table-column prop="linkUrl" label="链接" min-width="140" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">{{ row.status === 1 ? '上线' : '草稿' }}</template>
      </el-table-column>
      <el-table-column prop="startAt" label="开始" width="170" />
      <el-table-column prop="endAt" label="结束" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑 Banner' : '新增 Banner'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="图片 URL" required>
          <el-input v-model="form.imageUrl" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option :value="0" label="草稿" />
            <el-option :value="1" label="上线" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBanners, createBanner, updateBanner, deleteBanner } from '@/api/banner'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const form = reactive({
  id: null,
  title: '',
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  status: 0,
  startAt: null,
  endAt: null
})

function resetForm() {
  Object.assign(form, {
    id: null,
    title: '',
    imageUrl: '',
    linkUrl: '',
    sortOrder: 0,
    status: 0,
    startAt: null,
    endAt: null
  })
}

async function load() {
  loading.value = true
  try {
    const data = await listBanners({ page: 1, size: 100 })
    list.value = data?.records || data?.list || data || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  visible.value = true
}

function openEdit(row) {
  Object.assign(form, { ...row })
  visible.value = true
}

async function onSave() {
  if (!form.title || !form.imageUrl) {
    ElMessage.warning('请填写标题和图片 URL')
    return
  }
  saving.value = true
  try {
    const payload = { ...form }
    delete payload.id
    if (form.id) {
      await updateBanner(form.id, payload)
    } else {
      await createBanner(payload)
    }
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确认删除 Banner「${row.title}」？`, '提示', { type: 'warning' })
  await deleteBanner(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
</style>
