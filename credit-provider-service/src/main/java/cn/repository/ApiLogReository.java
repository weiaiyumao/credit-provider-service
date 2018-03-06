package cn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import cn.entity.ApiLog;

public interface ApiLogReository extends MongoRepository<ApiLog, String>{
	
	@Query("{ 'customerId' : ?0 ,'method' : ?1 }")
	Page<ApiLog> getPageByCustomerId(String customerId,String method,Pageable pageable);
}
