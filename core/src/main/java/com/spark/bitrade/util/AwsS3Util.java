package com.spark.bitrade.util;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 亚马逊云存储服务 s3 通信工具类
 * 使用 AWS S3 的SDK
 * @author tansitao
 * @time 2018.05.19 14:51
 */
public class AwsS3Util {


    public static String schema = "https";
    public static String endPoint = "s3-ap-southeast-1.amazonaws.com";
    public static String accessKeyId = "AKIAI5C2IUN4TGAMK2EA";
    public static String accessKeySecret = "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";
    public static String bucketName = "deakingbanner";
    public static String region = "ap-southeast-1";

//    aws.accessKeyId
//    aws.accessKeySecret=
//    aws.endpoint=
//    aws.bucketName=
//    aws.overTime=10
//    aws.region=

//    public AwsS3Util(String accessKeyId, String accessKeySecret, String endPoint, String bucketName, String region)
//    {
//        this.endPoint = endPoint;
//        this.accessKeyId = accessKeyId;
//        this.accessKeySecret = accessKeySecret;
//        this.bucketName = bucketName;
//        this.region = region;
//    }

    /**
      * 获取公有图片url
      * @author tansitao
      * @time 2018/5/20 14:41 
      */
    public String getPublicUrl(String key)
    {
        return String.format("%s://%s.%s/%s", schema,bucketName,endPoint,key);
    }

    /**
      * 获取公有图片url,专为img图片提供
      * @author tansitao
      * @time 2018/5/20 14:41 
      */
    public static String getImgPublicUrl(String uri,String key)
    {
        return String.format("%s://%s/%s", schema,uri,key);
    }

    /**
      * 获取私有图片url
      * @author tansitao
      * @time 2018/5/20 14:41 
      */
    public static String getPrivateUrl(String key, int expirationTime)
    {
        AmazonS3 s3 = null;
        //通过密钥、endPoint、region获取s3连接
        s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, accessKeySecret)))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
                .build();
        //设置URL过期时间
        Date expiration = new Date(System.currentTimeMillis() + expirationTime * 60 * 1000);
        URL url = s3.generatePresignedUrl(bucketName, key, expiration);
        return url.toString();
    }

    /**
      * 上传图片到亚马逊
      * @author tansitao
      * @time 2018/5/20 13:55 
      */
    public static String upLoadImg(InputStream inputStream, String key, boolean isPublic, int expirationTime) throws Exception
    {
        String urlString = "";
        AmazonS3 s3 = null;
        ObjectMetadata metadata = new ObjectMetadata();
        try
        {
            //设置metadata类型长度为stream长度
            metadata.setContentLength(inputStream.available());
            //通过密钥、endPoint、region获取s3连接
            s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, accessKeySecret)))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
                    .build();
            //声明上传文件类型，获取请求上传的request
            PutObjectRequest request = new PutObjectRequest(bucketName, key, inputStream, metadata);
            //判断是否是全球所有人都可以访问
            if(isPublic)
            {
                //设置为公有模式
                request.setCannedAcl(CannedAccessControlList.PublicRead);
                s3.putObject(request);
                urlString = String.format("%s://%s.%s/%s", schema,bucketName,endPoint,key);
            }
            else
            {
                //设置URL过期时间
                Date expiration = new Date(System.currentTimeMillis() + expirationTime * 60 * 1000);
                s3.putObject(request);
                //生成认证后的图片url
                URL url = s3.generatePresignedUrl(bucketName, key, expiration);
                urlString = url.toString();
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IllegalStateException("UPLOAD_FAIL");
        }
        finally
        {
            if(s3 != null)
            {
                s3.shutdown();
            }
            return  urlString;
        }


    }
}