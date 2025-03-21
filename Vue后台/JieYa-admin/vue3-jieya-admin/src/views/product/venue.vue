<template>
  <div class="venue-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-input
              v-model="searchForm.keyword"
              placeholder="请输入场馆名称"
              style="width: 200px"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-select
              v-model="searchForm.status"
              placeholder="场馆状态"
              style="width: 120px; margin-left: 10px"
              clearable
              @change="handleSearch"
            >
              <el-option label="营业中" :value="1" />
              <el-option label="已关闭" :value="0" />
            </el-select>
            <el-button type="primary" @click="handleSearch" style="margin-left: 10px">
              搜索
            </el-button>
          </div>
          <el-button type="success" @click="handleAdd">
            新增场馆
          </el-button>
        </div>
      </template>

      <!-- 场馆列表 -->
      <el-table :data="venueList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="场馆图片" width="120">
          <template #default="{ row }">
            <el-image
              style="width: 80px; height: 80px"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="场馆名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="openingHours" label="营业时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'info'">
              {{ row.status ? '营业中' : '已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              :type="row.status ? 'warning' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status ? '关闭' : '开启' }}
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
      :title="dialogType === 'add' ? '新增场馆' : '编辑场馆'"
      width="700px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="场馆名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入场馆名称" />
        </el-form-item>
        <el-form-item label="场馆图片" prop="imageUrl">
          <el-upload
            class="venue-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="venue-image" />
            <el-icon v-else class="venue-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入场馆地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业时间" prop="openingHours">
          <el-time-picker
            v-model="form.openTime"
            format="HH:mm"
            placeholder="开始时间"
          />
          <span class="mx-2">至</span>
          <el-time-picker
            v-model="form.closeTime"
            format="HH:mm"
            placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="场馆介绍" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入场馆介绍"
          />
        </el-form-item>
        <el-form-item label="营业状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 搜索表单
const searchForm = ref({
  keyword: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const venueList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框数据
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const form = ref({
  name: '',
  imageUrl: '',
  address: '',
  contactPhone: '',
  openTime: null,
  closeTime: null,
  description: '',
  status: 1
})

// 计算营业时间
const formatTime = (time) => {
  if (!time) return ''
  const hours = time.getHours().toString().padStart(2, '0')
  const minutes = time.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入场馆名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  imageUrl: [
    { required: true, message: '请上传场馆图片', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入场馆地址', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  openTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  closeTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入场馆介绍', trigger: 'blur' }
  ]
}

// 获取场馆列表
const getList = async () => {
  loading.value = true
  try {
    // TODO: 调用接口获取数据
    // 模拟数据
    venueList.value = [
      {
        id: 1,
        name: '示例场馆1',
        imageUrl: 'https://via.placeholder.com/200',
        address: '北京市朝阳区xxx街道xxx号',
        contactPhone: '13800138000',
        openingHours: '09:00-22:00',
        status: 1,
        description: '这是一个示例场馆'
      },
      {
        id: 2,
        name: '示例场馆2',
        imageUrl: 'https://via.placeholder.com/200',
        address: '北京市海淀区xxx街道xxx号',
        contactPhone: '13900139000',
        openingHours: '10:00-21:00',
        status: 0,
        description: '这是另一个示例场馆'
      }
    ]
    total.value = 2
  } catch (error) {
    ElMessage.error('获取场馆列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getList()
}

// 新增场馆
const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    name: '',
    imageUrl: '',
    address: '',
    contactPhone: '',
    openTime: null,
    closeTime: null,
    description: '',
    status: 1
  }
  dialogVisible.value = true
}

// 编辑场馆
const handleEdit = (row) => {
  dialogType.value = 'edit'
  const [openTime, closeTime] = row.openingHours.split('-')
  form.value = {
    ...row,
    openTime: new Date(`2000/01/01 ${openTime}`),
    closeTime: new Date(`2000/01/01 ${closeTime}`)
  }
  dialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row) => {
  try {
    // TODO: 调用切换状态接口
    ElMessage.success(row.status ? '场馆已关闭' : '场馆已开启')
    row.status = row.status ? 0 : 1
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除场馆
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认删除该场馆吗？',
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
  form.value.imageUrl = response.url
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = {
          ...form.value,
          openingHours: `${formatTime(form.value.openTime)}-${formatTime(form.value.closeTime)}`
        }
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
.venue-container {
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

  .venue-uploader {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);

    &:hover {
      border-color: var(--el-color-primary);
    }

    .venue-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 150px;
      height: 150px;
      text-align: center;
      line-height: 150px;
    }

    .venue-image {
      width: 150px;
      height: 150px;
      display: block;
      object-fit: cover;
    }
  }

  .mx-2 {
    margin: 0 10px;
  }
}
</style> 