package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM1703;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM1703Repository extends MongoRepository<KCM1703, String> ,BaseMobileDetailRepository<KCM1703,String>{

}
