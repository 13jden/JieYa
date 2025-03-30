<template>
  <div class="prop-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入道具名称"
              style="width: 200px"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" @click="handleSearch" style="margin-left: 10px">
              搜索
            </el-button>
          </div>
          <el-button type="success" @click="handleAdd">
            新增道具
          </el-button>
        </div>
      </template>

      <!-- 道具列表 -->
      <el-table :data="propList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面图片" width="120">
          <template #default="{ row }">
            <el-image
              style="width: 80px; height: 80px"
              :src="row.coverImage"
              :preview-src-list="row.imageList?.map(img => img.imageUrl) || []"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="道具名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="available" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.available ? 'success' : 'info'">
              {{ row.available ? '可用' : '不可用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
            <el-button
              size="small"
              type="warning"
              @click="handleEdit(row)"
            >
              编辑
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加道具' : '编辑道具'"
      width="700px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="prop-form"
      >
        <el-form-item label="道具名称" prop="propName">
          <el-input v-model="form.propName" placeholder="请输入道具名称" />
        </el-form-item>
        
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :precision="2"
            :step="0.01"
            :min="0"
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="是否可用" prop="available">
          <el-switch
            v-model="form.available"
            :active-value="true"
            :inactive-value="false"
          />
        </el-form-item>
        
        <el-form-item label="道具描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入道具描述"
          />
        </el-form-item>
        
        <!-- 轮播图片上传 -->
        <el-form-item label="轮播图片" prop="carousel" class="image-uploader-container">
          <div class="image-list" ref="carouselListRef">
            <div 
              v-for="(file, index) in carouselImageList" 
              :key="file.uid || index" 
              class="image-item"
              :class="{'is-cover': index === 0}"
            >
              <img :src="file.url" class="preview-image" />
              <div class="image-actions">
                <el-button 
                  type="danger" 
                  circle 
                  size="small" 
                  @click="removeCarouselImage(index)"
                  class="delete-btn"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
                <el-tag v-if="index === 0" type="success" size="small" class="cover-tag">封面</el-tag>
              </div>
            </div>
            
            <div class="image-uploader" v-if="carouselImageList.length < 9">
              <el-upload
                action=""
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleCarouselImageChange"
                :multiple="true"
                accept="image/*"
              >
                <el-icon class="uploader-icon"><Plus /></el-icon>
                <div class="upload-text">上传轮播图</div>
              </el-upload>
            </div>
          </div>
          <div class="upload-tip">
            提示：最多上传9张图片，<span class="cover-tip">第一个位置的图片将作为封面图</span>，<span class="drag-tip">拖拽图片可调整顺序</span>
          </div>
        </el-form-item>
        
        <!-- 详情图片上传 -->
        <el-form-item label="详情图片" prop="detail" class="image-uploader-container">
          <div class="image-list" ref="detailListRef">
            <div 
              v-for="(file, index) in detailImageList" 
              :key="file.uid || index" 
              class="image-item"
            >
              <img :src="file.url" class="preview-image" />
              <div class="image-actions">
                <el-button 
                  type="danger" 
                  circle 
                  size="small" 
                  @click="removeDetailImage(index)"
                  class="delete-btn"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            
            <div class="image-uploader" v-if="detailImageList.length < 9">
              <el-upload
                action=""
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleDetailImageChange"
                :multiple="true"
                accept="image/*"
              >
                <el-icon class="uploader-icon"><Plus /></el-icon>
                <div class="upload-text">上传详情图</div>
              </el-upload>
            </div>
          </div>
          <div class="upload-tip">
            提示：最多上传9张图片，<span class="drag-tip">拖拽图片可调整顺序</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情查看对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="道具详情"
      width="800px"
      destroy-on-close
    >
      <div class="prop-detail" v-loading="detailLoading">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-item">
            <span class="label">道具名称:</span>
            <span>{{ detailInfo.name }}</span>
          </div>
          <div class="detail-item">
            <span class="label">价格:</span>
            <span>¥{{ detailInfo.price?.toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">状态:</span>
            <el-tag :type="detailInfo.available ? 'success' : 'info'">
              {{ detailInfo.available ? '可用' : '不可用' }}
            </el-tag>
          </div>
          <div class="detail-item" v-if="detailInfo.description">
            <span class="label">描述:</span>
            <div class="description">{{ detailInfo.description }}</div>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>轮播图片</h3>
          <div class="image-gallery" v-if="detailInfo.imageList && detailInfo.imageList.length > 0">
            <el-image
              v-for="(img, index) in detailInfo.imageList"
              :key="index"
              :src="img.imageUrl"
              :preview-src-list="detailInfo.imageList.map(img => img.imageUrl)"
              class="gallery-image"
              fit="cover"
            />
          </div>
          <div class="no-image" v-else>暂无轮播图片</div>
        </div>
        
        <div class="detail-section">
          <h3>详情图片</h3>
          <div class="detail-images" v-if="detailInfo.detailImageList && detailInfo.detailImageList.length > 0">
            <el-image
              v-for="(img, index) in detailInfo.detailImageList"
              :key="index"
              :src="img.imageUrl"
              :preview-src-list="detailInfo.detailImageList.map(img => img.imageUrl)"
              class="detail-image"
              fit="contain"
            />
          </div>
          <div class="no-image" v-else>暂无详情图片</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Edit, View } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { getPropList, getPropDetail, addProp, updateProp, deleteProp } from '@/api/prop'

// 道具列表
const propList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchForm = ref({
  name: ''
})

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  getList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  getList()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getList()
}

// 获取道具列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getPropList({
      page: currentPage.value,
      limit: pageSize.value,
      name: searchForm.value.name
    })
    
    if (res.code === 1) {
      propList.value = res.data || []
      total.value = res.total || 0
      
      // 处理图片URL
      propList.value.forEach(prop => {
        // 确保imageList是数组
        if (!prop.imageList) {
          prop.imageList = []
        }
        // 确保detailImageList是数组
        if (!prop.detailImageList) {
          prop.detailImageList = []
        }
      })
      
    } else {
      ElMessage.error(res.message || '获取列表失败')
    }
  } catch (error) {
    console.error('获取道具列表出错:', error)
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // add或edit
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailInfo = ref({})

// 表单相关
const formRef = ref(null)
const form = ref({
  propName: '',
  price: 0,
  available: true,
  description: '',
  deletedImageIds: []
})

// 表单验证规则
const rules = {
  propName: [
    { required: true, message: '请输入道具名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ]
}

// 图片上传相关 - 轮播图
const carouselImageList = ref([])
const carouselListRef = ref(null)
let carouselSortable = null

// 图片上传相关 - 详情图
const detailImageList = ref([])
const detailListRef = ref(null)
let detailSortable = null

// 添加currentId变量
const currentId = ref(null)

// 生成唯一ID
const generateUid = () => {
  return `${Date.now()}-${Math.floor(Math.random() * 1000)}`
}

// 初始化拖拽 - 轮播图
const initCarouselSortable = () => {
  nextTick(() => {
    try {
      if (carouselListRef.value) {
        // 销毁之前的实例（如果存在）
        if (carouselSortable) {
          try {
            carouselSortable.destroy()
          } catch (e) {
            console.error('销毁轮播图Sortable实例失败:', e)
          }
        }
        
        // 初始化Sortable
        carouselSortable = new Sortable(carouselListRef.value, {
          animation: 150,
          ghostClass: 'sortable-ghost',
          chosenClass: 'sortable-chosen',
          dragClass: 'sortable-drag',
          filter: '.image-uploader', // 不允许拖拽上传按钮
          onEnd: (evt) => {
            try {
              // 获取拖拽前后的位置
              const { oldIndex, newIndex } = evt
              
              // 避免上传按钮被移动
              if (oldIndex >= carouselImageList.value.length || newIndex >= carouselImageList.value.length) {
                return
              }
              
              // 更新数组顺序
              const itemToMove = carouselImageList.value.splice(oldIndex, 1)[0]
              carouselImageList.value.splice(newIndex, 0, itemToMove)
              
              // 更新所有图片的sortOrder
              updateCarouselImageSortOrders()
              
              console.log('拖拽后的轮播图顺序:', carouselImageList.value.map((img, idx) => ({
                name: img.name,
                sortOrder: img.sortOrder,
                index: idx,
                uid: img.uid
              })))
            } catch (error) {
              console.error('轮播图拖拽处理错误:', error)
              ElMessage.error('排序操作失败')
            }
          },
          delay: 100
        })
      }
    } catch (error) {
      console.error('初始化轮播图Sortable失败:', error)
    }
  })
}

// 初始化拖拽 - 详情图
const initDetailSortable = () => {
  nextTick(() => {
    try {
      if (detailListRef.value) {
        // 销毁之前的实例（如果存在）
        if (detailSortable) {
          try {
            detailSortable.destroy()
          } catch (e) {
            console.error('销毁详情图Sortable实例失败:', e)
          }
        }
        
        // 初始化Sortable
        detailSortable = new Sortable(detailListRef.value, {
          animation: 150,
          ghostClass: 'sortable-ghost',
          chosenClass: 'sortable-chosen',
          dragClass: 'sortable-drag',
          filter: '.image-uploader', // 不允许拖拽上传按钮
          onEnd: (evt) => {
            try {
              // 获取拖拽前后的位置
              const { oldIndex, newIndex } = evt
              
              // 避免上传按钮被移动
              if (oldIndex >= detailImageList.value.length || newIndex >= detailImageList.value.length) {
                return
              }
              
              // 更新数组顺序
              const itemToMove = detailImageList.value.splice(oldIndex, 1)[0]
              detailImageList.value.splice(newIndex, 0, itemToMove)
              
              // 更新所有图片的sortOrder
              updateDetailImageSortOrders()
              
              console.log('拖拽后的详情图顺序:', detailImageList.value.map((img, idx) => ({
                name: img.name,
                sortOrder: img.sortOrder,
                index: idx,
                uid: img.uid
              })))
            } catch (error) {
              console.error('详情图拖拽处理错误:', error)
              ElMessage.error('排序操作失败')
            }
          },
          delay: 100
        })
      }
    } catch (error) {
      console.error('初始化详情图Sortable失败:', error)
    }
  })
}

// 更新轮播图的排序
const updateCarouselImageSortOrders = () => {
  carouselImageList.value.forEach((img, index) => {
    img.sortOrder = index + 1
  })
}

// 更新详情图的排序
const updateDetailImageSortOrders = () => {
  detailImageList.value.forEach((img, index) => {
    img.sortOrder = index + 1
  })
}

// 处理轮播图变化
const handleCarouselImageChange = (file) => {
  if (!file) return
  
  // 检查文件类型
  const isImage = file.raw.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return
  }
  
  // 检查文件大小（5MB）
  const isLt5M = file.raw.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }
  
  // 创建图片对象
  const imgObj = {
    uid: generateUid(),
    name: file.name,
    url: URL.createObjectURL(file.raw),
    file: file.raw,
    isExisting: false,
    sortOrder: carouselImageList.value.length + 1,
    isDetail: false // 不是详情图
  }
  
  // 添加到轮播图列表
  carouselImageList.value.push(imgObj)
  
  // 如果是第一张图片，自动设置为封面
  if (carouselImageList.value.length === 1) {
    imgObj.sortOrder = 1
  }
  
  console.log('添加轮播图:', imgObj)
}

// 处理详情图变化
const handleDetailImageChange = (file) => {
  if (!file) return
  
  // 检查文件类型
  const isImage = file.raw.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return
  }
  
  // 检查文件大小（5MB）
  const isLt5M = file.raw.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }
  
  // 创建图片对象
  const imgObj = {
    uid: generateUid(),
    name: file.name,
    url: URL.createObjectURL(file.raw),
    file: file.raw,
    isExisting: false,
    sortOrder: detailImageList.value.length + 1,
    isDetail: true // 是详情图
  }
  
  // 添加到详情图列表
  detailImageList.value.push(imgObj)
  
  console.log('添加详情图:', imgObj)
}

