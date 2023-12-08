package com.heima.model.user.vos;

import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class LoginVo {

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
