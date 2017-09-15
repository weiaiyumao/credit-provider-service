package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM151;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM151Repository extends MongoRepository<CM151, String> ,BaseMobileDetailRepository<CM151,String>{

}