// 移除轮播图
const removeCarouselImage = (index) => {
  // 如果是已有图片，添加到要删除的列表
  const img = carouselImageList.value[index]
  if (img.isExisting && img.id) {
    form.value.deletedImageIds = form.value.deletedImageIds || []
    form.value.deletedImageIds.push(img.id)
  }
  
  // 从数组中移除
  carouselImageList.value.splice(index, 1)
  
  // 更新排序
  updateCarouselImageSortOrders()
}

// 移除详情图
const removeDetailImage = (index) => {
  // 如果是已有图片，添加到要删除的列表
  const img = detailImageList.value[index]
  if (img.isExisting && img.id) {
    form.value.deletedImageIds = form.value.deletedImageIds || []
    form.value.deletedImageIds.push(img.id)
  }
  
  // 从数组中移除
  detailImageList.value.splice(index, 1)
  
  // 更新排序
  updateDetailImageSortOrders()
}

// 打开添加对话框
const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    propName: '',
    description: '',
    price: 1, // 默认价格设为1
    available: true,
    deletedImageIds: [] // 确保此字段存在
  }
  carouselImageList.value = []
  detailImageList.value = []
  
  dialogVisible.value = true  // 先打开对话框
  
  // 使用nextTick确保DOM更新后再操作表单
  nextTick(() => {
    // 检查formRef是否存在
    if (formRef.value) {
      formRef.value.resetFields()
    }
    
    // 调用初始化排序函数
    initCarouselSortable()
    initDetailSortable()
  })
}

