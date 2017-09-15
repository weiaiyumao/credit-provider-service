package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM138;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM138Service;

@Service
public class CM138ServiceImpl implements CM138Service {

	@Autowired
	private BaseMobileDetailRepository<CM138, String> repository;
	
	@Override
	public List<CM138> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM138> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}


}
