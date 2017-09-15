package cn.repository.ct;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.ct.CT177;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CT177Repository extends MongoRepository<CT177, String>,BaseMobileDetailRepository<CT177,String> {

}
