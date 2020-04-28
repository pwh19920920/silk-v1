package com.spark.bitrade.util;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.spark.bitrade.config.AliyunConfig;
import com.spark.bitrade.exception.UnexpectedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import sun.misc.BASE64Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class AliyunUtil {
    public static String schema = "https";
    private static String policy = "";

    /*
     * 计算MD5+BASE64
     */
    public static String MD5Base64(String s) {
        if (s == null)
            return null;
        String encodeStr = "";
        byte[] utfBytes = s.getBytes();
        MessageDigest mdTemp;
        try {
            mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(utfBytes);
            byte[] md5Bytes = mdTemp.digest();
            BASE64Encoder b64Encoder = new BASE64Encoder();
            encodeStr = b64Encoder.encode(md5Bytes);
        } catch (Exception e) {
            throw new Error("Failed to generate MD5 : " + e.getMessage());
        }
        return encodeStr;
    }


    /*
     * 计算 HMAC-SHA1
     */
    public static String HMACSha1(String data, String key) {
        String result;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = (new BASE64Encoder()).encode(rawHmac);
        } catch (Exception e) {
            throw new Error("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }

    public static JSONObject doPost(String url, String body, String accessId, String accessKey) throws MalformedURLException, UnirestException {
        String method = "POST";
        String accept = "application/json";
        String content_type = "application/json";
        String path = new URL(url).getFile();
        String date = DateUtil.toGMTString(new Date());
        // 1.对body做MD5+BASE64加密
        String bodyMd5 = MD5Base64(body);
        String stringToSign = method + "\n" + accept + "\n" + bodyMd5 + "\n" + content_type + "\n" + date + "\n"
                + path;
        // 2.计算 HMAC-SHA1
        String signature = HMACSha1(stringToSign, accessKey);
        // 3.得到 authorization header
        String authHeader = "Dataplus " + accessId + ":" + signature;

        HttpResponse<JsonNode> resp = Unirest.post(url)
                .header("accept", accept)
                .header("content-type", content_type)
                .header("date", date)
                .header("Authorization", authHeader)
                .body(body)
                .asJson();
        JSONObject json = resp.getBody().getObject();
        return json;
    }

    /**
     *  * 获取公有图片url
     *  * @author tansitao
     *  * @time 2018/5/20 14:41 
     *  
     */
    public static String getPublicUrl(AliyunConfig aliyunConfig, String key) {
        return String.format("%s://%s.%s/%s", schema, aliyunConfig.getPublicOssBucketName(), aliyunConfig.getPublicOssEndpoint(), key);
    }

    /**
     *  * 获取公有图片url,专为img图片提供
     *  * @author tansitao
     *  * @time 2018/5/20 14:41 
     *  
     */
    public static String getImgPublicUrl(String uri, String key) {
        return String.format("%s://%s/%s", schema, uri, key);
    }


    /**
     *  * 从阿里云获取私有图片url
     *  * @author tansitao
     *  * @time 2018/5/20 14:41 
     *  
     */
    public static String getPrivateUrl(AliyunConfig aliyunConfig, String key) {
        //通过密钥、endPoint获取阿里云连接
        OSSClient ossClient = null;
        String urlString = "";
        try {
            ossClient = new OSSClient(aliyunConfig.getOssEndpoint(), aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
            //设置URL过期时间
            Date expiration = new Date(System.currentTimeMillis() + aliyunConfig.getOverTime() * 60 * 1000);
            URL url = ossClient.generatePresignedUrl(aliyunConfig.getOssBucketName(), key, expiration);
            urlString = url.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnexpectedException("GET_IMG_FAIL");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
            return urlString;
        }

    }

    /**
     *  * 上传图片到阿里云
     *  * @author tansitao
     *  * @time 2018/5/20 13:55 
     *  
     */
    public static String upLoadImg(AliyunConfig aliyunConfig, InputStream inputStream, String key, boolean isPublic) throws Exception {
        String urlString = "";
        String ossEndpoint = aliyunConfig.getOssEndpoint();
        String accessKeyId = aliyunConfig.getAccessKeyId();
        String accessKeySecret = aliyunConfig.getAccessKeySecret();
        String bucketName = aliyunConfig.getOssBucketName();
        OSSClient ossClient = null;
        ObjectMetadata metadata = new ObjectMetadata();
        try {
            ossClient = new OSSClient(ossEndpoint, accessKeyId, accessKeySecret);

            //设置metadata类型长度为stream长度
            metadata.setContentLength(inputStream.available());
            //通过密钥、endPoint、region获取s3连接
            //声明上传文件类型，获取请求上传的request
            PutObjectRequest request = new PutObjectRequest(bucketName, key, inputStream, metadata);
            //判断是否是全球所有人都可以访问
            if (isPublic) {
                request = new PutObjectRequest(aliyunConfig.getPublicOssBucketName(), key, inputStream, metadata);
                //设置为公有模式
                ossClient.putObject(request);
                urlString = String.format("%s://%s.%s/%s", schema, aliyunConfig.getPublicOssBucketName(), aliyunConfig.getPublicOssEndpoint(), key);
            } else {
                //设置URL过期时间
                Date expiration = new Date(System.currentTimeMillis() + aliyunConfig.getOverTime() * 60 * 1000);
//                s3.putObject(request);
                ossClient.putObject(request);
                //生成认证后的图片url
                URL url = ossClient.generatePresignedUrl(bucketName, key, expiration);
                urlString = url.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("UPLOAD_FAIL");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
            return urlString;
        }
    }

    /**
     *  * 上传图片到阿里云
     *  * @author tansitao
     *  * @time 2018/5/20 13:55 
     *  
     */
    public static String upLoadImgForever(AliyunConfig aliyunConfig, InputStream inputStream, String key, boolean isPublic) throws Exception {
        String urlString = "";
        String ossEndpoint = aliyunConfig.getOssEndpoint();
        String accessKeyId = aliyunConfig.getAccessKeyId();
        String accessKeySecret = aliyunConfig.getAccessKeySecret();
        String bucketName = aliyunConfig.getOssBucketName();
        OSSClient ossClient = null;
        ObjectMetadata metadata = new ObjectMetadata();
        try {
            ossClient = new OSSClient(ossEndpoint, accessKeyId, accessKeySecret);

            //设置metadata类型长度为stream长度
            metadata.setContentLength(inputStream.available());
            //通过密钥、endPoint、region获取s3连接
            //声明上传文件类型，获取请求上传的request
            PutObjectRequest request = new PutObjectRequest(bucketName, key, inputStream, metadata);
            //判断是否是全球所有人都可以访问
            if (isPublic) {
                request = new PutObjectRequest(aliyunConfig.getPublicOssBucketName(), key, inputStream, metadata);
                //设置为公有模式
                ossClient.putObject(request);
                urlString = String.format("%s://%s.%s/%s", schema, aliyunConfig.getPublicOssBucketName(), aliyunConfig.getPublicOssEndpoint(), key);
            } else {
                //设置URL过期时间
                Calendar ca=Calendar.getInstance();
                ca.set(Calendar.YEAR,2030);
//                s3.putObject(request);
                ossClient.putObject(request);
                //生成认证后的图片url
                URL url = ossClient.generatePresignedUrl(bucketName, key, ca.getTime());
                urlString = url.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("UPLOAD_FAIL");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
            return urlString;
        }
    }

    /**
     * 上传文件
     */
    public static void uploadFile() throws Throwable {


        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = "oss-cn-hangzhou.aliyuncs.com";
// 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = "LTAIrRdlovZsUx1i";
        String accessKeySecret = "ZJHxMUxCj0JS0pgZZJ6doYO0cEK1Qr";
        String bucketName = "xinhuo-xindai";
        String objectName = "apk.apk";
// 您的回调服务器地址，如http://oss-demo.aliyuncs.com或http://127.0.0.1:9090。
        String callbackUrl = "<yourCallbackServerUrl>";
// 创建OSSClient实例。
        OSSClient ossClient = null;
        try {
            ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
// 上传文件流。
            InputStream inputStream = new FileInputStream(new File("D:\\1.txt"));
            ossClient.putObject(bucketName, objectName, inputStream);
            Date expiration = new Date(System.currentTimeMillis() + 60 * 1000);
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            System.out.println(url.toString());
// 关闭OSSClient。
            ossClient.shutdown();

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorCode());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            ossClient.shutdown();
        }
    }

    public static Map<String, String> getPolicy(AliyunConfig aliyunConfig) {
        String prefix = DateFormatUtils.format(new Date(), "yyyy/MM/dd");     // 根据日期存储文件
        String accessId = aliyunConfig.getPublicAccessKeyId();                        // 请填写您的 accessKeyId
        String accessKey = aliyunConfig.getPublicAccessKeyId();                       // 请填写您的 accessKeySecret
        String endpoint = aliyunConfig.getPublicOssEndpoint();                        // 请填写您的 endpoint
        String bucket = aliyunConfig.getPublicOssBucketName();                        // 请填写您的 bucket
        String host = "http://" + bucket + "." + endpoint;                            // host的格式为 bucket.endpoint
        String dir = "user/chat/" + prefix;                                           // 用户上传文件时指定的前缀

        Map<String, String> respMap = new LinkedHashMap<>();
        OSSClient client = new OSSClient(endpoint, accessId, accessKey);
        try {
            // 过期时间(默认30秒)
            long expireEndTime = System.currentTimeMillis() + 30_000;
            PolicyConditions policyCond = new PolicyConditions();
            // 文件大小
            policyCond.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyCond.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = client.generatePostPolicy(new Date(expireEndTime), policyCond);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            respMap.put("accessId", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respMap;
    }

    public static Map<String, String> sts(long memberId, String accessKeyId, String accessKeySecret, String roleArn, long tokenExpireTime, String policyFile) {
        Map<String, String> respMap = new LinkedHashMap<>();
        try {
            String policy = readPolicy(policyFile);
            String roleSessionName = "chat-" + memberId;

            // 创建一个 Aliyun Acs Client, 用于发起 OpenAPI 请求
            IClientProfile profile = DefaultProfile.getProfile("cn-beijing", accessKeyId, accessKeySecret);
            DefaultAcsClient client = new DefaultAcsClient(profile);

            // 创建一个 AssumeRoleRequest 并设置请求参数
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setVersion("2015-04-01");
            request.setSysMethod(MethodType.POST);
            request.setSysProtocol(ProtocolType.HTTPS);

            request.setRoleArn(roleArn);
            request.setPolicy(policy);
            request.setRoleSessionName(roleSessionName);
            request.setDurationSeconds(tokenExpireTime);

            // 发起请求，并得到response
            final AssumeRoleResponse response = client.getAcsResponse(request);
            respMap.put("AccessKeyId", response.getCredentials().getAccessKeyId());
            respMap.put("AccessKeySecret", response.getCredentials().getAccessKeySecret());
            respMap.put("SecurityToken", response.getCredentials().getBizSecurityToken());
            respMap.put("Expiration", response.getCredentials().getExpiration());
            respMap.put("StatusCode", "200");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respMap;
    }

    private static String readPolicy(String path) {
        if (StringUtils.isBlank(policy)) {
            StringBuilder builder = new StringBuilder();
            ClassPathResource resource;
            BufferedReader bufferedReader = null;
            try {
                resource = new ClassPathResource(path);
                bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(bufferedReader);
            }
            policy = builder.toString();
        }
        return policy;
    }

    private static void close(Closeable... closeables) {
        if (closeables != null && closeables.length > 0) {
            for (Closeable closeable : closeables) {
                try {
                    closeable.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void main(String[] args) throws Throwable {
//        AliyunUtil.uploadFile();
//        getPolicy();
    }
}
