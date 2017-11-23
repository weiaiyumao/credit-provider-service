package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM178;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM178Repository extends MongoRepository<KCM178, String>,BaseMobileDetailRepository<KCM178,String> {

}
