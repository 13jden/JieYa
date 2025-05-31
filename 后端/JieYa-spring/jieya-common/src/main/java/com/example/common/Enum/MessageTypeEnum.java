package com.example.common.Enum;

/**
 * 消息类型枚举
 * 定义了系统中不同类型的消息及其流向
 */
public enum MessageTypeEnum {
    // 管理员发送给用户的消息
    ADMIN_USER("ADMIN-USER", "管理员to用户"),
    
    // 用户发送的普通消息
    USER("USER", "用户to用户"),
    
    // 订单相关消息(发送给管理员)
    ORDER("ORDER", "订单to管理员"),
    
    // 系统消息(发送给管理员)
    SYSTEM("SYSTEM", "系统to管理员"),

    
    // 用户之间的消息
    USER_USER("USER-USER", "用户to用户"),
    
    // 用户发送的订单相关消息
    USER_ORDER("USER-ORDER", "用户to订单"),
    
    // 系统发送给用户的消息
    SYSTEM_USER("SYSTEM-USER", "系统to用户"),
    
    // 其他类型消息(发送给用户)
    OTHER_USER("OTHER-USER", "其他to用户"),
    
    // 订单相关消息(发送给用户)
    ORDER_USER("ORDER-USER", "订单to用户");
    
    // 消息类型代码
    private final String code;
    
    // 消息类型描述(备注用)
    private final String description;
    
    /**
     * 构造函数
     * @param code 消息类型代码
     * @param description 消息类型描述
     */
    MessageTypeEnum(String code, String description) {
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
    public static MessageTypeEnum getByCode(String code) {
        for (MessageTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}

