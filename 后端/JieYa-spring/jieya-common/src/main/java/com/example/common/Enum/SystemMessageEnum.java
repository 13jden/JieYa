package com.example.common.Enum;

/**
 * 系统消息类型枚举
 * 定义了系统中不同类型的系统消息
 */
public enum SystemMessageEnum {
    // 订单相关消息
    ORDER_CREATED("ORDER-CREATED", "订单创建提醒"),
    ORDER_CANCELED("ORDER-CANCELED", "订单取消提醒"),
    ORDER_COMPLETED("ORDER-COMPLETED", "订单完成提醒"),
    ORDER_PAID("ORDER-PAID", "订单支付提醒"),
    ORDER_REFUND("ORDER-REFUND", "订单退款提醒"),
    
    // 违规警告相关消息
    VIOLATION_WARNING("VIOLATION-WARNING", "违规警告"),
    VIOLATION_PENALTY("VIOLATION-PENALTY", "违规处罚"),
    ACCOUNT_FROZEN("ACCOUNT-FROZEN", "账号冻结通知"),
    
    // 物品归还相关消息
    RETURN_REMINDER("RETURN-REMINDER", "归还提醒"),
    RETURN_OVERDUE("RETURN-OVERDUE", "归还逾期"),
    RETURN_COMPLETED("RETURN-COMPLETED", "归还完成"),
    
    // 系统通知消息
    SYSTEM_MAINTENANCE("SYSTEM-MAINTENANCE", "系统维护通知"),
    SYSTEM_UPDATE("SYSTEM-UPDATE", "系统更新通知"),
    SERVICE_ANNOUNCEMENT("SERVICE-ANNOUNCEMENT", "服务公告"),
    
    // 账户相关消息
    ACCOUNT_REGISTERED("ACCOUNT-REGISTERED", "账号注册通知"),
    ACCOUNT_ACTIVATED("ACCOUNT-ACTIVATED", "账号激活通知"),
    PASSWORD_CHANGED("PASSWORD-CHANGED", "密码修改通知"),
    PROFILE_UPDATED("PROFILE-UPDATED", "个人资料更新通知"),
    
    // 活动相关消息
    PROMOTION_ANNOUNCE("PROMOTION-ANNOUNCE", "优惠活动通知"),
    NEW_SERVICE("NEW-SERVICE", "新服务上线通知"),
    INVITATION_REWARD("INVITATION-REWARD", "邀请奖励通知");
    
    // 消息类型代码
    private final String code;
    
    // 消息类型描述
    private final String description;
    
    /**
     * 构造函数
     * @param code 消息类型代码
     * @param description 消息类型描述
     */
    SystemMessageEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取消息类型代码
     * @return 消息类型代码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取消息类型描述
     * @return 消息类型描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据code获取对应的枚举值
     * @param code 消息类型代码
     * @return 对应的枚举值，若不存在则返回null
     */
    public static SystemMessageEnum getByCode(String code) {
        for (SystemMessageEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
