package cn.service;

import org.springframework.data.domain.Page;

import cn.entity.MobileTestLog;

public interface MobileTestLogService {

	Page<MobileTestLog> getPageByUserId(int pageNo, int pageSize, String userId,String type);
}
