package utils;

import model.chat.ChatMsg;
import model.chat.MsgFormat;
import model.chat.MsgType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 构建消息体工具类
 */
public class ChatMsgUtil {
    /**
     * 构建登录消息
     *
     * @param uid
     * @return
     */
    public static ChatMsg builLoginMsg(Long uid) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setMsgId(UUID.randomUUID().toString());
        chatMsg.setTimestamp(System.currentTimeMillis());
        chatMsg.setMsgType(MsgType.MSGTYPE_LOGIN);
        chatMsg.setFromUid(uid);
        return chatMsg;
    }

    /**
     * 构建私聊天消息
     * @return
     */
    public static ChatMsg buildSingleChatMsg(Long fromUid,Long toUid){
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setFromUid(fromUid);
        chatMsg.setToUid(toUid);
        chatMsg.setMsgType(MsgType.MSGTYPE_CHAT);
        chatMsg.setMsgId(UUID.randomUUID().toString());
        chatMsg.setTimestamp(System.currentTimeMillis());
        chatMsg.setFormat(MsgFormat.FORMAT_TXT);
        Map<String,Object> body = new HashMap<>();
        body.put("text","测试发消息，打通链路");
        chatMsg.setBody(body);
        return chatMsg;
    }

    /**
     * 构建私PING消息
     * @return
     */
    public static ChatMsg buildPingMsg(Long fromUid){
        ChatMsg chatMsg = new ChatMsg();

        chatMsg.setMsgType(MsgType.MSGTYPE_PING);
        chatMsg.setMsgId(UUID.randomUUID().toString());
        chatMsg.setTimestamp(System.currentTimeMillis());
        chatMsg.setFromUid(fromUid);
        return chatMsg;
    }

    /**
     * 构建私PING消息
     * @return
     */
    public static ChatMsg buildPongMsg(Long toUid){
        ChatMsg chatMsg = new ChatMsg();

        chatMsg.setMsgType(MsgType.MSGTYPE_PONG);
        chatMsg.setMsgId(UUID.randomUUID().toString());
        chatMsg.setTimestamp(System.currentTimeMillis());
        chatMsg.setToUid(toUid);
        return chatMsg;
    }
}
