package com.heima.wemedia.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 扫描降级文章类的包
 */
@Configuration
@ComponentScan("com.heima.apis.article.fallback")
public class InitConfig {
}