package cn.service.cu.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cu.CU156;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cu.CU156Service;

@Service
public class CU156ServiceImpl implements CU156Service {

	@Autowired
	private BaseMobileDetailRepository<CU156, String> repository;
	
	@Override
	public List<CU156> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CU156> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
