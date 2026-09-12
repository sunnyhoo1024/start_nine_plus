<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="userId" placeholder="用户 ID" style="width: 200px" clearable />
      <el-button type="primary" @click="search">查询</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="changeAmount" label="变动" width="100" />
      <el-table-column prop="balanceAfter" label="余额" width="100" />
      <el-table-column prop="bizType" label="业务类型" width="140" />
      <el-table-column prop="bizId" label="业务ID" width="120" />
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column prop="operatorId" label="操作人" width="90" />
      <el-table-column prop="createdAt" label="时间" width="170" />
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
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listLedger } from '@/api/points'

const userId = ref('')
const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)

function search() {
  page.value = 1
  load()
}

async function load() {
  if (!userId.value) {
    ElMessage.warning('请输入用户 ID')
    return
  }
  loading.value = true
  try {
    const data = await listLedger({ userId: userId.value, page: page.value, size: size.value })
    list.value = data?.records || data?.list || data || []
    total.value = data?.total ?? list.value.length
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
