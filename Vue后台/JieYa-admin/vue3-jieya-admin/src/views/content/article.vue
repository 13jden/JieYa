<template>
  <div class="node-post-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-input
              v-model="searchForm.keyword"
              placeholder="请输入标题关键词"
              style="width: 200px"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-select
              v-model="searchForm.categoryId"
              placeholder="分类"
              style="width: 120px; margin-left: 10px"
              clearable
              @change="handleSearch"
            >
              <el-option 
                v-for="item in categoryOptions" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id" 
              />
            </el-select>
            <el-select
              v-model="searchForm.status"
              placeholder="审核状态"
              style="width: 120px; margin-left: 10px"
              clearable
              @change="handleSearch"
            >
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
            <el-button type="primary" @click="handleSearch" style="margin-left: 10px">
              搜索
            </el-button>
            <el-button @click="resetSearch" style="margin-left: 10px">重置</el-button>
          </div>
        </div>
      </template>

      <!-- 笔记列表 -->
      <el-table :data="postList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image 
              v-if="row.coverImage" 
              :src="row.coverImage" 
              style="width: 80px; height:auto; object-fit: cover;"
              :preview-src-list="[row.coverImage]"
              preview-teleported
            ></el-image>
            <div v-else class="no-image">无图片</div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userId" label="作者ID" width="120" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.category) }}
          </template>
        </el-table-column>
        <el-table-column prop="postTime" label="提交时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              @click="handleAudit(row)"
            >
              审核
            </el-button>
            <el-button
              type="warning"
              link
              @click="handleCategory(row)"
            >
              修改分类
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
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

    <!-- 查看笔记对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="笔记详情"
      width="800px"
    >
      <div class="post-detail">
        <!-- 顶部轮播图 -->
        <div class="post-images" v-if="currentPost.images && currentPost.images.length > 0">
          <el-carousel height="400px" indicator-position="outside" arrow="always">
            <el-carousel-item v-for="(img, index) in currentPost.images" :key="index">
              <el-image 
                :src="img.imagePath" 
                fit="contain"
                style="height: 100%; width: 100%"
                preview-teleported
              ></el-image>
            </el-carousel-item>
          </el-carousel>
        </div>
        
        <!-- 封面图 -->
        <div class="cover-image" v-else-if="currentPost.coverImage">
          <el-image 
            :src="currentPost.coverImage" 
            fit="contain"
            style="width: 100%; aspect-ratio: 0.8/1;"
            preview-teleported
          ></el-image>
        </div>
        
        <h2>{{ currentPost.title }}</h2>
        <div class="post-meta">
          <span>作者ID: {{ currentPost.userId }}</span>
          <span>分类: {{ getCategoryName(currentPost.category) }}</span>
          <span>状态: {{ getStatusText(currentPost.status) }}</span>
        </div>
        
        <div class="post-content" v-html="currentPost.content"></div>
        
        <!-- 标签展示 -->
        <div v-if="currentPost.tags && currentPost.tags.length > 0" class="post-tags">
          <el-tag v-for="(tag, index) in parsedTags" :key="index" size="small" class="tag-item">
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="笔记审核"
      width="500px"
    >
      <el-form
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditRules"
        label-width="80px"
      >
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="拒绝原因"
          prop="reason"
          v-if="auditForm.status === 2"
        >
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAuditSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改分类对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      title="修改分类"
      width="400px"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryRules"
        label-width="80px"
      >
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="categoryForm.categoryId" placeholder="请选择分类">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="categoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCategorySubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getPostList, 
  getPendingList, 
  getPostDetail, 
  approvePost, 
  rejectPost, 
  deletePost, 
  updateCategory 
} from '@/api/nodePost'
import { getCategoryList } from '@/api/category'

// 搜索表单
const searchForm = ref({
  keyword: '',
  categoryId: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const postList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 分类选项
const categoryOptions = ref([])

// 查看笔记对话框
const viewDialogVisible = ref(false)
const currentPost = ref({})

// 审核对话框数据
const auditDialogVisible = ref(false)
const auditFormRef = ref(null)
const auditForm = ref({
  postId: '',
  status: 1,
  reason: ''
})

// 分类对话框数据
const categoryDialogVisible = ref(false)
const categoryFormRef = ref(null)
const categoryForm = ref({
  postId: '',
  categoryId: ''
})

// 表单验证规则
const auditRules = {
  status: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' }
  ]
}

const categoryRules = {
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ]
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return types[status]
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return texts[status]
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '未分类'
  const category = categoryOptions.value.find(item => item.id === categoryId)
  return category ? category.name : `分类(${categoryId})`
}

// 解析标签
const parsedTags = computed(() => {
  if (!currentPost.value.tags) return [];
  
  try {
    if (typeof currentPost.value.tags === 'string') {
      // 尝试多层解析，处理嵌套JSON字符串情况
      let parsedData = JSON.parse(currentPost.value.tags);
      
      // 如果解析出来的还是字符串，继续解析
      if (typeof parsedData === 'string') {
        parsedData = JSON.parse(parsedData);
      }
      
      // 扁平化处理，确保返回字符串数组
      if (Array.isArray(parsedData)) {
        return parsedData.flat().filter(tag => typeof tag === 'string');
      }
      return [];
    } else if (Array.isArray(currentPost.value.tags)) {
      return currentPost.value.tags.flat().filter(tag => typeof tag === 'string');
    }
    return [];
  } catch (e) {
    console.error('解析标签失败:', e);
    return [];
  }
});

// 初始化加载数据
onMounted(() => {
  fetchCategories()
  fetchPostList()
})

