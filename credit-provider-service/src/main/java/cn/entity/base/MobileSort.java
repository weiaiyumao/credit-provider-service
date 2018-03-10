package cn.entity.base;

import java.io.Serializable;
import java.util.Date;

public class MobileSort implements Serializable{

	private static final long serialVersionUID = 715147989705294190L;

	private String mobile; // 手机号码

	private String delivrd; // 状态

	private Date reportTime; // 返回时间
	
	private Integer sortRanking; // redis 排序排名

	/**
	 * @return the mobile
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * @param mobile the mobile to set
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * @return the delivrd
	 */
	public String getDelivrd() {
		return delivrd;
	}

	/**
	 * @param delivrd the delivrd to set
	 */
	public void setDelivrd(String delivrd) {
		this.delivrd = delivrd;
	}

	/**
	 * @return the reportTime
	 */
	public Date getReportTime() {
		return reportTime;
	}

	/**
	 * @param reportTime the reportTime to set
	 */
	public void setReportTime(Date reportTime) {
		this.reportTime = reportTime;
	}

	/**
	 * @return the sortRanking
	 */
	public Integer getSortRanking() {
		return sortRanking;
	}

	/**
	 * @param sortRanking the sortRanking to set
	 */
	public void setSortRanking(Integer sortRanking) {
		this.sortRanking = sortRanking;
	}
	
	
}
