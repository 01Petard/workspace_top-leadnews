package com.heima.wemedia.controller.v1;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.service.WmNewsService;
import org.simpleframework.xml.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Autowired
    private WmNewsService wmNewsService;

    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmNewsPageReqDto dto) {
        return wmNewsService.findAll(dto);
    }

    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto) {
        return wmNewsService.submitNews(dto);
    }

    /**
     * 根据文章id查询文章
     * @param id
     * @return
     */
    @GetMapping("/one/{id}")
    public ResponseResult findOne(@PathVariable(value = "id") Integer id) {
//        return ResponseResult.okResult(wmNewsService.getOne(Wrappers.<WmNews>lambdaQuery().eq(WmNews::getId, id)));
        //一样的效果
        return ResponseResult.okResult(wmNewsService.getById(id));
    }

    /**
     * 根据文章id删除文章
     * @param id
     * @return
     */
    @GetMapping("/del_news/{id}")
    public ResponseResult deleteOne(@PathVariable(value = "id") Integer id) {
        return ResponseResult.okResult(wmNewsService.removeById(id));
    }


}