package cn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import cn.entity.ApiLog;
import cn.entity.MobileTestLog;
import cn.repository.ApiLogReository;
import cn.repository.MobileTestLogReository;
import cn.service.MobileTestLogService;

@Service
public class MobileTestLogServiceImpl implements MobileTestLogService {

	@Autowired
	private MobileTestLogReository repository;
	
	@Autowired
	private ApiLogReository apiLogReository;
	
	@Override
	public Page<MobileTestLog> getPageByUserId(int pageNo, int pageSize, String userId,String type) {
		Sort sort = new Sort(Direction.DESC,"createTime");
		Pageable pageable = new PageRequest(pageNo - 1, pageSize, sort);
		Page<MobileTestLog> page = repository.getPageByUserId(userId,type, pageable);
		return page;
	}
	
	@Override
	public Page<ApiLog> getPageByCustomerId(int pageNo, int pageSize, String customerId,String method) {
		Sort sort = new Sort(Direction.DESC,"createtime");
		Pageable pageable = new PageRequest(pageNo - 1, pageSize, sort);
		Page<ApiLog> page = apiLogReository.getPageByCustomerId(customerId,method, pageable);
		return page;
	}

}
