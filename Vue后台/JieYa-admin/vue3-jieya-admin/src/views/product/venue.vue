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
              :src="row.coverImage"
              :preview-src-list="row.imageList?.map(img => img.imageUrl) || []"
              fit="cover"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="场馆名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="location" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="capacity" label="容纳人数" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            {{ row.price }}元/小时
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="150">
          <template #default="{ row }">
            <el-tag 
              v-for="tagId in row.tags" 
              :key="tagId" 
              class="tag-item" 
              size="small"
            >
              {{ getTagNameById(tagId) }}
            </el-tag>
            <span v-if="!row.tags || row.tags.length === 0">无标签</span>
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
      :title="dialogType === 'add' ? '新增场馆' : '编辑场馆'"
      width="800px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="venue-form"
      >
        <el-row>
          <el-col :span="24">
            <el-form-item label="场馆图片" prop="files" class="image-uploader-container">
              <div class="image-list" ref="imageListRef">
                <div 
                  v-for="(file, index) in imageList" 
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
                      @click="removeImage(index)"
                      class="delete-btn"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                    <el-tag v-if="index === 0" type="success" size="small" class="cover-tag">封面</el-tag>
                  </div>
                </div>
                
                <div class="image-uploader" v-if="imageList.length < 9">
                  <el-upload
                    action=""
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleImageChange"
                    :multiple="true"
                    accept="image/*"
                  >
                    <el-icon class="uploader-icon"><Plus /></el-icon>
                    <div class="upload-text">上传图片</div>
                  </el-upload>
                </div>
              </div>
              <div class="upload-tip">
                提示：最多上传9张图片，<span class="cover-tip">第一个位置的图片将作为封面图</span>，<span class="drag-tip">拖拽图片可调整顺序</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="场馆名称" prop="venueName">
              <el-input v-model="form.venueName" placeholder="请输入场馆名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="场地价格" prop="price">
              <el-input-number 
                v-model="form.price" 
                :precision="2" 
                :step="10" 
                :min="0" 
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="场地地址" prop="location">
              <el-input v-model="form.location" placeholder="请输入场地地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="容纳人数" prop="capacity">
              <el-input-number 
                v-model="form.capacity" 
                :min="1" 
                :max="1000" 
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="场馆标签" prop="tags">
          <div class="tags-container">
            <el-select
              v-model="form.tags"
              multiple
              filterable
              placeholder="请选择标签"
              style="width: 100%"
            >
              <el-option
                v-for="tag in tagOptions"
                :key="tag.id"
                :label="tag.tagName"
                :value="tag.id"
              />
            </el-select>
            <el-button 
              type="primary" 
              @click="showAddTagDialog"
              :icon="Plus"
              style="margin-left: 10px"
            >
              添加
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="场馆介绍" prop="introduction">
          <el-input
            v-model="form.introduction"
            type="textarea"
            :rows="4"
            placeholder="请输入场馆介绍"
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
    
    <!-- 添加标签对话框 -->
    <el-dialog
      v-model="addTagDialogVisible"
      title="添加标签"
      width="30%"
      destroy-on-close
    >
      <el-form :model="tagForm" label-width="80px" ref="tagFormRef">
        <el-form-item 
          label="标签名称" 
          prop="tagName"
          :rules="[{ required: true, message: '请输入标签名称', trigger: 'blur' }]"
        >
          <el-input v-model="tagForm.tagName" autocomplete="off" maxlength="20" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addTagDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddTag">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="场地详情"
      width="800px"
      destroy-on-close
    >
      <div class="venue-detail">
        <!-- 图片放在上方 -->
        <div class="detail-section">
          <h3>场地图片</h3>
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
          <div class="no-image" v-else>暂无场地图片</div>
        </div>
        
        <!-- 基本信息放在下方 -->
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-item">
            <span class="label">场地名称:</span>
            <span>{{ detailInfo.name }}</span>
          </div>
          <div class="detail-item">
            <span class="label">位置:</span>
            <span>{{ detailInfo.location }}</span>
          </div>
          <div class="detail-item">
            <span class="label">容量:</span>
            <span>{{ detailInfo.capacity }} 人</span>
          </div>
          <div class="detail-item">
            <span class="label">价格:</span>
            <span>¥{{ detailInfo.price?.toFixed(2) }}/小时</span>
          </div>
          <div class="detail-item" v-if="detailInfo.tags && detailInfo.tags.length > 0">
            <span class="label">标签:</span>
            <div>
              <el-tag
                v-for="tagId in detailInfo.tags"
                :key="tagId"
                class="tag-item"
                size="small"
              >
                {{ getTagNameById(tagId) }}
              </el-tag>
            </div>
          </div>
          <div class="detail-item" v-if="detailInfo.content">
            <span class="label">介绍:</span>
            <div class="description">{{ detailInfo.content }}</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Edit, View } from '@element-plus/icons-vue'
