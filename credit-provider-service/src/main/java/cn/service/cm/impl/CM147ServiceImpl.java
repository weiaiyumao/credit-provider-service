package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM147;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM147Service;

@Service
public class CM147ServiceImpl implements CM147Service {

	@Autowired
	private BaseMobileDetailRepository<CM147, String> repository;
	
	@Override
	public List<CM147> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM147> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}
}
