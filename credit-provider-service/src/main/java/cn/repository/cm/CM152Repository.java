package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM152;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM152Repository extends MongoRepository<CM152, String> ,BaseMobileDetailRepository<CM152,String>{

    
}
