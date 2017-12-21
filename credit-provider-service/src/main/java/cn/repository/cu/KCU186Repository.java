package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU186;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU186Repository extends MongoRepository<KCU186, String> ,BaseMobileDetailRepository<KCU186,String>{

}
