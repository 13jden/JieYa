package com.example.wx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.adminDto.PropDto;
import com.example.common.common.Result;
import com.example.common.constants.Constants;
import com.example.common.pojo.Prop;
import com.example.common.pojo.PropRental;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.StringTools;
import com.example.wx.mapper.PropMapper;
import com.example.wx.mapper.PropRentalMapper;
import com.example.wx.service.PropRentalService;
import com.example.wx.service.PropService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.common.constants.RentalStatus;
import com.example.common.WxDto.PropRentalDto;
import com.example.common.pojo.PropImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@Service
public class PropRentalServiceImpl extends ServiceImpl<PropRentalMapper, PropRental> implements PropRentalService {

    @Autowired
    private PropMapper propMapper;
    
    @Autowired
    private PropService propService;

    @Autowired
    private RedisComponent redisComponent;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createRental(Integer propId, String userId) {
        // 1. 检查道具是否存在且可用
        System.out.println("道具id:"+propId);
        Prop prop = propMapper.selectById(propId);
        if (prop == null) {
            throw new RuntimeException("道具不存在");
        }
        
        if (!prop.getAvailable()) {
            throw new RuntimeException("该道具当前不可租借");
        }
        
        // 2. 创建租借记录
        PropRental rental = new PropRental();
        rental.setId(StringTools.getRandomBumber(Constants.LENGTH_10));
        rental.setPropId(propId);
        rental.setUserId(userId);
        rental.setStartDate(new Date());
        rental.setStatus(RentalStatus.RENTING);
        rental.setCreatedAt(new Date());
        rental.setUpdatedAt(new Date());
        
        // 3. 保存租借记录
        this.save(rental);
        
        // 4. 更新道具状态为不可用
        prop.setAvailable(false);
        propMapper.updateById(prop);
        redisComponent.deleteUserRentalHistory(userId);
        redisComponent.deletePropList();
        return rental.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result completePropRental(String rentalId, String userId) {
        // 1. 检查租借记录是否存在
        PropRental rental = this.getById(rentalId);
        if (rental == null) {
            return Result.error("租借记录不存在");
        }
        
        // 2. 验证用户身份
        if (!rental.getUserId().equals(userId)) {
            return Result.error("无权操作此租借记录");
        }
        
        // 3. 检查租借状态
        if (!"租借中".equals(rental.getStatus())) {
            return Result.error("该道具已归还或已取消");
        }
        
        // 4. 获取道具信息
        Prop prop = propMapper.selectById(rental.getPropId());
        if (prop == null) {
            return Result.error("道具信息不存在");
        }
        
        // 5. 设置归还时间
        Date returnDate = new Date();
        rental.setEndDate(returnDate);
        
        // 6. 计算租借费用
        long durationMillis = returnDate.getTime() - rental.getStartDate().getTime();
        long durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
        
        BigDecimal totalPrice;
        
        if (durationMinutes <= 10) {
            // 10分钟内免费
            totalPrice = BigDecimal.ZERO;
        } else {
            // 计算天数，不足1天按1天计算
            long durationHours = TimeUnit.MILLISECONDS.toHours(durationMillis);
            int days = (int) Math.ceil(durationHours / 24.0);
            
            // 计算总价
            totalPrice = prop.getPrice().multiply(new BigDecimal(days));
        }
        
        // 7. 更新租借记录
        rental.setTotalPrice(totalPrice);
        rental.setStatus(RentalStatus.RETURNED);
        rental.setUpdatedAt(new Date());
        this.updateById(rental);
        
        // 8. 更新道具状态为可用
        prop.setAvailable(true);
        propMapper.updateById(prop);
        
        // 9. 构造返回信息
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rentalId", rental.getId());
        resultMap.put("propId", prop.getId());
        resultMap.put("propName", prop.getName());
        resultMap.put("startDate", rental.getStartDate());
        resultMap.put("endDate", rental.getEndDate());
        resultMap.put("totalPrice", totalPrice);
        redisComponent.deleteUserRentalHistory(userId);
        redisComponent.deletePropList();
        return Result.success(resultMap);
    }

    @Override
    public List<PropRentalDto> getRentalHistoryByUserId(String userId) {
        // 尝试从Redis获取
        List<PropRentalDto> rentalHistory = redisComponent.getUserRentalHistory(userId);
        if (rentalHistory != null) {
            return rentalHistory;
        }
        
        // Redis中不存在，从数据库获取
        QueryWrapper<PropRental> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                  .orderByDesc("created_at");
        
        List<PropRental> rentalList = this.list(queryWrapper);
        
        // 如果没有记录，返回空列表
        if (rentalList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 收集所有道具ID
        List<Integer> propIds = rentalList.stream()
            .map(PropRental::getPropId)
            .collect(Collectors.toList());
        
        // 3. 批量查询道具信息
        QueryWrapper<Prop> propQueryWrapper = new QueryWrapper<>();
        propQueryWrapper.in("id", propIds);
        List<Prop> propList = propMapper.selectList(propQueryWrapper);
        Map<Integer, Prop> propMap = propList.stream()
            .collect(Collectors.toMap(Prop::getId, prop -> prop));
        
        // 4. 获取所有道具的封面图
        Map<Integer, String> propCoverImageMap = propService.getPropCoverImages(propIds);
        
        // 5. 构建DTO列表
        rentalHistory = rentalList.stream().map(rental -> {
            Prop prop = propMap.get(rental.getPropId());
            // 如果找不到对应的道具(可能已删除)，则使用默认值
            String propName = prop != null ? prop.getName() : "未知道具";
            String description = prop != null ? prop.getDescription() : "";
            BigDecimal price = prop != null ? prop.getPrice() : BigDecimal.ZERO;
            String coverImage = propCoverImageMap.getOrDefault(rental.getPropId(), "");
            
            return PropRentalDto.fromPropRental(rental, propName, description, price, coverImage);
        }).collect(Collectors.toList());
        
        // 存入Redis缓存
        redisComponent.setUserRentalHistory(userId, rentalHistory);
        
        return rentalHistory;
    }

    @Override
    public Result getRentalDetail(Integer rentalId, String userId) {
        // 1. 获取租借记录
        PropRental rental = this.getById(rentalId);
        if (rental == null) {
            return Result.error("租借记录不存在");
        }
        
        // 2. 验证用户身份
        if (!rental.getUserId().equals(userId)) {
            return Result.error("无权查看此租借记录");
        }
        
        // 3. 获取道具详情
        PropDto propDto = propService.getPropWithImagesById(rental.getPropId());
        if (propDto == null) {
            return Result.error("道具信息不存在");
        }
        
        // 4. 构造返回信息
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rental", rental);
        resultMap.put("prop", propDto);
        
        return Result.success(resultMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String rentProp(String userId, Integer propId, Date startDate, Date endDate) {
        // 1. 检查道具是否存在且可用
        System.out.println("道具id:"+propId);
        Prop prop = propMapper.selectById(propId);
        if (prop == null) {
            throw new RuntimeException("道具不存在");
        }
        
        if (!prop.getAvailable()) {
            throw new RuntimeException("该道具当前不可租借");
        }
        
        // 2. 创建租借记录
        PropRental rental = new PropRental();
        rental.setId(StringTools.getRandomBumber(Constants.LENGTH_10));
        rental.setPropId(propId);
        rental.setUserId(userId);
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setStatus(RentalStatus.RENTING);
        rental.setCreatedAt(new Date());
        rental.setUpdatedAt(new Date());
        
        // 3. 保存租借记录
        this.save(rental);
        
        // 4. 更新道具状态为不可用
        prop.setAvailable(false);
        propMapper.updateById(prop);
        
        // 租借成功后，删除缓存
        redisComponent.deleteUserRentalHistory(userId);
        
        return rental.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnProp(String rentalId, String userId) {
        // 1. 检查租借记录是否存在
        PropRental rental = this.getById(rentalId);
        if (rental == null) {
            return false;
        }
        
        // 2. 验证用户身份
        if (!rental.getUserId().equals(userId)) {
            return false;
        }
        
        // 3. 检查租借状态
        if (!"租借中".equals(rental.getStatus())) {
            return false;
        }
        
        // 4. 获取道具信息
        Prop prop = propMapper.selectById(rental.getPropId());
        if (prop == null) {
            return false;
        }
        
        // 5. 设置归还时间
        Date returnDate = new Date();
        rental.setEndDate(returnDate);
        
        // 6. 计算租借费用
        long durationMillis = returnDate.getTime() - rental.getStartDate().getTime();
        long durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
        
        BigDecimal totalPrice;
        
        if (durationMinutes <= 10) {
            // 10分钟内免费
            totalPrice = BigDecimal.ZERO;
        } else {
            // 计算天数，不足1天按1天计算
            long durationHours = TimeUnit.MILLISECONDS.toHours(durationMillis);
            int days = (int) Math.ceil(durationHours / 24.0);
            
            // 计算总价
            totalPrice = prop.getPrice().multiply(new BigDecimal(days));
        }
        
        // 7. 更新租借记录
        rental.setTotalPrice(totalPrice);
        rental.setStatus(RentalStatus.RETURNED);
        rental.setUpdatedAt(new Date());
        this.updateById(rental);
        
        // 8. 更新道具状态为可用
        prop.setAvailable(true);
        propMapper.updateById(prop);
        // 归还成功后，删除缓存
        redisComponent.deleteUserRentalHistory(userId);
        redisComponent.deletePropList();
        return true;
    }
}
