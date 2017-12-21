package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM147;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM147Repository extends MongoRepository<KCM147, String>,BaseMobileDetailRepository<KCM147,String> {

}
