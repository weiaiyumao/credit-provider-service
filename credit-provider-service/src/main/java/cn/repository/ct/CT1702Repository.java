package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.CT1702;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CT1702Repository extends MongoRepository<CT1702, String>,BaseMobileDetailRepository<CT1702,String> {
    
}
