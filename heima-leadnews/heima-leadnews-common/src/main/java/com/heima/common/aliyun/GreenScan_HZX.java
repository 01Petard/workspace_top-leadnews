package com.heima.common.aliyun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.*;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.heima.common.constants.WemediaConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 文字、图片检测，自用
 */
@Getter
@Setter
@Component
@Slf4j
@ConfigurationProperties(prefix = "aliyun.green")
public class GreenScan_HZX {

    private String accessKeyId;
    private String secret;
    private String regionId;
    private String endpoint;

    private Integer readTimeout = 10000;
    private Integer connectTimeout = 10000;

    @Autowired
    private OssUtil ossUtil;

    /**
     * 阿里云内容安全配置初始化方法
     *
     * @param regionId
     * @param endpoint
     * @return
     */
    private Config getConfig(String accessKeyId, String secret, String regionId, String endpoint) {
        /*
         * 阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
         * 常见获取环境变量方式：
         * 方式一：
         *     获取RAM用户AccessKey ID：System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
         *     获取RAM用户AccessKey Secret：System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
         * 方式二：
         *     获取RAM用户AccessKey ID：System.getProperty("ALIBABA_CLOUD_ACCESS_KEY_ID");
         *     获取RAM用户AccessKey Secret：System.getProperty("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
         */
        Config config = new Config();
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(secret);
        // 接入区域和地址请根据实际情况修改。
        config.setRegionId(regionId);
        config.setEndpoint(endpoint);
        // 连接时超时时间，单位毫秒（ms）。
        config.setReadTimeout(6000);
        // 读取时超时时间，单位毫秒（ms）。
        config.setConnectTimeout(3000);
        // 设置http代理。
        // config.setHttpProxy("http://10.10.xx.xx:xxxx");
        // 设置https代理。
        //config.setHttpsProxy("https://10.10.xx.xx:xxxx");
        return config;
    }

