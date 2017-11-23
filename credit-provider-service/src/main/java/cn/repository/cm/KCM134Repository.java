package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM134;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM134Repository extends MongoRepository<KCM134, String>,BaseMobileDetailRepository<KCM134,String>{

}
