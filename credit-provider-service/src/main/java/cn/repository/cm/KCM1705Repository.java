package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.KCM1705;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCM1705Repository extends MongoRepository<KCM1705, String> ,BaseMobileDetailRepository<KCM1705,String>{

}
