package com.heima.file.service.impl;


import com.heima.file.config.MinIOConfig;
import com.heima.file.config.MinIOConfigProperties;
import com.heima.file.service.FileStorageService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@EnableConfigurationProperties(MinIOConfigProperties.class)
@Import(MinIOConfig.class)
public class MinIOFileStorageService implements FileStorageService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinIOConfigProperties minIOConfigProperties;

    private final static String separator = "/";

    /**
     * 根据文件名，返回文件的类型
     * @param fileName
     * @return
     */
    public String getFileType(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }

        switch (extension.toLowerCase()) {
            case "html":
            case "htm":
                return "text/html; charset=utf-8";
            case "css":
                return "text/css; charset=utf-8";
            case "js":
                return "text/javascript; charset=utf-8";
            case "txt":
            case "py":
            case "java":
                return "text/plain; charset=utf-8";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "wps":
                return "application/vnd.ms-works";
            case "xml":
                return "application/xml; charset=utf-8";
            case "json":
                return "application/json; charset=utf-8";
            case "gif":
                return "image/gif";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "zip":
                return "application/zip";
            case "tar":
                return "application/x-tar";
            case "gz":
                return "application/gzip";
            case "mp3":
                return "audio/mpeg";
            case "mp4":
                return "video/mp4";
            case "md":
                return "text/markdown";
            case "rp":
                return "application/x-rp";
            default:
                return "unknown/" + extension;
        }
    }

    /**
     * 在文件名前加入前缀、日期
     * @param dirPath
     * @param filename yyyy/mm/dd/file.jpg
     * @return
     */
    public String builderFilePath(String dirPath, String filename) {
        StringBuilder stringBuilder = new StringBuilder(50);
        if (!StringUtils.isEmpty(dirPath)) {
            stringBuilder.append(dirPath).append(separator);
        }
        stringBuilder.append(new SimpleDateFormat("yyyy/MM/dd").format(new Date()))
                .append(separator)
                .append(filename);
        return stringBuilder.toString();
    }


    /**
     * 上传文件
     * @param prefix      文件前缀
     * @param filename    文件名
     * @param inputStream 文件流
     * @return 文件全路径
     */
    @Override
    public String uploadFile(String prefix, String filename, InputStream inputStream) throws IOException {
        String filePath = builderFilePath(prefix, filename);
        String fileType = getFileType(filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType(fileType)
                    .bucket(minIOConfigProperties.getBucket()).stream(inputStream, inputStream.available(), -1)
                    .build();
            minioClient.putObject(putObjectArgs);
            return minIOConfigProperties.getReadPath() + separator + minIOConfigProperties.getBucket() +
                    separator +
                    filePath;
        } catch (Exception ex) {
            log.error("minio put file error.", ex);
            throw new RuntimeException("上传文件失败");
        } finally {
            inputStream.close();
        }
    }


    /**
     * 删除文件
     * @param pathUrl 文件全路径
     */
    @Override
    public void delete(String pathUrl) {
        String key = pathUrl.replace(minIOConfigProperties.getEndpoint() + "/", "");
        int index = key.indexOf(separator);
        String bucket = key.substring(0, index);
        String filePath = key.substring(index + 1);
        // 删除Objects
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucket).object(filePath).build();
        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("minio remove file error.  pathUrl:{}", pathUrl);
        }
    }


    /**
     * 下载文件
     * @param pathUrl 文件全路径
     * @return 文件流
     */
    @Override
    public byte[] downLoadFile(String pathUrl) throws IOException {
        String key = pathUrl.replace(minIOConfigProperties.getEndpoint() + "/", "");
        int index = key.indexOf(separator);
//        String bucket = key.substring(0, index);
        String filePath = key.substring(index + 1);
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(minIOConfigProperties.getBucket()).object(filePath).build());
        } catch (Exception e) {
            log.error("minio down file error.  pathUrl:{}", pathUrl);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while (true) {
            try {
                if (inputStream != null && !((rc = inputStream.read(buff, 0, 100)) > 0)) break;
            } catch (IOException e) {
                log.error("minio down file error.", e);
            }
            byteArrayOutputStream.write(buff, 0, rc);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
