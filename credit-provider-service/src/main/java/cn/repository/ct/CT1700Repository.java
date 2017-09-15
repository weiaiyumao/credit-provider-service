package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.CT1700;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CT1700Repository extends MongoRepository<CT1700, String>,BaseMobileDetailRepository<CT1700,String> {
    
}
