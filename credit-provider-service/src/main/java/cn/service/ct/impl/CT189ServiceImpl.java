package cn.service.ct.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.ct.CT189;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.ct.CT189Service;

@Service
public class CT189ServiceImpl implements CT189Service{
	
	@Autowired
	private BaseMobileDetailRepository<CT189, String> repository;
	
	@Override
	public List<CT189> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CT189> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
