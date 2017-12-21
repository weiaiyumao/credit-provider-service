package cn.service.unknown.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.entity.base.BaseMobileDetail;
import cn.entity.unknown.UnknownMobileDetail;
import cn.repository.UnknownMobileDetailReository;
import cn.service.unknown.UnknownMobileDetailService;

@Service
public class UnknownMobileDetailServiceImpl implements UnknownMobileDetailService {
	
	@Autowired
	private UnknownMobileDetailReository repository;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public List<UnknownMobileDetail> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		Sort sort = new Sort(Direction.DESC,"reportTime");
		Pageable pageable = new PageRequest(1 - 1, 1, sort);
		Page<UnknownMobileDetail> page  = repository.findByMobileAndReportTime(mobile,startTime,endTime,pageable);
		return page.getContent();
	}

	@Override
	public List<UnknownMobileDetail> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

	@Transactional
	@Override
	public void deleteByMobile(BaseMobileDetail mobileDetail,String mobile) {
		List<UnknownMobileDetail> resultList = this.findByMobile(mobile);
		if(resultList == null || resultList.size()<=0){
			mongoTemplate.insert(mobileDetail);
		}else{										
			if(resultList.get(0).getReportTime().getTime()<mobileDetail.getReportTime().getTime()){
				repository.delete(resultList.get(0).getId());
				mongoTemplate.insert(mobileDetail);
			}										
		}		
	}

}