    /**
     * 创建OSSClient对床
     * 注意，此处实例化的client请尽可能重复使用，避免重复建立连接，提升检测性能。
     *
     * @param config
     * @return
     */
    private Client getClient(Config config) {
        Client client;
        try {
            client = new Client(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return client;
    }

    /**
     * 创建RuntimeObject实例并设置运行参数。
     *
     * @return
     */
    private RuntimeOptions getRuntimeOptions(Integer readTimeout, Integer connectTimeout) {
        RuntimeOptions runtime = new RuntimeOptions();  // 创建RuntimeObject实例并设置运行参数。
        runtime.readTimeout = readTimeout;
        runtime.connectTimeout = connectTimeout;
        return runtime;
    }

    /**
     * 调用阿里云的图片安全检测
     *
     * @param imgUrls
     * @return
     */
    public List<Map> imgAutoScan(List<String> imgUrls) throws IOException {
        Config config = getConfig(accessKeyId, secret, regionId, endpoint);
        Client client = getClient(config);
        RuntimeOptions runtime = getRuntimeOptions(readTimeout, connectTimeout);

        List<Map> resultMaps = new ArrayList<>();
        //将图片上传到阿里云OSS
        List<String> urls = ossUtil.autoUpload(imgUrls);

        //逐个检测每个url对应的图片，并将结果resultMap添加到resultMaps中
        for (String url : urls) {
            // 检测参数构造。
            Map<String, String> serviceParameters = new HashMap<>();
            //公网可访问的URL。
            serviceParameters.put("imageUrl", url);
            serviceParameters.put("dataId", UUID.randomUUID().toString());

            ImageModerationRequest request = new ImageModerationRequest();
            // 图片检测service: baselineCheck通用基线检测。
            request.setService("profilePhotoCheck");
            request.setServiceParameters(JSON.toJSONString(serviceParameters));

            try {
                ImageModerationResponse response = null;
                if (client != null) {
                    response = client.imageModerationWithOptions(request, runtime);
                }
                // 自动路由。
                if (response != null) {
                    // 服务端错误，区域切换到cn-beijing。
                    if (500 == response.getStatusCode() || (response.getBody() != null && 500 == (response.getBody().getCode()))) {
                        // 接入区域和地址请根据实际情况修改。
                        config.setRegionId(regionId);
                        config.setEndpoint(endpoint);
                        client = new Client(config);
                        response = client.imageModerationWithOptions(request, runtime);
                    }
                }
                // 打印检测结果。
                HashMap<String, String> resultMap = new HashMap<>();
                resultMap.put("scanContent", url);
                if (response != null) {
                    if (response.getStatusCode() == 200) {
                        ImageModerationResponseBody responseBody = response.getBody();
                        resultMap.put("requestId", responseBody.getRequestId());
                        resultMap.put("code", responseBody.getCode().toString());
                        resultMap.put("msg", responseBody.getMsg());
                        if (responseBody.getCode() == 200) {
                            ImageModerationResponseBody.ImageModerationResponseBodyData data = responseBody.getData();
                            resultMap.put("dataId", data.getDataId());
                            List<ImageModerationResponseBody.ImageModerationResponseBodyDataResult> results = data.getResult();
                            for (ImageModerationResponseBody.ImageModerationResponseBodyDataResult result : results) {
                                resultMap.put("label", result.getLabel());
                                try {
                                    resultMap.put("confidence", result.getConfidence().toString());
                                } catch (Exception e) {
                                    resultMap.put("confidence", null);
                                }
                                if (result.getLabel() == null || result.getLabel().isEmpty() || result.getLabel().equals("nonLabel")) {
                                    resultMap.put("suggestion", WemediaConstants.WM_AUDIT_BY_PASS);
                                } else {
                                    resultMap.put("suggestion", WemediaConstants.WM_AUDIT_BY_BLOCK);
                                }
                            }
                            resultMaps.add(resultMap);
                        } else {
                            log.error("image moderation not success. code: {}", responseBody.getCode());
                        }
                    } else {
                        log.error("response not success. suggestion: {}", response.getStatusCode());
                    }
                }
            } catch (Exception e) {
                log.error(String.valueOf(e));
            }
        }

        return resultMaps;

    }

    /**
     * 调用阿里云的文字安全检测
     *
     * @param text
     * @return
     */
    public Map<String, String> textAutoScan(String text) {
        Config config = getConfig(accessKeyId, secret, regionId, endpoint);
        Client client = getClient(config);
        RuntimeOptions runtime = getRuntimeOptions(readTimeout, connectTimeout);

        //检测参数构造
        JSONObject serviceParameters = new JSONObject();
        serviceParameters.put("content", text);

        if (serviceParameters.get("content") == null || serviceParameters.getString("content").trim().isEmpty()) {
            System.out.println("text moderation content is empty");
            return null;
        }

        TextModerationRequest textModerationRequest = new TextModerationRequest();
        /*
        文本检测服务 service code
        */
        textModerationRequest.setService("comment_detection");
        textModerationRequest.setServiceParameters(serviceParameters.toJSONString());
        try {
            // 调用方法获取检测结果。
            TextModerationResponse response = null;
            if (client != null) {
                response = client.textModerationWithOptions(textModerationRequest, runtime);
            }

            // 自动路由。
            if (response != null) {
                // 服务端错误，区域切换到cn-beijing。
                if (500 == response.getStatusCode() || (response.getBody() != null && 500 == (response.getBody().getCode()))) {
                    // 接入区域和地址请根据实际情况修改。
                    config.setRegionId(regionId);
                    config.setEndpoint(endpoint);
                    client = new Client(config);
                    response = client.textModerationWithOptions(textModerationRequest, runtime);
                }
            }
            // 打印检测结果。
            HashMap<String, String> resultMap = new HashMap<>();
            resultMap.put("scanContent", text);
            if (response != null) {
                if (response.getStatusCode() == 200) {
                    TextModerationResponseBody responseBody = response.getBody();
                    resultMap.put("requestId", responseBody.getRequestId());
                    resultMap.put("code", responseBody.getCode().toString());
                    resultMap.put("msg", responseBody.getMessage());
                    Integer code = responseBody.getCode();
                    if (code != null && code == 200) {
                        TextModerationResponseBody.TextModerationResponseBodyData data = responseBody.getData();
                        resultMap.put("labels", data.getLabels());
//                        resultMap.put("reason", data.getReason());  //阿里云返回给标签解析原因，是一个这样的结果：[{"riskTips":"...,...,","riskWords":"...,..."}]
                        if (data.getLabels() == null || data.getLabels().isEmpty()) {
                            resultMap.put("suggestion", WemediaConstants.WM_AUDIT_BY_PASS);
                        } else {
                            resultMap.put("suggestion", WemediaConstants.WM_AUDIT_BY_BLOCK);
                        }
                        StringBuilder reason = addTextReasons(data.getLabels());
                        resultMap.put("reason", reason.toString());
                        return resultMap;
//                        return new GreenScan_HZX().checkTextStatus(data.getLabels());
                    } else {
                        log.error("text moderation not success. code: {}", responseBody.getCode());
                        return null;
                    }
                } else {
                    log.error("response not success. suggestion: {}", response.getStatusCode());
                    return null;
                }
            }
        } catch (Exception e) {
            log.error(String.valueOf(e));
        }
        return null;
    }

    /**
     * 解析labels标签，返回检测map
     *
     * @param labels
     * @return
     */
    public Map<String, String> checkTextStatus(String labels) {
        HashMap<String, String> map = new HashMap<>();
        if (labels == null || labels.isEmpty()) {
            map.put("suggestion", WemediaConstants.WM_AUDIT_BY_PASS);
            return map;
        }
        map.put("suggestion", WemediaConstants.WM_AUDIT_BY_BLOCK);

        StringBuilder reasons = addTextReasons(labels);
        map.put("reasons", reasons.toString());

        return map;
    }

    /**
     * 根据文本检测结果的labels标签，输出对应的违规原因
     *
     * @param labels
     * @return
     */
    private StringBuilder addTextReasons(String labels) {
        StringBuilder reason = new StringBuilder("审核原因：");
        List<String> collect = Arrays.stream(labels.split(",")).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            for (String s : collect) {
                switch (s) {
                    case "political_content":
//                        System.out.println("涉政内容");
                        reason.append("涉政内容");
                        break;
                    case "sexual_content":
//                        System.out.println("色情内容");
                        reason.append("色情内容");
                        break;
                    case "violence":
//                        System.out.println("暴恐内容");
                        reason.append("暴恐内容");
                        break;
                    case "contraband":
//                        System.out.println("违禁内容");
                        reason.append("违禁内容");
                        break;
                    case "ad":
//                        System.out.println("广告引流内容");
                        reason.append("广告引流内容");
                        break;
                    case "religion":
//                        System.out.println("宗教内容");
                        reason.append("宗教内容");
                        break;
                    case "profanity":
//                        System.out.println("辱骂内容");
                        reason.append("辱骂内容");
                        break;
                    case "negative_content":
//                        System.out.println("不良内容");
                        reason.append("不良内容");
                        break;
                    case "nonsense":
//                        System.out.println("无意义内容");
                        reason.append("无意义内容");
                        break;
                    default:
//                        System.out.println("无效的输入");
                        reason.append("无效的输入");
                }
            }
        }
        return reason;
    }


    /**
     * 解析labels标签，返回检测map
     * 注意，由于返回的检测内容标签过多此处不做处理直接判断结果
     *
     * @param labels
     * @return
     */
    public Map<String, String> checkPhotoStatus(String labels) {
        HashMap<String, String> map = new HashMap<>();
        if (labels == null || labels.isEmpty() || labels.equals("nonLabel")) {
            map.put("suggestion", WemediaConstants.WM_AUDIT_BY_PASS);
            return map;
        }
        map.put("suggestion", WemediaConstants.WM_AUDIT_BY_BLOCK);
        return map;
    }
}