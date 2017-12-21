package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM151;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM151Repository extends MongoRepository<KCM151, String> ,BaseMobileDetailRepository<KCM151,String>{

}
