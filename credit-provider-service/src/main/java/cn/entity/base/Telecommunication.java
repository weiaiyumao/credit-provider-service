package cn.entity.base;

/**
 * 中国电信 
 * 2G/3G号段（CDMA2000网络）133、153、180、181、189 
 * 4G号段 177
 * 补充 1700 
 * @author ChuangLan
 *
 */
public class Telecommunication extends BaseMobileDetail{
	
	private String mobilePhoneType = "01"; // 手机号码平台类型

	public String getMobilePhoneType() {
		return mobilePhoneType;
	}

	public void setMobilePhoneType(String mobilePhoneType) {
		this.mobilePhoneType = mobilePhoneType;
	}
	
	
}
