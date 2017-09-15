package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU176;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU176Repository extends MongoRepository<CU176, String> ,BaseMobileDetailRepository<CU176,String>{

}
