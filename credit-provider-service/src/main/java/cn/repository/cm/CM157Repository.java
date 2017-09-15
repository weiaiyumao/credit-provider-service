package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM157;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM157Repository extends MongoRepository<CM157, String> ,BaseMobileDetailRepository<CM157,String>{

}
