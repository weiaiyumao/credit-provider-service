package cn.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;  

@Component 
public class RedisClient {
	
	@Autowired  
    private JedisPool jedisPool;  
      
    public void set(String key, String value) {  
        Jedis jedis = null;  
        try {
        	jedis = jedisPool.getResource();  
            jedis.set(key, value);
            jedis.expire(key, 30 * 60 * 1000);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			//返还到连接池  
            jedis.close(); 
		}
    }  
      
    public String get(String key)  {  
  
        Jedis jedis = null;  
        String value = "";
        try {
        	jedis = jedisPool.getResource();  
        	value = jedis.get(key);  
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			 //返还到连接池  
            jedis.close();  
		}
        
        return value;
    }  
    
    public void remove(String key) {
    	Jedis jedis = null;  
        try {
        	jedis = jedisPool.getResource();  
        	jedis.del(key);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			 //返还到连接池  
            jedis.close();  
		}
        
    }
}
