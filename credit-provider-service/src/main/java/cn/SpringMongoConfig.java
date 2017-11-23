package cn;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

@Configuration
public class SpringMongoConfig extends AbstractMongoConfiguration{
  
	
    @Value("${spring.data.mongodb.uri}")
    String uri;
    
    @Value("${spring.data.mongodb.databaseName}")
    String databaseName;
	
    @Override  
    protected String getDatabaseName() {  
        return databaseName;  
    }  
  
    @Override  
    @Bean  
    public Mongo mongo() throws Exception {  
        MongoClientURI mongoClientURI = new MongoClientURI(uri);  
        return new MongoClient(mongoClientURI);  
    }  
    
    // 去掉_class
    @Override  
    @Bean  
    public MongoTemplate mongoTemplate() throws Exception {  
        MongoDbFactory factory = mongoDbFactory();  
  
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(factory), new MongoMappingContext());  
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));  
  
        return new MongoTemplate(factory, converter);  
    }  
  
    @Bean  
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {  
        return new PropertySourcesPlaceholderConfigurer();  
    }  
 
	
}
