package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;

import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.sql.Wrapper;
import java.util.Date;
import java.util.List;


@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    // 单页最大加载的数字
    private final static short MAX_PAGE_SIZE = 50;

    @Autowired
    private ApArticleMapper apArticleMapper;

    /**
     * 根据参数加载文章列表
     *
     * @param loadtype 1为加载更多  2为加载最新
     * @param dto
     * @return
     */
    @Override
    public ResponseResult load(ArticleHomeDto dto, Short loadtype) {
        //1.校验参数，确保分页数量不低于10
        Integer size = dto.getSize();
        if (size == null || size == 0) {
            size = 10;
        }
        size = Math.min(size, MAX_PAGE_SIZE);
        dto.setSize(size);
        //类型参数检验，如果加载类型既不是加载更多，也不是加载最新，就强制改成加载最新
        if (!loadtype.equals(ArticleConstants.LOADTYPE_LOAD_MORE) && !loadtype.equals(ArticleConstants.LOADTYPE_LOAD_NEW)) {
            loadtype = ArticleConstants.LOADTYPE_LOAD_NEW;
        }
        //文章频道校验，如果前端传递的频道id为空，就会不分频道展示所有频道的文章
        if (StringUtils.isEmpty(dto.getTag())) {
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }
        //时间校验，空的补为当前时间
        if (dto.getMaxBehotTime() == null) {
            dto.setMaxBehotTime(new Date());
        }
        if (dto.getMinBehotTime() == null) {
            dto.setMinBehotTime(new Date());
        }


        //2.查询数据
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto, loadtype);


        //3.结果封装
        return ResponseResult.okResult(apArticles);
    }


    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;

    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
//        //测试服务降级
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        //1. 检查参数
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto, apArticle);
        //2. 判断id是否存在
        if (dto.getId() == null) {
            //不存在id， 保存文章、保存文章配置、保存文章内容

            //保存文章
            save(apArticle);

            //保存文章配置
            ApArticleConfig apArticleConfig = new ApArticleConfig();
            apArticleConfig.setArticleId(apArticle.getId());
            apArticleConfig.setIsComment(false);
            apArticleConfig.setIsDown(false);
            apArticleConfig.setIsForward(false);
            apArticleConfig.setIsDelete(false);
            apArticleConfigMapper.insert(apArticleConfig);

            //保存文章内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);
        } else {
            //存在id，修改文章、修改文章内容
            //修改文章
            updateById(apArticle);

            //修改文章内容
            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, dto.getId()));
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.updateById(apArticleContent);
        }


        //异步调用，生成静态文件上传到minion中
        try {
            articleFreemarkerService.buildArticleToMinIO(apArticle, dto.getContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //3. 结果返回文章id
        return ResponseResult.okResult(apArticle.getId());

    }

}