// 获取笔记列表
const fetchPostList = async () => {
  loading.value = true
  try {
    const { keyword, categoryId, status } = searchForm.value
    const res = await getPostList(currentPage.value, pageSize.value, categoryId, keyword, status)
    if (res.code === 1) {
      postList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取笔记列表失败')
    }
  } catch (error) {
    console.error('获取笔记列表失败:', error)
    ElMessage.error('获取笔记列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPostList()
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchPostList()
}

// 当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchPostList()
}

// 查看笔记
const handleView = async (row) => {
  try {
    const res = await getPostDetail(row.id)
    if (res.code === 1) {
      currentPost.value = res.data
      
      // 确保images是数组
      if (!Array.isArray(currentPost.value.images)) {
        if (typeof currentPost.value.images === 'string') {
          try {
            currentPost.value.images = JSON.parse(currentPost.value.images)
          } catch (e) {
            currentPost.value.images = []
          }
        } else {
          currentPost.value.images = []
        }
      }
      
      // 确保tags正确处理
      if (typeof currentPost.value.tags === 'string') {
        try {
          currentPost.value.tags = JSON.parse(currentPost.value.tags)
        } catch (e) {
          currentPost.value.tags = []
        }
      }
      
      viewDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取笔记详情失败')
    }
  } catch (error) {
    console.error('获取笔记详情失败:', error)
    ElMessage.error('获取笔记详情失败')
  }
}

// 审核笔记
const handleAudit = (row) => {
  auditForm.value = {
    postId: row.id,
    status: 1,
    reason: ''
  }
  auditDialogVisible.value = true
}

// 提交审核
const handleAuditSubmit = async () => {
  if (!auditForm.value.postId) {
    ElMessage.error('笔记ID不能为空')
    return
  }
  
  try {
    await auditFormRef.value.validate()
    
    let res
    if (auditForm.value.status === 1) {
      // 通过
      res = await approvePost(auditForm.value.postId)
    } else {
      // 拒绝
      res = await rejectPost(auditForm.value.postId, auditForm.value.reason)
    }
    
    if (res.code === 1) {
      ElMessage.success(res.message || '审核成功')
      auditDialogVisible.value = false
      fetchPostList()
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 修改分类
const handleCategory = (row) => {
  categoryForm.value = {
    postId: row.id,
    categoryId: row.categoryId
  }
  categoryDialogVisible.value = true
}

// 提交分类修改
const handleCategorySubmit = async () => {
  if (!categoryForm.value.postId) {
    ElMessage.error('笔记ID不能为空')
    return
  }
  
  try {
    await categoryFormRef.value.validate()
    
    const res = await updateCategory(
      categoryForm.value.postId, 
      categoryForm.value.categoryId
    )
    
    if (res.code === 1) {
      ElMessage.success(res.message || '修改分类成功')
      categoryDialogVisible.value = false
      fetchPostList()
    } else {
      ElMessage.error(res.message || '修改分类失败')
    }
  } catch (error) {
    console.error('修改分类失败:', error)
    ElMessage.error('修改分类失败')
  }
}

// 删除笔记
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认删除该笔记吗？此操作不可恢复',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await deletePost(row.id)
      if (res.code === 1) {
        ElMessage.success(res.message || '删除成功')
        fetchPostList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 1) {
      categoryOptions.value = res.data.map(item => ({
        id: item.categoryId,
        code: item.categoryCode,
        name: item.categoryName,
        sort: item.sort
      }))
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    keyword: '',
    categoryId: '',
    status: ''
  }
  currentPage.value = 1
  fetchPostList()
}
</script>

<style lang="scss" scoped>
.node-post-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .post-detail {
    padding: 15px;
    
    h2 {
      margin-top: 15px;
      margin-bottom: 10px;
      font-weight: bold;
      font-size: 18px;
    }
    
    .post-meta {
      margin-bottom: 15px;
      color: #666;
      font-size: 14px;
      
      span {
        margin-right: 15px;
      }
    }
    
    .cover-image {
      margin-bottom: 15px;
      border-radius: 8px;
      overflow: hidden;
      text-align: center;
      background-color: #f5f7fa;
    }
    
    .post-content {
      line-height: 1.6;
      margin-bottom: 15px;
      font-size: 15px;
      color: #333;
    }
    
    .post-images {
      margin-bottom: 15px;
      border-radius: 8px;
      overflow: hidden;
      background-color: #f5f7fa;
      
      .el-carousel {
        border-radius: 8px;
      }
    }
    
    .post-tags {
      margin-top: 15px;
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .tag-item {
        border-radius: 15px;
        padding: 0 10px;
      }
    }
  }

  :deep(.el-image-viewer__wrapper) {
    z-index: 3000 !important;
  }

  :deep(.el-table .el-table__fixed-right) {
    z-index: 1;
  }

  .el-table-column[label="封面"] .el-image {
    width: 80px;
    aspect-ratio: 0.8/1;
    object-fit: cover;
  }

  .no-image {
    width: 80px;
    height: 100px; /* 保持0.8:1的比例 */
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    color: #909399;
    font-size: 12px;
    border-radius: 4px;
  }
}

@media screen and (max-width: 768px) {
  .card-header .left {
    flex-direction: column;
    align-items: stretch;
    
    .el-input, .el-select {
      margin: 5px 0;
      width: 100% !important;
    }
  }

  .post-detail {
    padding: 10px;
    
    .post-images {
      .el-carousel {
        height: 300px !important;
      }
    }
  }
}
</style>