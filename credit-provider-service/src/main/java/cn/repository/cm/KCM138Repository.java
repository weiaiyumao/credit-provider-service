package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM138;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM138Repository extends MongoRepository<KCM138, String>,BaseMobileDetailRepository<KCM138,String> {

}
