package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM134;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM134Service;

@Service
public class CM134ServiceImpl implements CM134Service {

	@Autowired
	private BaseMobileDetailRepository<CM134, String> repository;
	
	@Override
	public List<CM134> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM134> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}
	
}
