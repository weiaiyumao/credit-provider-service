package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU185;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU185Repository extends MongoRepository<KCU185, String> ,BaseMobileDetailRepository<KCU185,String>{

}
