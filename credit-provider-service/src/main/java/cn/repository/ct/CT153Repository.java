package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.CT153;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CT153Repository extends MongoRepository<CT153, String>,BaseMobileDetailRepository<CT153,String> {
    
}
