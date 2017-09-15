package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU156;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU156Repository extends MongoRepository<CU156, String> ,BaseMobileDetailRepository<CU156,String>{

}
