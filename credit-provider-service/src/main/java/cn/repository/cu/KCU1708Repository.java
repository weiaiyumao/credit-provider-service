package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU1708;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU1708Repository extends MongoRepository<KCU1708, String> ,BaseMobileDetailRepository<KCU1708,String>{

}
