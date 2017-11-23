package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM1706;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM1706Repository extends MongoRepository<CM1706, String> ,BaseMobileDetailRepository<CM1706,String>{

}
