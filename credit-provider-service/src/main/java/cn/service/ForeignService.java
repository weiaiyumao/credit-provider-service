package cn.service;

import main.java.cn.common.BackResult;
import main.java.cn.domain.RunTestDomian;
import main.java.cn.service.ForeignBusService;

/**
 * 对外服务类
 * @author ChuangLan
 *
 */
public interface ForeignService extends ForeignBusService{
	
//	/**
//	 * 返回文件地址检测结果
//	 * @param fileUrl
//	 * @return
//	 */
//	CvsFilePath runTheTest(String fileUrl,String userId);
	
	BackResult<RunTestDomian> theTest2(String fileUrl, String userId, String mobile, String source,
			String startLine, String type);
}
