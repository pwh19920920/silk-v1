package com.spark.bitrade.controller;

import com.spark.bitrade.config.AliyunConfig;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.util.AliyunUtil;
import com.spark.bitrade.util.GeneratorUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.UploadFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

@Controller
@Slf4j
public class UploadController {

    private String allowedFormat = ".jpg,.png";

    private String allowdFileFormat=".doc,.xdoc,.txt,.pdf,.xls,.xlsx,.wps,.docx,.jpg,.png";
    //    @Autowired
//    private AliyunConfig aliyunConfig;
    @Autowired
    private LocaleMessageSourceService sourceService;
    //add by tansitao 时间： 2018/5/20 原因：添加亚马逊s3
//    @Autowired
//    private AwsConfig awsConfig;

    @Autowired
    private AliyunConfig aliyunConfig;
    // 聊天图片OSS配置
    @Value("${aliyun.chatAccessKeyId:}")
    private String chatAccessKeyId;
    @Value("${aliyun.chatAccessKeySecret:}")
    private String chatAccessKeySecret;
    @Value("${aliyun.chatRoleArn:}")
    private String chatRoleArn;
    @Value("${aliyun.chatTokenExpireTime:}")
    private long chatTokenExpireTime;
    @Value("${aliyun.chatPolicyFile:}")
    private String chatPolicyFile;
    @Value("${aliyun.chatEndpoint:}")
    private String chatEndpoint;
    @Value("${aliyun.chatBucketName:}")
    private String chatBucketName;
    @Value("${aliyun.chatOverTime:10}")
    private Integer chatOverTime;

