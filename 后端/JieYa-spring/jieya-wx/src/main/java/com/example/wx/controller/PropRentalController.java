package com.example.wx.controller;

import com.example.common.common.Result;
import com.example.common.utils.JwtUtil;
import com.example.wx.service.PropRentalService;
import com.sun.istack.internal.NotNull;
import jakarta.validation.constraints.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@RestController
@RequestMapping("/prop-rental")
public class PropRentalController {
    
    @Autowired
    private PropRentalService propRentalService;
    
    /**
     * 租借道具
     */
    @RequestMapping("/rent")
    public Result rentProp(@RequestHeader("Authorization") String token,
                           @NotNull Integer propId) {
        try {
            // 从token获取用户ID
            String userId = JwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的认证信息");
            }
            
            // 创建租借记录
            String rentalId = propRentalService.createRental(propId, userId);
            
            return Result.success(rentalId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("租借道具失败: " + e.getMessage());
        }
    }
    
    /**
     * 归还道具
     */
    @PostMapping("/return/{rentalId}")
    public Result returnProp(@PathVariable String  rentalId,
                            @RequestHeader("Authorization") String token) {
        try {
            // 从token获取用户ID
            String userId = JwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的认证信息");
            }
            
            // 归还道具并计算费用
            return propRentalService.completePropRental(rentalId, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("归还道具失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的租借历史
     */
    @GetMapping("/history")
    public Result getRentalHistory(@RequestHeader("Authorization") String token) {
        try {
            // 从token获取用户ID
            String userId = JwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的认证信息");
            }
            
            return Result.success(propRentalService.getRentalHistoryByUserId(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取租借历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取租借详情
     */
    @GetMapping("/detail/{rentalId}")
    public Result getRentalDetail(@PathVariable Integer rentalId,
                                 @RequestHeader("Authorization") String token) {
        try {
            // 从token获取用户ID
            String userId = JwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的认证信息");
            }
            
            return propRentalService.getRentalDetail(rentalId, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取租借详情失败: " + e.getMessage());
        }
    }
}

