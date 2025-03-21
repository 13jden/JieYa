import request from '@/utils/request'

// 获取验证码
export function getCheckCode() {
  return request({
    url: '/checkcode',
    method: 'get'
  })
}

// 管理员登录
export function login(data) {
  const formData = new URLSearchParams()
  formData.append('account', data.account)
  formData.append('password', data.password)
  formData.append('checkCode', data.checkCode)
  formData.append('checkCodeKey', data.checkCodeKey)

  return request({
    url: '/admin/login',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
} 