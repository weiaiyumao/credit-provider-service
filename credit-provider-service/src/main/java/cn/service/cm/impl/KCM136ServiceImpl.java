package cn.service.cm.impl;

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
import cn.entity.cm.KCM136;
import cn.repository.cm.KCM136Repository;
import cn.service.cm.KCM136Service;

@Service
public class KCM136ServiceImpl implements KCM136Service {

	@Autowired
	private KCM136Repository repository;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public List<KCM136> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		Sort sort = new Sort(Direction.DESC,"reportTime");
		Pageable pageable = new PageRequest(1 - 1, 1, sort);
		Page<KCM136> page  = repository.findByMobileAndReportTime(mobile,startTime,endTime,pageable);
		return page.getContent();
	}

	@Override
	public List<KCM136> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}
	
	public List<KCM136> findByfindByMobile(String mobile) {
//	 	int startYear=2017,endYear=2017;  
//        int startMonth=7,endMonth=7;  
//        int startDay=6,endDay=9; 
//		return KCM136Repository.findByMobileAndReportTime(mobile,new Date(startYear - 1900, startMonth - 1, startDay,0,42,32),new Date(endYear - 1900, endMonth - 1, endDay,0,42,31));
        return null;
	}

	@Transactional
	@Override
	public void deleteByMobile(BaseMobileDetail mobileDetail,String mobile) {
		List<KCM136> resultList = this.findByMobile(mobile);
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
