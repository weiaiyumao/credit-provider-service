package cn.service;

import cn.entity.FileUpload;

public interface FileUploadService {

    FileUpload findById(String id);

    FileUpload save(FileUpload fileUpload);
}
