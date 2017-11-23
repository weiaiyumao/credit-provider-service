package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM157;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM157Repository extends MongoRepository<KCM157, String> ,BaseMobileDetailRepository<KCM157,String>{

}
