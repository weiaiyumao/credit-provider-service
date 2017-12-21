package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM183;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM183Repository extends MongoRepository<KCM183, String> ,BaseMobileDetailRepository<KCM183,String>{

}
