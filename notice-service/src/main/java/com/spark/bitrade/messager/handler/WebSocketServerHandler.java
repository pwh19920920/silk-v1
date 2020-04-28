package com.spark.bitrade.messager.handler;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.mysql.cj.util.StringUtils;
import com.spark.bitrade.constant.messager.JPushDeviceType;
import com.spark.bitrade.constant.LoginType;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.messager.NoticeServiceApplication;
import com.spark.bitrade.messager.dto.SocketClient;
import com.spark.bitrade.messager.service.IMemberInfoService;
import com.spark.bitrade.messager.service.impl.MemberInfoServiceImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * ClassName:WebSocketServerHandler Function: TODO ADD FUNCTION.
 *
 * @author ww
 */

@Component
@Slf4j
@ChannelHandler.Sharable
//SimpleChannelInboundHandler<Object>
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {


    @Qualifier("memberInfoServiceImpl")
    @Autowired
    IMemberInfoService memberInfoService;

    private WebSocketServerHandshaker handshaker;

    private static WebSocketServerHandler webSocketServerHandler;

    SocketClient socketClient = new SocketClient();

    @PostConstruct
    public void init() {
        webSocketServerHandler = this;
    }


    String xAuthToken ;
    String accessAuthToken;

    Long memeberId=0L;


    /**
     * channel 通道 action 活跃的 当客户端主动链接服务端的链接后，这个通道就是活跃的了。也就是客户端与服务端建立了通信通道并且可以传输数据
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 添加
        log.info("客户端与服务端连接成功：idx:{} , size:{} ,{}" ,SocketClient.socketClientIndex,SocketClient.group.size(),ctx.channel().remoteAddress().toString());

        //
        //ctx.channel().writeAndFlush(new TextWebSocketFrame("hello "));
    }

    /**
     * channel 通道 Inactive 不活跃的 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端关闭了通信通道并且不可以传输数据
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 移除
        SocketClient.remove(socketClient);
        // 添加
        log.info("客户端与服务端连接关闭：idx:{} , size:{} ,{}" ,SocketClient.socketClientIndex,SocketClient.group.size(),ctx.channel().remoteAddress().toString());
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        // 传统的HTTP接入
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;
            handleHttpRequest(ctx, req);
            // 判断是否ping消息

        } else if (msg instanceof PingWebSocketFrame) {
            PingWebSocketFrame pingWebSocketFrame = (PingWebSocketFrame) msg;
            ctx.channel().write(new PongWebSocketFrame(pingWebSocketFrame.content().retain()));
        } else if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) msg;
            //



            //文本跳包
            if (textWebSocketFrame.text().equals("ping")) {
                ctx.channel().writeAndFlush(new TextWebSocketFrame("pong"));
                //log.info("{}",(new PongWebSocketFrame()));
            }
            //log.info(textWebSocketFrame.text());

        } else {
            ctx.channel().close();
        }

    }

    /**
     * 接收客户端发送的消息 channel 通道 Read 读 简而言之就是从通道中读取数据，也就是服务端接收客户端发来的数据。但是这个数据在不进行解码时它是ByteBuf类型的
     */


    /*

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
    // 传统的HTTP接入
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(ctx, ((FullHttpRequest) msg));
    // WebSocket接入
            } else if (msg instanceof WebSocketFrame) {
                System.out.println(handshaker.uri());
                if ("anzhuo".equals(ctx.attr(AttributeKey.valueOf("type")).get())) {
                    handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
                } else {
                    handlerWebSocketFrame2(ctx, (WebSocketFrame) msg);
                }
            }
        }
*/

    /**
     * channel 通道 Read 读取 Complete 完成 在通道读取完成后会在这个方法里通知，对应可以做刷新操作 ctx.flush()
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // 如果HTTP解码失败，返回HHTP异常


        // 尝试兼容  sockjs
//        if(req.uri().startsWith("/notice/notice-ws/info")){
//            Random random = new Random();
//            String json = "{\"websocket\":true,\"origins\":[\"*:*\"],\"cookie_needed\":false,\"entropy\":"+Math.abs(random.nextInt())+"}";
//            ByteBuf in = Unpooled.directBuffer().writeBytes(json.getBytes());
//            DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,in);
//            ChannelFuture f = ctx.channel().writeAndFlush(res);
//
//            //ctx.close();
//            //ctx.channel().closeFuture();
//            f.addListener(ChannelFutureListener.CLOSE);
//            //sendHttpResponse(ctx,req,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,in));
//            return;
//        }else
//
        if (!req.getDecoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            log.info("本服务只支持 websockt");
            sendHttpResponse(ctx, "本服务只支持 websockt ");
            return;
        }
        //获取url后置参数
        HttpMethod method = req.getMethod();
        String uri = req.getUri();
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        Map<String, List<String>> parameters = queryStringDecoder.parameters();

        xAuthToken = parameters.containsKey("token")  ? parameters.get("token").get(0) :"";
        if(StringUtils.isNullOrEmpty(xAuthToken)) xAuthToken = parameters.containsKey("x-auth-token")  ? parameters.get("x-auth-token").get(0) :"";
        accessAuthToken = parameters.containsKey("access-auth-token")  ? parameters.get("access-auth-token").get(0) :"";


        if (StringUtils.isNullOrEmpty(xAuthToken) &&StringUtils.isNullOrEmpty(accessAuthToken)) {
            log.info("请提交 token参数 或  access-auth-token 头");
            //ChannelFuture channelFuture =  ctx.channel().writeAndFlush(new TextWebSocketFrame("请提交 token参数 或  access-auth-token 头"));
            //channelFuture.addListener(ChannelFutureListener.CLOSE);
            //sendHttpResponse(ctx,"请提交 token参数 或  access-auth-token 头");
            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST,Unpooled.copiedBuffer("请提交 token参数 或  access-auth-token 头", CharsetUtil.UTF_8));
            resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);

            return;
        }
        if (memberInfoService == null)memberInfoService = NoticeServiceApplication.getBean(MemberInfoServiceImpl.class);
        AuthMember authMember = memberInfoService.getLoginMemberByToken(xAuthToken, accessAuthToken);

        if (authMember == null) {
            log.info("登录失败");
            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST,Unpooled.copiedBuffer("登录失败", CharsetUtil.UTF_8));
            resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
            //ChannelFuture channelFuture =  ctx.channel().writeAndFlush(new TextWebSocketFrame("login_fail"));
            //channelFuture.addListener(ChannelFutureListener.CLOSE);
            //sendHttpResponse(ctx,"login_fail");
            ctx.close();
            return;
        }
        memeberId = authMember.getId();
        log.info("登录成功 memberId:{}",authMember.getId());
        //log.info("websocket logined {} ",mr.getData());
        SocketClient socketClient = new SocketClient();
        socketClient.setMemberId(authMember.getId());
        socketClient.setChannel(ctx.channel());


        //匹配 jpush 通知类型
        JPushDeviceType jPushDeviceType = null;

        LoginType loginType = authMember.getLoginType();

        if (loginType == LoginType.WEB) {
            socketClient.setDeviceType(JPushDeviceType.WEB);
        } else if (loginType == LoginType.IOS) {
            socketClient.setDeviceType(JPushDeviceType.IOS);

        } else if (loginType == LoginType.ANDROID) {
            socketClient.setDeviceType(JPushDeviceType.ANDROID);
        }

        SocketClient.add(socketClient);



        //log.info("{}", ((JSONObject) mr.getData()).getLongValue("id"));

        // 构造握手响应返回，本机测试  WSS  使用 Nginx去完成 以下直接用WS
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                "ws://" + req.headers().get(HttpHeaders.Names.HOST) + uri, null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel()); //sendUnsupportedVersionResponse(ctx.channel());//
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
        //AuthMember member1 = JSON.parseObject(mr.getData().toString(),AuthMember.class);


    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, String message) {

        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
        resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * exception 异常 Caught 抓住 抓住异常，当发生异常的时候，可以做一些相应的处理，比如打印日志、关闭链接
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}