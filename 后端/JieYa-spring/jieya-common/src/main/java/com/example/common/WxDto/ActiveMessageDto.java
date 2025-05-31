package com.example.common.WxDto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Data
public class ActiveMessageDto {
    private Long id;

    /**
     * user_id为空则为系统消息
     */
    private List<String> userName;

//    private HashMap<String,String> user;

    private String toUser;

    private String content;

    private String type;

    private Date postTime;

    private String noteId;

    private String FileUrl;

    private int status;

    private int userCount;

    private long thankMessageId;//关注自动添加对方的感谢消息

    private String commentId;
}
