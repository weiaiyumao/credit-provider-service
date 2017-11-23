package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU1708;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU1708Repository extends MongoRepository<CU1708, String> ,BaseMobileDetailRepository<CU1708,String>{

}
