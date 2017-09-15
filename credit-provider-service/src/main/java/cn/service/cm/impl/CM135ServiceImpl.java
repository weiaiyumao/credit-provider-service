package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM135;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM135Service;

@Service
public class CM135ServiceImpl implements CM135Service {
	
	@Autowired
	private BaseMobileDetailRepository<CM135, String> repository;
	
	@Override
	public List<CM135> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM135> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
