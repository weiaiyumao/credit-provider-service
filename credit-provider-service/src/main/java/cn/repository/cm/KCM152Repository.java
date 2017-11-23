package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM152;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM152Repository extends MongoRepository<KCM152, String> ,BaseMobileDetailRepository<KCM152,String>{

    
}
