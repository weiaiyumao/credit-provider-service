package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.KCT189;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCT189Repository extends MongoRepository<KCT189, String>,BaseMobileDetailRepository<KCT189,String> {
    
}
