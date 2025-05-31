package com.example.common.constants;

public class Constants {

    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{6,18}$";

    public static final Integer REDIS_KEY_EXPIRES_TIME = 60000;

    public static String REDIS_KEY_PRE = "JieYa:";

    public static String REDIS_KEY_CHECK_CODE =REDIS_KEY_PRE + "checkcode:";

    public static final String REDIS_KEY_ACTIVE_MESSAGE = REDIS_KEY_PRE + "userActive:message:";

    public static final Integer LENGTH_10 = 10;

    public static final Integer REDIS_KEY_EXPIRES_ONE_DAY = 60000*60*24;

    public static final String REDIS_KEY_TOKEN_WX = REDIS_KEY_PRE + "token:wx:";

    public static final String TOKEN_WEB =  "token";

    public static final String TOKEN_WX =  "token";


    public static final String TOKEN_ADMIN =  "adminToken";

    public static final String REDIS_KEY_TOKEN_ADMIN = REDIS_KEY_PRE + "token:admin";

    /**
     * Redis 分类列表 key
     */
    public static final String REDIS_KEY_CATEGORY_LIST = "category:list";

    /**
     * Redis Banner列表 key
     */
    public static final String REDIS_KEY_BANNER_LIST = "banner:list";
    
    /**
     * Redis 场地信息 key前缀
     */
    public static final String REDIS_KEY_VENUE = REDIS_KEY_PRE + "venue:";
    
    /**
     * Redis 场地列表 key
     */
    public static final String REDIS_KEY_VENUE_LIST = REDIS_KEY_PRE + "venue:list";
    
    /**
     * Redis 道具信息 key前缀
     */
    public static final String REDIS_KEY_PROP = REDIS_KEY_PRE + "prop:";
    
    /**
     * Redis 道具列表 key
     */
    public static final String REDIS_KEY_PROP_LIST = REDIS_KEY_PRE + "prop:list";

    /**
     * Redis 用户预约历史 key前缀
     */
    public static final String REDIS_KEY_USER_BOOKING_HISTORY = REDIS_KEY_PRE + "user:booking:";

    /**
     * Redis 用户租借历史 key前缀
     */
    public static final String REDIS_KEY_USER_RENTAL_HISTORY = REDIS_KEY_PRE + "user:rental:";

    public static final String MESSAGE_QUEUE_KEY = "message:queue";

    public static final String REDIS_KEY_NOTE_TEMP_IMAGE = REDIS_KEY_PRE +"temp:noteImage:";
}


