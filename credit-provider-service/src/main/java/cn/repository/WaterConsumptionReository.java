package cn.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import cn.entity.WaterConsumption;

public interface WaterConsumptionReository extends MongoRepository<WaterConsumption, String>{
	
	@Query("{ 'consumptionNum' : ?0 }")
	List<WaterConsumption> findByConsumptionNum(String consumptionNum);
}
