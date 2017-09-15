package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM1705;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM1705Service;

@Service
public class CM1705ServiceImpl implements CM1705Service {

	@Autowired
	private BaseMobileDetailRepository<CM1705, String> repository;
	
	@Override
	public List<CM1705> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM1705> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}
}
