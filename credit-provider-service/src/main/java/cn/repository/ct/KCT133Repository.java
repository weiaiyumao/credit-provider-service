package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.KCT133;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCT133Repository extends MongoRepository<KCT133, String> ,BaseMobileDetailRepository<KCT133,String>{

}
