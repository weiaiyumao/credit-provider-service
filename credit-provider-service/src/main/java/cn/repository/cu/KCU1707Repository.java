package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU1707;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU1707Repository extends MongoRepository<KCU1707, String> ,BaseMobileDetailRepository<KCU1707,String>{

}
