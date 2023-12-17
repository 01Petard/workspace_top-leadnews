package com.heima.wemedia;


import com.heima.common.aliyun.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class AliyunTextTest {


    @Autowired
    private GreenTextScan_heima greenTextScan;

    @Autowired
    private GreenScan_HZX greenScanHzx;

    /**
     * 测试黑马的文本审核
     *
     * @throws Exception
     */
    @Test
    public void testScanText() throws Exception {
//        String content = "我是一个好人，冰毒，鸡巴，习近平，89，64，包子，台独，PS平台独占";
        String content = "我是一个好人，平台独占，独不占";
        Map map = greenTextScan.greeTextScan(content);
        System.out.println("测试语句：" + content);
        System.out.println("测试结果：" + map);
    }


    @Test
    public void testContent() {
//        //文字审查
//        String content1 = "我是一个好人，冰毒，鸡巴，习近平，89，64，包子，台独，PS平台独占";
//        System.out.println(content1);
//        System.out.println(greenScanHzx.textAutoScan(content1));
//        System.out.println("============================");
//
//
//        String content2 = "我是一个好人，平台独占，独不占";
//        System.out.println(content2);
//        System.out.println(greenScanHzx.textAutoScan(content2));
//        System.out.println("============================");

        //图片审查
        String pathUrl1 = "http://192.168.179.129:9100/leadnews/1111.jpg";
        System.out.println(pathUrl1);
        System.out.println(GreenScan_TestDemo.imgAutoRoute(pathUrl1));
        System.out.println("============================");


        String pathUrl2 = "https://cdn.jsdelivr.net/gh/01Petard/imageURL@main/img/1111.jpg";
        System.out.println(pathUrl2);
        System.out.println(GreenScan_TestDemo.imgAutoRoute(pathUrl2));
        System.out.println("============================");


        String pathUrl3 = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRiF_a-N3JVggUZTTWctIgjEL6EF_zEQomtgQ&usqp=CAU";
        System.out.println(pathUrl3);
        System.out.println(GreenScan_TestDemo.imgAutoRoute(pathUrl3));
        System.out.println("============================");


        String pathUrl4 = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fsafe-img.xhscdn.com%2Fbw1%2F5723a630-077e-45c5-9e73-49ed809b3f43%3FimageView2%2F2%2Fw%2F1080%2Fformat%2Fjpg&refer=http%3A%2F%2Fsafe-img.xhscdn.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1694165323&t=149871d4e26b4b9a386e0e98bedc2929";
        System.out.println(pathUrl4);
        System.out.println(GreenScan_TestDemo.imgAutoRoute(pathUrl4));
    }




}
