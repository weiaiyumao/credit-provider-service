package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.KCU130;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCU130Repository extends MongoRepository<KCU130, String> ,BaseMobileDetailRepository<KCU130,String>{

}
