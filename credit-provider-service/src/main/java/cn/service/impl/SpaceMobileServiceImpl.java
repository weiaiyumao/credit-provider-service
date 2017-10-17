package cn.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.SpaceMobile;
import cn.repository.SpaceMobileReository;
import cn.service.SpaceMobileService;
import cn.utils.CommonUtils;

@Service
public class SpaceMobileServiceImpl implements SpaceMobileService{
	
	@Autowired
	private SpaceMobileReository spaceMobileReository;

	@Override
	public SpaceMobile findByMobile(String mobile) {
		List<SpaceMobile> list = spaceMobileReository.findByMobile(mobile);
		return CommonUtils.isNotEmpty(list) ? null : list.get(0);
	}

}
