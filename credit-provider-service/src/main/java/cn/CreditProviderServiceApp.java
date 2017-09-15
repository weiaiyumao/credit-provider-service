package cn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Hello world!
 *
 */
@EnableEurekaClient     // Eureka Client 标识
@SpringBootApplication  // Spring Boot 应用标识
public class CreditProviderServiceApp 
{
    public static void main( String[] args )
    {
    	SpringApplication.run(CreditProviderServiceApp.class, args);
    }
    
}
