package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU131;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU131Repository extends MongoRepository<KCU131, String> ,BaseMobileDetailRepository<KCU131,String>{

}
