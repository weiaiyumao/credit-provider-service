package cn.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfiguration {

	// @Bean(name = "jedis.pool")
	// @Autowired
	// public JedisPool jedisPool(@Qualifier("jedis.pool.config")
	// JedisPoolConfig config,
	// @Value("${jedis.pool.host}") String host, @Value("${jedis.pool.port}")
	// int port,@Value("${jedis.pool.password}") String password) {
	// return new JedisPool(config, host, port, 10000, password);
	// }
	//
	// @Bean(name = "jedis.pool.config")
	// public JedisPoolConfig
	// jedisPoolConfig(@Value("${jedis.pool.config.maxTotal}") int maxTotal,
	// @Value("${jedis.pool.config.maxIdle}") int maxIdle,
	// @Value("${jedis.pool.config.maxWaitMillis}") int maxWaitMillis) {
	// JedisPoolConfig config = new JedisPoolConfig();
	// config.setMaxTotal(maxTotal);
	// config.setMaxIdle(maxIdle);
	// config.setMaxWaitMillis(maxWaitMillis);
	// return config;
	// }
	
	@Value("${jedis.pool.host}")
	private String host;

	@Value("${jedis.pool.port}")
	private int port;

	@Value("${jedis.pool.config.maxTotal}")
	private int maxTotal;

	@Value("${jedis.pool.config.maxIdle}")
	private int maxIdle;

	@Value("${jedis.pool.config.maxWaitMillis}")
	private int maxWaitMillis;
	

	@Bean(name = "jedis.pool")
	@Autowired
	public JedisPool jedisPool(@Qualifier("jedis.pool.config") JedisPoolConfig config) {
		return new JedisPool(config, host, port);
	}

	@Bean(name = "jedis.pool.config")
	public JedisPoolConfig jedisPoolConfig() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMaxWaitMillis(maxWaitMillis);
		return config;
	}

	@Bean
	public JedisConnectionFactory connectionFactory() {
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
		jedisConnectionFactory.setUsePool(true);
		jedisConnectionFactory.setPoolConfig(this.jedisPoolConfig());
		jedisConnectionFactory.setHostName(host);
		jedisConnectionFactory.setPort(port);
		return jedisConnectionFactory;
	}

	@Bean
	public RedisTemplate<?, ?> redisTemplate() {
		RedisTemplate<?, ?> rt = new RedisTemplate<Object, Object>();
		rt.setConnectionFactory(connectionFactory());
		return rt;
	}
}

