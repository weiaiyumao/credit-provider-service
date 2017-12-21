package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM1703;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM1703Repository extends MongoRepository<CM1703, String> ,BaseMobileDetailRepository<CM1703,String>{

}
