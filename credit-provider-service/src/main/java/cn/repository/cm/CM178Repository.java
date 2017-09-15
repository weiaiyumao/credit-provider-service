package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM178;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM178Repository extends MongoRepository<CM178, String>,BaseMobileDetailRepository<CM178,String> {

}
