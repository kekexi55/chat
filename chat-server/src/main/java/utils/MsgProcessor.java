package utils;

import com.alibaba.fastjson.JSON;
import constans.RedisKey;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import model.chat.ChatMsg;
import model.chat.ChatType;
import model.chat.MsgType;
import model.chat.RpcMsg;
import model.domain.User;
import properties.CommonPropertiesFile;
import properties.PropertiesMap;
import redis.clients.jedis.Jedis;
import session.ServerSession;
import session.ServerSessionMap;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class MsgProcessor {

    // 当前节点的在线人数
    private static AtomicInteger connectionCount = new AtomicInteger(0);

    /**
     * 处理消息
     * @param channel
     * @param chatMsg
     */
    public void msgProcessor(Channel channel, RpcMsg.Msg msg) {
        validateChatMsg(msg);
        if (msg.getMsgType() == MsgType.MSGTYPE_LOGIN) {
            processorLoginMsg(channel, msg);
        } else if (msg.getMsgType() == MsgType.MSGTYPE_CHAT) { // 聊天消息
            processorChatMsg(msg);
        }
    }

    /**
     * 处理断开连接的事件
     *
     * @param channel
     */
    public void channelInactive(Channel channel) {
        removeSession(channel);
    }

    /**
     * 处理读超时时间
     *
     * @param channel
     */
    public void chennelReadTimeoutEvent(Channel channel) {
        if (channel == null) {
            return;
        }
        removeSession(channel);
        // 删除会话
        channel.close().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                System.out.println("客户端长时间没有发送ping消息，读超时，关闭与客户端连接");
            }
        });
    }

    /**
     * 消息校验
     *
     * @param chatMsg
     */
    private void validateChatMsg(RpcMsg.Msg msg) {
        if (msg == null) {
            throw new IllegalArgumentException("chatMsg is null");
        }
        if (msg.getMsgType() == 0) {
            throw new IllegalArgumentException("msgType error");
        }
    }


    /**
     * 处理连接事件
     *
     * @param channel
     */
    public void channelActive(Channel channel) {
        System.out.println("有新连接了，当前连接数：" + connectionCount.incrementAndGet());
    }

    private void removeSession(Channel channel) {
        System.out.println("断开连接，当前连接数：" + connectionCount.decrementAndGet());
        Attribute<Object> serverSessionKey = channel.attr(AttributeKey.valueOf("SESSION_KEY"));
        Object obj = serverSessionKey.get();
        if (obj instanceof ServerSession) {
            ServerSession session = (ServerSession) obj;
            User user = session.getUser();
            System.out.println("连接断开事件==>" + user + "断开连接");
            ServerSessionMap.removeSession(user.getUid());
            // 删除集群redis的会话
            Jedis jedis = null;
            try {
                jedis = RedisUtil.getJedis();
                jedis.del(RedisKey.sessionStore(user.getUid()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

    private void processorLoginMsg(Channel channel, RpcMsg.Msg msg) {
        System.out.println("登录消息==> uid是 " + msg.getFromUid() + " 的用户登录了");
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getJedis();
            String userJson = jedis.hget(RedisKey.userMapKey(), msg.getFromUid() + "");
            User u = JSON.parseObject(userJson, User.class);

            // 处理本机器内会话
            ServerSession session = new ServerSession(u, channel).bind();
            ServerSessionMap.add(msg.getFromUid(), session);

            //处理集群会话,value->ip:port
            jedis.set(RedisKey.sessionStore(msg.getFromUid()), NodeUtil.node(CommonPropertiesFile.getHost(), Integer.parseInt(PropertiesMap.getProperties("port"))));
            // 暂时模拟向其他用户发消息，打通链路
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 处理聊天消息
     *
     * @param chatMsg
     */
    private void processorChatMsg(RpcMsg.Msg msg) {
        if (msg.getChatType() == ChatType.SINGLE) {
            System.out.println("收到client fromUid= " + msg.getFromUid() + " 的消息");
            SendMsgUtil.sendMsg(msg);
        } else if (msg.getChatType() == ChatType.GROUP) {
            System.out.println("收到群聊消息：" + msg);
            List<Long> toUidList = msg.getToUidListList();
            if (!CollectionUtil.isEmpty(toUidList)) {
                for (Long toUid : toUidList) {
                    RpcMsg.Msg.Builder builder = RpcMsg.Msg.newBuilder(msg);
                    SendMsgUtil.sendMsg(builder.setToUid(toUid).build());
                }
            } else {
                System.out.println("toUidList is empty");
            }
        }
    }

}
