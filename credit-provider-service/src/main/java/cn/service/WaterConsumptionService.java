package cn.service;

import java.util.List;

import cn.entity.WaterConsumption;

public interface WaterConsumptionService {
	
	List<WaterConsumption> findByConsumptionNum(String consumptionNum);

}
