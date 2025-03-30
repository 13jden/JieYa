package com.example.wx.service;

import com.example.common.common.Result;
import com.example.common.pojo.PropRental;
import com.example.common.WxDto.PropRentalDto;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
public interface PropRentalService extends IService<PropRental> {

    /**
     * 创建租借记录
     * @param propId 道具ID
     * @param userId 用户ID
     * @return 租借记录ID
     */
    String  createRental(Integer propId, String userId);
    
    /**
     * 完成道具租借（归还）
     * @param rentalId 租借记录ID
     * @param userId 用户ID
     * @return 结果
     */
    Result completePropRental(String rentalId, String userId);
    
    /**
     * 获取用户的租借历史
     * @param userId 用户ID
     * @return 租借历史列表（包含道具信息）
     */
    List<PropRentalDto> getRentalHistoryByUserId(String userId);
    
    /**
     * 获取租借详情
     * @param rentalId 租借记录ID
     * @param userId 用户ID
     * @return 租借详情
     */
    Result getRentalDetail(Integer rentalId, String userId);

    @Transactional(rollbackFor = Exception.class)
    String rentProp(String userId, Integer propId, Date startDate, Date endDate);

    @Transactional(rollbackFor = Exception.class)
    boolean returnProp(String rentalId, String userId);
}
