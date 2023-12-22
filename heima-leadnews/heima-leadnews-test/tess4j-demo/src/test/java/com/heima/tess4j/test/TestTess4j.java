package com.heima.tess4j.test;

import com.heima.tess4j.Tess4jApplication;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@SpringBootTest(classes = Tess4jApplication.class)
@RunWith(SpringRunner.class)
public class TestTess4j {



    public byte[] downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(5000);
        InputStream inStream = conn.getInputStream();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    public String doOCR(BufferedImage image) throws TesseractException {
        //创建Tesseract对象
        ITesseract tesseract = new Tesseract();
        //设置字体库路径
        tesseract.setDatapath("E:\\devTools\\tessdata");
        //中文识别
        tesseract.setLanguage("chi_sim");
        //执行ocr识别
        String result = tesseract.doOCR(image);
        //替换回车和tal键  使结果为一行
        result = result.replaceAll("\\r|\\n", "-").replaceAll(" ", "");
        return result;
    }

    @Test
    public void testTess4j() throws IOException, TesseractException {
        String imageURL = "https://sky-itcast-hzx.oss-cn-hangzhou.aliyuncs.com/bb475315-fa8d-4804-b205-ac9e84f5407a.png";
        byte[] bytes = downloadImage(imageURL);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BufferedImage bufferedImage = ImageIO.read(in);
        //图片识别
        String resultOCR = doOCR(bufferedImage);
        System.out.println(resultOCR);
    }


}