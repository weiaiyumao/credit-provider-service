package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM150;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM150Repository extends MongoRepository<KCM150, String> ,BaseMobileDetailRepository<KCM150,String>{
    
	
//	Page<CM150> findByMobile(String mobile,Pageable pageable); 
}
