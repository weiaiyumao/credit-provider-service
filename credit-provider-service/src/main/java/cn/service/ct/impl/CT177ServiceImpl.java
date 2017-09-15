package cn.service.ct.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.ct.CT177;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.ct.CT177Service;

@Service
public class CT177ServiceImpl implements CT177Service{
	
	@Autowired
	private BaseMobileDetailRepository<CT177, String> repository;
	
	@Override
	public List<CT177> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CT177> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
