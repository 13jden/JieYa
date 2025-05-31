package com.example.wx.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.WxDto.NoteDto;
import com.example.common.pojo.Node;
import com.baomidou.mybatisplus.extension.service.IService;
import org.aspectj.weaver.ast.Not;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
public interface NodeService extends IService<Node> {

    Page<NoteDto> getList(String userId,int pageNum, int pageSize);

    NoteDto getDetail(String userId,String id);
    
    Page<NoteDto> searchByKeyword(String userId, String keyword, int pageNum, int pageSize);
}
