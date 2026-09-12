<template>
  <el-card>
    <div class="toolbar">
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="用户ID" width="90" />
      <el-table-column prop="openid" label="OpenID" min-width="160" show-overflow-tooltip />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="avatarUrl" label="头像" min-width="160" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">{{ row.status === 1 ? '正常' : '禁用' }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openAdjust(row)">调积分</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        v-model:current-page="page"
        :page-size="size"
        @current-change="load"
      />
    </div>

    <el-dialog v-model="visible" title="调整积分" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户ID">
          <el-input :model-value="form.userId" disabled />
        </el-form-item>
        <el-form-item label="变动值" required>
          <el-input-number v-model="form.delta" style="width: 100%" />
          <div class="tip">正数增加，负数扣减</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onAdjust">确认</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listUsers } from '@/api/user'
import { adjustPoints } from '@/api/points'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const form = reactive({ userId: null, delta: 0, remark: '' })

async function load() {
  loading.value = true
  try {
    const data = await listUsers({ page: page.value, size: size.value })
    list.value = data?.records || data?.list || data || []
    total.value = data?.total ?? list.value.length
  } finally {
    loading.value = false
  }
}

function openAdjust(row) {
  form.userId = row.id
  form.delta = 0
  form.remark = ''
  visible.value = true
}

async function onAdjust() {
  if (!form.delta) {
    ElMessage.warning('请填写非零变动值')
    return
  }
  saving.value = true
  try {
    await adjustPoints({ userId: form.userId, delta: form.delta, remark: form.remark })
    ElMessage.success('调账成功')
    visible.value = false
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
  margin-top: 4px;
}
</style>
