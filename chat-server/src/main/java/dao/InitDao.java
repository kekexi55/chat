package dao;

import com.alibaba.fastjson.JSON;
import model.domain.User;
import redis.clients.jedis.Jedis;
import utils.RedisUtil;

import java.util.HashMap;
import java.util.Map;

public class InitDao {
    /**
     * 模拟数据库，创建用户
     */
    public static String userMapKey() {
        return "chat:user:map";
    }

    private static void createUser() {

        Map<String, String> userMap = new HashMap<>();
        for (int i = 0; i < 30; i++) {
            User u = new User(i + 1, "用户" + (i + 1));
            userMap.put(u.getUid() + "", JSON.toJSONString(u));
        }
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getJedis();
            if(!jedis.exists(userMapKey())){
                jedis.hset(userMapKey(), userMap);
                jedis.expire(userMapKey(),3600);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public static void init(){
        createUser();
    }
}