package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM184;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM184Service;

@Service
public class CM184ServiceImpl implements CM184Service {

	@Autowired
	private BaseMobileDetailRepository<CM184, String> repository;
	
	@Override
	public List<CM184> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM184> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
