package cn.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import cn.entity.WaterConsumption;

public interface WaterConsumptionReository extends MongoRepository<WaterConsumption, String>{
	
	@Query("{ 'consumptionNum' : ?0 }")
	List<WaterConsumption> findByConsumptionNum(String consumptionNum);
	
	@Query("{ 'userId' : ?0 }")
	List<WaterConsumption> findByuserId(String userId);
	
	@Query("{ 'createTime' : { $gte : ?0 , $lte : ?1 } }")
	List<WaterConsumption> findByTime(Date startTime, Date endTime);
}
