package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU171;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU171Repository extends MongoRepository<KCU171, String> ,BaseMobileDetailRepository<KCU171,String>{

}
