package com.heima.minio.test;

import com.heima.file.service.FileStorageService;
import com.heima.minio.MinIOApplication;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest(classes = MinIOApplication.class)
@RunWith(SpringRunner.class)
public class MinIOTest {


    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void getPathName() throws IOException {
        Path path = Paths.get("C:\\Users\\hzx\\Desktop\\tou.png");

        Path fileName = path.getFileName();
        Path name = path.getName(0);
        Path name1 = path.getName(1);
        Path name2 = path.getName(2);
        Path name3 = path.getName(3);
        Path parent = path.getParent();
        Path root = path.getRoot();
        int nameCount = path.getNameCount();
        FileSystem fileSystem = path.getFileSystem();
        boolean absolute = path.isAbsolute();
    }

    /**
     * 测试上传图片、视频、文件
     * @throws IOException
     */
    @Test
    public void testUpload() throws IOException {

//        Path path = Paths.get("C:/Users/hzx/Desktop/da12bc8cbca7ce47e4bbe848979a66727198052.jpg");
//        Path path = Paths.get("C:/Users/hzx/Desktop/infer_fish_count_old.py");
//        Path path = Paths.get("D:/浙工大/组会汇报/黄泽校 组会06.05.pptx");
//        Path path = Paths.get("D:/output_2s_fish.mp4");
        Path path = Paths.get("C:\\Users\\hzx\\Desktop\\tou.png");
//        Path path = Paths.get("D:/Projects_Java/workspace_heima-leadnews/02-list.html");
//        Path path = Paths.get("C:\\Users\\hzx\\Desktop\\鱼计数_测试报告20231109.xlsx");
        String filePath = fileStorageService.uploadFile("", path.getFileName().toString(), Files.newInputStream(path));
        System.out.println(filePath);
    }

    /**
     * 测试上传文件（和上面一样）
     * @throws IOException
     */
    @Test
    public void testUploadTemplate() throws IOException {
        Path path = Paths.get("D:/output_306.mp4");
        String filePath = fileStorageService.uploadFile("", path.getFileName().toString(), Files.newInputStream(path));
        System.out.println(filePath);
    }

    /**
     * 测试删除文件
     * @throws IOException
     */
    @Test
    public void testDelete() throws IOException {
//        String pathUrl = "http://192.168.113.132:9100/leadnews/2023/11/23/02-list.html";
        String pathUrl = "http://192.168.113.132:9100/leadnews/2023/12/07/tou.png";
        fileStorageService.delete(pathUrl);
    }

    @Test
    public void testDownload() throws IOException {
//        String pathUrl = "http://192.168.113.132:9100/leadnews/2023/11/23/02-list.html";
        String pathUrl = "http://192.168.113.132:9100/leadnews/2023/12/07/tou.png";
        byte[] bytes = fileStorageService.downLoadFile(pathUrl);
        System.out.println(new String(bytes));
    }

    public static void main(String[] args) {

        FileInputStream fileInputStream = null;
        try {
            //1.创建minio链接客户端
            MinioClient minioClient = MinioClient.builder()
                    .credentials("minio", "12345678")
                    .endpoint("http://192.168.113.132:9100")
                    .build();
            fileInputStream = new FileInputStream("D:\\Projects_Java\\黑马程序员Java微服务项目《黑马头条》\\day02-app端文章查看，静态化freemarker,分布式文件系统minIO\\资料\\模板文件\\plugins\\css\\index.css");
            //2.上传
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object("plugins/css/index.css")//文件名
                    .contentType("text/css")//文件类型
                    .bucket("leadnews")//桶名词，与minio创建的名词一致
                    .stream(fileInputStream, fileInputStream.available(), -1) //文件流
                    .build();
            minioClient.putObject(putObjectArgs);

            System.out.println(minioClient.getObjectUrl("leadnews", "plugins/css/index.css"));

            fileInputStream = new FileInputStream("D:\\Projects_Java\\黑马程序员Java微服务项目《黑马头条》\\day02-app端文章查看，静态化freemarker,分布式文件系统minIO\\资料\\模板文件\\plugins\\js\\index.js");
            PutObjectArgs putObjectArgs2 = PutObjectArgs.builder()
                    .object("plugins/js/index.js")//文件名
                    .contentType("text/javascript")//文件类型
                    .bucket("leadnews")//桶名词，与minio创建的名词一致
                    .stream(fileInputStream, fileInputStream.available(), -1) //文件流
                    .build();
            minioClient.putObject(putObjectArgs2);

            System.out.println(minioClient.getObjectUrl("leadnews", "plugins/js/index.js"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}