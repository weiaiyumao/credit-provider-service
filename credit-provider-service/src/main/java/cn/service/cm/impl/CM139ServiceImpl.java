package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM139;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM139Service;

@Service
public class CM139ServiceImpl implements CM139Service {

	@Autowired
	private BaseMobileDetailRepository<CM139, String> repository;
	
	@Override
	public List<CM139> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM139> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
