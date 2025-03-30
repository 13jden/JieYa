<template>
  <div class="booking-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-form :inline="true" :model="searchForm" class="search-form">
              <el-form-item>
                <el-input v-model="searchForm.userId" placeholder="用户ID" clearable />
              </el-form-item>
              <el-form-item>
                <el-select v-model="searchForm.venueId" placeholder="场地" clearable>
                  <el-option 
                    v-for="venue in venueOptions" 
                    :key="venue.id" 
                    :label="venue.venueName" 
                    :value="venue.id" 
                  />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-select v-model="searchForm.status" placeholder="预约状态" clearable>
                  <el-option label="待确认" value="PENDING" />
                  <el-option label="已确认" value="CONFIRMED" />
                  <el-option label="已完成" value="COMPLETED" />
                  <el-option label="已取消" value="CANCELLED" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-date-picker
                  v-model="searchForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="resetSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="right">
            <el-button type="danger" :disabled="selectedBookings.length === 0" @click="handleBatchDelete">
              批量删除
            </el-button>
          </div>
        </div>
      </template>

      <!-- 订单列表 -->
      <el-table 
        :data="bookingList" 
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="订单号" width="120" show-overflow-tooltip />
        <el-table-column label="场地图片" width="100">
          <template #default="{ row }">
            <el-image
              style="width: 60px; height: 60px"
              :src="row.venueCoverImage"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="venueName" label="场地名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="venueLocation" label="场地位置" min-width="140" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="120" show-overflow-tooltip />
        <el-table-column prop="startTimeStr" label="开始时间" width="140" />
        <el-table-column prop="endTimeStr" label="结束时间" width="140" />
        <el-table-column prop="totalPrice" label="订单金额" width="100">
          <template #default="{ row }">
            {{ row.totalPrice ? `¥${row.totalPrice}` : '免费' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAtStr" label="创建时间" width="140" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              link type="success" 
              @click="handleUpdateStatus(row, 'CONFIRMED')"
            >
              确认
            </el-button>
            <el-button
              v-if="row.status === 'CONFIRMED'"
              link type="warning"
              @click="handleUpdateStatus(row, 'COMPLETED')"
            >
              完成
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              link type="danger"
              @click="handleUpdateStatus(row, 'CANCELLED')"
            >
              取消
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="预约详情"
        width="600px"
      >
        <div class="booking-detail" v-if="detailInfo">
          <div class="detail-item">
            <div class="label">订单号：</div>
            <div class="value">{{ detailInfo.id }}</div>
          </div>
          <div class="detail-item">
            <div class="label">场地图片：</div>
            <div class="value">
              <el-image
                style="width: 100px; height: 100px"
                :src="detailInfo.venueCoverImage"
                fit="cover"
              ></el-image>
            </div>
          </div>
          <div class="detail-item">
            <div class="label">场地名称：</div>
            <div class="value">{{ detailInfo.venueName }}</div>
          </div>
          <div class="detail-item">
            <div class="label">场地位置：</div>
            <div class="value">{{ detailInfo.venueLocation }}</div>
          </div>
          <div class="detail-item">
            <div class="label">场地简介：</div>
            <div class="value">{{ detailInfo.venueContent }}</div>
          </div>
          <div class="detail-item">
            <div class="label">用户ID：</div>
            <div class="value">{{ detailInfo.userId }}</div>
          </div>
          <div class="detail-item">
            <div class="label">开始时间：</div>
            <div class="value">{{ detailInfo.startTimeStr }}</div>
          </div>
          <div class="detail-item">
            <div class="label">结束时间：</div>
            <div class="value">{{ detailInfo.endTimeStr }}</div>
          </div>
          <div class="detail-item">
            <div class="label">订单金额：</div>
            <div class="value">{{ detailInfo.totalPrice ? `¥${detailInfo.totalPrice}` : '免费' }}</div>
          </div>
          <div class="detail-item">
            <div class="label">订单状态：</div>
            <div class="value">
              <el-tag :type="getStatusType(detailInfo.status)">
                {{ getStatusText(detailInfo.status) }}
              </el-tag>
            </div>
          </div>
          <div class="detail-item">
            <div class="label">创建时间：</div>
            <div class="value">{{ detailInfo.createdAtStr }}</div>
          </div>
          <div class="detail-item">
            <div class="label">更新时间：</div>
            <div class="value">{{ detailInfo.updatedAt }}</div>
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="detailDialogVisible = false">关闭</el-button>
            <el-button 
              v-if="detailInfo && detailInfo.status === 'PENDING'" 
              type="success" 
              @click="handleUpdateStatus(detailInfo, 'CONFIRMED')"
            >
              确认
            </el-button>
            <el-button
              v-if="detailInfo && detailInfo.status === 'CONFIRMED'"
              type="warning"
              @click="handleUpdateStatus(detailInfo, 'COMPLETED')"
            >
              完成
            </el-button>
            <el-button
              v-if="detailInfo && detailInfo.status === 'PENDING'"
              type="danger"
              @click="handleUpdateStatus(detailInfo, 'CANCELLED')"
            >
              取消
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVenueList } from '@/api/venue'
import { getVenueBookingPage, updateVenueBookingStatus, deleteVenueBooking, batchDeleteVenueBookings } from '@/api/venue-booking'

const loading = ref(false)
const bookingList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const venueOptions = ref([])
const selectedBookings = ref([])
const detailDialogVisible = ref(false)
const detailInfo = ref(null)

const searchForm = ref({
  userId: '',
  venueId: '',
  status: '',
  dateRange: []
})

// 获取场地列表用于筛选
const getVenues = async () => {
  try {
    const res = await getVenueList()
    if (res.code === 1) {
      venueOptions.value = res.data || []
    }
  } catch (error) {
    console.error('获取场地列表错误:', error)
  }
}

// 获取预约列表
const getList = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      userId: searchForm.value.userId || undefined,
      venueId: searchForm.value.venueId || undefined,
      status: searchForm.value.status || undefined
    }
    
    // 处理日期范围
    if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
      params.startDate = searchForm.value.dateRange[0]
      params.endDate = searchForm.value.dateRange[1]
    }
    
    const res = await getVenueBookingPage(params)
    if (res.code === 1) {
      bookingList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取预约列表失败')
    }
  } catch (error) {
    console.error('获取预约列表出错:', error)
    ElMessage.error('获取预约列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleViewDetail = (row) => {
  detailInfo.value = row
  detailDialogVisible.value = true
}

// 更新状态
const handleUpdateStatus = async (row, status) => {
  const statusTextMap = {
    'CONFIRMED': '确认',
    'COMPLETED': '完成',
    'CANCELLED': '取消'
  }
  
  try {
    const res = await updateVenueBookingStatus(row.id, status)
    if (res.code === 1) {
      ElMessage.success(`${statusTextMap[status]}预约成功`)
      getList() // 刷新列表
      if (detailDialogVisible.value) {
        detailDialogVisible.value = false
      }
    } else {
      ElMessage.error(res.message || `${statusTextMap[status]}预约失败`)
    }
  } catch (error) {
    console.error(`${statusTextMap[status]}预约出错:`, error)
    ElMessage.error(`${statusTextMap[status]}预约失败`)
  }
}

// 删除预约
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除订单号为"${row.id}"的预约记录吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteVenueBooking(row.id)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        getList() // 刷新列表
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除预约出错:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消删除操作
  })
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedBookings.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  const ids = selectedBookings.value.map(item => item.id)
  ElMessageBox.confirm(
    `确定要删除所选的${ids.length}条预约记录吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await batchDeleteVenueBookings(ids)
      if (res.code === 1) {
        ElMessage.success('批量删除成功')
        getList() // 刷新列表
      } else {
        ElMessage.error(res.message || '批量删除失败')
      }
    } catch (error) {
      console.error('批量删除预约记录出错:', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {
    // 用户取消删除操作
  })
}

// 多选变化
const handleSelectionChange = (selection) => {
  selectedBookings.value = selection
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value.userId = ''
  searchForm.value.venueId = ''
  searchForm.value.status = ''
  searchForm.value.dateRange = []
  handleSearch()
}

// 分页相关
const handleSizeChange = (val) => {
  pageSize.value = val
  getList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  getList()
}

// 状态显示
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'CONFIRMED': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  getVenues()
  getList()
})
</script>

<style lang="scss" scoped>
.booking-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;

    .left {
      .search-form {
        display: flex;
        flex-wrap: wrap;
      }
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .booking-detail {
    padding: 10px;

    .detail-item {
      margin-bottom: 12px;
      line-height: 24px;
      font-size: 14px;
      display: flex;

      .label {
        width: 120px;
        font-weight: bold;
        color: #606266;
      }

      .value {
        flex: 1;
        color: #333;
      }
    }
  }
}
</style> 