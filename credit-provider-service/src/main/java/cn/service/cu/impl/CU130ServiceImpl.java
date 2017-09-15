package cn.service.cu.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.cu.CU130;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cu.CU130Service;

@Service
public class CU130ServiceImpl implements CU130Service {

	@Autowired
	private BaseMobileDetailRepository<CU130, String> repository;
	
	@Override
	public List<CU130> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		return repository.findByMobileAndReportTime(mobile, startTime, endTime);
	}

	@Override
	public List<CU130> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
