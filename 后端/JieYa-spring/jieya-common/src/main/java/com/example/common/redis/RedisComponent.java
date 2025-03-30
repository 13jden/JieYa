package com.example.common.redis;
import com.example.common.WxDto.PropRentalDto;
import com.example.common.WxDto.VenueBookingDto;
import com.example.common.adminDto.PropDto;
import com.example.common.adminDto.VenueDto;
import com.example.common.constants.Constants;
import com.example.common.WxDto.TokenUserInfoDto;
import com.example.common.pojo.Banner;
import com.example.common.pojo.Category;
import com.example.common.pojo.Message;
import com.example.common.pojo.Prop;
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

        /**
     * 获取场地信息
     */
    public VenueDto getVenue(Integer venueId) {
        String key = Constants.REDIS_KEY_VENUE + venueId;
        VenueDto value = (VenueDto) redisUtils.get(key);
        if (value != null) {
            return value;
        }
        return null;
    }

    /**
     * 保存场地信息
     */
    public void setVenue(VenueDto venueDto) {
        String key = Constants.REDIS_KEY_VENUE + venueDto.getId();
        redisUtils.setex(key,venueDto,Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);
    }

    /**
     * 删除场地信息
     */
    public void deleteVenue(Integer venueId) {
        String key = Constants.REDIS_KEY_VENUE + venueId;
        redisUtils.delete(key);
    }

    /**
     * 获取场地列表
     */
    public List<VenueDto> getVenueList() {
        String key = Constants.REDIS_KEY_VENUE_LIST;
        List<VenueDto> value = (List<VenueDto>) redisUtils.get(key);
        if (value != null) {
            return value;
        }
        return null;
    }

    /**
     * 保存场地列表
     */
    public void setVenueList(List<VenueDto> venueDtoList) {
        String key = Constants.REDIS_KEY_VENUE_LIST;
        redisUtils.setex(key,venueDtoList,Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);
    }

    /**
     * 删除场地列表
     */
    public void deleteVenueList() {
        String key = Constants.REDIS_KEY_VENUE_LIST;
        redisUtils.delete(key);
    }

    /**
     * 获取道具信息
     */
    public PropDto getProp(Integer propId) {
        String key = Constants.REDIS_KEY_PROP + propId;
        PropDto value = (PropDto) redisUtils.get(key);
        if (value != null) {
            return value;
        }
        return null;
    }

    /**
     * 保存道具信息
     */
    public void setProp(PropDto propDto) {
        String key = Constants.REDIS_KEY_PROP + propDto.getId();
        redisUtils.setex(key,propDto,Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);
    }

    /**
     * 删除道具信息
     */
    public void deleteProp(Integer propId) {
        String key = Constants.REDIS_KEY_PROP + propId;
        redisUtils.delete(key);
    }

    /**
     * 获取道具列表
     */
    public List<PropDto> getPropList() {
        String key = Constants.REDIS_KEY_PROP_LIST;
        List<PropDto> value = (List<PropDto>) redisUtils.get(key);
        if (value != null) {
            return value;
        }
        return null;
    }

    /**
     * 保存道具列表
     */
    public void setPropList(List<PropDto> propDtoList) {
        String key = Constants.REDIS_KEY_PROP_LIST;
        redisUtils.setex(key,propDtoList,Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);
    }

    /**
     * 删除道具列表
     */
    public void deletePropList() {
        String key = Constants.REDIS_KEY_PROP_LIST;
        redisUtils.delete(key);
    }

    /**
     * 获取用户预约历史
     */
    public List<VenueBookingDto> getUserBookingHistory(String userId) {
        String key = Constants.REDIS_KEY_USER_BOOKING_HISTORY + userId;
        List<VenueBookingDto> value = (List<VenueBookingDto>) redisUtils.get(key);
        return value;
    }

    /**
     * 保存用户预约历史
     */
    public void setUserBookingHistory(String userId, List<VenueBookingDto> bookingHistory) {
        String key = Constants.REDIS_KEY_USER_BOOKING_HISTORY + userId;
        redisUtils.setex(key, bookingHistory, Constants.REDIS_KEY_EXPIRES_ONE_DAY);
    }

    /**
     * 删除用户预约历史
     */
    public void deleteUserBookingHistory(String userId) {
        String key = Constants.REDIS_KEY_USER_BOOKING_HISTORY + userId;
        redisUtils.delete(key);
    }

    /**
     * 获取用户租借历史
     */
    public List<PropRentalDto> getUserRentalHistory(String userId) {
        String key = Constants.REDIS_KEY_USER_RENTAL_HISTORY + userId;
        List<PropRentalDto> value = (List<PropRentalDto>) redisUtils.get(key);
        return value;
    }

    /**
     * 保存用户租借历史
     */
    public void setUserRentalHistory(String userId, List<PropRentalDto> rentalHistory) {
        String key = Constants.REDIS_KEY_USER_RENTAL_HISTORY + userId;
        redisUtils.setex(key, rentalHistory, Constants.REDIS_KEY_EXPIRES_ONE_DAY);
    }

    /**
     * 删除用户租借历史
     */
    public void deleteUserRentalHistory(String userId) {
        String key = Constants.REDIS_KEY_USER_RENTAL_HISTORY + userId;
        redisUtils.delete(key);
    }

    /**
     * 从队列中获取并移除一条消息
     * 使用POP操作，获取后自动从队列中删除
     */
    public Message getMessage() {
        // 使用rPOP操作从队列中获取并移除消息
        Object value = redisUtils.rpop(Constants.MESSAGE_QUEUE_KEY);
        if (value != null) {
            return (Message) value;
        }
        return null;
    }

    /**
     * 将消息添加到队列
     * 使用lPUSH操作
     */
    public void setMessage(Message message) {
        redisUtils.lpush(Constants.MESSAGE_QUEUE_KEY, message,Constants.REDIS_KEY_EXPIRES_TIME);
    }
}
