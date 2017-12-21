package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM188;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM188Repository extends MongoRepository<KCM188, String> ,BaseMobileDetailRepository<KCM188,String>{

}
