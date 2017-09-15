package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM151;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM151Service;

@Service
public class CM151ServiceImpl implements CM151Service {

	@Autowired
	private BaseMobileDetailRepository<CM151, String> repository;
	
	@Override
	public List<CM151> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM151> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
