<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增内容块</el-button>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="code" label="编码" width="140" />
      <el-table-column prop="title" label="标题" min-width="120" />
      <el-table-column prop="contentJson" label="内容 JSON" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ typeof row.contentJson === 'string' ? row.contentJson : JSON.stringify(row.contentJson) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">{{ row.status === 1 ? '上线' : '草稿' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑内容块' : '新增内容块'" width="640px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="编码" required>
          <el-input v-model="form.code" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容 JSON">
          <el-input v-model="form.contentJsonText" type="textarea" :rows="8" placeholder='例如 {"text":"介绍文案"}' />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option :value="0" label="草稿" />
            <el-option :value="1" label="上线" />
          </el-select>
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
import { listBlocks, createBlock, updateBlock, deleteBlock } from '@/api/block'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const form = reactive({
  id: null,
  code: '',
  title: '',
  contentJsonText: '',
  status: 0
})

function resetForm() {
  Object.assign(form, { id: null, code: '', title: '', contentJsonText: '', status: 0 })
}

async function load() {
  loading.value = true
  try {
    const data = await listBlocks({ page: 1, size: 100 })
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
  form.id = row.id
  form.code = row.code
  form.title = row.title
  form.status = row.status
  form.contentJsonText =
    typeof row.contentJson === 'string' ? row.contentJson : JSON.stringify(row.contentJson || {}, null, 2)
  visible.value = true
}

async function onSave() {
  if (!form.code || !form.title) {
    ElMessage.warning('请填写编码和标题')
    return
  }
  let contentJson = form.contentJsonText
  if (contentJson) {
    try {
      contentJson = JSON.parse(contentJson)
    } catch {
      ElMessage.error('内容 JSON 格式不正确')
      return
    }
  } else {
    contentJson = null
  }
  saving.value = true
  try {
    const payload = { code: form.code, title: form.title, contentJson, status: form.status }
    if (form.id) {
      await updateBlock(form.id, payload)
    } else {
      await createBlock(payload)
    }
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确认删除内容块「${row.code}」？`, '提示', { type: 'warning' })
  await deleteBlock(row.id)
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
