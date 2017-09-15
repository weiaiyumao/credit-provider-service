package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM188;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM188Repository extends MongoRepository<CM188, String> ,BaseMobileDetailRepository<CM188,String>{

}
