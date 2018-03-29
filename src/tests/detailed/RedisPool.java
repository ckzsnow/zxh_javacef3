package tests.detailed;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {

	public static JedisPool pool = null;
	
	static {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(1024);
		config.setMaxIdle(10);
		config.setMaxWaitMillis(10000);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		pool = new JedisPool(
				config,
				Configuration.getValue("redis_server_ip"),
				Integer.valueOf(Configuration.getValue("redis_server_port")),
				Integer.valueOf(Configuration.getValue("redis_timeout")),
				Configuration.getValue("redis_server_passwd"),
				false);
	}

	public static void main(String[] args){
		Jedis jedis = RedisPool.pool.getResource();
		System.out.println(jedis.lpop("ckz"));
		jedis.close();
	}
	
}
