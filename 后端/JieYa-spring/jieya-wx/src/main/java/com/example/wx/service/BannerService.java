package com.example.wx.service;

import com.example.common.pojo.Banner;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
public interface BannerService extends IService<Banner> {

    List<Banner> getList();
}
