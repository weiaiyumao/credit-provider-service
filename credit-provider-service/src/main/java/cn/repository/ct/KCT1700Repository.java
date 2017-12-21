package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.KCT1700;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCT1700Repository extends MongoRepository<KCT1700, String>,BaseMobileDetailRepository<KCT1700,String> {
    
}
