package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU1704;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU1704Repository extends MongoRepository<KCU1704, String> ,BaseMobileDetailRepository<KCU1704,String>{

}
