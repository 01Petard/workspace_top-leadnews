package com.heima.model.wemedia.vos;

import lombok.Data;

@Data
public class WmLoginVo {


    /**
     * 用户id，主键
     */
    private Integer id;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 用户名
     */
    private String name;

}
