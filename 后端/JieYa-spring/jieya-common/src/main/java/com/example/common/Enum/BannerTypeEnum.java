package com.example.common.Enum;

import lombok.Getter;

/**
 * Banner类型枚举
 */
@Getter
public enum BannerTypeEnum {

    PLACE(1, "场地"),
    PRODUCT(2, "商品"),
    NOTE(3, "笔记");

    private final Integer code;
    private final String desc;

    BannerTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取描述
     * @param code 代码
     * @return 描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BannerTypeEnum item : BannerTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item.getDesc();
            }
        }
        return null;
    }
    
    /**
     * 检查code是否有效
     * @param code 代码
     * @return 是否有效
     */
    public static boolean isValid(Integer code) {
        if (code == null) {
            return false;
        }
        for (BannerTypeEnum item : BannerTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
} 