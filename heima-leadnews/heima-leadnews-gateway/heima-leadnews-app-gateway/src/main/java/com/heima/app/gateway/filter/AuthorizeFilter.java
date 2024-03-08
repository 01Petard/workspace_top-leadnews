package com.heima.app.gateway.filter;

import com.heima.app.gateway.util.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizeFilter implements Ordered, GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1、获取request和response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 2、判断是否是登录
        if (request.getURI().getPath().contains("/login/")) {
            //放行
            return chain.filter(exchange);
        }
        //3、获取token
        String token = request.getHeaders().getFirst("token");
        //4、判断token是否存在
        if (StringUtils.isBlank(token)) {
            //token不存在，返回401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //5、判断token是否有效
        try {
            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
            //是否过期
            int result = AppJwtUtil.verifyToken(claimsBody);  //-1:有效; 0:有效; 1:过期; 2:过期
            if (result == 1 || result == 2) {
                //token过期，返回401
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            /*
             * 获取app端用户的id
             */
            //获取用户的信息，存入header
            Object app_userId = claimsBody.get("id");
            //存储header中
            ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
                httpHeaders.add("app_userId", app_userId + "");
            }).build();
            //重置请求
            exchange.mutate().request(serverHttpRequest);
        } catch (Exception e) {
            //解析失败
            log.error("token解析失败：", e);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //6、放行
        return chain.filter(exchange);
    }

    /**
     * 设置优先级，值越小优先级越高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
