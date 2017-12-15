package cn.service;

import java.util.Date;
import java.util.List;

import cn.entity.WaterConsumption;

public interface WaterConsumptionService {
	
	List<WaterConsumption> findByConsumptionNum(String consumptionNum);

	List<WaterConsumption> findByUserId(String userId);
	
	List<WaterConsumption> findByTime(Date startTime, Date endTime);
}
