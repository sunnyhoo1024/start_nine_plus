<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="status" clearable placeholder="订单状态" style="width: 160px">
        <el-option value="CREATED" label="待履约" />
        <el-option value="FULFILLED" label="已履约" />
        <el-option value="CANCELLED" label="已取消" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderNo" label="订单号" min-width="160" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="skuName" label="商品" min-width="120" />
      <el-table-column prop="pointsCost" label="积分" width="90" />
      <el-table-column prop="quantity" label="数量" width="70" />
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="success"
            :disabled="row.status !== 'CREATED'"
            @click="onFulfill(row)"
          >履约</el-button>
          <el-button
            link
            type="danger"
            :disabled="row.status !== 'CREATED'"
            @click="onCancel(row)"
          >取消</el-button>
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
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOrders, fulfillOrder, cancelOrder } from '@/api/redeem'

const list = ref([])
const loading = ref(false)
const status = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)

function search() {
  page.value = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (status.value) params.status = status.value
    const data = await listOrders(params)
    list.value = data?.records || data?.list || data || []
    total.value = data?.total ?? list.value.length
  } finally {
    loading.value = false
  }
}

async function onFulfill(row) {
  await ElMessageBox.confirm(`确认履约订单 ${row.orderNo}？`, '提示')
  await fulfillOrder(row.id)
  ElMessage.success('已履约')
  load()
}

async function onCancel(row) {
  await ElMessageBox.confirm(`确认取消订单 ${row.orderNo}？取消将退还积分。`, '提示', { type: 'warning' })
  await cancelOrder(row.id)
  ElMessage.success('已取消')
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
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
