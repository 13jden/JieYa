import request from '@/utils/request'

export function getCategoryList() {
  return request({
    url: '/category/getList',
    method: 'get'
  })
}

export function addCategory(data) {
  return request({
    url: '/category/add',
    method: 'post',
    params: {
      categoryName: data.name,
      categoryCode: data.code
    }
  })
}

export function updateCategory(data) {
  return request({
    url: '/category/update',
    method: 'post',
    params: {
      id: data.id,
      categoryName: data.name,
      categoryCode: data.code
    }
  })
}

export function deleteCategory(id) {
  return request({
    url: '/category/delete',
    method: 'post',
    params: { id }
  })
}

export function updateCategorySort(sortList) {
  return request({
    url: '/category/sort',
    method: 'post',
    data: sortList
  })
} 