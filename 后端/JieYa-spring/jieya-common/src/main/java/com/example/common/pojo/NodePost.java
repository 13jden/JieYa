package com.example.common.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2025-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class NodePost implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 笔记id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 封面地址
     */
    private String coverImage;

    /**
     * 审核状态，0待审核，1审核成功，-1审核不通过
     */
    private Integer status;

    /**
     * 标签数组
     */
    private String tags;

    /**
     * 上传时间
     */
    private Date postTime;

    /**
     * 发布时间
     */
    private Date auditTime;

    /**
     * 分类id
     */
    private Integer category;

    private String userId;


}
