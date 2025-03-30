package com.example.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.WxDto.PropRentalDto;
import com.example.common.pojo.PropRental;
import com.baomidou.mybatisplus.extension.service.IService;

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

    // 分页查询租借记录
    Page<PropRentalDto> pageRentals(Integer current, Integer size, String userId, Integer propId, String status);

    // 获取租借详情
    PropRentalDto getRentalDetailById(String id);

    // 根据用户ID查询租借记录
    List<PropRentalDto> getRentalsByUserId(String userId);

    // 根据道具ID查询租借记录
    List<PropRentalDto> getRentalsByPropId(Integer propId);

    // 更新租借状态
    boolean updateRentalStatus(String id, String status);
}
