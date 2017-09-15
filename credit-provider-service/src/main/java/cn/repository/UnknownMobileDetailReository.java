package cn.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.unknown.UnknownMobileDetail;
import cn.repository.base.BaseMobileDetailRepository;

public interface UnknownMobileDetailReository extends MongoRepository<UnknownMobileDetail, String>,BaseMobileDetailRepository<UnknownMobileDetail,String>{

}
