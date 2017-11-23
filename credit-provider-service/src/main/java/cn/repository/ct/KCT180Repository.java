package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.KCT180;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCT180Repository extends MongoRepository<KCT180, String>,BaseMobileDetailRepository<KCT180,String> {
    
}
