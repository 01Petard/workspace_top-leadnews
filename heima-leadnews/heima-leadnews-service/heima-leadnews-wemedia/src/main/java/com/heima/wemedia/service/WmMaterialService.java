package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

public interface WmMaterialService extends IService<WmMaterial> {
    /**
     * 图片上传
     * @param multipartFile
     * @return
     */
    ResponseResult uploadPicture(MultipartFile multipartFile);

    /**
     * 查询素材列表
     * @param dto
     * @return
     */
    ResponseResult findList(WmMaterialDto dto);

    /**
     * 根据id删除素材
     * @param id
     * @return
     */
    ResponseResult deleteMaterial(Integer id);

    /**
     * 修改素材的收藏状态（收藏、取消收藏）
     * @param id
     * @return
     */
    ResponseResult collectMaterial(Integer id);
}