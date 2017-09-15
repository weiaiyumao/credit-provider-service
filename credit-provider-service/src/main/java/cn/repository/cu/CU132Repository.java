package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU132;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU132Repository extends MongoRepository<CU132, String> ,BaseMobileDetailRepository<CU132,String>{

}
