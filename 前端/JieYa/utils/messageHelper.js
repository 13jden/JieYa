// 将消息列表转换为好友列表展示格式
export function mapFriendList(messageList = []) {
  // 创建一个Map用于按用户分组
  const userMap = new Map();
  
  // 处理每条消息
  messageList.forEach(msg => {
    // 确定用户ID (不是自己的那一方)
    const userId = msg.userId;
    
    // 如果Map中没有这个用户，添加一个新条目
    if (!userMap.has(userId)) {
      userMap.set(userId, {
        userId: userId,
        username: msg.username || '未知用户',
        avatarUrl: msg.avatarUrl || '/static/default-avatar.png',
        message: msg.lastMessage || '',
        time: msg.lastMessageTime || new Date(),
        unreadCount: msg.unreadCount || 0
      });
    } else {
      // 如果已存在，更新最新消息和未读数
      const user = userMap.get(userId);
      
      // 只有当消息更新时间更晚时才更新
      const msgTime = new Date(msg.lastMessageTime || 0);
      const currentTime = new Date(user.time || 0);
      
      if (msgTime > currentTime) {
        user.message = msg.lastMessage || '';
        user.time = msg.lastMessageTime;
      }
      
      // 累加未读数
      user.unreadCount = (user.unreadCount || 0) + (msg.unreadCount || 0);
    }
  });
  
  // 将Map转换为数组，并按最后消息时间排序
  return Array.from(userMap.values())
    .sort((a, b) => {
      const timeA = new Date(a.time || 0);
      const timeB = new Date(b.time || 0);
      return timeB - timeA; // 降序排列，最新的在前面
    });
}

// 格式化消息时间为友好显示
export function formatMessageTime(time) {
  if (!time) return '';
  
  const msgDate = new Date(time);
  const now = new Date();
  
  // 如果是今天的消息
  if (msgDate.toDateString() === now.toDateString()) {
    const hours = msgDate.getHours().toString().padStart(2, '0');
    const minutes = msgDate.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }
  
  // 如果是昨天的消息
  const yesterday = new Date();
  yesterday.setDate(now.getDate() - 1);
  if (msgDate.toDateString() === yesterday.toDateString()) {
    return '昨天';
  }
  
  // 如果是今年的消息
  if (msgDate.getFullYear() === now.getFullYear()) {
    const month = (msgDate.getMonth() + 1).toString().padStart(2, '0');
    const day = msgDate.getDate().toString().padStart(2, '0');
    return `${month}-${day}`;
  }
  
  // 其他消息
  const year = msgDate.getFullYear();
  const month = (msgDate.getMonth() + 1).toString().padStart(2, '0');
  const day = msgDate.getDate().toString().padStart(2, '0');
  return `${year}-${month}-${day}`;
} 