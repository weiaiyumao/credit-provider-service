package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM1705;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM1705Repository extends MongoRepository<CM1705, String> ,BaseMobileDetailRepository<CM1705,String>{

}
