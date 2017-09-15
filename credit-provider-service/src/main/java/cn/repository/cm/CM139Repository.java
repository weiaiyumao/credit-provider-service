package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM139;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM139Repository extends MongoRepository<CM139, String> ,BaseMobileDetailRepository<CM139,String>{
    
}
