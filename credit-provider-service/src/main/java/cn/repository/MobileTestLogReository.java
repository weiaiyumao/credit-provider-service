package cn.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import cn.entity.MobileTestLog;

public interface MobileTestLogReository extends MongoRepository<MobileTestLog, String>{

	@Query("{ 'userId' : ?0 ,'type' : ?1 }")
	Page<MobileTestLog> getPageByUserId(String userId,String type,Pageable pageable);
	
	@Query("{ 'userId' : ?0 ,'type' : ?1 }")
	List<MobileTestLog> getListByUserId(String userId,String type);
}
