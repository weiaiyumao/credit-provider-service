package cn.entity.base;

/**
 * 中国联通  
 * 2G号段（GSM网络）130、131、132、155、156 
 * 3G上网卡145 
 * 3G号段（WCDMA网络）185、186 
 * 4G号段 176
 * 补充号段 145 1709
 * 
 * @author ChuangLan
 *
 */
public class Unicom extends BaseMobileDetail{
	
	private String mobilePhoneType = "02"; // 手机号码平台类型

	public String getMobilePhoneType() {
		return mobilePhoneType;
	}

	public void setMobilePhoneType(String mobilePhoneType) {
		this.mobilePhoneType = mobilePhoneType;
	}
	
}
