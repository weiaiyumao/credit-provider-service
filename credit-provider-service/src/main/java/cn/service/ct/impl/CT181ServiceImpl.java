package cn.service.ct.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.ct.CT181;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.ct.CT181Service;

@Service
public class CT181ServiceImpl implements CT181Service{
	
	@Autowired
	private BaseMobileDetailRepository<CT181, String> repository;
	
	@Override
	public List<CT181> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CT181> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
