package cn.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import cn.entity.CvsFilePath;
import cn.entity.SpaceMobile;

public interface SpaceMobileReository extends MongoRepository<CvsFilePath, String>{

	@Query("{ 'mobile' : ?0 }")
	List<SpaceMobile> findByMobile(String mobile);
}
