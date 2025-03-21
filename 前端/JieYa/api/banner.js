/**
 * Banner API 服务
 */
import request from './request';

/**
 * 获取Banner列表
 * @returns {Promise} Banner列表数据
 */
export function getBannerList() {
  return request({
    url: '/banner/list',
    method: 'GET'
  });
} 