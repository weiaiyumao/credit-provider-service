package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM152;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM152Service;

@Service
public class CM152ServiceImpl implements CM152Service {

	@Autowired
	private BaseMobileDetailRepository<CM152, String> repository;
	
	@Override
	public List<CM152> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CM152> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
