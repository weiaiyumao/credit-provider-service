package cn.entity.base;

import java.util.Date;

import org.springframework.data.mongodb.core.index.Indexed;

/**
 * 手机号码实体类 1349卫星通信
 * 
 * @author ChuangLan
 *
 */
public class BaseMobileDetail {

	@Indexed(name = "{'mobile_': 1}")
	private String mobile; // 手机号码

	private String delivrd; // 状态

	@Indexed(name = "{'reportTime_': 1}")
	private Date reportTime; // 返回时间

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getDelivrd() {
		return delivrd;
	}

	public void setDelivrd(String delivrd) {
		this.delivrd = delivrd;
	}

	public Date getReportTime() {
		return reportTime;
	}

	public void setReportTime(Date reportTime) {
		this.reportTime = reportTime;
	}

}
