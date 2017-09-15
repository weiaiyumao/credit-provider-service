package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM158;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM158Service;

@Service
public class CM158ServiceImpl implements CM158Service {

	@Autowired
	private BaseMobileDetailRepository<CM158, String> repository;
	
	@Override
	public List<CM158> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM158> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
