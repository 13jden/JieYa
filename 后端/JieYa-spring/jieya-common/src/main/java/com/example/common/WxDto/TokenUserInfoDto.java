package com.example.common.WxDto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenUserInfoDto implements Serializable {

        private String userId;
        private String nickName;
        private String avatar;
        private Long expireAt;
        private String school;

        private String personIntruduction;

        private Boolean sex;

        private String birthday;

        private int likesCount;

        private String token;

        private Integer fansCount;

        private Integer focusCount;

}
