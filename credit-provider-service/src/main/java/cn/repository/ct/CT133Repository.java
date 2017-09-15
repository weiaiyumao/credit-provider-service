package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.CT133;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CT133Repository extends MongoRepository<CT133, String> ,BaseMobileDetailRepository<CT133,String>{

}
