package cn.service;

import java.util.Date;

import cn.entity.base.BaseMobileDetail;
import main.java.cn.service.MobileTestBusService;

/**
 * 
 * @author ChuangLan
 *
 */
public interface SpaceDetectionService extends MobileTestBusService{

	/**
	 * 根据手机号检测该号码是否为空号 (默认去第一条)
	 * @param mobile
	 * @return
	 */
	BaseMobileDetail findByMobile(String mobile);
	
	/**
	 * 根据手机号检测该号码是否为空号
	 * 默认 reportTime desc
	 * 默认取查询出来的第一条数据
	 * @param mobile
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	BaseMobileDetail findByMobileAndReportTime(String mobile, Date startTime, Date endTime);
	
	/**
	 * 新增或者删除记录
	 * @param mobile
	 * @return
	 */
	void smSCallBack(String mobile,String status,String notifyTime);
}
