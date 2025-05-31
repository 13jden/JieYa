package com.example.common.WxDto;

import lombok.Data;

import java.util.Date;
@Data
public class UserMessageDto {

    private String user;

    private String userName;

    private String avatarUrl;

    private String toUser;

    private String content;

    private String type;

    private Date time;

    private String FileUrl;

    private int notReadCount;
}
