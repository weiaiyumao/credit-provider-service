package cn.service.cu.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cu.CU1709;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cu.CU1709Service;

@Service
public class CU1709ServiceImpl implements CU1709Service {

	@Autowired
	private BaseMobileDetailRepository<CU1709, String> repository;
	
	@Override
	public List<CU1709> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CU1709> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
