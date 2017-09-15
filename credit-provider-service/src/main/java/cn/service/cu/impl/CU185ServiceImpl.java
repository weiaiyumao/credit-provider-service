package cn.service.cu.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cu.CU185;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cu.CU185Service;

@Service
public class CU185ServiceImpl implements CU185Service {

	@Autowired
	private BaseMobileDetailRepository<CU185, String> repository;
	
	@Override
	public List<CU185> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CU185> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
