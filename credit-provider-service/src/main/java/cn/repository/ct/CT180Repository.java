package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.CT180;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CT180Repository extends MongoRepository<CT180, String>,BaseMobileDetailRepository<CT180,String> {
    
}
