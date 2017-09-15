package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU1709;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU1709Repository extends MongoRepository<CU1709, String> ,BaseMobileDetailRepository<CU1709,String>{

}
