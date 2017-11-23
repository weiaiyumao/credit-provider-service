package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU176;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU176Repository extends MongoRepository<KCU176, String> ,BaseMobileDetailRepository<KCU176,String>{

}
