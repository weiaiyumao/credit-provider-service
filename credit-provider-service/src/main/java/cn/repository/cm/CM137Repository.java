package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM137;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM137Repository extends MongoRepository<CM137, String> ,BaseMobileDetailRepository<CM137,String>{

    
}
