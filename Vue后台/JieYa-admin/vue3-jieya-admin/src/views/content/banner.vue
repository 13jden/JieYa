<template>
  <div class="banner-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>Banner管理</span>
          <el-button type="primary" @click="handleAdd">
            新增Banner
          </el-button>
        </div>
      </template>

      <!-- Banner表格 -->
      <el-table 
        :data="bannerList" 
        style="width: 100%" 
        v-loading="loading"
        row-key="bannerId"
      >
        <el-table-column prop="sort" label="排序" width="100">
          <template #default="{ row, $index }">
            <el-tag class="drag-handle">
              {{ row.sort }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="图片">
          <template #default="{ row }">
            <img :src="row.image" alt="Banner Image" style="width: 100px; height: auto;" />
          </template>
        </el-table-column>
        <el-table-column label="类型">
          <template #default="{ row }">
            <span>{{ getTypeLabel(row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="text" label="标题" />
        <el-table-column label="跳转ID">
          <template #default="{ row }">
            <span>{{ row.contentId || '无' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="启用状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.on"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
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
      :title="dialogType === 'add' ? '新增Banner' : '编辑Banner'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="图片" prop="image">
          <el-upload
            action=""
            :http-request="uploadRequest"
            :file-list="fileList"
            :on-remove="handleRemove"
            :limit="1"
            :disabled="fileList.length >= 1"
          >
            <el-button size="small" type="primary" :disabled="fileList.length >= 1">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">只能上传一张图片，如需更换请先删除当前图片</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option label="场地" :value="1" />
            <el-option label="商品" :value="2" />
            <el-option label="笔记" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="text">
          <el-input v-model="form.text" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="跳转ID" prop="contentId">
          <el-input v-model="form.contentId" placeholder="请输入跳转ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBannerList, addBanner, updateBanner, deleteBanner, updateBannerSort } from '@/api/banner'
import { uploadImage, deleteTempImage, deleteBannerImage } from '@/api/image'

// 表格数据
const loading = ref(false)
const bannerList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
let dragIndex = -1
let dragenterIndex = -1

// 对话框数据
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const form = ref({
  image: '',
  type: null,
  text: '',
  contentId: null
})
const fileList = ref([])

// 表单验证规则
const rules = {
  image: [
    { required: true, message: '请上传图片', trigger: 'change' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ]
}

// 保存原始图片地址，用于判断是否修改
const originalImage = ref('')

// 获取Banner列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getBannerList()
    if (res.code === 1) {
      bannerList.value = res.data.map(item => ({
        ...item,
        id: item.bannerId
      }))
      total.value = bannerList.value.length
    } else {
      ElMessage.error(res.message || '获取Banner列表失败')
    }
  } catch (error) {
    ElMessage.error('获取Banner列表失败')
  } finally {
    loading.value = false
  }
}

// 新增Banner
const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    image: '',
    type: null,
    text: '',
    contentId: null
  }
  fileList.value = []
  dialogVisible.value = true
}

// 编辑Banner
const handleEdit = (row) => {
  dialogType.value = 'edit'
  originalImage.value = row.image  // 保存原始图片地址
  form.value = { 
    id: row.bannerId,
    image: row.image,
    type: row.type,
    text: row.text,
    contentId: row.contentId,
    on: row.on
  }
  fileList.value = [{ name: row.image, url: row.image }]
  dialogVisible.value = true
}

// 删除Banner
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认删除该Banner吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteBanner(row.bannerId)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        getList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let res
        if (dialogType.value === 'add') {
          res = await addBanner({
            image: form.value.image,
            type: form.value.type,
            text: form.value.text,
            contentId: form.value.contentId,
            on: 1
          })
        } else {
          // 创建更新数据对象
          const updateData = {
            bannerId: form.value.id,
            type: form.value.type,
            text: form.value.text,
            contentId: form.value.contentId,
            on: form.value.on
          }
          
          // 只有在图片被修改时才传递image字段
          if (form.value.image !== originalImage.value) {
            updateData.image = form.value.image
          }
          
          res = await updateBanner(updateData)
        }
        
        if (res.code === 1) {
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功')
          dialogVisible.value = false
          getList()
        } else {
          ElMessage.error(res.message || (dialogType.value === 'add' ? '新增失败' : '编辑失败'))
        }
      } catch (error) {
        ElMessage.error(dialogType.value === 'add' ? '新增失败' : '编辑失败')
      }
    }
  })
}

// 取消操作
const handleCancel = () => {
  if (dialogType.value === 'add' && form.value.image) {
    const fileName = getFileNameFromUrl(form.value.image)
    deleteTempImage(fileName)
  }
  dialogVisible.value = false
  if (formRef.value) {
    formRef.value.resetFields()
  }
  fileList.value = []
}

