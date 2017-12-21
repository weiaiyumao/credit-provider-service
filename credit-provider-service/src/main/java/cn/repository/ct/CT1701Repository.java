package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.CT1701;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CT1701Repository extends MongoRepository<CT1701, String>,BaseMobileDetailRepository<CT1701,String> {
    
}
