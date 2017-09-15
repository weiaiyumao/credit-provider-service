package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM159;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM159Service;

@Service
public class CM159ServiceImpl implements CM159Service {

	@Autowired
	private BaseMobileDetailRepository<CM159, String> repository;
	
	@Override
	public List<CM159> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM159> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}
}
