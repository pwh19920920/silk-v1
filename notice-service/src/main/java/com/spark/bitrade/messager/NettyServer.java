package com.spark.bitrade.messager;

/**
 * @author ww
 * @time 2019.09.18 13:28
 */

import com.spark.bitrade.messager.config.ApplicationProperties;
import com.spark.bitrade.messager.handler.WebSocketServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**

 */
//@Service
@Slf4j
@Component
public class NettyServer {
    /*public static void main(String[] args) {
        new NettyServer().run();
    }

    @PostConstruct
    public void initNetty() {
        new Thread() {
            public void run() {
                new NettyServer().run();
            }
        }.start();
    }*/


    int port = 7010;

    public int getPort(){

        //Netty 端口
        if(ApplicationProperties.propertiesMap.containsKey("notice.netty.port")){
            port = Integer.valueOf(ApplicationProperties.getProperty("notice.netty.port"));
        }
        return port;

    }

    WebSocketServerHandler webSocketServerHandler = new WebSocketServerHandler();

    public void run() {
        log.info("===========================Netty端口启动========");
        // Boss线程：由这个线程池提供的线程是boss种类的，用于创建、连接、绑定socket， （有点像门卫）然后把这些socket传给worker线程池。
        // 在服务器端每个监听的socket都有一个boss线程来处理。在客户端，只有一个boss线程来处理所有的socket。
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // Worker线程：Worker线程执行所有的异步I/O，即处理操作
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            // ServerBootstrap 启动NIO服务的辅助启动类,负责初始话netty服务器，并且开始监听端口的socket请求
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup);
            // 设置非阻塞,用它来建立新accept的连接,用于构造serversocketchannel的工厂类
            b.channel(NioServerSocketChannel.class);
            // ChildChannelHandler 对出入的数据进行的业务操作,其继承ChannelInitializer
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel e) throws Exception {
                    // 设置30秒没有读到数据，则触发一个READER_IDLE事件。
                    //IdleStateHandler(READ_IDEL_TIME_OUT, WRITE_IDEL_TIME_OUT, IDEL_TIME_OUT)
                    //e.pipeline().addLast(new IdleStateHandler(30, 0, 0));
                    // HttpServerCodec：将请求和应答消息解码为HTTP消息
                    e.pipeline().addLast("http-codec", new HttpServerCodec());
                    // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
                    e.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
                    // ChunkedWriteHandler：向客户端发送HTML5文件
                    e.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                    // 在管道中添加我们自己的接收数据实现方法
                    e.pipeline().addLast("http-handler", webSocketServerHandler);
                    e.pipeline().addLast(new WebSocketServerProtocolHandler("/notice/notice-ws", "WebSocket", true, 65536 * 10));
                    e.pipeline().addLast("handler", webSocketServerHandler);
                }
            });
            log.info("服务端开启等待客户端连接 . 端口:{}.. ...", getPort());
            Channel ch = b.bind(getPort()).sync().channel();
            ch.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}