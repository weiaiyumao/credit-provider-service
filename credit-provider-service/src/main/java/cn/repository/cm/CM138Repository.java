package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM138;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM138Repository extends MongoRepository<CM138, String>,BaseMobileDetailRepository<CM138,String> {

}
