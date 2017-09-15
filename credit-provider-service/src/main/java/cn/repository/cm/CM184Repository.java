package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM184;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM184Repository extends MongoRepository<CM184, String> ,BaseMobileDetailRepository<CM184,String>{

}
