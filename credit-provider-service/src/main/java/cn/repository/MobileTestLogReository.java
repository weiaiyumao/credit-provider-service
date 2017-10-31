package cn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import cn.entity.MobileTestLog;

public interface MobileTestLogReository extends MongoRepository<MobileTestLog, String>{

	@Query("{ 'userId' : ?0 }")
	Page<MobileTestLog> getPageByUserId(String userId,Pageable pageable);
}
