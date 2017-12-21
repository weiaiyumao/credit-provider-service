package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM136;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */

public interface KCM136Repository extends MongoRepository<KCM136, String>,BaseMobileDetailRepository<KCM136,String>{

}
