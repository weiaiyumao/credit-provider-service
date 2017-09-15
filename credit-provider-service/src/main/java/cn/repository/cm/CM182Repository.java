package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM182;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM182Repository extends MongoRepository<CM182, String> ,BaseMobileDetailRepository<CM182,String>{

}
