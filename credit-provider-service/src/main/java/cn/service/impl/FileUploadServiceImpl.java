package cn.service.impl;

import cn.entity.FileUpload;
import cn.repository.FileUploadRepository;
import cn.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Override
    public FileUpload findById(String id) {
        return fileUploadRepository.findOne(id);
    }

    @Override
    public FileUpload save(FileUpload fileUpload) {
        return fileUploadRepository.save(fileUpload);
    }
}
