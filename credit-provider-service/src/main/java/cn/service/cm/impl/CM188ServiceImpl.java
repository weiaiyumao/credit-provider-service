package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM188;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM188Service;

@Service
public class CM188ServiceImpl implements CM188Service {

	@Autowired
	private BaseMobileDetailRepository<CM188, String> repository;
	
	@Override
	public List<CM188> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM188> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
