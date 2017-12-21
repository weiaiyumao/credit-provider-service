package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM137;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM137Repository extends MongoRepository<KCM137, String> ,BaseMobileDetailRepository<KCM137,String>{

    
}
