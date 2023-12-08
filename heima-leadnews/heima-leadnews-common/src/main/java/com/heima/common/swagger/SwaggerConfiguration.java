package com.heima.common.swagger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Slf4j
public class SwaggerConfiguration {

    @Bean
    public Docket buildDocket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("黑马头条-平台管理API文档")
                .description("黑马头条后台api")
                .contact(new Contact("01Petard", "", ""))
                .version("1.0.0").build();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("黑马头条-用户管理端swagger")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.heima"))  // 要扫描的API(Controller)基础包
                .paths(PathSelectors.any())
                .build();
    }

//    private ApiInfo buildApiInfo() {
//        Contact contact = new Contact("01Petard","","");
//        return new ApiInfoBuilder()
//                .title("黑马头条-平台管理API文档")
//                .description("黑马头条后台api")
//                .contact(contact)
//                .version("1.0.0").build();
//    }

    /**
     * 通过knife4j生成接口文档
     * @return
     */
    @Bean
    public Docket docket_admin() {
        log.info("准备生成接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("苍穹外卖-管理端")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.admin"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket docket_user() {
        log.info("准备生成接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("苍穹外卖-用户端")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.user"))
                .paths(PathSelectors.any())
                .build();
    }


}