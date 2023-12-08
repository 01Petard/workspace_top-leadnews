package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.wemedia.service.WmNewsMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;

    @Autowired
    private WmNewsMaterialService wmNewsMaterialService;


    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        return wmMaterialService.uploadPicture(multipartFile);
    }

    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmMaterialDto dto) {
        return wmMaterialService.findList(dto);
    }

    /**
     * 根据素材id删除素材
     * @param id
     * @return
     */
    @GetMapping("/del_picture/{id}")
    public ResponseResult deleteOne(@PathVariable(value = "id") Integer id) {
        return wmMaterialService.deleteMaterial(id);
    }

    /**
     * 收藏素材
     * @param id
     * @return
     */
    @GetMapping("/collect/{id}")
    public ResponseResult collectMaterial(@PathVariable(value = "id") Integer id) {
        return wmMaterialService.collectMaterial(id);

    }

    /**
     * 取消收藏素材
     * @param id
     * @return
     */
    @GetMapping("/cancel_collect/{id}")
    public ResponseResult cancel_collectMaterial(@PathVariable(value = "id") Integer id) {
        return wmMaterialService.collectMaterial(id);
    }



}
