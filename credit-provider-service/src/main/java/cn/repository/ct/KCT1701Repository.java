package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.KCT1701;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCT1701Repository extends MongoRepository<KCT1701, String>,BaseMobileDetailRepository<KCT1701,String> {
    
}