// 打开编辑对话框
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  currentId.value = row.id
  const res = await getPropDetail(row.id);
  // 复制基本信息
  form.value = {
    id: row.id, // 确保设置ID
    propName: res.data.name,
    description: res.data.description,
    price: parseFloat(res.data.price) || 1, // 确保价格为数字，默认为1
    available: res.data.available
  }
  
  // 处理图片
  carouselImageList.value = []
  detailImageList.value = []
  
  // 处理轮播图片 - 使用后端返回的已分离数据
  if (res.data.imageList && res.data.imageList.length > 0) {
    carouselImageList.value = res.data.imageList.map(img => ({
      uid: Date.now() + '_' + Math.random(),
      url: img.imageUrl,
      isExisting: true,
      id: img.id,
      file: null
    }))
  }
  
  // 处理详情图片 - 使用后端返回的已分离数据
  if (res.data.detailImageList && res.data.detailImageList.length > 0) {
    detailImageList.value = res.data.detailImageList.map(img => ({
      uid: Date.now() + '_' + Math.random(),
      url: img.imageUrl,
      isExisting: true,
      id: img.id,
      file: null
    }))
  }
  
  nextTick(() => {
    if (formRef.value) {
      formRef.value.resetFields()
    }
    dialogVisible.value = true
    
    // 初始化拖拽排序
    nextTick(() => {
      initCarouselSortable()
      initDetailSortable()
    })
  })
}

