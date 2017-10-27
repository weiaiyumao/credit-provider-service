package cn.entity;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;

/**
 * 账号二次清洗日志类
 * @author ChuangLan
 *
 */
public class MobileTestLog implements Serializable{
	
	private static final long serialVersionUID = -2341165805832552654L;

	@Id
	private String id;
	
	private String userId; // 用户ID
	
	private String mobile; // 手机号码
	
	private String area; // 地区
	
	private String numberType; // 号码类型
	
	private String chargesStatus; // 验证状态 0 不收费  1:收费
	
	private String status; // 1：实号  0：空号 
	
	private Date createTime; // 创建时间
	
	private Date lastTime; // 最近活跃时间

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getNumberType() {
		return numberType;
	}

	public void setNumberType(String numberType) {
		this.numberType = numberType;
	}

	public String getChargesStatus() {
		return chargesStatus;
	}

	public void setChargesStatus(String chargesStatus) {
		this.chargesStatus = chargesStatus;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getLastTime() {
		return lastTime;
	}

	public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	
}
