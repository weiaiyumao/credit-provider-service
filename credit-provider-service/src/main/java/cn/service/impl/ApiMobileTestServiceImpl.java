package cn.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import cn.entity.MobileNumberSection;
import cn.entity.MobileTestLog;
import cn.entity.WaterConsumption;
import cn.entity.base.BaseMobileDetail;
import cn.redis.RedisLock;
import cn.service.ApiMobileTestService;
import cn.service.MobileNumberSectionService;
import cn.service.SpaceDetectionService;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import cn.utils.UUIDTool;
import main.java.cn.common.BackResult;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.MobileInfoDomain;
import main.java.cn.sms.util.ChuangLanSmsUtil;

@Service
public class ApiMobileTestServiceImpl implements ApiMobileTestService {

	private final static Logger logger = LoggerFactory.getLogger(ApiMobileTestServiceImpl.class);

	@Autowired
	private SpaceDetectionService spaceDetectionService;

	@Autowired
	private MobileNumberSectionService mobileNumberSectionService;

	@Autowired
	private MongoTemplate mongoTemplate;

	// 注入RedisTemplate对象
	@Resource(name = "redisTemplate")
	private RedisTemplate<String, String> redisTemplate;

	@Override
	public BackResult<List<MobileInfoDomain>> findByMobileNumbers(String mobileNumbers, String userId) {

		RedisLock lock = new RedisLock(redisTemplate, "sh_" + System.currentTimeMillis() + "_" + userId, 0, 3 * 1000);

		try {

			// 处理加锁业务
			if (lock.lock()) {
				
				BackResult<List<MobileInfoDomain>> result = new BackResult<List<MobileInfoDomain>>();

				List<MobileInfoDomain> list = new ArrayList<MobileInfoDomain>();
				
				String[] mobiles = mobileNumbers.split(",");
				Date startTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -90);
				Date endTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), 1);
				for (String mobile : mobiles) {

					// 创建对象设置初始手机号码
					MobileInfoDomain domain = new MobileInfoDomain();
					domain.setMobile(mobile);

					// 验证是否为正常的１１位有效数字
					if (!CommonUtils.isNumeric(mobile)) {
						domain.setLastTime(DateUtils.getCurrentDateTime());
						domain.setChargesStatus("0"); // 不收费
						domain.setStatus("0"); // 空号
						list.add(domain);
						continue;
					}

					// 1 查询号段
					MobileNumberSection section = mobileNumberSectionService.findByNumberSection(mobile.substring(0, 7));

					if (null == section) {
						domain.setLastTime(DateUtils.getCurrentDateTime());
						domain.setChargesStatus("1");
						domain.setStatus("0"); // 空号
						list.add(domain);
						continue;
					}

					// 2 查询库 最近3个月
					BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(mobile, startTime, endTime);

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

					// 发送短信
					ChuangLanSmsUtil.getInstance().sendYxByMobile(mobile);

					Thread.sleep(1000); // 等待系统短信发送回调执行查询

					detail = spaceDetectionService.findByMobileAndReportTime(mobile, startTime, endTime);

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
					}

				}

				this.logInfoSaveDB(list,userId);
				
				lock.unlock(); // 注销锁
				result.setResultObj(list);
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
			lock.unlock(); // 注销锁
			logger.error("账号二次清洗出现系统异常：" + e.getMessage());
		}

		return  new BackResult<List<MobileInfoDomain>>(ResultCode.RESULT_FAILED,"系统异常");
	}

	/**
	 * 状态0 ：未知 1：实号 2：空号
	 * 
	 * @param delivrd
	 * @return
	 */
	public Boolean isSpaceMobile(String delivrd) {
		if (delivrd.equals("DELIVRD") || delivrd.equals("MC:0055") || delivrd.equals("CJ:0007")
				|| delivrd.equals("CJ:0008") || delivrd.equals("DB:0141") || delivrd.equals("DISTURB")
				|| delivrd.equals("HD:0001") || delivrd.equals("IC:0151") || delivrd.equals("ID:0004")
				|| delivrd.equals("MBBLACK") || delivrd.equals("MC:0055") || delivrd.equals("MK:0008")
				|| delivrd.equals("MK:0010") || delivrd.equals("MK:0022") || delivrd.equals("MK:0024")
				|| delivrd.equals("MK:0029") || delivrd.equals("MN:0017") || delivrd.equals("MN:0044")
				|| delivrd.equals("MN:0051") || delivrd.equals("MN:0054") || delivrd.equals("REJECT")
				|| delivrd.equals("SME19") || delivrd.equals("TIMEOUT") || delivrd.equals("UNDELIV")
				|| delivrd.equals("GG:0024") || delivrd.equals("DB:0309") || delivrd.equals("SME92")
				|| delivrd.equals("DB:0114") || delivrd.equals("MN:0174") || delivrd.equals("YX:7000")
				|| delivrd.equals("MK:0004") || delivrd.equals("NOROUTE") || delivrd.equals("CJ:0005")
				|| delivrd.equals("IC:0055") || delivrd.equals("REJECTE") || delivrd.equals("MN:0053")
				|| delivrd.equals("MB:1026")) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * 日志数据入库
	 * @param list
	 * @param userId
	 */
	public void  logInfoSaveDB(List<MobileInfoDomain> list,String userId){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (!CommonUtils.isNotEmpty(list)) {

						
						int changeCount = 0;
						
						List<MobileTestLog> listMobile = new ArrayList<MobileTestLog>();

						for (MobileInfoDomain mobileInfoDomain : list) {
							MobileTestLog mobileTestLog = new MobileTestLog();
							mobileTestLog.setId(DateUtils.getCurrentTimeMillis().substring(0, 4) + System.currentTimeMillis());
							mobileTestLog.setArea(mobileInfoDomain.getArea());
							mobileTestLog.setChargesStatus(mobileTestLog.getChargesStatus());
							mobileTestLog.setCreateTime(new Date());
							mobileTestLog.setLastTime(mobileInfoDomain.getLastTime());
							mobileTestLog.setMobile(mobileInfoDomain.getMobile());
							mobileTestLog.setNumberType(mobileInfoDomain.getNumberType());
							mobileTestLog.setStatus(mobileInfoDomain.getStatus());
							mobileTestLog.setUserId(userId);
							listMobile.add(mobileTestLog);
							
							if (mobileInfoDomain.getChargesStatus().equals("1")) {
								changeCount = changeCount + 1;
							}
							
						}

						// 检测结果日志入库
						mongoTemplate.insertAll(listMobile);
						
						if (changeCount > 0) {
							// 记录流水记录
							WaterConsumption waterConsumption = new WaterConsumption();
							waterConsumption.setUserId(userId);
							waterConsumption.setId(UUIDTool.getInstance().getUUID());
							waterConsumption.setConsumptionNum("ECJC_" + System.currentTimeMillis());
							waterConsumption.setMenu("客户API接口账户二次清洗");
							waterConsumption.setStatus("1");
							waterConsumption.setType("2"); // 账户二次检测
							waterConsumption.setCreateTime(new Date());
							waterConsumption.setCount(String.valueOf(listMobile.size())); // 条数
							waterConsumption.setUpdateTime(new Date());
							mongoTemplate.save(waterConsumption);
						}
					}
				} catch (Exception e) {
					logger.error("账号二次清洗日志信息入库系统异常：" + e.getMessage());
				}
			}
		}, "日志信息入库").start();
	}
}
