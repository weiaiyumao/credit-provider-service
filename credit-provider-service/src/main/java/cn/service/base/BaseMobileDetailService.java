package cn.service.base;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import cn.entity.base.BaseMobileDetail;

/**
 * 底层服务
 * @author ChuangLan
 *
 * @param <T>
 * @param <ID>
 */
public interface BaseMobileDetailService<T, ID extends Serializable> {

	/**
	 * 根据手机号码和时间段查询 默认reportTime降序
	 * @param mobile
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<T> findByMobileAndReportTime(String mobile, Date startTime, Date endTime);
	
	/**
	 * 根据手机号码查询
	 * @param mobile
	 * @return
	 */
	List<T> findByMobile(String mobile);
	
	/**
	 * 根据手机号码删除
	 * @param mobile
	 * @return
	 */
	void deleteByMobile(BaseMobileDetail mobileDetail,String mobile);
}
