package com.example.common.redis;
import com.example.common.constants.Constants;
import com.example.common.WxDto.TokenUserInfoDto;
import com.example.common.pojo.Banner;
import com.example.common.pojo.Category;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    public String saveCheckCode(String code){
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey,code, Constants.REDIS_KEY_EXPIRES_TIME);
        return checkCodeKey;
    }

    public String getCheckCode(String checkCodeKey){
        return (String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }
    public TokenUserInfoDto getTokenInfo(String token){
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_TOKEN_WX + token);
    }
    public void setAdminToken(String token) {
        this.redisUtils.setex(Constants.REDIS_KEY_TOKEN_ADMIN + token,"admin",Constants.REDIS_KEY_EXPIRES_ONE_DAY);
    }
    public String getAdminToken(String token){
        return  (String)redisUtils.get(Constants.REDIS_KEY_TOKEN_ADMIN  + token);
    }


    public void saveTokenInfo(String token,TokenUserInfoDto tokenUserInfoDto){
        tokenUserInfoDto.setToken(token);
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WX + token, tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);
    }

    public String saveAdminTokenInfo(String account){
        String token = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_ADMIN+token,account,-1);
        return token;
    }


    public void cleanToken(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_WX + token);
    }
    public void cleanCheckCode(String checkCodeKey){
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey);
    }
    public void cleanAdminToken(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN+token);
    }


    /**
     * 删除分类列表缓存
     */
    public void deleteCategoryList() {
        redisUtils.delete(Constants.REDIS_KEY_CATEGORY_LIST);
    }

    /**
     * 设置分类列表缓存
     * @param categoryList 分类列表
     */
    public void setCategoryList(List<Category> categoryList) {
        if (categoryList != null && !categoryList.isEmpty()) {
            redisUtils.setex(Constants.REDIS_KEY_CATEGORY_LIST, categoryList, Constants.REDIS_KEY_EXPIRES_ONE_DAY);
        }
    }

    /**
     * 获取分类列表缓存
     * @return 分类列表
     */
    @SuppressWarnings("unchecked")
    public List<Category> getCategoryList() {
        return (List<Category>) redisUtils.get(Constants.REDIS_KEY_CATEGORY_LIST);
    }

    public List<Banner> getBannerList() {
        return (List<Banner>) redisUtils.get(Constants.REDIS_KEY_BANNER_LIST);
    }

    public void setBannerList(List<Banner> banners) {
        redisUtils.setex(Constants.REDIS_KEY_BANNER_LIST,banners,Constants.REDIS_KEY_EXPIRES_ONE_DAY);
    }

    public void deleteBannerList() {
        redisUtils.delete(Constants.REDIS_KEY_BANNER_LIST);
    }


}
