package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU185;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU185Repository extends MongoRepository<CU185, String> ,BaseMobileDetailRepository<CU185,String>{

}
