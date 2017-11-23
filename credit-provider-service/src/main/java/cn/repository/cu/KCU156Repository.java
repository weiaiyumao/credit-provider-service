package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU156;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU156Repository extends MongoRepository<KCU156, String> ,BaseMobileDetailRepository<KCU156,String>{

}
