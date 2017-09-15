package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM178;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM178Service;

@Service
public class CM178ServiceImpl implements CM178Service {

	@Autowired
	private BaseMobileDetailRepository<CM178, String> repository;
	
	@Override
	public List<CM178> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM178> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