import { getVenueList, addVenue, updateVenue, deleteVenue } from '@/api/venue'
import { getVenueTagList, addVenueTag } from '@/api/venue'
import Sortable from 'sortablejs'

// 搜索表单
const searchForm = ref({
  keyword: ''
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
  venueName: '',
  location: '',
  capacity: 30,
  price: 100.00,
  introduction: '',
  tags: []
})

// 图片上传相关
const imageList = ref([])
const fileList = ref([])

// 标签相关
const tagOptions = ref([])
const addTagDialogVisible = ref(false)
const tagForm = ref({ tagName: '' })
const tagFormRef = ref(null)

// 表单验证规则
const rules = {
  venueName: [
    { required: true, message: '请输入场馆名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入场馆地址', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入容纳人数', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入场地价格', trigger: 'change' }
  ]
}

// 图片拖拽相关
const imageListRef = ref(null)
let imageSortable = null

// 为每个图片生成唯一ID（确保Vue能正确跟踪元素）
const generateUid = () => {
  return 'img_' + Date.now() + '_' + Math.floor(Math.random() * 1000)
}

// 初始化拖拽 - 添加错误处理
const initSortable = () => {
  nextTick(() => {
    try {
      if (imageListRef.value) {
        // 销毁之前的实例（如果存在）
        if (imageSortable) {
          try {
            imageSortable.destroy()
          } catch (e) {
            console.error('销毁Sortable实例失败:', e)
          }
        }
        
        // 初始化Sortable
        imageSortable = new Sortable(imageListRef.value, {
          animation: 150,
          ghostClass: 'sortable-ghost',
          chosenClass: 'sortable-chosen',
          dragClass: 'sortable-drag',
          filter: '.image-uploader', // 不允许拖拽上传按钮
          onEnd: (evt) => {
            try {
              // 获取拖拽前后的位置
              const { oldIndex, newIndex } = evt
              
              // 避免拖拽上传按钮或超出范围
              if (oldIndex >= imageList.value.length || newIndex >= imageList.value.length) {
                return
              }
              
              // 创建一个新数组来反映拖拽后的顺序
              const newList = [...imageList.value]
              const movedItem = newList.splice(oldIndex, 1)[0]
              newList.splice(newIndex, 0, movedItem)
              
              // 重要：使用新数组替换旧数组，触发Vue的响应式更新
              imageList.value = newList
              
              // 更新所有图片的sortOrder
              updateImageSortOrders()
              
              console.log('拖拽后的图片顺序:', imageList.value.map((img, idx) => ({
                index: idx,
                name: img.name,
                sortOrder: img.sortOrder,
                uid: img.uid
              })))
            } catch (error) {
              console.error('拖拽处理错误:', error)
              ElMessage.error('排序操作失败')
            }
          },
          // 添加安全的延迟时间
          delay: 100
        })
      }
    } catch (error) {
      console.error('初始化Sortable失败:', error)
    }
  })
}

// 更新所有图片的sortOrder
const updateImageSortOrders = () => {
  // 根据当前位置设置sortOrder，第一个位置的图片sortOrder为1
  imageList.value.forEach((item, index) => {
    item.sortOrder = index + 1
  })
}

