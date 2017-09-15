package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM150;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM150Service;

@Service
public class CM150ServiceImpl implements CM150Service {

	@Autowired
	private BaseMobileDetailRepository<CM150, String> repository;
	
	@Override
	public List<CM150> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM150> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
