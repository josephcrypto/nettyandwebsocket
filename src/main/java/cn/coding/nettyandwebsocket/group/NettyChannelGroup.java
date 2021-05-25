package cn.coding.nettyandwebsocket.group;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

public class NettyChannelGroup {

    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static AttributeKey<String> userIdKey = AttributeKey.valueOf("userId");

}
