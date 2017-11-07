package cn.service;

import cn.entity.CvsFilePath;
import main.java.cn.common.BackResult;
import main.java.cn.domain.RunTestDomian;
import main.java.cn.service.ForeignBusService;

/**
 * 对外服务类
 * @author ChuangLan
 *
 */
public interface ForeignService extends ForeignBusService{
	
	/**
	 * 返回文件地址检测结果
	 * @param fileUrl
	 * @return
	 */
	BackResult<RunTestDomian> runTheTest(String fileUrl, String userId, String timestamp, String mobile);
	
	
}
