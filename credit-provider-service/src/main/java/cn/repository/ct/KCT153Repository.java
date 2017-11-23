package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.KCT153;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCT153Repository extends MongoRepository<KCT153, String>,BaseMobileDetailRepository<KCT153,String> {
    
}
