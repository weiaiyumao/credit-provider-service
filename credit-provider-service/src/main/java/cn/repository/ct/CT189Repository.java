package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.CT189;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CT189Repository extends MongoRepository<CT189, String>,BaseMobileDetailRepository<CT189,String> {
    
}
