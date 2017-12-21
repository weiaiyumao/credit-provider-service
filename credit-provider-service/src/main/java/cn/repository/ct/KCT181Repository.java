package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.KCT181;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCT181Repository extends MongoRepository<KCT181, String>,BaseMobileDetailRepository<KCT181,String> {
    
}
