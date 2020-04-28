package com.spark.bitrade.controller.system;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectRequest;
import com.spark.bitrade.config.AliyunConfig;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.util.AliyunUtil;
import com.spark.bitrade.util.MessageResult;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**目前作用 上传apk文件
 * @author lingxing
 * @time 2018.07.24 11:27
 */
@RestController
public class FileUploadManageController  extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadManageController.class);
    //    //    // 站点真实路径
//    private String savePath = "D:\\up_file";
//    //    // 文件保存目录URL
//    private String saveUrl = "http://127.0.0.1:8080";
    // 允许上传文件后缀MAP数组
    private static final HashMap<String, String> extMap = new HashMap<String, String>();
    // 允许上传文件大小MAP数组
    private static final HashMap<String, Long> sizeMap = new HashMap<String, Long>();
    static{
        // 初始后缀名称MAP数组
        extMap.put("image", "apk,ipa");
        // 初始文件大小MAP数组
        sizeMap.put("fileSize", 10 * 1024 * 1024L);
    }
    @Autowired
    AliyunConfig aliyunConfig;
    @PostMapping(value = "/fileUpload")
    @ResponseBody
    public MessageResult fileUpload(HttpServletRequest request, HttpServletResponse response,@RequestParam(value = "file") MultipartFile multipartFile)
            throws ServletException, IOException, FileUploadException {
        String urlString=null;
        //String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
        // 最大文件大小
//        long maxSize = 1000000;
        response.setContentType("text/html; charset=UTF-8");
        if (!ServletFileUpload.isMultipartContent(request)) {
            return MessageResult.error("请选择文件。");
        }
        String dirName = request.getParameter("dir");
        if (dirName == null) {
            dirName = "image";
        }
        if (!extMap.containsKey(dirName)) {
            return MessageResult.error("目录名不正确。");
        }
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding("UTF-8");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Iterator item = multipartRequest.getFileNames();
        //  CommonsMultipartFile multipartFile = null;
        while (item.hasNext()) {
            // 检查文件大小
            //System.out.println(sizeMap.get("imageSize").longValue());
            if (multipartFile.getSize() > sizeMap.get("fileSize").longValue()) {
                return MessageResult.error("上传文件大小超过限制。");
            }
            // 检查扩展名
            String fileExt = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".") + 1)
                    .toLowerCase();
            if (!Arrays.asList(extMap.get(dirName).split(",")).contains(fileExt)) {
                return MessageResult.error("上传文件扩展名是不允许的扩展名。\n只允许" + extMap.get(dirName) + "格式。");
            }
            try {
                // Endpoint以杭州为例，其它Region请按实际情况填写。
                String endpoint = aliyunConfig.getPublicOssEndpoint();
                // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
                String filename = multipartFile.getOriginalFilename();
                String accessKeyId =aliyunConfig.getPublicAccessKeyId();
                String accessKeySecret = aliyunConfig.getPublicAccessKeySecret();
                String bucketName = aliyunConfig.getPublicOssBucketName();
                String objectName = aliyunConfig.getPublicDirectory()+filename;
                // 获取文件名
                // 获取文件后缀
             //   String prefix=filename.substring(filename.lastIndexOf("."));
                // 用uuid作为文件名，防止生成的临时文件重复
//                final File excelFile = File.createTempFile(UUID.randomUUID().toString(), prefix);
//                multipartFile.transferTo(excelFile);

                OSSClient client = new OSSClient(endpoint, accessKeyId, accessKeySecret);
                // 上传网络流。
                //InputStream inputStream = new URL("https://www.aliyun.com/").openStream();
                client.putObject(bucketName, objectName, multipartFile.getInputStream());
// 关闭OSSClient。
                client.shutdown();

//                client.putObject(new PutObjectRequest(bucketName, objectName, excelFile).
//                        <PutObjectRequest>withProgressListener(new GetProgressSample.PutObjectProgressListener()));
                urlString = String.format("%s://%s.%s/%s", "https", bucketName, endpoint, objectName);
              //  deleteFile(excelFile);
            } catch (Exception e) {
                return MessageResult.error("上传文件失败。"+e);
            }
            return MessageResult.success("上传成功",urlString);
        }
        return null;
    }
    private void deleteFile(File... files) {
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
