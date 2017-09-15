package cn.service.ct.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.ct.CT1700;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.ct.CT1700Service;

@Service
public class CT1700ServiceImpl implements CT1700Service{
	
	@Autowired
	private BaseMobileDetailRepository<CT1700, String> repository;
	
	@Override
	public List<CT1700> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CT1700> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
