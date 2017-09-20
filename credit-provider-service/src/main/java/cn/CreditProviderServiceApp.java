package cn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Hello world!
 *
 */
@EnableEurekaClient     // Eureka Client 标识
@SpringBootApplication  // Spring Boot 应用标识
public class CreditProviderServiceApp extends SpringBootServletInitializer
{
    public static void main( String[] args )
    {
    	SpringApplication.run(CreditProviderServiceApp.class, args);
    }
    
    @Override  
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {  
        builder.sources(this.getClass());  
        return super.configure(builder);  
    }  
}
