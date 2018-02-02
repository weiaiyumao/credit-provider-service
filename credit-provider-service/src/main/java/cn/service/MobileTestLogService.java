package cn.service;

import java.util.List;

import org.springframework.data.domain.Page;

import cn.entity.MobileTestLog;

public interface MobileTestLogService {

	Page<MobileTestLog> getPageByUserId(int pageNo, int pageSize, String userId,String type);
	
	List<MobileTestLog> getListByUserId(String userId,String type);
}
