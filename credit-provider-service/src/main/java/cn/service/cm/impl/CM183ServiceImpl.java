package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM183;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM183Service;

@Service
public class CM183ServiceImpl implements CM183Service {

	@Autowired
	private BaseMobileDetailRepository<CM183, String> repository;
	
	@Override
	public List<CM183> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM183> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}
}
