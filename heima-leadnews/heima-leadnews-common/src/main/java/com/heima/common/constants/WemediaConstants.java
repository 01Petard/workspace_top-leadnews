package com.heima.common.constants;

public class WemediaConstants {

    /**
     * 文章收藏
     */
    public static final Short COLLECT_MATERIAL = 1;
    /**
     * 文章取消收藏
     */
    public static final Short CANCEL_COLLECT_MATERIAL = 0;
    public static final String WM_NEWS_TYPE_IMAGE = "image";
    /**
     * 文章封面类型，无图
     */
    public static final Short WM_NEWS_NONE_IMAGE = 0;
    /**
     * 文章封面类型，单图
     */
    public static final Short WM_NEWS_SINGLE_IMAGE = 1;
    /**
     * 文章封面类型，多图
     */
    public static final Short WM_NEWS_MANY_IMAGE = 3;
    /**
     * 文章封面类型，自动，根据文章中的数量判断无图、单图，还是多图
     */
    public static final Short WM_NEWS_TYPE_AUTO = -1;
    public static final Short WM_CONTENT_REFERENCE = 0;
    public static final Short WM_COVER_REFERENCE = 1;
    /**
     * 审核通过
     */
    public static final String WM_AUDIT_BY_PASS = "pass";
    /**
     * 审核不通过
     */
    public static final String WM_AUDIT_BY_BLOCK = "block";
    /**
     * 审核不确定，需要人工审核
     */
    public static final String WM_AUDIT_BY_REVIEW = "review";


}