package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM139;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM139Repository extends MongoRepository<KCM139, String> ,BaseMobileDetailRepository<KCM139,String>{
    
}
