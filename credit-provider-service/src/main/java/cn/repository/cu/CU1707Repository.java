package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU1707;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU1707Repository extends MongoRepository<CU1707, String> ,BaseMobileDetailRepository<CU1707,String>{

}
