package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.admin.mapper.VenueTagMapper;
import com.example.common.pojo.VenueTag;
import com.example.admin.service.VenueTagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@Service
public class VenueTagServiceImpl extends ServiceImpl<VenueTagMapper, VenueTag> implements VenueTagService {

    @Autowired
    private VenueTagMapper venueTagMapper;

    @Override
    public boolean existsByName(String tagName) {
        QueryWrapper<VenueTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tag_name", tagName);
        return baseMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean isTagInUse(Integer tagId) {
        QueryWrapper<VenueTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tag_id", tagId);
        return venueTagMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    @Transactional
    public void saveVenueTag(Integer venueId, Integer[] tagIds) {
        List<VenueTag> relations = new ArrayList<>();
        
        for (Integer tagId : tagIds) {
            VenueTag relation = new VenueTag();
            relation.setId(tagId);
            relations.add(relation);
        }
        
        if (!relations.isEmpty()) {
            for (VenueTag tag : relations) {
                venueTagMapper.insert(tag);
            }
        }
    }
}
