package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU145;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU145Repository extends MongoRepository<KCU145, String> ,BaseMobileDetailRepository<KCU145,String>{

}
