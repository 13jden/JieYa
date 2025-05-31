package com.example.common.WxDto;

import lombok.Data;

import java.util.Date;

@Data
public class CommentDto {

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

    private int ChildrenCount;

    private String userName;

    private String avatar;

    private String replyName;//子评论中如果回复别入才有，只回复父评论为空

}
