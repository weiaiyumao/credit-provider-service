package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.KCT1702;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCT1702Repository extends MongoRepository<KCT1702, String>,BaseMobileDetailRepository<KCT1702,String> {
    
}
