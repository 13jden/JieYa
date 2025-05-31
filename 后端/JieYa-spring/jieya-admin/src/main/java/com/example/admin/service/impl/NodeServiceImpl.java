package com.example.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.pojo.Node;
import com.example.admin.mapper.NodeMapper;
import com.example.admin.service.NodeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@Service
public class NodeServiceImpl extends ServiceImpl<NodeMapper, Node> implements NodeService {

}