// 根据标签ID获取标签名称
const getTagNameById = (id) => {
  if (!id) return '未知标签'
  const tag = tagOptions.value.find(tag => tag.id === id)
  return tag ? tag.tagName : `标签#${id}`
}

// 获取场馆列表并处理标签
const getList = async () => {
  loading.value = true
  try {
    const res = await getVenueList({
      page: currentPage.value,
      limit: pageSize.value,
      name: searchForm.value.name,
      location: searchForm.value.location
    })
    
    if (res.code === 1) {
      venueList.value = res.data
      total.value = res.total
      
      // 确保所有场馆的tags字段都存在
      venueList.value.forEach(venue => {
        if (!venue.tags) {
          venue.tags = []
        } else if (!Array.isArray(venue.tags)) {
          // 如果tags不是数组，尝试转换
          try {
            venue.tags = JSON.parse(venue.tags)
          } catch (e) {
            console.error('解析标签失败:', e)
            venue.tags = []
          }
        }
        
        // 调试日志
        console.log(`场馆 ${venue.id} 的标签:`, venue.tags)
      })
    } else {
      ElMessage.error(res.message || '获取列表失败')
    }
  } catch (error) {
    console.error('获取场馆列表出错:', error)
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

// 获取标签列表
const getTagList = async () => {
  try {
    const res = await getVenueTagList()
    if (res.code === 1) {
      tagOptions.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取标签列表失败')
    }
  } catch (error) {
    console.error('获取标签列表错误:', error)
    ElMessage.error('获取标签列表失败')
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getList()
}

// 初始化表单
const initForm = () => {
  form.value = {
    venueName: '',
    location: '',
    capacity: '',
    price: '',
    introduction: '',
    tags: []  // 初始化为空数组而不是undefined
  }
  imageList.value = []
}

// 打开添加对话框
const handleAdd = () => {
  dialogType.value = 'add'
  initForm()  // 使用初始化函数
  dialogVisible.value = true
}

// 编辑场馆
const handleEdit = (row) => {
  dialogType.value = 'edit'
  form.value = {
    id: row.id,
    venueName: row.name,
    location: row.location,
    capacity: row.capacity,
    price: row.price,
    introduction: row.content,
    tags: Array.isArray(row.tags) ? [...row.tags] : []  // 确保是数组类型
  }
  
  console.log('编辑场馆的标签:', form.value.tags)
  console.log('可用标签列表:', tagOptions.value)
  
  // 设置图片列表，保留原始sortOrder信息
  imageList.value = row.imageList?.map(img => ({
    url: img.imageUrl,
    name: img.imageUrl.split('/').pop(),
    isExisting: true,
    sortOrder: img.sortOrder,
    uid: generateUid() // 为每个图片生成唯一ID
  })) || []
  
  dialogVisible.value = true
  
  // 初始化拖拽
  nextTick(() => {
    initSortable()
  })
}

// 监听图片列表变化，重新初始化拖拽
watch(imageList, () => {
  nextTick(() => {
    initSortable()
  })
}, { deep: true })

// 处理图片上传
const handleImageChange = (file) => {
  console.log('选择图片:', file)
  if (!file || !file.raw) return

  // 确保文件是图片类型
  const isImage = file.raw.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return
  }
  
  // 确保文件不超过2MB
  const isLt5M = file.raw.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB!')
    return
  }
  
  // 创建预览URL并添加到列表
  const url = URL.createObjectURL(file.raw)
  imageList.value.push({
    uid: Date.now() + '_' + Math.random(),
    url: url,
    raw: file.raw,  // 保存原始文件对象
    isExisting: false
  })
  
  // 更新Sortable
  nextTick(() => {
    initSortable()
  })
  
  return false // 阻止自动上传
}

// 移除图片
const removeImage = (index) => {
  // 如果有URL对象，需要释放
  if (imageList.value[index].url.startsWith('blob:')) {
    URL.revokeObjectURL(imageList.value[index].url)
  }
  
  // 从数组中移除
  imageList.value.splice(index, 1)
  
  // 更新排序
  initSortable()
  
  // 打印日志确认删除操作
  console.log('删除后图片列表长度:', imageList.value.length)
}

// 显示添加标签对话框
const showAddTagDialog = () => {
  addTagDialogVisible.value = true
  tagForm.value = { tagName: '' }
}

// 提交添加标签
const submitAddTag = async () => {
  if (!tagFormRef.value) return
  
  await tagFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await addVenueTag(tagForm.value.tagName)
        if (res.code === 1) {
          ElMessage.success('添加标签成功')
          
          // 防止引用类型导致的问题，使用基本类型
          const newTag = {
            id: res.data.id,
            tagName: res.data.tagName
          }
          
          // 正确地更新标签列表
          tagOptions.value = [...tagOptions.value, newTag]
          
          // 确保form.tags是一个数组
          if (!form.value.tags) {
            form.value.tags = []
          }
          
          // 将新标签添加到选中标签中
          form.value.tags.push(newTag.id)
          console.log('添加新标签后的选中标签:', form.value.tags)
          
          // 关闭对话框
          addTagDialogVisible.value = false
        } else {
          ElMessage.error(res.message || '添加标签失败')
        }
      } catch (error) {
        console.error('添加标签错误:', error)
        ElMessage.error('添加标签失败')
      }
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true
        try {
          // 创建FormData对象
          const formData = new FormData()
          
          // 添加基本信息
          formData.append('venueName', form.value.venueName)
          formData.append('location', form.value.location)
          formData.append('capacity', form.value.capacity)
          formData.append('price', form.value.price)
          formData.append('introduction', form.value.introduction)
          
          // 处理标签
          if (form.value.tags && form.value.tags.length > 0) {
            form.value.tags.forEach(tagId => {
              formData.append('tagIds', tagId)
            })
          }
          
          if (dialogType.value === 'add') {
            // 准备图片信息 - 所有新图片
            const imageDtos = []
            
            imageList.value.forEach((img, index) => {
              imageDtos.push({
                id: null,  // 新上传的图片id为null
                imageUrl: "",  // 新图片URL为空，由后端填充
                sortOrder: index + 1,
                isDetail: "0"  // 场馆没有详情图，使用字符串"0"
              })
            })
            
            // 添加图片信息JSON
            formData.append('imageDtosJson', JSON.stringify(imageDtos))
            
            // 添加图片文件 - 确保文件被添加到formData中
            if (imageList.value.length > 0) {
              imageList.value.forEach(img => {
                if (img.raw) {
                  // 确保使用正确的参数名 'files'
                  formData.append('files', img.raw)
                }
              })
            } else {
              console.warn('没有选择图片文件')
            }
            
            // 发送添加请求
            const res = await addVenue(formData)
            if (res.code === 1) {
              ElMessage.success('添加成功')
              dialogVisible.value = false
              getList() // 刷新列表
            } else {
              ElMessage.error(res.message || '添加失败')
            }
          } else {
            // 编辑场馆 - id已存在
            formData.append('id', form.value.id)
            
            // 准备所有图片信息（现有和新的）放在一个数组中
            const allImageDtos = []
            
            imageList.value.forEach((img, index) => {
              allImageDtos.push({
                id: img.isExisting ? img.id : null,  // 现有图片保留id，新图片id为null
                imageUrl: img.isExisting ? img.url : "",  // 现有图片保留URL，新图片URL为空
                sortOrder: index + 1,
                isDetail: "0"  // 场馆没有详情图，使用字符串"0"
              })
            })
            
            // 将所有图片信息转为JSON字符串
            formData.append('imageDtosJson', JSON.stringify(allImageDtos))
            
            // 只添加新图片文件 - 确保文件被添加到formData
            const newImages = imageList.value.filter(img => !img.isExisting)
            
            if (newImages.length > 0) {
              newImages.forEach(img => {
                if (img.raw) {
                  // 确保使用正确的参数名 'files'
                  formData.append('files', img.raw)
                }
              })
            }
            
            // 发送更新请求
            const res = await updateVenue(formData)
            if (res.code === 1) {
              ElMessage.success('更新成功')
              dialogVisible.value = false
              getList() // 刷新列表
            } else {
              ElMessage.error(res.message || '更新失败')
            }
          }
          
          // 调试输出，检查formData内容
          console.log('FormData内容:')
          for(let pair of formData.entries()) {
            console.log(pair[0]+ ': ' + pair[1]); 
          }
          
        } catch (error) {
          console.error('提交表单出错:', error)
          ElMessage.error('提交失败')
        } finally {
          loading.value = false
        }
      }
    })
  } catch (error) {
    console.error('表单验证出错:', error)
    ElMessage.error('表单验证失败')
  }
}

