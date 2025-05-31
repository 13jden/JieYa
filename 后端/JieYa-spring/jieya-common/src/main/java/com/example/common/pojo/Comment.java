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
 * @since 2025-04-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String content;

    /**
     * 没有就是父评论，有就是子评论
     */
    private Integer  parentId;

    private String fileUrl;

    private String userId;

    /**
     * 父评论的回复数量
     */
    private Integer commentCount;

    private Integer likeCount;

    private Integer noLikeCount;

    private Date time;

    private String toUserId;

    private Integer isTop;

    private String noteId;

}
