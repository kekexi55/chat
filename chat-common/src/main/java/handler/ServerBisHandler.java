package handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import model.chat.ChatMsg;
import model.chat.MsgType;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerBisHandler extends SimpleChannelInboundHandler<ChatMsg> {
    public static AtomicInteger connectionCount = new AtomicInteger(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for(int i=0;i<100;i++){
            ChatMsg chatMsg = new ChatMsg();
            chatMsg.setMsgId(UUID.randomUUID().toString());
            chatMsg.setTimestamp(System.currentTimeMillis());
            chatMsg.setMsgType(MsgType.MSGTYPE_CHAT);

            ctx.channel().writeAndFlush(chatMsg);
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatMsg chatMsg) throws Exception {


    }

    /*@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("有新连接了，当前连接数：" + connectionCount.incrementAndGet());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("断开连接，当前连接数：" + connectionCount.decrementAndGet());
        ctx.fireChannelInactive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {

    }*/


}