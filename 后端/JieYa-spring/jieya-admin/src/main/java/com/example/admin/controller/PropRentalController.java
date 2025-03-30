package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.service.PropRentalService;
import com.example.common.WxDto.PropRentalDto;
import com.example.common.common.Result;
import com.example.common.pojo.PropRental;
import com.example.common.redis.RedisComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  道具租借前端控制器
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
    
    @Autowired
    private RedisComponent redisComponent;
    
    /**
     * 分页查询所有租借记录
     */
    @GetMapping("/page")
    public Result page(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "propId", required = false) Integer propId,
            @RequestParam(value = "status", required = false) String status) {
        
        try {
            Page<PropRentalDto> page = propRentalService.pageRentals(current, size, userId, propId, status);
            return Result.success(page);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询租借记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询租借详情
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable String id) {
        try {
            PropRentalDto rentalDto = propRentalService.getRentalDetailById(id);
            if (rentalDto == null) {
                return Result.error("租借记录不存在");
            }
            return Result.success(rentalDto);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询租借详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID查询租借记录
     */
    @GetMapping("/user/{userId}")
    public Result getByUserId(@PathVariable String userId) {
        try {
            List<PropRentalDto> rentals = propRentalService.getRentalsByUserId(userId);
            return Result.success(rentals);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询用户租借记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据道具ID查询租借记录
     */
    @GetMapping("/prop/{propId}")
    public Result getByPropId(@PathVariable Integer propId) {
        try {
            List<PropRentalDto> rentals = propRentalService.getRentalsByPropId(propId);
            return Result.success(rentals);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询道具租借记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新租借状态
     */
    @PostMapping("/update-status")
    public Result updateStatus(
            @RequestParam String id, 
            @RequestParam String status) {
        
        try {
            boolean success = propRentalService.updateRentalStatus(id, status);
            if (success) {
                // 更新成功后，删除用户租借历史缓存
                PropRental rental = propRentalService.getById(id);
                if (rental != null) {
                    redisComponent.deleteUserRentalHistory(rental.getUserId());
                }
                return Result.success("更新状态成功");
            } else {
                return Result.error("更新状态失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新租借状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除租借记录
     */
    @PostMapping("/delete/{id}")
    public Result delete(@PathVariable String id) {
        try {
            // 先获取用户ID，以便删除缓存
            PropRental rental = propRentalService.getById(id);
            if (rental == null) {
                return Result.error("租借记录不存在");
            }
            
            boolean success = propRentalService.removeById(id);
            if (success) {
                // 删除成功后，删除用户租借历史缓存
                redisComponent.deleteUserRentalHistory(rental.getUserId());
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除租借记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量删除租借记录
     */
    @PostMapping("/batch-delete")
    public Result batchDelete(@RequestBody List<String> ids) {
        try {
            // 获取所有相关用户ID
            QueryWrapper<PropRental> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id", ids);
            List<PropRental> rentals = propRentalService.list(queryWrapper);
            
            boolean success = propRentalService.removeByIds(ids);
            if (success) {
                // 删除成功后，删除所有相关用户的租借历史缓存
                for (PropRental rental : rentals) {
                    redisComponent.deleteUserRentalHistory(rental.getUserId());
                }
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除租借记录失败：" + e.getMessage());
        }
    }
}