// 查看详情 - 直接使用列表数据，不再发送请求
const handleViewDetail = (row) => {
  // 直接使用列表中的数据
  detailInfo.value = row
  detailDialogVisible.value = true
}

// 删除道具
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除道具"${row.name}"吗？删除后将无法恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteProp({ id: row.id })
      if (res.code === 1) {
        ElMessage.success('删除成功')
        getList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除道具出错:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消删除操作
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  // 检查表单必填项
  if (!form.value.propName) {
    ElMessage.warning('请填写道具名称')
    return
  }
  
  // 检查轮播图片列表
  if (carouselImageList.value.length === 0) {
    ElMessage.warning('请至少上传一张轮播图片')
    return
  }
  
  try {
    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          // 创建FormData对象
          const formData = new FormData()
          
          // 添加基本信息
          formData.append('propName', form.value.propName)
          formData.append('description', form.value.description)
          formData.append('price', form.value.price)
          formData.append('available', form.value.available)
          
          if (dialogType.value === 'add') {
            // 准备图片信息 - 所有图片都放在一个数组中
            const imageDtos = []
            
            // 添加轮播图信息
            carouselImageList.value.forEach((img, index) => {
              imageDtos.push({
                id: null,  // 新上传的图片id为null
                imageUrl: "",  // 新图片URL为空，由后端填充
                sortOrder: index + 1,
                isDetail: "0"  // 使用字符串"0"表示不是详情图
              })
            })
            
            // 添加详情图信息
            detailImageList.value.forEach((img, index) => {
              imageDtos.push({
                id: null,  // 新上传的图片id为null
                imageUrl: "",  // 新图片URL为空，由后端填充
                sortOrder: index + 1,
                isDetail: "1"  // 使用字符串"1"表示是详情图
              })
            })
            
            // 将图片信息转为JSON字符串
            formData.append('imageDtosJson', JSON.stringify(imageDtos))
            
            // 添加图片文件
            const allFiles = [...carouselImageList.value, ...detailImageList.value]
            allFiles.forEach(img => {
              if (img.file) {
                formData.append('files', img.file)
              }
            })
            
            // 发送请求
            const res = await addProp(formData)
            if (res.code === 1) {
              ElMessage.success('添加成功')
              dialogVisible.value = false
              getList()
            } else {
              ElMessage.error(res.message || '添加失败')
            }
          } else {
            // 编辑道具 - 使用currentId
            formData.append('id', currentId.value)
            
            // 准备所有图片信息（现有的和新的）放在一个数组中
            const allImageDtos = []
            
            // 添加轮播图信息（现有和新的）
            carouselImageList.value.forEach((img, index) => {
              allImageDtos.push({
                id: img.isExisting ? img.id : null,  // 现有图片保留id，新图片id为null
                imageUrl: img.isExisting ? img.url : "",  // 现有图片保留URL，新图片URL为空
                sortOrder: index + 1,
                isDetail: "0"  // 使用字符串"0"表示不是详情图
              })
            })
            
            // 添加详情图信息（现有和新的）
            detailImageList.value.forEach((img, index) => {
              allImageDtos.push({
                id: img.isExisting ? img.id : null,  // 现有图片保留id，新图片id为null
                imageUrl: img.isExisting ? img.url : "",  // 现有图片保留URL，新图片URL为空
                sortOrder: index + 1,
                isDetail: "1"  // 使用字符串"1"表示是详情图
              })
            })
            
            // 将所有图片信息转为JSON字符串
            formData.append('imageDtosJson', JSON.stringify(allImageDtos))
            
            // 只添加新图片文件
            const newFiles = [
              ...carouselImageList.value.filter(img => !img.isExisting),
              ...detailImageList.value.filter(img => !img.isExisting)
            ]
            
            newFiles.forEach(img => {
              if (img.file) {
                formData.append('files', img.file)
              }
            })
            
            // 发送请求
            const res = await updateProp(formData)
            if (res.code === 1) {
              ElMessage.success('更新成功')
              dialogVisible.value = false
              getList()
            } else {
              ElMessage.error(res.message || '更新失败')
            }
          }
        } catch (error) {
          console.error('提交表单出错:', error)
          ElMessage.error('提交失败')
        }
      }
    })
  } catch (error) {
    console.error('表单处理出错:', error)
    ElMessage.error('提交失败，请重试')
  }
}

