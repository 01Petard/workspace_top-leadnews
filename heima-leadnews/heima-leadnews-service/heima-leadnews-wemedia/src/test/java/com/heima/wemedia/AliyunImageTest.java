package com.heima.wemedia;


import com.heima.common.aliyun.*;
import com.heima.file.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.List;

import java.util.*;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class AliyunImageTest {


    @Autowired
    private OssUtil aliOssUtil;

    @Autowired
    private GreenScan_HZX greenScanHzx;

    /**
     * 测试黑马的图片上传
     * 测试图片全部为网络图片
     * 测试通过
     */
    @Test
    public void Test_ImageScan_demo() {
        String pathUrl8 = "https://img0.baidu.com/it/u=3929882364,2688626547&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=666";
        System.out.println(GreenScan_TestDemo.imgAutoRoute(pathUrl8));
        System.out.println("============================");
        String pathUrl9 = "https://sky-itcast-hzx.oss-cn-hangzhou.aliyuncs.com/1111.jpg";
        System.out.println(GreenScan_TestDemo.imgAutoRoute(pathUrl9));
        System.out.println("============================");
        String pathUrl10 = "https://sky-itcast-hzx.oss-cn-hangzhou.aliyuncs.com/2222.jpg";
        System.out.println(GreenScan_TestDemo.imgAutoRoute(pathUrl10));
        System.out.println("============================");
        String pathUrl11 = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fsafe-img.xhscdn.com%2Fbw1%2F5723a630-077e-45c5-9e73-49ed809b3f43%3FimageView2%2F2%2Fw%2F1080%2Fformat%2Fjpg&refer=http%3A%2F%2Fsafe-img.xhscdn.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1694165323&t=149871d4e26b4b9a386e0e98bedc2929";
        System.out.println(GreenScan_TestDemo.imgAutoRoute(pathUrl11));
    }


    /**
     * 阿里云OSS：根据传入的url，自动上传到OSS
     *
     * @throws IOException
     */
    @Test
    public void Test_AutoUpload() throws IOException {
        String url1 = "C:\\Users\\hzx\\Downloads\\test_oss.png";
        System.out.println("==========================\n" + url1);
        System.out.println(aliOssUtil.autoUpload(Collections.singletonList(url1)));

        String url2 = "http://192.168.179.129:9100/leadnews/1111.jpg";
        System.out.println("==========================\n" + url2);
        System.out.println(aliOssUtil.autoUpload(Collections.singletonList(url2)));

        String url4 = "https://img0.baidu.com/it/u=3929882364,2688626547&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=666";
        System.out.println("==========================\n" + url4);
        System.out.println(aliOssUtil.autoUpload(Collections.singletonList(url4)));
    }

    @Test
    public void Test_AutoScan() throws IOException {

        String url1 = "C:\\Users\\hzx\\Downloads\\test_oss.png";
        String url2 = "http://192.168.179.129:9100/leadnews/1111.jpg";
        String url3 = "https://img0.baidu.com/it/u=3929882364,2688626547&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=666";
        String url4 = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fsafe-img.xhscdn.com%2Fbw1%2F5723a630-077e-45c5-9e73-49ed809b3f43%3FimageView2%2F2%2Fw%2F1080%2Fformat%2Fjpg&refer=http%3A%2F%2Fsafe-img.xhscdn.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1694165323&t=149871d4e26b4b9a386e0e98bedc2929";


        ArrayList<String> urls = new ArrayList<>();
        urls.add(url1);
        urls.add(url2);
        urls.add(url3);
        urls.add(url4);

        List<Map<String,String>> maps = greenScanHzx.imgAutoScan(urls);
        System.out.println(maps);
    }

}
