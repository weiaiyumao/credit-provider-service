package cn.service.cu.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cu.CU131;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cu.CU131Service;

@Service
public class CU131ServiceImpl implements CU131Service {

	@Autowired
	private BaseMobileDetailRepository<CU131, String> repository;
	
	@Override
	public List<CU131> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CU131> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
