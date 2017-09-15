package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM182;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM182Service;

@Service
public class CM182ServiceImpl implements CM182Service {

	@Autowired
	private BaseMobileDetailRepository<CM182, String> repository;
	
	@Override
	public List<CM182> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM182> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
