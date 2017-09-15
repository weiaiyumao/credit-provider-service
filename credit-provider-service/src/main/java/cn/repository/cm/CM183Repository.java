package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM183;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM183Repository extends MongoRepository<CM183, String> ,BaseMobileDetailRepository<CM183,String>{

}
