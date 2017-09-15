package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM187;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM187Repository extends MongoRepository<CM187, String> ,BaseMobileDetailRepository<CM187,String>{

}
