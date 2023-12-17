package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenScan_HZX;
import com.heima.common.aliyun.GreenTextScan_heima;
import com.heima.common.constants.WemediaConstants;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {


    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Override
    @Async  //异步审核
    public void autoScanWmNews(Integer id) {
        //为文章的保存留出一定时间
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //1. 根据查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }
        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {
            //从内容中提取纯文本内容和图片
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);

            //2.调用阿里云接口审核文章内容
            boolean isTextScan = handleTextScan(textAndImages.get("content").toString(), wmNews);
            if (!isTextScan) return;

            //3.调用阿里云接口审核文章图片
            boolean isImagesScan = handleImagesScan((List<String>) textAndImages.get("images"), wmNews);
            if (!isImagesScan) return;
            //4.审核成功，保存app端文章数据
            ResponseResult responseResult = saveAppApArticle(wmNews);
            if (!responseResult.getCode().equals(200)) {
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据库失败");
            }
            //回填article_id
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews, (short) 9, "审核成功，文章发布！");
        }
    }

    @Autowired
    private IArticleClient articleClient;

    @Autowired
    private WmChannelMapper wmChannelMapper;

    @Autowired
    private WmUserMapper wmUserMapper;


    /**
     * 保存app端相关的文章数据
     *
     * @param wmNews
     */
    private ResponseResult saveAppApArticle(WmNews wmNews) {
        ArticleDto dto = new ArticleDto();
        //属性拷贝
        BeanUtils.copyProperties(wmNews, dto);
        dto.setLayout(wmNews.getType());
        //文章布局
        dto.setChannelId(wmNews.getChannelId());
        //频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null) {
            dto.setChannelName(wmChannel.getName());
        }
        //作者
        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser != null) {
            dto.setAuthorName(wmUser.getName());
        }
        //设置文章id
        if (wmNews.getArticleId() != null) {
            //文章id不为空，说明之前保存过，现在的操作是修改
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());

        ResponseResult responseResult = articleClient.saveArticle(dto);
        return responseResult;

    }


    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private GreenScan_HZX greenScanHzx;

    /**
     * 审核图片
     *
     * @param images
     * @param wmNews
     * @return
     */
    private boolean handleImagesScan(List<String> images, WmNews wmNews) {
        boolean flag = true;

        if (images == null || images.isEmpty()) {
            return flag;
        }

        //图片去重
        images = images.stream().distinct().collect(Collectors.toList());

        //审核图片
        try {
            List<Map> autoScanMaps = greenScanHzx.imgAutoScan(images);
            for (Map autoScanMap : autoScanMaps) {
                if (autoScanMap.get("suggestion").equals(WemediaConstants.WM_AUDIT_BY_BLOCK)) {
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容！");
                }
                if (autoScanMap.get("suggestion").equals(WemediaConstants.WM_AUDIT_BY_REVIEW)) {
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "当前文章中存在不确定元素，需要人工审核！");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return flag;

    }

    @Autowired
    private GreenTextScan_heima greenTextScan_heima;

    /**
     * 审核文本内容
     *
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleTextScan(String content, WmNews wmNews) {

        boolean flag = true;

        if (content.isEmpty()) {
            return flag;
        }

        try {
            Map map = greenTextScan_heima.greeTextScan(content);
            if (map != null) {
                if (map.get("suggestion").equals(WemediaConstants.WM_AUDIT_BY_BLOCK)) {
                    //审核不通过，失败
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容！");
                }
                if (map.get("suggestion").equals(WemediaConstants.WM_AUDIT_BY_REVIEW)) {
                    //审核不确定，人工审核
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "当前文章中存在不确定元素，需要人工审核！");
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 修改文章状态和修改原因
     *
     * @param wmNews
     * @param status
     * @param reason
     */
    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    /**
     * 1.从文章内容中提取文本和图片
     * 2.从文章中提取封面图片
     *
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {

        //存储文本内容
        StringBuilder stringBuilder = new StringBuilder();
        //存储图片url
        ArrayList<String> images = new ArrayList<>();

        //1.从文章内容中提取文本和图片的url
        if (StringUtils.isNotBlank(wmNews.getContent())) {
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")) {
                    //文本内容
                    stringBuilder.append(map.get("value"));
                }
                if (map.get("type").equals("image")) {
                    //图片内容
                    images.add((String) map.get("value"));
                }
            }
        }
        //将标题也存放到文本内容中
        stringBuilder.append(wmNews.getTitle());

        //2.从文章中提取封面图片的url
        if (StringUtils.isNoneBlank(wmNews.getImages())) {
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("content", stringBuilder);
        resultMap.put("images", images);
        return resultMap;
    }
}
