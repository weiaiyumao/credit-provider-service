package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM1706;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM1706Repository extends MongoRepository<KCM1706, String> ,BaseMobileDetailRepository<KCM1706,String>{

}
