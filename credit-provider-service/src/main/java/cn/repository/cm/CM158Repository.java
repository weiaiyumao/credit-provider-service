package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM158;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM158Repository extends MongoRepository<CM158, String> ,BaseMobileDetailRepository<CM158,String> {

}
