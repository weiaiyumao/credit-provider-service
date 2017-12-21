package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM184;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM184Repository extends MongoRepository<KCM184, String> ,BaseMobileDetailRepository<KCM184,String>{

}
