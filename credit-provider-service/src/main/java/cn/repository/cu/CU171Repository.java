package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU171;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU171Repository extends MongoRepository<CU171, String> ,BaseMobileDetailRepository<CU171,String>{

}
