<template>
  <div class="article-container">
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
          </div>
          <el-button type="success" @click="handleAdd">
            新增稿件
          </el-button>
        </div>
      </template>

      <!-- 稿件列表 -->
      <el-table :data="articleList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="createTime" label="提交时间" width="180" />
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
              v-if="row.status !== 0"
              type="warning"
              link
              @click="handleEdit(row)"
            >
              编辑
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="getDialogTitle"
      width="800px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图" prop="coverUrl">
          <el-upload
            class="cover-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="form.coverUrl" :src="form.coverUrl" class="cover-image" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <!-- 这里可以集成富文本编辑器，如 TinyMCE、Wangeditor 等 -->
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="请输入内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="稿件审核"
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
          label="审核意见"
          prop="remark"
          v-if="auditForm.status === 2"
        >
          <el-input
            v-model="auditForm.remark"
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 搜索表单
const searchForm = ref({
  keyword: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const articleList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 分类选项
const categoryOptions = ref([
  { id: 1, name: '新闻资讯' },
  { id: 2, name: '活动公告' }
])

// 对话框数据
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const form = ref({
  title: '',
  categoryId: '',
  coverUrl: '',
  content: ''
})

// 审核对话框数据
const auditDialogVisible = ref(false)
const auditFormRef = ref(null)
const auditForm = ref({
  id: null,
  status: 1,
  remark: ''
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ]
}

// 审核表单验证规则
const auditRules = {
  status: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' }
  ]
}

// 计算属性
const getDialogTitle = computed(() => {
  const titles = {
    add: '新增稿件',
    edit: '编辑稿件',
    view: '查看稿件'
  }
  return titles[dialogType.value]
})

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

// 获取稿件列表
const getList = async () => {
  loading.value = true
  try {
    // TODO: 调用接口获取数据
    // 模拟数据
    articleList.value = [
      {
        id: 1,
        title: '示例稿件1',
        author: '张三',
        categoryId: 1,
        categoryName: '新闻资讯',
        createTime: '2024-03-16 12:00:00',
        status: 0
      },
      {
        id: 2,
        title: '示例稿件2',
        author: '李四',
        categoryId: 2,
        categoryName: '活动公告',
        createTime: '2024-03-16 13:00:00',
        status: 1
      }
    ]
    total.value = 2
  } catch (error) {
    ElMessage.error('获取稿件列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getList()
}

// 新增稿件
const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    title: '',
    categoryId: '',
    coverUrl: '',
    content: ''
  }
  dialogVisible.value = true
}

// 查看稿件
const handleView = (row) => {
  dialogType.value = 'view'
  form.value = { ...row }
  dialogVisible.value = true
}

// 编辑稿件
const handleEdit = (row) => {
  dialogType.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

// 审核稿件
const handleAudit = (row) => {
  auditForm.value = {
    id: row.id,
    status: 1,
    remark: ''
  }
  auditDialogVisible.value = true
}

// 删除稿件
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认删除该稿件吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // TODO: 调用删除接口
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 上传相关
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传文件只能是图片格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  // TODO: 处理上传成功后的响应
  form.value.coverUrl = response.url
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用新增/编辑接口
        ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功')
        dialogVisible.value = false
        getList()
      } catch (error) {
        ElMessage.error(dialogType.value === 'add' ? '新增失败' : '编辑失败')
      }
    }
  })
}

// 提交审核
const handleAuditSubmit = async () => {
  if (!auditFormRef.value) return
  await auditFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用审核接口
        ElMessage.success('审核提交成功')
        auditDialogVisible.value = false
        getList()
      } catch (error) {
        ElMessage.error('审核提交失败')
      }
    }
  })
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

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.article-container {
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

  .cover-uploader {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);

    &:hover {
      border-color: var(--el-color-primary);
    }

    .cover-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 200px;
      height: 120px;
      text-align: center;
      line-height: 120px;
    }

    .cover-image {
      width: 200px;
      height: 120px;
      display: block;
      object-fit: cover;
    }
  }
}
</style> 