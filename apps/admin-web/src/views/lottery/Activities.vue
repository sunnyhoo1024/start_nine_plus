<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增活动</el-button>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="costPoints" label="消耗积分" width="100" />
      <el-table-column prop="dailyLimit" label="日限" width="80" />
      <el-table-column prop="totalLimit" label="总限" width="80" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">{{ row.status === 1 ? '上线' : '草稿' }}</template>
      </el-table-column>
      <el-table-column prop="startAt" label="开始" width="170" />
      <el-table-column prop="endAt" label="结束" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="success" @click="openPrizes(row)">奖品</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑活动' : '新增活动'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="消耗积分">
          <el-input-number v-model="form.costPoints" :min="0" />
        </el-form-item>
        <el-form-item label="每日上限">
          <el-input-number v-model="form.dailyLimit" :min="0" />
        </el-form-item>
        <el-form-item label="总上限">
          <el-input-number v-model="form.totalLimit" :min="0" />
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

    <el-dialog v-model="prizeVisible" :title="`奖品管理 - ${currentActivity?.name || ''}`" width="860px">
      <div class="toolbar">
        <el-button type="primary" size="small" @click="openPrizeCreate">新增奖品</el-button>
        <el-button size="small" @click="loadPrizes">刷新</el-button>
      </div>
      <el-table :data="prizes" v-loading="prizeLoading" border size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" min-width="100" />
        <el-table-column prop="prizeType" label="类型" width="100" />
        <el-table-column prop="pointsValue" label="积分值" width="80" />
        <el-table-column prop="weight" label="权重" width="70" />
        <el-table-column prop="stock" label="库存" width="70" />
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="openPrizeEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onPrizeDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="prizeFormVisible" :title="prizeForm.id ? '编辑奖品' : '新增奖品'" width="480px" append-to-body>
        <el-form :model="prizeForm" label-width="90px">
          <el-form-item label="名称" required>
            <el-input v-model="prizeForm.name" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="prizeForm.prizeType" style="width: 100%">
              <el-option value="POINTS" label="积分" />
              <el-option value="THANKS" label="谢谢参与" />
              <el-option value="PHYSICAL" label="实物" />
            </el-select>
          </el-form-item>
          <el-form-item label="积分值">
            <el-input-number v-model="prizeForm.pointsValue" :min="0" />
          </el-form-item>
          <el-form-item label="权重">
            <el-input-number v-model="prizeForm.weight" :min="1" />
          </el-form-item>
          <el-form-item label="库存">
            <el-input-number v-model="prizeForm.stock" />
            <div class="tip">-1 表示无限</div>
          </el-form-item>
          <el-form-item label="图片 URL">
            <el-input v-model="prizeForm.imageUrl" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="prizeForm.sortOrder" :min="0" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="prizeForm.status" style="width: 100%">
              <el-option :value="0" label="停用" />
              <el-option :value="1" label="启用" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="prizeFormVisible = false">取消</el-button>
          <el-button type="primary" :loading="prizeSaving" @click="onPrizeSave">保存</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listActivities,
  createActivity,
  updateActivity,
  deleteActivity,
  listPrizes,
  createPrize,
  updatePrize,
  deletePrize
} from '@/api/lottery'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const form = reactive({
  id: null,
  name: '',
  description: '',
  costPoints: 0,
  dailyLimit: 0,
  totalLimit: 0,
  status: 0,
  startAt: null,
  endAt: null
})

const prizeVisible = ref(false)
const prizeLoading = ref(false)
const prizeSaving = ref(false)
const prizeFormVisible = ref(false)
const prizes = ref([])
const currentActivity = ref(null)
const prizeForm = reactive({
  id: null,
  name: '',
  prizeType: 'POINTS',
  pointsValue: 0,
  weight: 1,
  stock: -1,
  imageUrl: '',
  sortOrder: 0,
  status: 1
})

function resetForm() {
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    costPoints: 0,
    dailyLimit: 0,
    totalLimit: 0,
    status: 0,
    startAt: null,
    endAt: null
  })
}

function resetPrizeForm() {
  Object.assign(prizeForm, {
    id: null,
    name: '',
    prizeType: 'POINTS',
    pointsValue: 0,
    weight: 1,
    stock: -1,
    imageUrl: '',
    sortOrder: 0,
    status: 1
  })
}

async function load() {
  loading.value = true
  try {
    const data = await listActivities({ page: 1, size: 100 })
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
  if (!form.name) {
    ElMessage.warning('请填写活动名称')
    return
  }
  saving.value = true
  try {
    const payload = { ...form }
    delete payload.id
    if (form.id) {
      await updateActivity(form.id, payload)
    } else {
      await createActivity(payload)
    }
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确认删除活动「${row.name}」？`, '提示', { type: 'warning' })
  await deleteActivity(row.id)
  ElMessage.success('已删除')
  load()
}

async function openPrizes(row) {
  currentActivity.value = row
  prizeVisible.value = true
  await loadPrizes()
}

async function loadPrizes() {
  if (!currentActivity.value) return
  prizeLoading.value = true
  try {
    const data = await listPrizes(currentActivity.value.id)
    prizes.value = data?.records || data?.list || data || []
  } finally {
    prizeLoading.value = false
  }
}

function openPrizeCreate() {
  resetPrizeForm()
  prizeFormVisible.value = true
}

function openPrizeEdit(row) {
  Object.assign(prizeForm, { ...row })
  prizeFormVisible.value = true
}

async function onPrizeSave() {
  if (!prizeForm.name) {
    ElMessage.warning('请填写奖品名称')
    return
  }
  prizeSaving.value = true
  try {
    const payload = { ...prizeForm }
    delete payload.id
    const aid = currentActivity.value.id
    if (prizeForm.id) {
      await updatePrize(aid, prizeForm.id, payload)
    } else {
      await createPrize(aid, payload)
    }
    ElMessage.success('保存成功')
    prizeFormVisible.value = false
    loadPrizes()
  } finally {
    prizeSaving.value = false
  }
}

async function onPrizeDelete(row) {
  await ElMessageBox.confirm(`确认删除奖品「${row.name}」？`, '提示', { type: 'warning' })
  await deletePrize(currentActivity.value.id, row.id)
  ElMessage.success('已删除')
  loadPrizes()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
.tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
</style>
