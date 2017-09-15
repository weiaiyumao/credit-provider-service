package cn.service.cu.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cu.CU176;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cu.CU176Service;

@Service
public class CU176ServiceImpl implements CU176Service {

	@Autowired
	private BaseMobileDetailRepository<CU176, String> repository;
	
	@Override
	public List<CU176> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CU176> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
