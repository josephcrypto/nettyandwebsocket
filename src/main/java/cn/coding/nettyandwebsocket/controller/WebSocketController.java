package cn.coding.nettyandwebsocket.controller;

import cn.coding.nettyandwebsocket.group.NettyChannelGroup;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelMatcher;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

@Controller
public class WebSocketController {

    @RequestMapping("/")
    @ResponseBody
    public String home() throws Exception {
        return "index";
    }

    @RequestMapping("/sendNettyMessageToUsers")
    @ResponseBody
    public String sendNettyMessageToUsers(String userIds, String message) throws Exception {
        List<String> userIdList = Arrays.asList(userIds.split(","));
        ChannelMatcher ChannelMatcher = new ChannelMatcher() {
            @Override
            public boolean matches(Channel channel) {
                if (userIdList.contains(channel.attr(NettyChannelGroup.userIdKey).get())) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        NettyChannelGroup.group.writeAndFlush(new TextWebSocketFrame(message), ChannelMatcher);
        return "1";
    }
}
