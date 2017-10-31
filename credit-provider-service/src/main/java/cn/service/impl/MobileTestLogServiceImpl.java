package cn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import cn.entity.MobileTestLog;
import cn.repository.MobileTestLogReository;
import cn.service.MobileTestLogService;

@Service
public class MobileTestLogServiceImpl implements MobileTestLogService {

	@Autowired
	private MobileTestLogReository repository;
	
	@Override
	public Page<MobileTestLog> getPageByUserId(int pageNo, int pageSize, String userId) {
		Sort sort = new Sort(Direction.DESC,"createTime");
		Pageable pageable = new PageRequest(pageNo - 1, pageSize, sort);
		Page<MobileTestLog> page = repository.getPageByUserId(userId, pageable);
		return page;
	}

}
