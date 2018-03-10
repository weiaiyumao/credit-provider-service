package cn.service.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.FileUpload;
import cn.repository.FileUploadRepository;
import cn.service.FileUploadService;
import cn.utils.DateUtils;
import main.java.cn.common.BackResult;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.FileUploadDomain;

@Service
public class FileUploadServiceImpl implements FileUploadService {

	private final static Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);

	@Autowired
	private FileUploadRepository fileUploadRepository;

	@Override
	public FileUpload findByOne(String id) {
		return fileUploadRepository.findOne(id);
	}

	@Override
	public FileUpload save(FileUpload fileUpload) {
		fileUpload.setCreateTime(new Date());
		fileUpload.setIsDeleted("0");
		return fileUploadRepository.save(fileUpload);
	}

	@Override
	public BackResult<FileUploadDomain> findById(String arg0) {

		BackResult<FileUploadDomain> result = new BackResult<FileUploadDomain>();

		try {
			FileUploadDomain domain = new FileUploadDomain();

			FileUpload fileUpload = this.findByOne(arg0);

			BeanUtils.copyProperties(fileUpload, domain);

			result.setResultObj(domain);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("系统异常：" + e.getMessage());
			result = new BackResult<FileUploadDomain>(ResultCode.RESULT_FAILED, "系统异常");
		}

		return result;
	}

	@Override
	public BackResult<FileUploadDomain> save(FileUploadDomain arg0) {

		BackResult<FileUploadDomain> result = new BackResult<FileUploadDomain>();

		try {
			
			FileUploadDomain domain = new FileUploadDomain();
			
			FileUpload fileUpload = new FileUpload();
			
			BeanUtils.copyProperties(arg0, fileUpload);
			
			fileUpload = this.save(fileUpload);
			
			BeanUtils.copyProperties(fileUpload, domain);
			
			result.setResultObj(domain);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("系统异常：" + e.getMessage());
			result = new BackResult<FileUploadDomain>(ResultCode.RESULT_FAILED, "系统异常");
		}

		return result;
	}
}
