package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenScan_HZX;
import com.heima.common.aliyun.GreenTextScan_heima;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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
            //1.从内容中提取纯文本内容和图片
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);

            //自管理的敏感词过滤
            boolean isSensitive = handleSensitiveScan(textAndImages.get("content").toString(), wmNews);
            if (!isSensitive) {
                return;
            } else {
                log.info("文字内容存在敏感词，审核不通过，用户id：{}", wmNews.getUserId());
            }


            //2.调用阿里云接口审核文章内容
            boolean isTextScan = handleTextScan(textAndImages.get("content").toString(), wmNews);
            if (!isTextScan) {
                log.info("文字内容存在违规内容，审核不通过，用户id：{}", wmNews.getUserId());
                return;
            }

            //3.调用阿里云接口审核文章图片
            boolean isImagesScan = handleImagesScan((List<String>) textAndImages.get("images"), wmNews);
            if (!isImagesScan) {
                log.info("图片画面存在违规内容，审核不通过，用户id：{}", wmNews.getUserId());
                return;
            }


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
    private WmSensitiveMapper wmSensitiveMapper;

    /**
     * 自管理的敏感词审核
     *
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(String content, WmNews wmNews) {
        //false: 没有敏感词，true: 有敏感词
        boolean flag = false;

        //获取所有的敏感词类对象
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));

        //获取所有的敏感词类对象中的敏感词字符
        List<String> sensitiveList = wmSensitives
                .stream()
                .map(WmSensitive::getSensitives)
                .collect(Collectors.toList());

        //初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);

        //查看文章中是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content + wmNews.getTitle());
        if (!map.isEmpty()) {
            updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容" + map);
            flag = true;
        }

        return flag;
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
    private GreenScan_HZX greenScanHzx;

    @Autowired
    private Tess4jClient tess4jClient;

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
            return true;
        }

        //图片去重
        images = images.stream().distinct().collect(Collectors.toList());


        //根据规则将图片上传到阿里云OSS，并进行内容安全审查
        List<Map<String, String>> autoScanMaps = null;
        try {
            autoScanMaps = greenScanHzx.imgAutoScan(images);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //图片识别文字
        try {
            for (Map<String, String> autoScanMap : autoScanMaps) {
                String imageURL = autoScanMap.get("scanContent");
                byte[] bytes = tess4jClient.downloadImage(imageURL);
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage bufferedImage = ImageIO.read(in);
                //图片识别
                String resultOCR = tess4jClient.doOCR(bufferedImage);
                //过滤文字
                boolean isSensitive = handleSensitiveScan(resultOCR, wmNews);
                if (!isSensitive) {
                    //图片文字不通过审核，直接返回false
                    log.info("图片文字存在敏感词，审核不通过，用户id：{}", wmNews.getUserId());
                    return false;
                }
            }
        } catch (IOException | TesseractException e) {
            throw new RuntimeException(e);
        }


        //审核图片
        for (Map<String, String> autoScanMap : autoScanMaps) {
            if (autoScanMap.get("suggestion").equals(WemediaConstants.WM_AUDIT_BY_BLOCK)) {
                flag = false;
                updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容！");
            }
            if (autoScanMap.get("suggestion").equals(WemediaConstants.WM_AUDIT_BY_REVIEW)) {
                flag = false;
                updateWmNews(wmNews, (short) 3, "当前文章中存在不确定元素，需要人工审核！");
            }
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
            return true;
        }

        try {
            Map<String, String> map = greenTextScan_heima.greeTextScan(content);
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
            log.error(e.toString());
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
