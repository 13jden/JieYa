package com.example.admin.service;

import com.example.common.adminDto.PropDto;
import com.example.common.pojo.Prop;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
public interface PropService extends IService<Prop> {
    
    /**
     * 获取道具及其图片信息
     */
    PropDto getPropWithImagesById(Integer id);
    
    /**
     * 获取所有道具及其图片列表
     */
    List<PropDto> listPropsWithImages();
    
    /**
     * 删除道具及其图片
     */
    boolean deletePropWithImages(Integer id);
    
    /**
     * 更新道具及其图片
     */
    PropDto updatePropWithImages(Integer id, String propName, String description,
                                BigDecimal price, Boolean available,
                                String imageDtosJson, MultipartFile[] newFiles) throws Exception;
}
