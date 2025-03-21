package com.example.common.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("banner")
public class Banner implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "banner_id", type = IdType.AUTO)
    private Integer bannerId;

    private String image;

    /**
     * 1为场地，2为商品，3为笔记
     */
    private Integer type;

    /**
     * 是否启用，1是，0否
     */
    @TableField(value = "`on`")
    private Integer on;

    /**
     * 标题（可选）
     */
    private String text;

    /**
     * 跳转id（如果没有则跳转到整体页面，比如type等于1跳转到场地页面）
     */
    @TableField("content_id")
    private Integer contentId;

    /**
     * 排序号
     */
    private Integer sort;


}
