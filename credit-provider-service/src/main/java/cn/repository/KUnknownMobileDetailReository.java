package cn.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.unknown.KUnknownMobileDetail;
import cn.repository.base.BaseMobileDetailRepository;

public interface KUnknownMobileDetailReository extends MongoRepository<KUnknownMobileDetail, String>,BaseMobileDetailRepository<KUnknownMobileDetail,String>{

}
