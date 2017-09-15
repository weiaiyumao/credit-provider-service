package cn.service.ct.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.ct.CT153;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.ct.CT153Service;

@Service
public class CT153ServiceImpl implements CT153Service{
	
	@Autowired
	private BaseMobileDetailRepository<CT153, String> repository;
	
	@Override
	public List<CT153> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CT153> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}
}
