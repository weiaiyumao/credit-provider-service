package cn.service.impl;

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

}
