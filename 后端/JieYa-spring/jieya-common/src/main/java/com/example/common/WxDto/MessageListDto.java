package com.example.common.WxDto;

import lombok.Data;

import java.util.Date;

@Data
public class MessageListDto {
    private String type;
    private String userName;
    private String userId;
    private String avatarUrl;
    private String lastContent;
    private Date time;
    private String FileUrl;
    private int notReadCount;
}
