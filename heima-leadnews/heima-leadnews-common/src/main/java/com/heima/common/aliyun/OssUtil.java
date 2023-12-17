package com.heima.common.aliyun;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 个人自用的阿里云OSS图片上传方法
 */
@Getter
@Setter
@Component
@Slf4j
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssUtil {

    private String accessKeyId;
    private String accessKeySecret;
    private String endpoint;
    private String bucketName;

    /**
     * 阿里云OSS：根据传入的url，自动上传到OSS
     * 直接调用该方法，传入图片的url或uri，即可上传
     *
     * @param urls 传入的url
     * @return 图片上传后的url
     * @throws IOException
     */
    public List<String> autoUpload(List<String> urls) throws IOException {
        List<String> uploadUrls = new ArrayList<>();
        for (String url : urls) {
            if (!(url == null || url.isEmpty())) {
                //windows本地图片的上传方式
                if (url.matches("^[a-zA-Z]:\\\\.*$")) {
                    uploadUrls.add(upload_local(url));
                }
                //非windows本地图片的上传方式（例如：虚拟机中的图片、网路上的图片，但是不确定Linux和MacOS的本地图片能否兼容这种方式）
                else {
                    uploadUrls.add(upload_remote(url));
                }
            }
        }
        return uploadUrls;
    }


    /**
     * 阿里云OSS：上传本地图片
     *
     * @throws IOException
     */
    public String upload_local(String pathUrl) throws IOException {
//        String pathUrl = "C:\\Users\\hzx\\Downloads\\test_oss.png";
        Path path = Paths.get(pathUrl);
        //生成一个图片的名字
        String object_name = "default" + ".jpg";
        //文件名.文件后缀
        String originalFilename = path.getFileName().toString();
        //文件后缀
        String extension_name = originalFilename.substring(originalFilename.lastIndexOf("."));
        //用UUID构造新文件名，并加上文件后缀
        object_name = UUID.randomUUID().toString() + extension_name;
        //返回文件在OSS存储中的请求路径
        return this.upload(Files.readAllBytes(path), object_name);
    }

    /**
     * 阿里云OSS：上传网络中的图片
     *
     * @throws IOException
     */
    public String upload_remote(String imageUrl) throws IOException {
        //生成一个图片的名字
        String object_name = "default" + ".jpg";
        try {
            //文件名.文件后缀
            String originalFilename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            //文件后缀
            String extension_name = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (extension_name.length() >= 6) {
                //如果后缀不是.jgp、.png、.gif或.webp，则认为不是图片后缀
                return imageUrl;
            }
            //用UUID构造新文件名，并加上文件后缀
            object_name = UUID.randomUUID().toString() + extension_name;
        } catch (Exception e) {
            /*
            一般来说，这个报错的原因是网络中的图片没有后缀，例如下面这个链接：
            https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fsafe-img.xhscdn.com%2Fbw1%2F5723a630-077e-45c5-9e73-49ed809b3f43%3FimageView2%2F2%2Fw%2F1080%2Fformat%2Fjpg&refer=http%3A%2F%2Fsafe-img.xhscdn.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1694165323&t=149871d4e26b4b9a386e0e98bedc2929
            这个链接其实没必要上传到OSS中，直接调用阿里云的Green审查即可
            所以，这里我选择跳过upload，直接返回原图片的链接
             */
            return imageUrl;
        }

        //将图片转化成byte流的形式
        URL url = new URL(imageUrl);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
        }
        byte[] imageBytes = output.toByteArray();


        //返回文件在OSS存储中的请求路径
        return this.upload(imageBytes, object_name);
    }


    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, " + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered " + "a serious internal problem while trying to communicate with OSS, " + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder.append(bucketName).append(".").append(endpoint).append("/").append(objectName);
        log.info("文件上传到:{}", stringBuilder.toString());
        return stringBuilder.toString();
    }


}
