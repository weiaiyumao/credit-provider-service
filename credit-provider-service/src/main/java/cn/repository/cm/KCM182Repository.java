package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM182;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM182Repository extends MongoRepository<KCM182, String> ,BaseMobileDetailRepository<KCM182,String>{

}