    /**
     * 上传图片到阿里云OSS,
     *
     * @param request
     * @param response
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "upload/oss/image", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult uploadOssImage(HttpServletRequest request, HttpServletResponse response,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) String QR_CODE) throws IOException {
        log.info(request.getSession().getServletContext().getResource("/").toString());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), sourceService.getMessage("FORM_FORMAT_ERROR"));
        Assert.isTrue(file != null, sourceService.getMessage("NOT_FIND_FILE"));
        String fileType = UploadFileUtil.getFileType(file.getInputStream());
        //edit by tansitao 时间： 2018/5/5 原因：修改图片上传处理逻辑
        log.info("============上传文件类型====================" + fileType);
        String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
        try {

            String fileName = file.getOriginalFilename();
            String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            if (!allowedFormat.contains(suffix.trim().toLowerCase())) {
                return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
            }
            if (fileType == null || !allowedFormat.contains(fileType.trim().toLowerCase())) {
                return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
            }
            String key = directory + GeneratorUtil.getUUID() + suffix;

            //add|edit|del by tansitao 时间： 2018/5/5 原因：修改压缩文件
            //压缩文件
            InputStream inputStream = file.getInputStream();

            //如果二维码参数不为空且满足条件，则验证是否是支付二维码
            if (!StringUtils.isEmpty(QR_CODE) && "QR_CODE".equals(QR_CODE)) {
                InputStream inputStream1 = file.getInputStream();
                boolean isPay = UploadFileUtil.decodeQrCode(inputStream1);
                if (!isPay) {
                    log.error("不是支付二维码");
                    return MessageResult.error("请上传正确的收款二维码");
                }
            }

            // 存储图片文件byte数组
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //edit by tansitao 时间： 2018/5/17 原因：修改为等比例压缩
            UploadFileUtil.zipImageFileByRate(inputStream, bos, fileType);
            //获取输出流
            inputStream = new ByteArrayInputStream(bos.toByteArray());

            log.info("==================压缩后文件大小=================" + inputStream.available() / 1024 + "KB");
            //edit by tansitao 时间： 2018/5/20 原因：修改为上传到亚马逊
//            AwsS3Util awsS3Util = new AwsS3Util(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret(), awsConfig.getEndpoint(), awsConfig.getBucketName(), awsConfig.getRegion());
//            String urlString = awsS3Util.upLoadImg(inputStream, key, false, awsConfig.getOverTime());
//            String urlString =  AwsS3Util.upLoadImg(inputStream, key, false, awsConfig.getOverTime());
            String urlString = AliyunUtil.upLoadImg(aliyunConfig, inputStream, key, false);

            log.info("=============上传图片成功 {}==============", urlString);
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setMessage(key);
            mr.setData(urlString);
            return mr;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, sourceService.getMessage("UPLOAD_FAIL"));
        }
    }


    /**
     * 上传文件到阿里云OSS,
     *
     * @param request
     * @param response
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "upload/oss/file", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult uploadOssFile(HttpServletRequest request, HttpServletResponse response,
                                        @RequestParam("file") MultipartFile file) throws IOException {
        log.info(request.getSession().getServletContext().getResource("/").toString());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), sourceService.getMessage("FORM_FORMAT_ERROR"));
        Assert.isTrue(file != null, sourceService.getMessage("NOT_FIND_FILE"));
        String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
        try {

            String fileName = file.getOriginalFilename();
            String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            log.info("============上传文件类型====================" + suffix);
            if (!allowdFileFormat.contains(suffix.trim().toLowerCase())) {
                return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
            }
            String key = directory + GeneratorUtil.getUUID() + suffix;
            //压缩文件
            InputStream inputStream = file.getInputStream();
            // 存储图片文件byte数组
            //获取输出流
            log.info("==================压缩后文件大小=================" + inputStream.available() / 1024 + "KB");
            String urlString = AliyunUtil.upLoadImgForever(aliyunConfig, inputStream, key, false);
            log.info("=============上传文件成功 {}==============", urlString);
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(urlString);
            return mr;
        } catch (Exception e) {
            log.info("上传失败:{}", ExceptionUtils.getFullStackTrace(e));
            return MessageResult.error(500, sourceService.getMessage("UPLOAD_FAIL"));
        }
    }

    /**
     * 上传公有文件到阿里云OSS,
     *
     * @param request
     * @param response
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "upload/oss/publicImage", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult uploadOssPublicImage(HttpServletRequest request, HttpServletResponse response,
                                       @RequestParam("file") MultipartFile file) throws IOException {
        log.info(request.getSession().getServletContext().getResource("/").toString());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), sourceService.getMessage("FORM_FORMAT_ERROR"));
        Assert.isTrue(file != null, sourceService.getMessage("NOT_FIND_FILE"));
        String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
        try {

            String fileName = file.getOriginalFilename();
            String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            log.info("============上传文件类型====================" + suffix);
            if (!allowdFileFormat.contains(suffix.trim().toLowerCase())) {
                return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
            }
            String key = directory + GeneratorUtil.getUUID() + suffix;
            //压缩文件
            InputStream inputStream = file.getInputStream();
            // 存储图片文件byte数组
            //获取输出流
            log.info("==================压缩后文件大小=================" + inputStream.available() / 1024 + "KB");
            String urlString = AliyunUtil.upLoadImg(aliyunConfig, inputStream, key, true);
            log.info("=============上传文件成功 {}==============", urlString);
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(urlString);
            return mr;
        } catch (Exception e) {
            log.info("上传失败:{}", ExceptionUtils.getFullStackTrace(e));
            return MessageResult.error(500, sourceService.getMessage("UPLOAD_FAIL"));
        }
    }

    @RequestMapping(value = "upload/local/image", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult uploadLocalImage(HttpServletRequest request, HttpServletResponse response,
                                          @RequestParam("file") MultipartFile file) throws IOException {
        log.info(request.getSession().getServletContext().getResource("/").toString());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), sourceService.getMessage("FORM_FORMAT_ERROR"));
        Assert.isTrue(file != null, sourceService.getMessage("NOT_FIND_FILE"));
        //验证文件类型
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
        if (!allowedFormat.contains(suffix.trim().toLowerCase())) {
            return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
        }
        String result = UploadFileUtil.uploadFile(file, fileName);
        if (result != null) {
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(result);
            return mr;
        } else {
            MessageResult mr = new MessageResult(0, sourceService.getMessage("FAILED_TO_WRITE"));
            return mr;
        }
    }

    @RequestMapping(value = "/upload/oss/stream", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult streamUpload(String shareUrl, int type) {
        //默认投票背景图
        String bgImg = "http://silktraderpub.oss-cn-hongkong.aliyuncs.com/appdown/%E6%8A%95%E7%A5%A8%E6%88%90%E5%8A%9F.png";
        if (type == 1) {//领奖背景图
            bgImg = "http://silktraderpub.oss-cn-hongkong.aliyuncs.com/appdown/%E9%A2%86%E5%A5%96%E6%88%90%E5%8A%9F.jpg";
        }
//        String shearUrl = "https://www.silktrader.net/#/register?agent=U100899Zn";

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            UploadFileUtil.zxingCodeCreate(shareUrl, 100, bos, "jpg");
            InputStream inputStream1 = new ByteArrayInputStream(bos.toByteArray());
            byte[] bytes = UploadFileUtil.compoundImg(bgImg, inputStream1, "jpg", type);

            InputStream inputStream = new ByteArrayInputStream(bytes);

            String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
            String key = directory + GeneratorUtil.getUUID() + ".jpg";
            // 存储图片文件byte数组
            bos = new ByteArrayOutputStream();
            //edit by tansitao 时间： 2018/5/17 原因：修改为等比例压缩
            UploadFileUtil.zipImageFileByRate(inputStream, bos, "jpg");
            //获取输出流
            inputStream = new ByteArrayInputStream(bos.toByteArray());
            log.info("==================压缩后文件大小=================" + inputStream.available() / 1024 + "KB");
            String urlString = AliyunUtil.upLoadImg(aliyunConfig, inputStream, key, false);

            log.info("=============上传图片成功 {}==============", urlString);
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(urlString);
            return mr;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, sourceService.getMessage("UPLOAD_FAIL"));
        }
    }


    /**
     * 上传base64处理后的图片
     *
     * @param base64Data
     * @return
     */
    @RequestMapping(value = "/upload/oss/base64", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult base64UpLoad(@RequestParam String base64Data, @RequestParam(required = false) String QR_CODE) {
        MessageResult result = new MessageResult();
        try {
            log.debug("上传文件的数据：" + base64Data);
            String dataPrix = "";
            String data = "";
            if (base64Data == null || "".equals(base64Data)) {
                throw new Exception(sourceService.getMessage("NOT_FIND_FILE"));
            } else {
                String[] d = base64Data.split("base64,");
                if (d != null && d.length == 2) {
                    dataPrix = d[0];
                    data = d[1];
                } else {
                    throw new Exception(sourceService.getMessage("DATA_ILLEGAL"));
                }
            }
            log.debug("对数据进行解析，获取文件名和流数据");
            String suffix = "";
            if ("data:image/jpeg;".equalsIgnoreCase(dataPrix)) {//data:image/jpeg;base64,base64编码的jpeg图片数据
                suffix = ".jpg";
            } else if ("data:image/x-icon;".equalsIgnoreCase(dataPrix)) {//data:image/x-icon;base64,base64编码的icon图片数据
                suffix = ".ico";
            } else if ("data:image/gif;".equalsIgnoreCase(dataPrix)) {//data:image/gif;base64,base64编码的gif图片数据
                suffix = ".gif";
            } else if ("data:image/png;".equalsIgnoreCase(dataPrix)) {//data:image/png;base64,base64编码的png图片数据
                suffix = ".png";
            } else {
                throw new Exception(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
            }
            String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
            String key = directory + GeneratorUtil.getUUID() + suffix;

            //因为BASE64Decoder的jar问题，此处使用spring框架提供的工具包
            byte[] bs = Base64Utils.decodeFromString(data);
//            OSSClient ossClient = new OSSClient(aliyunConfig.getOssEndpoint(), aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
            try {
                //如果二维码参数不为空且满足条件，则验证是否是支付二维码
                if (!StringUtils.isEmpty(QR_CODE) && "QR_CODE".equals(QR_CODE)) {
                    InputStream inputStream1 = new ByteArrayInputStream(bs);
                    boolean isPay = UploadFileUtil.decodeQrCode(inputStream1);
                    if (!isPay) {
                        log.error("不是支付二维码");
                        return MessageResult.error("上传图片不是支付二维码");
                    }
                }

                //使用apache提供的工具类操作流
                InputStream is = new ByteArrayInputStream(bs);
                //FileUtils.writeByteArrayToFile(new File(Global.getConfig(UPLOAD_FILE_PAHT), tempFileName), bs);
//                ossClient.putObject(aliyunConfig.getOssBucketName(), key, is);
//                String uri = aliyunConfig.toUrl(key);
                //edit by tansitao 时间： 2018/5/20 原因：修改为上传到亚马逊
//                AwsS3Util awsS3Util = new AwsS3Util(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret(), awsConfig.getEndpoint(), awsConfig.getBucketName(), awsConfig.getRegion());
//                String uri = awsS3Util.upLoadImg(is, key, false, awsConfig.getOverTime());
//                String uri =  AwsS3Util.upLoadImg(is, key, false, awsConfig.getOverTime());
                String uri = AliyunUtil.upLoadImg(aliyunConfig, is, key, false);
                MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
                mr.setData(uri);
                mr.setMessage(key);
                // mr.setMessage(sourceService.getMessage("UPLOAD_SUCCESS"));
                log.debug("上传成功,key:{}", key);
                return mr;
            } catch (Exception ee) {
                log.info(ee.getMessage());
                throw new Exception(sourceService.getMessage("FAILED_TO_WRITE"));
            }
        } catch (Exception e) {
            log.debug("上传失败," + e.getMessage());
            result.setCode(500);
            result.setMessage(e.getMessage());
        }
        return result;
    }


    /**
     * 前端直传OSS, 获取policy
     */
    @ResponseBody
    @RequestMapping(value = "/upload/oss/policy", method = RequestMethod.POST)
    public MessageResult ossUploadPolicy() {
        AliyunConfig aliyunConfig = new AliyunConfig();
        aliyunConfig.setPublicAccessKeyId(chatAccessKeyId);
        aliyunConfig.setPublicAccessKeySecret(chatAccessKeySecret);
        aliyunConfig.setPublicOssEndpoint(chatEndpoint);
        aliyunConfig.setPublicOssBucketName(chatBucketName);
        aliyunConfig.setOverTime(chatOverTime);
        return MessageResult.success("获取成功", AliyunUtil.getPolicy(aliyunConfig));
    }


    /**
     * 前端直传OSS, 获取policy
     */
    @ResponseBody
    @RequestMapping(value = "/upload/sts/policy", method = RequestMethod.POST)
    public MessageResult stsUpload(@SessionAttribute(SESSION_MEMBER) AuthMember member) {
        Map<String, String> ret = AliyunUtil.sts(member.getId(), chatAccessKeyId, chatAccessKeySecret, chatRoleArn, chatTokenExpireTime, chatPolicyFile);
        if (ret.size() > 0) {
            ret.put("endpoint", chatEndpoint);
            ret.put("bucket", chatBucketName);
            return MessageResult.success("获取成功", ret);
        } else {
            return MessageResult.error("获取失败");
        }
    }
}
