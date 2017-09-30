//package cn;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Arrays;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//
//import com.mongodb.MongoClient;
//import com.mongodb.MongoClientOptions;
//import com.mongodb.MongoClientURI;
//import com.mongodb.MongoCredential;
//import com.mongodb.ServerAddress;
//
//import cn.controller.Controller;
//
//@Component
//public class CloudDatabaseConfiguration {
//
//	private final static Logger logger = LoggerFactory.getLogger(Controller.class);
//
//	// @Bean
//	// @ConditionalOnMissingBean(MongoClient.class)
//	// public MongoClient getMongodbClients() {
//	//
//	// ///
//	// //
//	// spring.data.mongodb.uri=mongodb://root:yumao08076619@dds-uf612994c0c73af41.mongodb.rds.aliyuncs.com:3717,dds-uf612994c0c73af42.mongodb.rds.aliyuncs.com:3717/credit?replicaSet=mgset-4536355
//	//
//	// List addresses = new ArrayList();
//	// ServerAddress address1 = new
//	// ServerAddress("dds-uf612994c0c73af41.mongodb.rds.aliyuncs.com", 3717);
//	// ServerAddress address2 = new
//	// ServerAddress("dds-uf612994c0c73af42.mongodb.rds.aliyuncs.com", 3717);
//	// addresses.add(address1);
//	// addresses.add(address2);
//	//
//	//
//	// MongoCredential credential =
//	// MongoCredential.createMongoCRCredential("root", "credit",
//	// "yumao08076619".toCharArray());
//	// MongoClient client = new MongoClient(addresses,
//	// Arrays.asList(credential));
//	// return client;
//	// }
//	
//	
//	 public static ServerAddress seed1 = new
//	 ServerAddress("dds-uf612994c0c73af41.mongodb.rds.aliyuncs.com", 3717);
//	 public static ServerAddress seed2 = new
//	 ServerAddress("dds-uf612994c0c73af42.mongodb.rds.aliyuncs.com", 3717);
//	 public static String username = "root";
//	 public static String password = "yumao08076619";
//	 public static String ReplSetName = "mgset-4536355";
//	 public static String DEFAULT_DB = "admin";
//	 public static String DEMO_DB = "admin";
//	 public static String DEMO_COLL = "admin";
//
//	@Bean
//	@ConditionalOnMissingBean(MongoClient.class)
//	public MongoClient createMongoDBClientWithURI() {
//		// 另一种通过URI初始化
//		// mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
//		MongoClientURI connectionString = new MongoClientURI("mongodb://" + username + ":" + password + "@" + seed1
//				+ "," + seed2 + "/" + DEFAULT_DB + "?replicaSet=" + ReplSetName);
//		return new MongoClient(connectionString);
//	}
//
//
//	//
//	// @Bean
//	// @ConditionalOnMissingBean(MongoClient.class)
//	// public static MongoClient createMongoDBClient() {
//	// // 构建Seed列表
//	// List<ServerAddress> seedList = new ArrayList<ServerAddress>();
//	// seedList.add(seed1);
//	// seedList.add(seed2);
//	// // 构建鉴权信息
//	// List<MongoCredential> credentials = new ArrayList<MongoCredential>();
//	// credentials.add(MongoCredential.createScramSha1Credential(username,
//	// DEFAULT_DB, password.toCharArray()));
//	// // 构建操作选项，requiredReplicaSetName属性外的选项根据自己的实际需求配置，默认参数满足大多数场景
//	// MongoClientOptions options =
//	// MongoClientOptions.builder().requiredReplicaSetName(ReplSetName)
//	// .socketTimeout(2000).connectionsPerHost(1).build();
//	// return new MongoClient(seedList, credentials, options);
//	// }
//	
//	
//	
//	
//}
