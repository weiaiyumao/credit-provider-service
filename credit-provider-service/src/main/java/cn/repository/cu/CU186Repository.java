package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU186;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU186Repository extends MongoRepository<CU186, String> ,BaseMobileDetailRepository<CU186,String>{

}
