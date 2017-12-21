package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU175;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU175Repository extends MongoRepository<KCU175, String> ,BaseMobileDetailRepository<KCU175,String>{

}
