import request from '@/utils/request'

export function getBannerList() {
  return request({
    url: '/banner/getList',
    method: 'get'
  })
}

export function addBanner(data) {
  return request({
    url: '/banner/add',
    method: 'post',
    params: {
      image: data.image,
      type: data.type,
      text: data.text,
      on: data.on,
      contentId: data.contentId
    }
  })
}

export function updateBanner(data) {
  return request({
    url: '/banner/update',
    method: 'post',
    params: {
      bannerId: data.bannerId,
      image: data.image,
      type: data.type,
      on: data.on,
      text: data.text,
      contentId: data.contentId
    }
  })
}

export function deleteBanner(bannerId) {
  return request({
    url: '/banner/delete',
    method: 'post',
    params: { bannerId }
  })
}

export function updateBannerSort(sortList) {
  return request({
    url: '/banner/sort',
    method: 'post',
    data: sortList.map(item => ({
      bannerId: item.bannerId,
      sort: item.sort
    }))
  })
} 