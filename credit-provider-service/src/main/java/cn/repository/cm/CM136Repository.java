package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM136;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */

public interface CM136Repository extends MongoRepository<CM136, String>,BaseMobileDetailRepository<CM136,String>{

}
