package com.spark.bitrade.config;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.transform.EmailEnity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 多邮箱配置，并提供了邮件内容发送的接口
 * @author yangch
 * @date 2018-4-6
 */
@Component
@EnableConfigurationProperties(MailProperties.class)
@ConfigurationProperties
@Slf4j
public class MultiMailConfiguration  {
    private final MailProperties email;
    @Autowired
    private Environment environment;

    //多邮箱地址
    @Value("#{'${app.email.multi.activate.list}'.split(',')}")
    private List<String> emailActivateList;

    //是否启用多邮箱地址
    @Value("${spring.mail.multi.enable}")
    private boolean emailMultiEnable;

    //是否启用MDaemon
//    @Value("${spring.mail.isOpenMD}")
//    private boolean isOpenMD;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    public MultiMailConfiguration(MailProperties properties) {
        this.email = properties;
    }

    public MailProperties getEmail() {
        return email;
    }

    public List<String> getEmailActivateList() {
        return emailActivateList;
    }

    public void setEmailActivateList(List<String> emailActivateList) {
        this.emailActivateList = emailActivateList;
    }

    //获取激活列表中的一个实例ID
    public String getActivateId(){
        int random = (int) (Math.random() * emailActivateList.size());
        return emailActivateList.get(random);
    }

    /**
     * 获取发送邮箱账户
     * @param activateId
     * @return
     */
    public String getAgreedUser(String activateId) {
        return environment.getProperty("app.email.multi.agreedUser."+activateId);
    }

    /**
     * 获取发送邮箱密码
     * @param activateId
     * @return
     */
    public String getAgreedPwd(String activateId) {
        return environment.getProperty("app.email.multi.agreedPwd."+activateId);
    }


    /**
     * 发送邮件内容
     *
     * @param toEmail 接受邮箱地址
     * @param subject 邮件主题
     * @param htmlConent 邮件内容
     * @throws MessagingException
     * @throws IOException
     */
    public void sentEmailHtml( String toEmail, String subject, String htmlConent) throws MessagingException, IOException {
        //edit by yangch 时间： 2018.10.08 原因：修改为发送消息到kafka
        EmailEnity emailEnity = new EmailEnity();
        emailEnity.setToEmail(toEmail);
        emailEnity.setSubject(subject);
        emailEnity.setHtmlConent(htmlConent);

        kafkaTemplate.send(EmailEnity.MSG_EMAIL_HANDLER,
                toEmail, JSON.toJSONString(emailEnity));

//        String encode = email.getDefaultEncoding().toString();
//        String fromEmail = email.getUsername();
//        JavaMailSender mailSender = javaMailSender;
//
//        if(emailMultiEnable){
//            JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
//
//            if(!StringUtils.isEmpty(email.getHost())) {
//                javaMailSenderImpl.setHost(email.getHost());
//            }
//            if(!StringUtils.isEmpty(email.getPort())) {
//                javaMailSenderImpl.setPort(email.getPort());
//            }
//            if(!StringUtils.isEmpty(email.getProtocol())) {
//                javaMailSenderImpl.setProtocol(email.getProtocol());
//            }
//
//            String activeId= getActivateId();
//            fromEmail = getAgreedUser(activeId);
//            javaMailSenderImpl.setUsername(fromEmail);
//            javaMailSenderImpl.setPassword(getAgreedPwd(activeId));
//
//            //加认证机制
//            Properties javaMailProperties = new Properties();
//            for (Map.Entry<String, String> entry : email.getProperties().entrySet() ) {
//                javaMailProperties.put(entry.getKey(), entry.getValue());
//            }
//            javaMailSenderImpl.setJavaMailProperties(javaMailProperties);
//
//            mailSender = javaMailSenderImpl;
//        }
//
//        //邮件配置
//        MimeMessage mimeMessage = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, encode);
//        helper.setFrom(fromEmail);
//        helper.setTo(toEmail);
//        //helper.setReplyTo(from);
//        helper.setSubject(MimeUtility.encodeText(subject, encode, "B"));
//        helper.setText(htmlConent, true);
//
//        log.info("send email from {} to {},content:{}", fromEmail, toEmail, htmlConent);
//        //是否使用自己搭建的MDaemon邮件服务器
//        if(!isOpenMD){
//            //发送邮件
//            mailSender.send(mimeMessage);
//        }else
//        {
//            String activeId= getActivateId();
//            //端口
//            String port = email.getPort().toString();
//            // 设置服务器
//            String host = email.getHost();
//            // 同时通过认证
//            String auth = "true";
//            // 发送协议
//            String protocol = email.getProtocol();
//            // 发送用户
//            String emailUser = emailMultiEnable == true?getAgreedUser(activeId):email.getUsername();
//            // 认证用户
//            String agreedUser = emailMultiEnable == true?getAgreedUser(activeId):email.getUsername();
//            // 认证密码
//            String agreedPwd = emailMultiEnable == true?getAgreedPwd(activeId):email.getPassword();
//            //自定义发件人名称
//            String customName = emailMultiEnable == true?getAgreedUser(activeId).split("@")[0]:email.getUsername().split("@")[0];
//
//
//            Properties props = new Properties(); // 可以加载一个配置文件
//            // 使用smtp：简单邮件传输协议
//            if (null == port) {
//                props.put("mail.smtp.host", host);
//            }
//
//            //props.put("mail.smtp.ssl.enable", true);
//
//            props.put("mail.smtp.auth", auth);
//            props.put("mail.transport.protocol", protocol);
//
//            //props.put("mail.smtp.starttls.enable", true);
//
//            //props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
//            Session session = Session.getDefaultInstance(props);// 根据属性新建一个邮件会话
//            session.setDebug(true); // 设置为debug模式, 可以查看详细的发送 log
//            MimeMessage message = new MimeMessage(session);// 由邮件会话新建一个消息对象
//
//
//            message.setFrom(new InternetAddress(emailUser));
//
//            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));// 设置收件人,并设置其接收类型为TO
//
//
//            message.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));// 设置标题
//            //设置自定义发件人昵称
//            String nick = "";
//            try {
//                nick = MimeUtility.encodeText(customName);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            message.setFrom(new InternetAddress(nick + " <" + emailUser + ">"));
//            // 设置信件内容
//            //message.setText(htmlConent); // 发送 纯文本 邮件 todo
//            message.setContent(htmlConent, "text/html;charset=gbk"); // 发送HTML邮件，内容样式比较丰富
//            message.setSentDate(new Date());// 设置发信时间
//            message.saveChanges();// 存储邮件信息
//            // 发送邮件
//            Transport transport = session.getTransport();
//            // 第一个参数  认证用户     第二个参数 认证密码
//
//            if (null == port) {
//                transport.connect(agreedUser, agreedPwd);
//            } else {
//
//                transport.connect(host, Integer.valueOf(port), agreedUser, agreedPwd);
//            }
//            transport.sendMessage(message, message.getAllRecipients());// 发送邮件,其中第二个参数是所有已设好的收件人地址
//            transport.close();
//        }

    }

}
