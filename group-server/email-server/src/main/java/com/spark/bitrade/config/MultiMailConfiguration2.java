//package com.spark.bitrade.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.mail.MailProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.core.env.Environment;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import javax.mail.MessagingException;
//import javax.mail.internet.MimeMessage;
//import javax.mail.internet.MimeUtility;
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//
///**
// * 多邮箱配置，并提供了邮件内容发送的接口
// * @author yangch
// * @date 2018-4-6
// */
//@Component
//@EnableConfigurationProperties(MailProperties.class)
//@ConfigurationProperties
//@Slf4j
//public class MultiMailConfiguration2 {
//    private final MailProperties email;
//    @Autowired
//    private Environment environment;
//
//    //多邮箱地址
//    @Value("#{'${app.email.multi.activate.list}'.split(',')}")
//    private List<String> emailActivateList;
//
//    //是否启用多邮箱地址
//    @Value("${spring.mail.multi.enable}")
//    private boolean emailMultiEnable;
//
//    @Autowired
//    private JavaMailSender javaMailSender;
//
//    public MultiMailConfiguration2(MailProperties properties) {
//        this.email = properties;
//    }
//
//    public MailProperties getEmail() {
//        return email;
//    }
//
//    public List<String> getEmailActivateList() {
//        return emailActivateList;
//    }
//
//    public void setEmailActivateList(List<String> emailActivateList) {
//        this.emailActivateList = emailActivateList;
//    }
//
//    //获取激活列表中的一个实例ID
//    public String getActivateId(){
//        int random = (int) (Math.random() * emailActivateList.size());
//        return emailActivateList.get(random);
//    }
//
//    /**
//     * 获取发送邮箱账户
//     * @param activateId
//     * @return
//     */
//    public String getAgreedUser(String activateId) {
//        return environment.getProperty("app.email.multi.agreedUser."+activateId);
//    }
//
//    /**
//     * 获取发送邮箱密码
//     * @param activateId
//     * @return
//     */
//    public String getAgreedPwd(String activateId) {
//        return environment.getProperty("app.email.multi.agreedPwd."+activateId);
//    }
//
//
//    /**
//     * 发送邮件内容
//     *
//     * @param toEmail 接受邮箱地址
//     * @param subject 邮件主题
//     * @param htmlConent 邮件内容
//     * @throws MessagingException
//     * @throws IOException
//     */
//    public void sentEmailHtml( String toEmail, String subject, String htmlConent) throws MessagingException, IOException {
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
//        //发送邮件
//        mailSender.send(mimeMessage);
//    }
//
//}
