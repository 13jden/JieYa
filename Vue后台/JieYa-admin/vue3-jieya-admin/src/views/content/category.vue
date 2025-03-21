<template>
  <div class="category-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>分类管理</span>
          <el-button type="primary" @click="handleAdd">
            新增分类
          </el-button>
        </div>
      </template>

      <!-- 分类表格 -->
      <el-table 
        :data="categoryList" 
        style="width: 100%" 
        v-loading="loading"
        row-key="id"
      >
        <el-table-column prop="sort" label="排序" width="100">
          <template #default="{ row, $index }">
            <el-tag class="drag-handle">
              {{ row.sort }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryCode" label="分类编号" />
        <el-table-column prop="categoryName" label="分类名称" />
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
      :title="dialogType === 'add' ? '新增分类' : '编辑分类'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编号" prop="code">
          <el-input v-model="form.code" placeholder="请输入分类编号" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryList, addCategory, updateCategory, deleteCategory, updateCategorySort } from '@/api/category'

// 表格数据
const loading = ref(false)
const categoryList = ref([])
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
  name: '',
  code: '',
  sort: 0
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入分类编号', trigger: 'blur' }
  ],

}

// 获取分类列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getCategoryList()
    if (res.code === 1) {
      categoryList.value = res.data.map(item => ({
        ...item,
        name: item.categoryName,
        code: item.categoryCode,
        id: item.categoryId
      }))
      total.value = categoryList.value.length
    } else {
      ElMessage.error(res.message || '获取分类列表失败')
    }
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 新增分类
const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    name: '',
    code: '',
    sort: categoryList.value.length > 0 ? Math.max(...categoryList.value.map(item => item.sort)) + 1 : 1
  }
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  dialogType.value = 'edit'
  form.value = { 
    id: row.id,
    name: row.categoryName,
    code: row.categoryCode,
    sort: row.sort
  }
  dialogVisible.value = true
}

// 删除分类
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认删除该分类吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteCategory(row.id)
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
          res = await addCategory({
            name: form.value.name,
            code: form.value.code
          })
        } else {
          res = await updateCategory({
            id: form.value.id,
            name: form.value.name,
            code: form.value.code
          })
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

// 初始化时添加拖拽事件监听
onMounted(() => {
  getList()
  
  // 添加拖拽事件监听
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

  const temp = [...categoryList.value]
  const dragRow = temp[dragIndex]
  
  temp.splice(dragIndex, 1)
  temp.splice(dragenterIndex, 0, dragRow)
  
  const sortList = temp.map((item, index) => ({
    id: item.categoryId || item.id,
    sort: index + 1
  }))
  
  try {
    loading.value = true
    const res = await updateCategorySort(sortList)
    if (res.code === 1) {
      ElMessage.success('排序更新成功')
      categoryList.value = temp.map((item, index) => ({
        ...item,
        sort: index + 1
      }))
      
      // 重新设置拖拽事件
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
</script>

<style lang="scss" scoped>
.category-container {
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