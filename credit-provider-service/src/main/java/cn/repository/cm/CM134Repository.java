package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM134;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM134Repository extends MongoRepository<CM134, String>,BaseMobileDetailRepository<CM134,String>{

}
