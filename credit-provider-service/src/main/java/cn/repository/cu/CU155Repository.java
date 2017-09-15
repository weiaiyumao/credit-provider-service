package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU155;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU155Repository extends MongoRepository<CU155, String> ,BaseMobileDetailRepository<CU155,String>{

}
