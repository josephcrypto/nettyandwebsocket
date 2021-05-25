package cn.coding.nettyandwebsocket.server;

import cn.coding.nettyandwebsocket.handler.MyWebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class WebSocketServerListener implements ApplicationListener<ApplicationStartedEvent> {

    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();

    public void run() {
        ServerBootstrap boot = new ServerBootstrap();
        boot.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                        pipeline.addLast("handler", new MyWebSocketHandler());
                    }
                });
        try {
            Channel ch = boot.bind(2048).sync().channel();
            System.out.println("WebSocket server start at port:2048");
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        new WebSocketServerListener().run();
    }
}