// 初始化所有排序功能的集中函数
const initSortable = () => {
  nextTick(() => {
    initCarouselSortable()
    initDetailSortable()
  })
}

// 添加监听对话框打开状态
watch(dialogVisible, (newVal) => {
  if (newVal) {
    // 对话框打开时，初始化排序
    nextTick(() => {
      initCarouselSortable()
      initDetailSortable()
    })
  } else {
    // 对话框关闭，清理资源
    carouselImageList.value.forEach(img => {
      if (img.url && img.url.startsWith('blob:')) {
        URL.revokeObjectURL(img.url)
      }
    })
    
    detailImageList.value.forEach(img => {
      if (img.url && img.url.startsWith('blob:')) {
        URL.revokeObjectURL(img.url)
      }
    })
    
    // 重置表单
    if (formRef.value) {
      formRef.value.resetFields()
    }
    
    // 销毁Sortable实例
    if (carouselSortable) {
      carouselSortable.destroy()
      carouselSortable = null
    }
    
    if (detailSortable) {
      detailSortable.destroy()
      detailSortable = null
    }
  }
})

// 初始化
onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.prop-container {
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
  
  .prop-form {
    .image-uploader-container {
      .image-list {
        display: flex;
        flex-wrap: wrap;
        gap: 15px;
        margin-bottom: 10px;
        
        .image-item {
          position: relative;
          width: 150px;
          height: 150px;
          border: 1px solid #dcdfe6;
          border-radius: 6px;
          overflow: hidden;
          cursor: move;
          
          &.is-cover {
            border: 2px solid #67c23a;
          }
          
          .preview-image {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
          
          .image-actions {
            position: absolute;
            top: 5px;
            right: 5px;
            display: flex;
            gap: 5px;
          }
          
          .cover-tag {
            position: absolute;
            top: 10px;
            right: 10px;
            z-index: 2;
          }
        }
        
        .image-uploader {
          width: 150px;
          height: 150px;
          border: 1px dashed #d9d9d9;
          border-radius: 6px;
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          cursor: pointer;
          
          &:hover {
            border-color: #409eff;
          }
          
          .uploader-icon {
            font-size: 28px;
            color: #8c939d;
          }
          
          .upload-text {
            margin-top: 8px;
            font-size: 12px;
            color: #606266;
          }
        }
      }
      
      .upload-tip {
        font-size: 12px;
        color: #606266;
        margin-top: 5px;
        
        .cover-tip {
          color: #67c23a;
          font-weight: bold;
        }
        
        .drag-tip {
          color: #409eff;
          font-weight: bold;
        }
      }
    }
  }
  
  // 详情对话框相关样式
  .prop-detail {
    .detail-section {
      margin-bottom: 30px;
      
      h3 {
        margin-bottom: 15px;
        padding-bottom: 8px;
        border-bottom: 1px solid #ebeef5;
        color: #303133;
      }
      
      .detail-item {
        margin-bottom: 15px;
        
        .label {
          display: inline-block;
          width: 100px;
          font-weight: bold;
          color: #606266;
        }
        
        .description {
          margin-top: 10px;
          padding: 15px;
          background-color: #f8f8f8;
          border-radius: 4px;
          white-space: pre-wrap;
          line-height: 1.6;
        }
      }
      
      .image-gallery {
        display: flex;
        flex-wrap: wrap;
        gap: 15px;
        
        .gallery-image {
          width: 150px;
          height: 150px;
          border-radius: 4px;
          object-fit: cover;
          border: 1px solid #ebeef5;
          cursor: pointer;
          transition: transform 0.2s;
          
          &:hover {
            transform: scale(1.05);
          }
        }
      }
      
      .detail-images {
        display: flex;
        flex-direction: column;
        width: 100%;
        
        .detail-image {
          width: 100%;
          height: auto;
          object-fit: contain;
          cursor: pointer;
          margin-bottom: 0;
          transition: transform 0.2s;
          
          &:hover {
            transform: scale(1.02);
          }
        }
      }
      
      .no-image {
        padding: 30px;
        text-align: center;
        color: #909399;
        background-color: #f8f8f8;
        border-radius: 4px;
      }
    }
  }
}

// 拖拽相关样式
.sortable-ghost {
  opacity: 0.5;
  background: #c8ebfb;
  border: 2px dashed #409eff;
}

.sortable-chosen {
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
}

.sortable-drag {
  opacity: 0.8;
}
</style> 