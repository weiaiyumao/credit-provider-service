package cn.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.WaterConsumption;
import cn.repository.WaterConsumptionReository;
import cn.service.WaterConsumptionService;

@Service
public class WaterConsumptionServiceImpl implements WaterConsumptionService{

	@Autowired
	private WaterConsumptionReository waterConsumptionReository;
	
	@Override
	public List<WaterConsumption> findByConsumptionNum(String consumptionNum) {
		return waterConsumptionReository.findByConsumptionNum(consumptionNum);
	}

	@Override
	public List<WaterConsumption> findByUserId(String userId) {
		return waterConsumptionReository.findByuserId(userId);
	}

	@Override
	public List<WaterConsumption> findByTime(Date startTime, Date endTime) {
		return waterConsumptionReository.findByTime(startTime, endTime);
	}

}
