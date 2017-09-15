package cn.service.unknown.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.unknown.UnknownMobileDetail;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.unknown.UnknownMobileDetailService;

@Service
public class UnknownMobileDetailServiceImpl implements UnknownMobileDetailService {
	
	@Autowired
	private BaseMobileDetailRepository<UnknownMobileDetail, String> repository;
	
	@Override
	public List<UnknownMobileDetail> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<UnknownMobileDetail> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
