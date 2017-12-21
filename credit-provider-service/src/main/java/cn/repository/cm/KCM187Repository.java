package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM187;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM187Repository extends MongoRepository<KCM187, String> ,BaseMobileDetailRepository<KCM187,String>{

}
