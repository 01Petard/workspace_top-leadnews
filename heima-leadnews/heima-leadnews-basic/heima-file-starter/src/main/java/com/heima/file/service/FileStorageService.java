package com.heima.file.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author itheima
 */
public interface FileStorageService {


    /**
     * 上传文件
     * @param prefix      文件前缀
     * @param filename    文件名
     * @param inputStream 文件流
     * @return 文件全路径
     */
    String uploadFile(String prefix, String filename, InputStream inputStream) throws IOException;

    /**
     * 删除文件
     * @param pathUrl 文件全路径
     */
    void delete(String pathUrl);

    /**
     * 下载文件
     * @param pathUrl 文件全路径
     * @return
     */
    byte[] downLoadFile(String pathUrl) throws IOException;

}
