package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM158;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM158Repository extends MongoRepository<KCM158, String> ,BaseMobileDetailRepository<KCM158,String> {

}
