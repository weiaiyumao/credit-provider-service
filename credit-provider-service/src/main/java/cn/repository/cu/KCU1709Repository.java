package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU1709;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU1709Repository extends MongoRepository<KCU1709, String> ,BaseMobileDetailRepository<KCU1709,String>{

}
