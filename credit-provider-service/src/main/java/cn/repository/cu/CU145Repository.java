package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU145;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU145Repository extends MongoRepository<CU145, String> ,BaseMobileDetailRepository<CU145,String>{

}
