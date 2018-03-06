package cn.service;

import org.springframework.data.domain.Page;

import cn.entity.ApiLog;
import cn.entity.MobileTestLog;

public interface MobileTestLogService {

	Page<MobileTestLog> getPageByUserId(int pageNo, int pageSize, String userId,String type);
	
	Page<ApiLog> getPageByCustomerId(int pageNo, int pageSize, String customerId,String method);
}