// 上传图片请求
const uploadRequest = async ({ file }) => {
  // 如果已经有图片，提示先删除
  if (fileList.value.length > 0) {
    ElMessage.warning('请先删除当前图片再上传新图片')
    return
  }
  
  try {
    const res = await uploadImage(file)
    if (res.code === 1) {
      form.value.image = res.data
      fileList.value = [{ name: res.data, url: res.data }]
    } else {
      ElMessage.error(res.message || '图片上传失败')
    }
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
}

// 移除图片
const handleRemove = (file) => {
  const fileName = getFileNameFromUrl(file.url || file.name)
  
  if (dialogType.value === 'add') {
    deleteTempImage(fileName)
  } else {
    deleteBannerImage(fileName)
  }
  
  form.value.image = ''
}

// 从URL中提取文件名
const getFileNameFromUrl = (url) => {
  if (!url) return ''
  
  if (url.includes('/')) {
    return url.substring(url.lastIndexOf('/') + 1)
  }
  
  return url
}

// 上传成功
const handleSuccess = (response, file, fileList) => {
  form.value.image = response.data
}

// 初始化时添加拖拽事件监听
onMounted(() => {
  getList()
  
  setTimeout(() => {
    setupDragEvents()
  }, 500)
})

// 设置拖拽事件
const setupDragEvents = () => {
  const table = document.querySelector('.el-table__body-wrapper table')
  if (!table) return
  
  const rows = table.querySelectorAll('tr')
  rows.forEach((row, index) => {
    row.setAttribute('draggable', 'true')
    
    row.addEventListener('dragstart', (e) => {
      dragIndex = index
      e.dataTransfer.effectAllowed = 'move'
      row.classList.add('dragging')
    })
    
    row.addEventListener('dragover', (e) => {
      e.preventDefault()
    })
    
    row.addEventListener('dragenter', (e) => {
      e.preventDefault()
      if (index !== dragIndex) {
        dragenterIndex = index
        rows.forEach(r => r.classList.remove('drag-over'))
        row.classList.add('drag-over')
      }
    })
    
    row.addEventListener('drop', (e) => {
      e.preventDefault()
      handleRowDrop()
    })
    
    row.addEventListener('dragend', () => {
      rows.forEach(r => {
        r.classList.remove('dragging', 'drag-over')
      })
    })
  })
}

// 处理行拖拽
const handleRowDrop = async () => {
  if (dragIndex === dragenterIndex) return

  const temp = [...bannerList.value]
  const dragRow = temp[dragIndex]
  
  temp.splice(dragIndex, 1)
  temp.splice(dragenterIndex, 0, dragRow)
  
  const sortList = temp.map((item, index) => ({
    bannerId: item.bannerId,
    sort: index + 1
  }))
  
  try {
    loading.value = true
    const res = await updateBannerSort(sortList)
    if (res.code === 1) {
      ElMessage.success('排序更新成功')
      bannerList.value = temp.map((item, index) => ({
        ...item,
        sort: index + 1
      }))
      
      setTimeout(() => {
        setupDragEvents()
      }, 500)
    } else {
      ElMessage.error(res.message || '排序更新失败')
      await getList()
    }
  } catch (error) {
    console.error('排序更新失败:', error)
    ElMessage.error('排序更新失败')
    await getList()
  } finally {
    loading.value = false
    dragIndex = -1
    dragenterIndex = -1
  }
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

// 根据type值返回对应的文字
const getTypeLabel = (type) => {
  switch (type) {
    case 1:
      return '场地'
    case 2:
      return '商品'
    case 3:
      return '笔记'
    default:
      return '未知'
  }
}

// 处理启用状态变更
const handleStatusChange = async (row, value) => {
  try {
    const res = await updateBanner({
      bannerId: row.bannerId,
      on: value
    })
    
    if (res.code === 1) {
      ElMessage.success('状态更新成功')
    } else {
      ElMessage.error(res.message || '状态更新失败')
      // 恢复原状态
      row.on = row.on === 1 ? 0 : 1
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.on = row.on === 1 ? 0 : 1
  }
}
</script>

<style lang="scss" scoped>
.banner-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .drag-handle {
    cursor: move;
    user-select: none;
  }

  :deep(.el-table__row) {
    &.dragging {
      opacity: 0.5;
      cursor: move;
    }

    &.drag-over {
      border-top: 2px solid #409eff;
    }
  }

  :deep(.el-table__row:hover) {
    cursor: move;
  }
}
</style> 