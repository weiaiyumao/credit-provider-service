package cn.entity;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;

/**
 * 检查消费流水记录
 * @author ChuangLan
 *
 */
public class WaterConsumption implements Serializable{

	private static final long serialVersionUID = -7139110658207345806L;
	
	@Id
	private String id;
	
	private String consumptionNum; //流水号
	
	private String userId; // 用户ID
	
	private String type; // 消费类型  1.实号检测 
	
	private String count; // 消费条数
	
	private String menu; // 描述
	
	private String status; // 状态  0处理中 1处理成功 2处理失败
	
	private Date createTime; // 创建时间
	
	private Date updateTime; // 修改时间
	
	private String source; // 来源

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getConsumptionNum() {
		return consumptionNum;
	}

	public void setConsumptionNum(String consumptionNum) {
		this.consumptionNum = consumptionNum;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getMenu() {
		return menu;
	}

	public void setMenu(String menu) {
		this.menu = menu;
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

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	
}
