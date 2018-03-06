package cn.repository;

import cn.entity.FileUpload;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileUploadRepository extends MongoRepository<FileUpload,String>{

}