// 删除场馆
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除场馆"${row.name}"吗？删除后将无法恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteVenue({ id: row.id })
      if (res.code === 1) {
        ElMessage.success('删除成功')
        getList() // 刷新列表
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除场馆出错:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消删除操作
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

// 对话框关闭后清理资源
watch(dialogVisible, (newVal) => {
  if (!newVal) {
    // 对话框关闭，清理blob URL
    imageList.value.forEach(img => {
      if (img.url && img.url.startsWith('blob:')) {
        URL.revokeObjectURL(img.url)
      }
    })
    
    // 重置表单
    if (formRef.value) {
      formRef.value.resetFields()
    }
    imageList.value = []
    
    // 销毁Sortable实例
    if (imageSortable) {
      imageSortable.destroy()
      imageSortable = null
    }
  }
})

// 详情对话框相关
const detailDialogVisible = ref(false)
const detailInfo = ref({})

// 查看详情 - 直接使用列表数据
const handleViewDetail = (row) => {
  detailInfo.value = row
  detailDialogVisible.value = true
}

onMounted(async () => {
  try {
    await getTagList()  // 先获取标签
    await getList()     // 再获取场馆列表
  } catch (error) {
    console.error('初始化数据失败:', error)
    ElMessage.error('获取数据失败')
  }
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
  
  .venue-form {
    .image-uploader-container {
      .image-list {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;
        margin-bottom: 10px;
        
        .image-item {
          position: relative;
          width: 150px;
          height: 150px;
          border: 1px solid #dcdfe6;
          border-radius: 6px;
          overflow: hidden;
          cursor: move; /* 添加手型光标，表示可拖动 */
          
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
            flex-direction: column;
            gap: 5px;
            
            .delete-btn {
              padding: 6px;
              line-height: 1;
            }
          }
          
          &.is-cover {
            border: 2px solid #67c23a;  // 封面图使用绿色边框突出显示
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
          border: 1px dashed #dcdfe6;
          border-radius: 6px;
          cursor: pointer;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          
          &:hover {
            border-color: #409eff;
          }
          
          .uploader-icon {
            font-size: 28px;
            color: #8c939d;
            margin-bottom: 8px;
          }
          
          .upload-text {
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
    
    .tags-container {
      display: flex;
      align-items: center;
    }
  }
  
  .tag-item {
    margin-right: 5px;
    margin-bottom: 5px;
  }
}

/* 拖拽相关样式 */
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

.venue-detail {
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
      display: flex;
      align-items: flex-start;
      
      .label {
        display: inline-block;
        width: 100px;
        font-weight: bold;
        color: #606266;
        flex-shrink: 0;
      }
      
      .tag-item {
        margin-right: 8px;
        margin-bottom: 5px;
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
        width: 180px;
        height: 180px;
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
    
    .no-image {
      padding: 30px;
      text-align: center;
      color: #909399;
      background-color: #f8f8f8;
      border-radius: 4px;
    }
  }
}

:deep(.el-image-viewer__wrapper) {
  z-index: 3000 !important;
}

:deep(.el-table .el-table__fixed-right) {
  z-index: 1;
}
</style> 
