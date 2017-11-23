package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM135;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM135Repository extends MongoRepository<KCM135, String>,BaseMobileDetailRepository<KCM135,String> {

    
}
