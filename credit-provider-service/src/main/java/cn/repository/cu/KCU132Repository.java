package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU132;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU132Repository extends MongoRepository<KCU132, String> ,BaseMobileDetailRepository<KCU132,String>{

}
