package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU155;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU155Repository extends MongoRepository<KCU155, String> ,BaseMobileDetailRepository<KCU155,String>{

}
