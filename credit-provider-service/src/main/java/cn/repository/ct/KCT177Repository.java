package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.KCT177;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface KCT177Repository extends MongoRepository<KCT177, String>,BaseMobileDetailRepository<KCT177,String> {

}
