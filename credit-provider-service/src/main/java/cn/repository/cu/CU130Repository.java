package cn.repository.cu;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cu.CU130;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CU130Repository extends MongoRepository<CU130, String> ,BaseMobileDetailRepository<CU130,String>{

}
