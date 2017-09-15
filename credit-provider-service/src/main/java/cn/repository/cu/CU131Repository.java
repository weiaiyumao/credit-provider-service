package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU131;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU131Repository extends MongoRepository<CU131, String> ,BaseMobileDetailRepository<CU131,String>{

}
