package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM159;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM159Repository extends MongoRepository<CM159, String> ,BaseMobileDetailRepository<CM159,String>{

}
