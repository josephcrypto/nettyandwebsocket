package cn.coding.nettyandwebsocket.handler;

import cn.coding.nettyandwebsocket.group.NettyChannelGroup;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.Attribute;
import io.netty.util.CharsetUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MyWebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;

    /**
     * webSocket requires an http handshake, then transfers data on the webSocket protocol
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            //http handshake processing
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            //Transfer data on the webSocket protocol
            handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        ///url contains userId to intercept it in the attr attribute of the channel, put the channel into the ChannelGroup
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.getUri());
        Map<String, List<String>> parameters = queryStringDecoder.parameters();
        //If you do not include userId can not be processed
        if (parameters.size() == 0 || !parameters.containsKey("userId")) {
            return;
        }
        //Set the user id attribute to the channel
        Attribute<String> userId = ctx.channel().attr(NettyChannelGroup.userIdKey);
        userId.set(parameters.get("userId").get(0));
        NettyChannelGroup.group.add(ctx.channel());
        System.out.println(parameters.get("userId").get(0) + " Connected to Server");

        if (!req.getDecoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                "ws:/localhost:2048", null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req,
                                         DefaultFullHttpResponse res) {
        //Return a response to the client
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        //If it is not keep-Alive,close the connection
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static boolean isKeepAlive(FullHttpRequest req) {
        return false;
    }
    //Determine whether to close the link command
    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        //Deter whether to ping the message
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof TextWebSocketFrame) {
            //If you receive ping return tong
            String request = ((TextWebSocketFrame) frame).text();
            if ("ping" .equals(request)){
                ctx.channel().write(new TextWebSocketFrame("tong"));
            }
        }
    }
}
