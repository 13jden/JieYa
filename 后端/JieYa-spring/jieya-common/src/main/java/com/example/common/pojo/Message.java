package com.example.common.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2025-03-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * user_id为空则为系统消息
     */
    private String user;

    /**
     * user_id/admin(admin是发给管理端的消息)
     */
    private String toUser;

    private String content;

    /**
     * 消息类型：
发给admin:用户预约，用户投稿，用户申诉，用户租借，用户注册，用户消息（userid不为空）
发给用户：审核消息，租借消息（租借成功，归还成功，归还提醒），预约消息（预约成功，签到成功，签到提醒，取消通知）账号消息（违规提醒等），好友消息（user不为空）
user不为空的是用户消息，为空则为系统消息
     */
    private String type;

    private Date time;

    /**
     * 图片/视频
     */
    @TableField("FileUrl")
    private String FileUrl;

    private int status;


}
