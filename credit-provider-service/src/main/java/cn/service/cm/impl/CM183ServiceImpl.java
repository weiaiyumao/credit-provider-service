package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM183;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM183Service;

@Service
public class CM183ServiceImpl implements CM183Service {

	@Autowired
	private BaseMobileDetailRepository<CM183, String> repository;
	
	@Override
	public List<CM183> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		Sort sort = new Sort(Direction.DESC,"reportTime");
		Pageable pageable = new PageRequest(1 - 1, 1, sort);
		Page<CM183> page  = repository.findByMobileAndReportTime(mobile,startTime,endTime,pageable);
		return page.getContent();
	}

	@Override
	public List<CM183> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}
}
