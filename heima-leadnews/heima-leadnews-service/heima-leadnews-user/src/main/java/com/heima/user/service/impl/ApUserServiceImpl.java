package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.vos.LoginVo;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    @Override
    public ResponseResult login(LoginDto dto) {
        //1、正常登录：用户名+密码
        if (StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())) {
            //1.1、查询用户
            ApUser dbUser = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
            if (dbUser == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户信息不存在");
            }
            //1.2、比对密码
            String salt = dbUser.getSalt();
            String password = dto.getPassword();
            String password_md5 = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            //若密码错误，则返回信息“密码错误”
            if (!password_md5.equals(dbUser.getPassword())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            //1.3、返回数据 jwt user
            Map<String, Object> map = new HashMap<>();

            LoginVo loginVo = new LoginVo();
            BeanUtils.copyProperties(dbUser, loginVo);
            map.put("user", loginVo);

            String token = AppJwtUtil.getToken(dbUser.getId().longValue());
            map.put("token", token);

            // 将token添加到响应头中
            return ResponseResult.okResult(map);
        } else {
            //2、游客登陆
            Map<String, Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(map);
        }
    }
}
