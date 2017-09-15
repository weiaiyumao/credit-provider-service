package cn.service.ct.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.ct.CT133;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.ct.CT133Service;

@Service
public class CT133ServiceImpl implements CT133Service{
	
	@Autowired
	private BaseMobileDetailRepository<CT133, String> repository;
	
	@Override
	public List<CT133> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CT133> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
