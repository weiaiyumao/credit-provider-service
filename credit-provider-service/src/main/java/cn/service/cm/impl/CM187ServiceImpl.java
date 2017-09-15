package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM187;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM187Service;

@Service
public class CM187ServiceImpl implements CM187Service {

	@Autowired
	private BaseMobileDetailRepository<CM187, String> repository;
	
	@Override
	public List<CM187> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM187> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
