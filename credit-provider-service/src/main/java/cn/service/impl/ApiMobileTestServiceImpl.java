package cn.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.MobileNumberSection;
import cn.entity.base.BaseMobileDetail;
import cn.service.ApiMobileTestService;
import cn.service.MobileNumberSectionService;
import cn.service.SpaceDetectionService;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import main.java.cn.common.BackResult;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.MobileInfoDomain;

@Service
public class ApiMobileTestServiceImpl implements ApiMobileTestService{
	
	private final static Logger logger = LoggerFactory.getLogger(ApiMobileTestServiceImpl.class);

	@Autowired
	private SpaceDetectionService spaceDetectionService;
	
	@Autowired
	private MobileNumberSectionService mobileNumberSectionService;
	
	@Override
	public BackResult<List<MobileInfoDomain>> findByMobileNumbers(String mobileNumbers) {
		
		BackResult<List<MobileInfoDomain>> result = new BackResult<List<MobileInfoDomain>>();
		
		List<MobileInfoDomain> list = new ArrayList<MobileInfoDomain>();
		
		try {
			String[] mobiles = mobileNumbers.split(",");
			Date startTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -90);
			for (String mobile : mobiles) {
				
				// 创建对象设置初始手机号码
				MobileInfoDomain domain = new MobileInfoDomain();
				domain.setMobile(mobile);
				
				//验证是否为正常的１１位有效数字
				if (!CommonUtils.isNumeric(mobile)) {
					domain.setLastTime(DateUtils.getCurrentDateTime());
					domain.setChargesStatus("0"); // 不收费
					domain.setStatus("0"); // 空号
					list.add(domain);
					continue;
				}
				
				//  1 查询号段
				MobileNumberSection section = mobileNumberSectionService.findByNumberSection(mobile.substring(0, 7));
				
				if (null == section) {
					domain.setLastTime(DateUtils.getCurrentDateTime());
					domain.setChargesStatus("1");
					domain.setStatus("0"); // 空号
					list.add(domain);
					continue;
				}
				
				//  2 查询库 最近3个月
				BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(mobile, startTime,
						DateUtils.getCurrentDateTime());
				
				if (null != detail) {
					
					domain.setArea(section.getProvince() + "-" + section.getCity());
					domain.setNumberType(section.getMobilePhoneType());
					domain.setLastTime(detail.getReportTime());
					domain.setChargesStatus("1");
					
					if (this.isSpaceMobile(detail.getDelivrd())) {
						domain.setStatus("1"); // 实号
					} else {
						domain.setStatus("0"); // 空号
					}
					
					list.add(domain);
					continue;
				}
				
				//  发送短信
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("账号二次清洗出现系统异常：" + e.getMessage());
			result.setResultObj(null);
			result.setResultMsg("系统异常");
			result.setResultCode(ResultCode.RESULT_FAILED);
		}
		
		result.setResultObj(list);
		return result;
	}
	
	/**
	 * 状态0 ：未知	 1：实号	 2：空号
	 * @param delivrd
	 * @return
	 */
	public Boolean isSpaceMobile(String delivrd){
		if (delivrd.equals("DELIVRD") || delivrd.equals("MC:0055")
				|| delivrd.equals("CJ:0007") || delivrd.equals("CJ:0008")
				|| delivrd.equals("DB:0141") || delivrd.equals("DISTURB")
				|| delivrd.equals("HD:0001") || delivrd.equals("IC:0151")
				|| delivrd.equals("ID:0004") || delivrd.equals("MBBLACK")
				|| delivrd.equals("MC:0055") || delivrd.equals("MK:0008")
				|| delivrd.equals("MK:0010") || delivrd.equals("MK:0022")
				|| delivrd.equals("MK:0024") || delivrd.equals("MK:0029")
				|| delivrd.equals("MN:0017") || delivrd.equals("MN:0044")
				|| delivrd.equals("MN:0051") || delivrd.equals("MN:0054")
				|| delivrd.equals("REJECT") || delivrd.equals("SME19")
				|| delivrd.equals("TIMEOUT") || delivrd.equals("UNDELIV")
				|| delivrd.equals("GG:0024") || delivrd.equals("DB:0309")
				|| delivrd.equals("SME92") || delivrd.equals("DB:0114")
				|| delivrd.equals("MN:0174") || delivrd.equals("YX:7000")
				|| delivrd.equals("MK:0004") || delivrd.equals("NOROUTE")
				|| delivrd.equals("CJ:0005") || delivrd.equals("IC:0055")
				|| delivrd.equals("REJECTE") || delivrd.equals("MN:0053")
				|| delivrd.equals("MB:1026")) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	

	
}
