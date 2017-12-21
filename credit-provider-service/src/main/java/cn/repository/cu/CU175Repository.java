package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU175;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU175Repository extends MongoRepository<CU175, String> ,BaseMobileDetailRepository<CU175,String>{

}
