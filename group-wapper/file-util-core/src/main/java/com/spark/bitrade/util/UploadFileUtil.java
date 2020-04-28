package com.spark.bitrade.util;

import com.alibaba.simpleimage.ImageRender;
import com.alibaba.simpleimage.render.ReadRender;
import com.alibaba.simpleimage.render.ScaleParameter;
import com.alibaba.simpleimage.render.ScaleRender;
import com.alibaba.simpleimage.render.WriteRender;
//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGEncodeParam;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhang yingxin
 * @date 2018/4/10
 */
public class UploadFileUtil {
    public static String uploadFile(MultipartFile file, String fileName) throws IOException {
        String directory = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        FileOutputStream fop = null;
        try {
            InputStream inputStream = file.getInputStream();
            byte[] bytes = IOUtils.toByteArray(inputStream);
            String uploadPath = "";
            //constructs upload file path
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                uploadPath = System.getProperty("user.dir") + File.separator + "webapp"
                        + File.separator + "common/upload" + File.separator + directory;
            } else {
                //linux服务器上传  //System.getProperty("user.dir")+ File.separator+
                uploadPath = "/web/upload" + File.separator + directory;
            }
            File file2 = new File(uploadPath);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            String fileTrueName = uploadPath + File.separator + fileName;
            File file3 = new File(fileTrueName);
            if (!file3.exists()) {
                file3.createNewFile();
            }
            fop = new FileOutputStream(file3);
            fop.write(bytes);
            fop.flush();
            fop.close();
            return "upload" + File.separator + directory + File.separator + fileName;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fop.close();
        }
        return null;
    }

    /**
     * 按设置的宽度高度压缩图片文件<br> 先保存原文件，再压缩、上传
     * @param oldFile  要进行压缩的文件全路径
     * @param newFile  新文件
     * @param width  宽度
     * @param height 高度
     * @param quality 质量
     * @return 返回压缩后的文件的全路径
     */
    public static String zipWidthHeightImageFile(File oldFile,File newFile, int width, int height,float quality) {
        if (oldFile == null) {
            return null;
        }
        String newImage = null;
        try {
            /** 对服务器上的临时文件进行处理 */
            Image srcFile = ImageIO.read(oldFile);

            String srcImgPath = newFile.getAbsoluteFile().toString();
            System.out.println(srcImgPath);
            String subfix = "jpg";
            subfix = srcImgPath.substring(srcImgPath.lastIndexOf(".")+1,srcImgPath.length());

            BufferedImage buffImg = null;
            if(subfix.equals("png")){
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }else{
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }

            Graphics2D graphics = buffImg.createGraphics();
            graphics.setBackground(new Color(255,255,255));
            graphics.setColor(new Color(255,255,255));
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(srcFile.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

            ImageIO.write(buffImg, subfix, new File(srcImgPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newImage;
    }



    /**
      *  等比例压缩图片
      * @author tansitao
      * @time 2018/5/5 14:59 
     * @param
     */
    public static String zipWidthHeightImageFile(InputStream inputStream,ByteArrayOutputStream  bos, String subfix,float quality) {
        if (inputStream == null) {
            return null;
        }
        String newImage = null;
        try {
            /** 对服务器上的临时文件进行处理 */
            BufferedImage srcFile = ImageIO.read(inputStream);
            int width = srcFile.getWidth();
            int height = srcFile.getHeight();
            BufferedImage buffImg = null;
            if(subfix.equals("png")){
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }else{
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }

            Graphics2D graphics = buffImg.createGraphics();
            graphics.setBackground(new Color(255,255,255));
            graphics.setColor(new Color(255,255,255));
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(srcFile.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

            ImageIO.write(buffImg, subfix, bos);
//            FileOutputStream out = new FileOutputStream(destFile); // 输出到文件流
            // 可以正常实现bmp、png、gif转jpg
//            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
//            JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(buffImg);
//			/* 压缩质量 */
//            jep.setQuality(quality, true);
//            encoder.encode(buffImg, jep); // JPEG编码
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newImage;
    }

    /**
     * @Title: compressPicByQuality
     * @Description: 压缩图片,通过压缩图片质量，保持原图大小
     * @param  quality：0-1
     * @return byte[]
     * @throws
     */
    public static byte[] compressPicByQuality(InputStream inputStream, float quality) throws Exception {
//        byte[] inByte = null;
//        try {
//            ByteArrayInputStream byteInput = new ByteArrayInputStream(imgByte);
            BufferedImage image = ImageIO.read(inputStream);

            // 原图
            File in = new File(
                    "d:/1.png");
            // 目的图
            File out = new File(
                    "d:/2.png");
            FileInputStream input = null;
            FileOutputStream outStream = null;
            WriteRender wr = null;
        ScaleParameter scaleParam = new ScaleParameter(1024, 1024);  //将图像缩略到1024x1024以内，不足1024x1024则不做任何处理 // 将图像缩略到1024x1024以内，不足1024x1024则不做任何处理
            input = new FileInputStream(in);
            outStream = new FileOutputStream(out);
            ImageRender rr = new ReadRender(input);
            ImageRender sr = new ScaleRender(rr, scaleParam);
            wr = new WriteRender(sr, outStream);

            wr.render(); // 触发图像处理
//
//// 如果图片空，返回空
//            if (image == null) {
//                return null;
//            }
//// 得到指定Format图片的writer
//            Iterator<ImageWriter> iter = ImageIO
//                    .getImageWritersByFormatName("jpeg");// 得到迭代器
//            ImageWriter writer = (ImageWriter) iter.next(); // 得到writer
//
//// 得到指定writer的输出参数设置(ImageWriteParam )
//            ImageWriteParam iwp = writer.getDefaultWriteParam();
//            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); // 设置可否压缩
//            iwp.setCompressionQuality(quality); // 设置压缩质量参数
//
//            iwp.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
//
////            ColorModel colorModel = ColorModel.getRGBdefault();
//            int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
//            ColorModel colorModel = new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);
//// 指定压缩时使用的色彩模式
//            iwp.setDestinationType(new javax.imageio.ImageTypeSpecifier(colorModel,
//                    colorModel.createCompatibleSampleModel(16, 16)));
//
//// 开始打包图片，写入byte[]
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // 取得内存输出流
//            IIOImage iIamge = new IIOImage(image, null, null);
//
//// 此处因为ImageWriter中用来接收write信息的output要求必须是ImageOutput
//// 通过ImageIo中的静态方法，得到byteArrayOutputStream的ImageOutput
//            writer.setOutput(ImageIO
//                    .createImageOutputStream(byteArrayOutputStream));
//            writer.write(null, iIamge, iwp);
//            inByte = byteArrayOutputStream.toByteArray();
//        } catch (IOException e) {
//            System.out.println("write errro");
//            e.printStackTrace();
//        }
//        return inByte;
        return null;
    }



    /**
      *  等比例压缩图片
      * @author tansitao
      * @time 2018/5/5 14:59 
     * @param
     */
    public static String zipImageFileByRate(InputStream inputStream,ByteArrayOutputStream  bos, String subfix) {
        if (inputStream == null) {
            return null;
        }
        String newImage = null;
        float rate = 1;
        try {
            /** 对服务器上的临时文件进行处理 */
            BufferedImage srcFile = ImageIO.read(inputStream);
            //根据不同的图片宽度，设置压缩率
            if(500 <= srcFile.getWidth() && srcFile.getWidth() < 1000)
            {
                rate = 0.6f;
            }
            else if (1000 <= srcFile.getWidth() && srcFile.getWidth() <= 1500)
            {
                rate = 0.4f;
            }
            else if(srcFile.getWidth() > 1500)
            {
                rate = 0.3f;
            }
            Float width = srcFile.getWidth() * rate;
            Float height = srcFile.getHeight() * rate;
            BufferedImage buffImg = null;
            if(subfix.equals("png")){
                buffImg = new BufferedImage(width.intValue(), height.intValue(), BufferedImage.TYPE_INT_ARGB);
            }else{
                buffImg = new BufferedImage(width.intValue(), height.intValue(), BufferedImage.TYPE_INT_RGB);
            }

            Graphics2D graphics = buffImg.createGraphics();
            graphics.setBackground(new Color(255,255,255));
            graphics.setColor(new Color(255,255,255));
            graphics.fillRect(0, 0, width.intValue(), height.intValue());
            graphics.drawImage(srcFile.getScaledInstance(width.intValue(), height.intValue(), Image.SCALE_SMOOTH), 0, 0, null);

            ImageIO.write(buffImg, subfix, bos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newImage;
    }
    /**
      * 将二维码与图片进行合成
      * @author tansitao
      * @time 2018/5/18 11:26 
      */
    public static byte[] compoundImg(String bgImg, InputStream erImg, String imageType , int rectType)
    {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        try {
            BufferedImage bg = ImageIO.read(new URL(bgImg));//从oss读取背景图片
            BufferedImage er = ImageIO.read(erImg);//读取二维码输入流
            int bgWidth = bg.getWidth();
            int bgHeight = bg.getHeight();
            int erWidth = er.getWidth();
            int erX;
            int erY;
            if (rectType == 1) {//领奖布局
                //左下角
                erX = 15;
                erY = bgHeight - erWidth - 65;
            } else if (rectType == 0) {//投票布局
                //右下角
                erX = bgWidth - erWidth - 20;
                erY = bgHeight - erWidth - erWidth + 60;
            } else{
                //居中
                erX = bgWidth/2 - erWidth/2;
                erY = bgHeight - erWidth - erWidth;
            }
            BufferedImage img = new BufferedImage(bgWidth, bgHeight, BufferedImage.TYPE_INT_RGB);//创建图片
            Graphics g = img.getGraphics();//开启画图

            g.drawImage(bg.getScaledInstance(bgWidth,bgHeight, Image.SCALE_DEFAULT), 0, 0, null); // 绘制缩小后的图
            g.drawImage(er.getScaledInstance(erWidth, erWidth, Image.SCALE_DEFAULT), erX, erY, null); // 绘制缩小后的图
            g.setColor(Color.white); //设置边框
//            g.drawRect(erX, erY, erWidth - 1, erWidth - 1);
//            g.drawRect(erX + 1,  erY + 1, erWidth - 1, erWidth - 1);
//            g.drawRect(erX, erY, erWidth-2, erWidth- 2);

            g.dispose();
//            ImageIO.write(img, imageType, new File("F:\\1.jpg"));

            ImageIO.write(img, imageType, bs);
            bs.flush();
            bs.close();
        }
        catch (Exception e)
        {
            System.out.println("生成二维码图片失败");

            e.printStackTrace();
        }
        return bs.toByteArray();
    }


    /**
     * 生成二维码
     * @author tansitao
     * @time 2018/5/18 11:26 
     *
     */
    public static void zxingCodeCreate(String text, int erWidth, ByteArrayOutputStream  bos, String imageType) throws  Exception
    {
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        try
        {
            //设置编码字符集
            hints.put(EncodeHintType.MARGIN, 0);
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //1、生成二维码
            BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, erWidth, erWidth, hints);
            //2去白边
            int[] rec = bitMatrix.getEnclosingRectangle();
            int resWidth = rec[2] + 1;
            int resHeight = rec[3] + 1;
            BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
            resMatrix.clear();
            for (int i = 0; i < resWidth; i++)
            {
                for (int j = 0; j < resHeight; j++)
                {
                    if (bitMatrix.get(i + rec[0], j + rec[1]))
                    {
                        resMatrix.set(i, j);
                    }
                }
            }
            //3、获取二维码宽高
            int width = resMatrix.getWidth();
            int height = resMatrix.getHeight();
            //4、将二维码放入缓冲流
            BufferedImage image = new BufferedImage(width, height,BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < width; x++)
            {
                for (int y = 0; y < height; y++)
                {
                    //4、循环将二维码内容定入图片
                    image.setRGB(x, y, resMatrix.get(x, y) == true ? Color.BLACK.getRGB():Color.WHITE.getRGB());
                }
            }
            //5、将二维码写入图片
            ImageIO.write(image, imageType, bos);
        }
        catch (Exception e)
        {
            System.out.println("生成二维码图片失败");
            e.printStackTrace();
        }

    }

    // 缓存文件头信息-文件头信息
    public static final HashMap<String, String> mFileTypes = new HashMap<String, String>();

    static {
        // images
        //edit by tansitao 时间： 2018/5/5 原因：修改jpg文件头
        mFileTypes.put("FFD8FFE1", "jpg");
        mFileTypes.put("FFD8FFE0", "jpg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("47494638", "gif");
        mFileTypes.put("49492A00", "tif");
        mFileTypes.put("424D", "bmp");

        mFileTypes.put("41433130", "dwg"); // CAD
        mFileTypes.put("38425053", "psd");
        mFileTypes.put("7B5C727466", "rtf"); // 日记本
        mFileTypes.put("3C3F786D6C", "xml");
        mFileTypes.put("68746D6C3E", "html");
        mFileTypes.put("44656C69766572792D646174653A", "eml"); // 邮件
        mFileTypes.put("D0CF11E0", "doc");
        mFileTypes.put("D0CF11E0", "xls");//excel2003版本文件
        mFileTypes.put("5374616E64617264204A", "mdb");
        mFileTypes.put("252150532D41646F6265", "ps");
        mFileTypes.put("255044462D312E", "pdf");
        mFileTypes.put("504B0304", "docx");
        mFileTypes.put("504B0304", "xlsx");//excel2007以上版本文件
        mFileTypes.put("52617221", "rar");
        mFileTypes.put("57415645", "wav");
        mFileTypes.put("41564920", "avi");
        mFileTypes.put("2E524D46", "rm");
        mFileTypes.put("000001BA", "mpg");
        mFileTypes.put("000001B3", "mpg");
        mFileTypes.put("6D6F6F76", "mov");
        mFileTypes.put("3026B2758E66CF11", "asf");
        mFileTypes.put("4D546864", "mid");
        mFileTypes.put("1F8B08", "gz");
    }

    /**
     * @param inputStream 上传文件的流
     * @return 文件头信息
     * @author guoxk
     * <p>
     * 方法描述：根据文件路径获取文件头信息
     */
    public static String getFileType(InputStream inputStream) {
        return mFileTypes.get(getFileHeader(inputStream));
    }

    /**
     * @param inputStream 上传文件的流
     * @return 文件头信息
     * @author guoxk
     * <p>
     * 方法描述：根据文件路径获取文件头信息
     */
    public static String getFileHeader(InputStream inputStream) {
        String value = null;
        try {
            byte[] b = new byte[4];
            /*
             * int read() 从此输入流中读取一个数据字节。int read(byte[] b) 从此输入流中将最多 b.length
             * 个字节的数据读入一个 byte 数组中。 int read(byte[] b, int off, int len)
             * 从此输入流中将最多 len 个字节的数据读入一个 byte 数组中。
             */
            inputStream.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    /**
     * @param src 要读取文件头信息的文件的byte数组
     * @return 文件头信息
     * @author guoxk
     * <p>
     * 方法描述：将要读取文件头信息的文件的byte数组转换成string类型表示
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
//      System.out.println(builder.toString());
        return builder.toString();
    }

    /**
     * 识别是否是支付宝、微信的支付二维码
     * @author fumy
     * @time 2018.11.22 16:26
     * @param inputStream
     * @return true
     */
    public static boolean decodeQrCode(InputStream inputStream){
        BufferedImage bufImg = null;
        try {
            bufImg = ImageIO.read(inputStream);

            LuminanceSource source = new BufferedImageLuminanceSource(bufImg);
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            // 对图像进行解码
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);

            String resStr = result.getText();
            System.out.println(resStr);
            resStr = resStr.toUpperCase();
            String wxp = "wxp://".toUpperCase();
            String alipay = "HTTPS://QR.ALIPAY.COM/".toUpperCase();
            if(contains(resStr,wxp) || contains(resStr,alipay)){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
//        return "不是支付二维码";
        return false;
    }


//    public static void main(String[] args) {
//        File file = new File("F:\\1000.png");
//        try {
//            boolean t = decodeQrCode(new FileInputStream(file));
//            System.out.println(t);
//        }catch (Exception e){
//        }
//
//    }


    /**
     * 字符串1是否包含字符串2
     * @param obj
     * @param element
     * @return
     */
    public static boolean contains(String obj, String element) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            if (element == null) {
                return false;
            }
            return ((String) obj).contains(element.toString());
        }
        return false;
    }
}
