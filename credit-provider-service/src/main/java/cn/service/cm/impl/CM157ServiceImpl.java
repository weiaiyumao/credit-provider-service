package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM157;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM157Service;

@Service
public class CM157ServiceImpl implements CM157Service {

	@Autowired
	private BaseMobileDetailRepository<CM157, String> repository;
	
	@Override
	public List<CM157> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM157> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
