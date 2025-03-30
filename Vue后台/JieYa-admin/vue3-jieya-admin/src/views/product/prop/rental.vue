<template>
    <div class="rental-container">
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <div class="left">
              <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item>
                  <el-input v-model="searchForm.userId" placeholder="用户ID" clearable />
                </el-form-item>
                <el-form-item>
                  <el-select v-model="searchForm.propId" placeholder="道具" clearable>
                    <el-option v-for="prop in propOptions" :key="prop.id" :label="prop.propName" :value="prop.id" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-select v-model="searchForm.status" placeholder="租借状态" clearable>
                    <el-option label="待确认" value="PENDING" />
                    <el-option label="已确认" value="CONFIRMED" />
                    <el-option label="已归还" value="RETURNED" />
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
              <el-button type="danger" :disabled="selectedRentals.length === 0" @click="handleBatchDelete">
                批量删除
              </el-button>
            </div>
          </div>
        </template>
  
        <!-- 订单列表 -->
        <el-table 
          :data="rentalList" 
          style="width: 100%" 
          v-loading="loading"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="订单号" width="120" show-overflow-tooltip />
          <el-table-column prop="propName" label="道具名称" min-width="120" show-overflow-tooltip />
          <el-table-column label="道具图片" width="100">
            <template #default="{ row }">
              <el-image
                style="width: 60px; height: 60px"
                :src="row.propCoverImage"
                fit="cover"
              />
            </template>
          </el-table-column>
          <el-table-column prop="userId" label="用户ID" width="120" show-overflow-tooltip />
          <el-table-column label="租借时间" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.startDate) }}
            </template>
          </el-table-column>
          <el-table-column label="归还日期" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.endDate) }}
            </template>
          </el-table-column>
          <el-table-column label="租借天数" width="90">
            <template #default="{ row }">
              {{ calculateDuration(row) }}天
            </template>
          </el-table-column>
          <el-table-column label="订单金额" width="120">
            <template #default="{ row }">
              {{ row.totalPrice ? `¥${row.totalPrice}` : '免费' }}
            </template>
          </el-table-column>
          <el-table-column label="订单状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button 
                size="small" 
                type="primary" 
                @click="handleViewDetail(row)"
              >
                详情
              </el-button>
              <el-button 
                v-if="row.status === 'PENDING'" 
                size="small" 
                type="success" 
                @click="handleUpdateStatus(row, 'CONFIRMED')"
              >
                确认
              </el-button>
              <el-button
                v-if="row.status === 'CONFIRMED'"
                size="small"
                type="warning"
                @click="handleUpdateStatus(row, 'RETURNED')"
              >
                归还
              </el-button>
              <el-button
                v-if="row.status === 'PENDING'"
                size="small"
                type="info"
                @click="handleUpdateStatus(row, 'CANCELLED')"
              >
                取消
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="handleDelete(row)"
              >
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
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
  
      <!-- 详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="租借详情"
        width="600px"
      >
        <div class="rental-detail" v-if="detailInfo">
          <div class="detail-item">
            <div class="label">订单号：</div>
            <div class="value">{{ detailInfo.id }}</div>
          </div>
          <div class="detail-item">
            <div class="label">道具图片：</div>
            <div class="value">
              <el-image
                style="width: 100px; height: 100px"
                :src="detailInfo.propCoverImage"
                fit="cover"
              ></el-image>
            </div>
          </div>
          <div class="detail-item">
            <div class="label">道具名称：</div>
            <div class="value">{{ detailInfo.propName }}</div>
          </div>
          <div class="detail-item">
            <div class="label">道具描述：</div>
            <div class="value">{{ detailInfo.propDescription }}</div>
          </div>
          <div class="detail-item">
            <div class="label">道具单价：</div>
            <div class="value">{{ detailInfo.propPrice ? `¥${detailInfo.propPrice}/天` : '免费' }}</div>
          </div>
          <div class="detail-item">
            <div class="label">用户ID：</div>
            <div class="value">{{ detailInfo.userId }}</div>
          </div>
          <div class="detail-item">
            <div class="label">用户名：</div>
            <div class="value">{{ detailInfo.userName || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="label">租借日期：</div>
            <div class="value">{{ formatDateTime(detailInfo.startDate) }}</div>
          </div>
          <div class="detail-item">
            <div class="label">归还日期：</div>
            <div class="value">{{ formatDateTime(detailInfo.endDate) }}</div>
          </div>
          <div class="detail-item">
            <div class="label">租借天数：</div>
            <div class="value">{{ calculateDuration(detailInfo) }}天</div>
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
            <div class="value">{{ formatDateTime(detailInfo.createdAt) }}</div>
          </div>
          <div class="detail-item">
            <div class="label">更新时间：</div>
            <div class="value">{{ formatDateTime(detailInfo.updatedAt) }}</div>
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
              @click="handleUpdateStatus(detailInfo, 'RETURNED')"
            >
              归还
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
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { getPropList } from '@/api/prop'
  import { getPropRentalPage, updatePropRentalStatus, deletePropRental, batchDeletePropRentals} from '@/api/prop-rental'
  const loading = ref(false)
  const rentalList = ref([])
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const propOptions = ref([])
  const selectedRentals = ref([])
  const detailDialogVisible = ref(false)
  const detailInfo = ref(null)
  
  const searchForm = ref({
    userId: '',
    propId: '',
    status: '',
    dateRange: []
  })
  
  // 获取道具列表用于筛选
  const getProps = async () => {
    try {
      const res = await getPropList()
      if (res.code === 1) {
        propOptions.value = res.data || []
      }
    } catch (error) {
      console.error('获取道具列表错误:', error)
    }
  }
  
  // 获取租借列表
  const getList = async () => {
    loading.value = true
    try {
      const params = {
        current: currentPage.value,
        size: pageSize.value,
        userId: searchForm.value.userId || undefined,
        propId: searchForm.value.propId || undefined,
        status: searchForm.value.status || undefined
      }
      
      // 处理日期范围
      if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
        params.startDate = searchForm.value.dateRange[0]
        params.endDate = searchForm.value.dateRange[1]
      }
      
      const res = await getPropRentalPage(params)
      if (res.code === 1) {
        rentalList.value = res.data.records || []
        total.value = res.data.total || 0
      } else {
        ElMessage.error(res.message || '获取租借列表失败')
      }
    } catch (error) {
      console.error('获取租借列表出错:', error)
      ElMessage.error('获取租借列表失败')
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
      'RETURNED': '归还',
      'CANCELLED': '取消'
    }
    
    try {
      const res = await updatePropRentalStatus(row.id, status)
      if (res.code === 1) {
        ElMessage.success(`${statusTextMap[status]}成功`)
        if (detailDialogVisible.value) {
          detailDialogVisible.value = false
        }
        getList() // 刷新列表
      } else {
        ElMessage.error(res.message || `${statusTextMap[status]}失败`)
      }
    } catch (error) {
      console.error(`${statusTextMap[status]}租借状态出错:`, error)
      ElMessage.error(`${statusTextMap[status]}失败`)
    }
  }
  
  // 删除
  const handleDelete = (row) => {
    ElMessageBox.confirm(
      `确定要删除租借记录"${row.id}"吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      try {
        const res = await deletePropRental(row.id)
        if (res.code === 1) {
          ElMessage.success('删除成功')
          getList() // 刷新列表
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (error) {
        console.error('删除租借出错:', error)
        ElMessage.error('删除失败')
      }
    }).catch(() => {
      // 用户取消删除操作
    })
  }
  
  // 批量删除
  const handleBatchDelete = () => {
    if (selectedRentals.value.length === 0) {
      ElMessage.warning('请至少选择一条记录')
      return
    }
    
    const ids = selectedRentals.value.map(item => item.id)
    ElMessageBox.confirm(
      `确定要删除所选的${ids.length}条租借记录吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      try {
        const res = await batchDeletePropRentals(ids)
        if (res.code === 1) {
          ElMessage.success('批量删除成功')
          getList() // 刷新列表
        } else {
          ElMessage.error(res.message || '批量删除失败')
        }
      } catch (error) {
        console.error('批量删除租借记录出错:', error)
        ElMessage.error('批量删除失败')
      }
    }).catch(() => {
      // 用户取消删除操作
    })
  }
  
  // 多选变化
  const handleSelectionChange = (selection) => {
    selectedRentals.value = selection
  }
  
  // 搜索
  const handleSearch = () => {
    currentPage.value = 1
    getList()
  }
  
  // 重置搜索
  const resetSearch = () => {
    searchForm.value.userId = ''
    searchForm.value.propId = ''
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
      'RETURNED': '已归还',
      'CANCELLED': '已取消'
    }
    return statusMap[status] || status
  }
  
  const getStatusType = (status) => {
    const typeMap = {
      'PENDING': 'warning',
      'CONFIRMED': 'primary',
      'RETURNED': 'success',
      'CANCELLED': 'info'
    }
    return typeMap[status] || 'info'
  }
  
  // 格式化日期时间
  const formatDateTime = (dateStr) => {
    if (!dateStr) return '-'
    const date = new Date(dateStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}`
  }
  
  // 计算租借天数
  const calculateDuration = (row) => {
    // 如果已有duration字段，直接返回
    if (row.duration) return row.duration
    
    // 如果没有，则根据价格计算
    if (row.totalPrice && row.propPrice && row.propPrice > 0) {
      return Math.ceil(row.totalPrice / row.propPrice)
    }
    
    // 如果无法通过价格计算，则尝试通过日期计算
    if (row.startDate && row.endDate) {
      const start = new Date(row.startDate)
      const end = new Date(row.endDate)
      const diffTime = Math.abs(end - start)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      return diffDays || 1 // 至少1天
    }
    
    return 1 // 默认值
  }
  
  onMounted(() => {
    getProps()
    getList()
  })
  </script>
  
  <style lang="scss" scoped>
  .rental-container {
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
  
    .rental-detail {
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