package com.example.common.adminDto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;
@Data
public class AdminMessageDto {

    private String user;

    private String userName;

    private String avatarUrl;

    private String toUser;

    private String content;


    private String type;

    private Date postTime;

    private String FileUrl;

    private int notReadCount;

}
