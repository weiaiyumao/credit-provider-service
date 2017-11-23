package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM159;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM159Repository extends MongoRepository<KCM159, String> ,BaseMobileDetailRepository<KCM159,String>{

}
