package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.CT181;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CT181Repository extends MongoRepository<CT181, String>,BaseMobileDetailRepository<CT181,String> {
    
}
