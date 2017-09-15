package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM135;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM135Repository extends MongoRepository<CM135, String>,BaseMobileDetailRepository<CM135,String> {

    
}
