package com.spark.bitrade.controller.common;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSException;
import com.baidu.ueditor.ActionEnter;
import com.baidu.ueditor.PathFormat;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.upload.StorageManager;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.config.AliyunConfig;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.util.*;
import com.sparkframework.lang.Convert;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/common/upload")
public class UploadController extends BaseController {
    @Autowired
    private LocaleMessageSourceService sourceService;

    private static Log log = LogFactory.getLog(UploadController.class);
    private Logger logger = LoggerFactory.getLogger(UploadController.class);
    private String savePath = "data/upload/{:cate}/{yyyy}{mm}{dd}/{time}{rand:6}";
    private String allowedFormat = ".jpg,.gif,.png";
    private long maxAllowedSize = 1024 * 10000;

    @Autowired
    private AliyunConfig aliyunConfig;
    //add by tansitao 时间： 2018/5/20 原因：添加亚马逊s3
//    @Autowired
//    private AwsConfig awsConfig;

    @RequestMapping(value = "/oss/image", method = RequestMethod.POST)
    @ResponseBody
    @AccessLog(module = AdminModule.COMMON, operation = "上传oss图片")
    public String uploadOssImage(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam("file") MultipartFile file, BooleanEnum isPublic) throws IOException {
        log.info(request.getSession().getServletContext().getResource("/"));

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        if (!ServletFileUpload.isMultipartContent(request)) {
            return MessageResult.error(500, "表单格式不正确").toString();
        }
        if (file == null) {
            return MessageResult.error(500, "未找到上传数据").toString();
        }

        String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
        //add by tanst 时间： 2018.04.20 原因：添加阿里云配置
        try {
            String fileName = file.getOriginalFilename();
            String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            String key = directory + GeneratorUtil.getUUID() + suffix;
            //edit by tansitao 时间： 2018/5/20 原因：修改为上传到亚马逊
//            AwsS3Util awsS3Util = new AwsS3Util(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret(), awsConfig.getEndpoint(), awsConfig.getBucketName(), awsConfig.getRegion());
//            String uri = AwsS3Util.upLoadImg(file.getInputStream(), key, true, awsConfig.getOverTime());
            log.info("==================上传文件大小=================" + file.getInputStream().available()/1024 + "KB");
           // >>> add by zyj 2018-11-08 增加阿里云私有库和公有库的判断 start
            boolean isPub=true;
            if (isPublic==BooleanEnum.IS_FALSE){
                isPub=false;
            }
            if (isPublic==BooleanEnum.IS_TRUE){
                isPub=true;
            }
            // <<< add by zyj 2018-11-08 增加阿里云私有库和公有库的判断 end
            String uri = AliyunUtil.upLoadImg(aliyunConfig, file.getInputStream(), key, isPub);
                    //edit by tansitao 时间： 2018/6/10 原因：判断是否上传成功
            Assert.isTrue(!StringUtils.isEmpty(uri),"上传失败");
            //edit by tansitao 时间： 2018/6/10 原因：生成公有图片专用url路径
//            String url = AwsS3Util.getImgPublicUrl(awsConfig.getImgUri(), key);
//            String url = AliyunUtil.getImgPublicUrl(aliyunConfig, key);
            MessageResult mr = new MessageResult(0, "上传成功");
            mr.setData(uri);

            return mr.toString();
        } catch (OSSException oe) {
            return MessageResult.error(500, oe.getErrorMessage()).toString();
        } catch (ClientException ce) {
            System.out.println("Error Message: " + ce.getMessage());
            return MessageResult.error(500, ce.getErrorMessage()).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, e.getMessage()).toString();
        }
    }

    /**
     * 增加限制图片上传方法
     * @author tansitao
     * @time 2018/9/4 15:58 
     */
    @RequestMapping(value = "/oss/limitImage", method = RequestMethod.POST)
    @ResponseBody
    @AccessLog(module = AdminModule.COMMON, operation = "上传oss图片")
    public String uploadOssLimitImage(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam("file") MultipartFile file) throws IOException {
        log.info(request.getSession().getServletContext().getResource("/"));

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        if (!ServletFileUpload.isMultipartContent(request)) {
            return MessageResult.error(500, "表单格式不正确").toString();
        }
        if (file == null) {
            return MessageResult.error(500, "未找到上传数据").toString();
        }

        String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
        //add by tanst 时间： 2018.04.20 原因：添加阿里云配置
        try {
            String fileName = file.getOriginalFilename();
            String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            String key = directory + GeneratorUtil.getUUID() + suffix;
            //edit by tansitao 时间： 2018/5/20 原因：修改为上传到亚马逊
//            AwsS3Util awsS3Util = new AwsS3Util(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret(), awsConfig.getEndpoint(), awsConfig.getBucketName(), awsConfig.getRegion());
//            String uri = AwsS3Util.upLoadImg(file.getInputStream(), key, true, awsConfig.getOverTime());
            log.info("==================上传文件大小=================" + file.getInputStream().available()/1024 + "KB");
            Assert.isTrue(file.getInputStream().available()/1024 <= 20 , "上传图片不能大于20KB");
            String uri = AliyunUtil.upLoadImg(aliyunConfig, file.getInputStream(), key, true);
            //edit by tansitao 时间： 2018/6/10 原因：判断是否上传成功
            Assert.isTrue(!StringUtils.isEmpty(uri),"上传失败");
            //edit by tansitao 时间： 2018/6/10 原因：生成公有图片专用url路径
//            String url = AwsS3Util.getImgPublicUrl(awsConfig.getImgUri(), key);
//            String url = AliyunUtil.getImgPublicUrl(aliyunConfig, key);
            MessageResult mr = new MessageResult(0, "上传成功");
            mr.setData(uri);

            return mr.toString();
        } catch (OSSException oe) {
            return MessageResult.error(500, oe.getErrorMessage()).toString();
        } catch (ClientException ce) {
            System.out.println("Error Message: " + ce.getMessage());
            return MessageResult.error(500, ce.getErrorMessage()).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResult.error(500, e.getMessage()).toString();
        }
    }

    @RequestMapping(value = "/local/image", method = RequestMethod.POST)
    @ResponseBody
    @AccessLog(module = AdminModule.COMMON, operation = "上传oss图片")
    public String uploadLocalImage(HttpServletRequest request, HttpServletResponse response,
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
            return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT")).toString();
        }
            String result= UploadFileUtil.uploadFile(file,fileName);
        if(result!=null){
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(result);
            return mr.toString();
        }else{
            MessageResult mr = new MessageResult(0, sourceService.getMessage("FAILED_TO_WRITE"));
            mr.setData(result);
            return mr.toString();
        }
    }

    @RequiresPermissions("common:upload:oss:base64")
    @RequestMapping(value = "/oss/base64", method = RequestMethod.POST)
    @ResponseBody
    @AccessLog(module = AdminModule.COMMON, operation = "base64上传oss")
    public MessageResult base64UpLoad(@RequestParam String base64Data) {
        MessageResult result = new MessageResult();
        try {
            logger.debug("上传文件的数据：" + base64Data);
            String dataPrix = "";
            String data = "";

            logger.debug("对数据进行判断");
            if (base64Data == null || "".equals(base64Data)) {
                throw new Exception("上传失败，上传图片数据为空");
            } else {
                String[] d = base64Data.split("base64,");
                if (d != null && d.length == 2) {
                    dataPrix = d[0];
                    data = d[1];
                } else {
                    throw new Exception("上传失败，数据不合法");
                }
            }

            logger.debug("对数据进行解析，获取文件名和流数据");
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
                throw new Exception("上传图片格式不合法");
            }
            String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
            String key = directory + GeneratorUtil.getUUID() + suffix;

            //edit by yangch 时间： 2018.04.20 原因：修改阿里云oss配置
            //因为BASE64Decoder的jar问题，此处使用spring框架提供的工具包
            byte[] bs = Base64Utils.decodeFromString(data);
            try {
                //使用apache提供的工具类操作流
                InputStream is = new ByteArrayInputStream(bs);
                //edit by tansitao 时间： 2018/5/20 原因：修改为上传到亚马逊
//                AwsS3Util awsS3Util = new AwsS3Util(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret(), awsConfig.getEndpoint(), awsConfig.getBucketName(), awsConfig.getRegion());
//                String uri = AwsS3Util.upLoadImg(is, key, true, awsConfig.getOverTime());
                String uri = AliyunUtil.upLoadImg(aliyunConfig, is, key, true);
                MessageResult mr = new MessageResult(0, "上传成功");
                mr.setData(uri);
                logger.debug("上传成功,key:{}", key);
                return mr;
            } catch (Exception ee) {
                throw new Exception("上传失败，写入文件失败，" + ee.getMessage());
            }
        } catch (Exception e) {
            logger.debug("上传失败," + e.getMessage());
            result.setCode(500);
            result.setMessage("上传失败," + e.getMessage());
        }
        return result;
    }

    @RequiresPermissions("common:upload:um:image")
    @RequestMapping(value = "/um/image", method = RequestMethod.POST)
    @ResponseBody
    @AccessLog(module = AdminModule.CMS, operation = "上传um图片")
    public String uploadUMImage(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam("upfile") MultipartFile file) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        JSONObject result = new JSONObject();
        if (!ServletFileUpload.isMultipartContent(request)) {
            result.put("state", "表单格式不正确");
            return result.toString();
        }
        if (file == null) {
            result.put("state", "未找到上传数据");
            return result.toString();
        }

        String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());

        try {
            String fileName = file.getOriginalFilename();
            String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            String key = directory + GeneratorUtil.getUUID() + suffix;
            //edit by tansitao 时间： 2018/5/20 原因：修改为上传到亚马逊
//            AwsS3Util awsS3Util = new AwsS3Util(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret(), awsConfig.getEndpoint(), awsConfig.getBucketName(), awsConfig.getRegion());
//            String uri = AwsS3Util.upLoadImg(file.getInputStream(), key, false, awsConfig.getOverTime());
            String uri = AliyunUtil.upLoadImg(aliyunConfig, file.getInputStream(), key, true);
            result.put("url", uri);
            result.put("state", "SUCCESS");
            return result.toString();
        } catch (OSSException oe) {
            result.put("state", oe.getErrorMessage());
            return result.toString();
        } catch (ClientException ce) {
            result.put("state", ce.getErrorMessage());
            return result.toString();
        } catch (Throwable e) {
            e.printStackTrace();
            result.put("state", "系统错误");
            return result.toString();
        }
    }

    @RequiresPermissions("common:upload:images")
    @RequestMapping(value = "/images", method = RequestMethod.POST)
    @ResponseBody
    @AccessLog(module = AdminModule.COMMON, operation = "上传图片")
    public String uploadBatchFile(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam("file") MultipartFile[] files) throws IOException {
        int type = Convert.strToInt(request.getParameter("type"), 0);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        if (!ServletFileUpload.isMultipartContent(request)) {
            out.write(error(500, "表单格式不正确").toString());
        }
        try {
            List<String> savedFiles = new ArrayList<String>();
            if (files.length == 0) {
                return error(500, "未找到上传数据").toString();
            }
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                log.info("fileName:" + file.getName());
                log.info("fileOrginName:" + file.getOriginalFilename());
                // 获取文件后缀
                String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
                if (!allowedFormat.contains(suffix.trim().toLowerCase())) {
                    return MessageResult.error(401, "不允许的图片格式，请上传" + allowedFormat + "格式！").toString();
                }
                InputStream is = file.getInputStream();
                // 相对工程路径
                String relativePath = PathFormat.parse(parseCatePath(savePath, type), fileName) + suffix;
                // 磁盘绝对路径
                String physicalPath = request.getSession().getServletContext().getRealPath("/") + relativePath;

                BaseState storageState = (BaseState) StorageManager.saveFileByInputStream(is, physicalPath,
                        maxAllowedSize);
                is.close();
                if (storageState.isSuccess()) {
                    String relativeUrl = "/" + relativePath;
                    String absPath = request.getContextPath() + relativeUrl;
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("text/html; charset=UTF-8");
                    savedFiles.add(absPath);

                } else {
                    out.write(error(500, storageState.getInfo()).toString());
                }
            }
            JSONObject json = new JSONObject();
            json.put("code", 0);
            json.put("message", "上传成功");
            json.put("data", savedFiles);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
            // fileStream = null;
        }
        return error("上传失败").toString();
    }

    /**
     * 处理ueditor 后台动作
     */
    @RequiresPermissions("common:upload:ueditor:dispatch")
    @RequestMapping("/ueditor/dispatch")
    @AccessLog(module = AdminModule.COMMON, operation = "处理ueditor 后台动作")
    public void ueditor(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        String rootPath = request.getSession().getServletContext().getRealPath("/");
        log.info(rootPath);
        try {
            String cfgPath = rootPath + "classes/ueditor";
            String result = new ActionEnter(request, cfgPath).exec();
            PrintWriter writer = response.getWriter();
            writer.write(result);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private JSONObject success(String relativePath, String absPath) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("code", 0);
        obj.put("relativePath", relativePath);
        obj.put("absPath", absPath);
        return obj;
    }

    private String parseCatePath(String origin, int type) {
        String replacement = "default";
        if (type == 1)
            replacement = "member";
        else if (type == 2)
            replacement = "banner";
        else if (type == 3)
            replacement = "cms";
        return origin.replaceFirst("\\{:cate\\}", replacement);
    }

}
