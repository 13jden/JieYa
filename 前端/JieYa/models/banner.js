/**
 * Banner数据模型
 */
export default class Banner {
  constructor(data = {}) {
    this.bannerId = data.bannerId || 0;
    this.image = data.image || '';
    this.type = data.type || 0; // 1-场地 2-商品 3-笔记
    this.on = data.on || 0;
    this.text = data.text || '';
    this.contentId = data.contentId || null;
    this.sort = data.sort || 0;
  }
  
  /**
   * 获取跳转URL
   * @returns {string} 跳转URL
   */
  getJumpUrl() {
    switch (this.type) {
      case 1: // 场地
        return this.contentId ? `/pages/venue/venue-detail?id=${this.contentId}` : '/pages/venue/venue';
      case 2: // 商品
        return this.contentId ? `/pages/prop/prop-detail?id=${this.contentId}` : '/pages/prop/prop';
      case 3: // 笔记
        return this.contentId ? `/pages/find/find-detail?id=${this.contentId}` : '/pages/find/find';
      default:
        return '';
    }
  }
  
  /**
   * 获取类型文本
   * @returns {string} 类型文本
   */
  getTypeText() {
    switch (this.type) {
      case 1:
        return '场地';
      case 2:
        return '商品';
      case 3:
        return '笔记';
      default:
        return '未知';
    }
  }
} 