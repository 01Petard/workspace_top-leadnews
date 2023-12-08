package com.heima.wemedia.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WemediaConstants;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    WmNewsMaterialMapper wmNewsMaterialMapper;

    /**
     * 图片上传
     * @param multipartFile
     * @return
     */
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {

        //1.检查参数
        if (multipartFile == null || multipartFile.getSize() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.上传图片到minIO中
        String fileName = UUID.randomUUID().toString().replace("-", "");
        //获取文件名+后缀,例如:aa.jpg
        String originalFilename = multipartFile.getOriginalFilename();
        //获取文件的后缀
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileId = null;
        try {
            fileId = fileStorageService.uploadFile("", fileName + postfix, multipartFile.getInputStream());
            log.info("上传图片到MinIO中，fileId:{}", fileId);
        } catch (IOException e) {
            log.error("WmMaterialServiceImpl-上传文件失败");
        }

        //3.保存到数据库中
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId());
        wmMaterial.setUrl(fileId);
        wmMaterial.setIsCollection((short) 0);
        wmMaterial.setType((short) 0);
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);

        //4.返回结果
        return ResponseResult.okResult(wmMaterial);
    }

    /**
     * 素材列表查询
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findList(WmMaterialDto dto) {

        //1.检查参数
        dto.checkParam();

        //2.分页查询
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //是否收藏
        if (dto.getIsCollection() != null && dto.getIsCollection() == 1) {
            //查询收藏的素材
            lambdaQueryWrapper.eq(WmMaterial::getIsCollection, dto.getIsCollection());
        }
        //按照用户查询
        lambdaQueryWrapper.eq(WmMaterial::getUserId, WmThreadLocalUtil.getUser().getId());

        //按照时间倒序
        lambdaQueryWrapper.orderByDesc(WmMaterial::getCreatedTime);

        page = page(page, lambdaQueryWrapper);

        //3.结果返回
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }


    @Override
    public ResponseResult deleteMaterial(Integer id) {
        //1、查询该素材是否被引用
        Integer relationCounts = wmNewsMaterialMapper.selectCount(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getMaterialId, id));

        //2、如果该素材被引用数量大于0，说明该素材正在被引用中，返回删除失败
        if (relationCounts > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.MATERIASL_REFERENCE_USING);
        }

        //3、如果该素材没有被引用，则删除数据库中的素材和minio中的素材
        removeById(id);
        try {
            fileStorageService.delete(getById(id).getUrl());
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        } catch (NullPointerException e) {
            //该url指向的素材不在minio中，删除异常（但是也能完成删除）
            return ResponseResult.okResult(AppHttpCodeEnum.MATERIASL_REFERENCE_URL_NOT_EXIST);
        }

    }

    @Override
    public ResponseResult collectMaterial(Integer id) {
        WmMaterial wmMaterial = getById(id);
        if (wmMaterial.getIsCollection().equals(WemediaConstants.COLLECT_MATERIAL)){
            wmMaterial.setIsCollection(WemediaConstants.CANCEL_COLLECT_MATERIAL);
        }
        if (wmMaterial.getIsCollection().equals(WemediaConstants.CANCEL_COLLECT_MATERIAL)){
            wmMaterial.setIsCollection(WemediaConstants.COLLECT_MATERIAL);
        }
        updateById(wmMaterial);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


}