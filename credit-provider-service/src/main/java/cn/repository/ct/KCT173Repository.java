package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.KCT173;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCT173Repository extends MongoRepository<KCT173, String>,BaseMobileDetailRepository<KCT173,String> {

}
