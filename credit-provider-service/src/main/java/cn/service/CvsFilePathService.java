package cn.service;

import java.util.List;

import org.springframework.data.domain.Page;

import cn.entity.CvsFilePath;

public interface CvsFilePathService {
	
	List<CvsFilePath> findByUserId(String userId);

	void deleteByIds(String ids);
	
	public Page<CvsFilePath> getPageByUserId(int pageNo, int pageSize, String userId);
}
