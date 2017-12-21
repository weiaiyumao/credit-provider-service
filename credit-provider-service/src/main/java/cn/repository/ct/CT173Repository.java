package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.CT173;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CT173Repository extends MongoRepository<CT173, String>,BaseMobileDetailRepository<CT173,String> {

}
