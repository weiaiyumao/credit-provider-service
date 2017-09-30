package cn.service;

import java.util.List;

import cn.entity.CvsFilePath;

public interface CvsFilePathService {
	
	List<CvsFilePath> findByUserId(String userId);

	void deleteByIds(String ids);
}
