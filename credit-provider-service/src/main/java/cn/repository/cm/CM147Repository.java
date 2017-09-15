package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM147;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM147Repository extends MongoRepository<CM147, String>,BaseMobileDetailRepository<CM147,String> {

}
