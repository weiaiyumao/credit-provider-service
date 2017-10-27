package cn.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.CvsFilePath;

public interface MobileTestLogReository extends MongoRepository<CvsFilePath, String>{

}
