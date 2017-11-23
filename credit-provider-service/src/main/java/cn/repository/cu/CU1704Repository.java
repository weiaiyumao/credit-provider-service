package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU1704;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU1704Repository extends MongoRepository<CU1704, String> ,BaseMobileDetailRepository<CU1704,String>{

}
