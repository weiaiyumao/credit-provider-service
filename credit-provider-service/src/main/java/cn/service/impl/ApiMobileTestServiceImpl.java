package cn.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import cn.entity.MobileNumberSection;
import cn.entity.MobileTestLog;
import cn.entity.WaterConsumption;
import cn.entity.base.BaseMobileDetail;
import cn.redis.DistributedLock;
import cn.service.ApiMobileTestService;
import cn.service.MobileNumberSectionService;
import cn.service.MobileTestLogService;
import cn.service.SpaceDetectionService;
import cn.thread.ThreadExecutorService;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import cn.utils.UUIDTool;
import main.java.cn.common.BackResult;
import main.java.cn.common.RedisKeys;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.MobileInfoDomain;
import main.java.cn.domain.MobileTestLogDomain;
import main.java.cn.domain.page.PageDomain;
import main.java.cn.sms.util.ChuangLanSmsUtil;
import redis.clients.jedis.JedisPool;

@Service
public class ApiMobileTestServiceImpl implements ApiMobileTestService {

	private final static Logger logger = LoggerFactory.getLogger(ApiMobileTestServiceImpl.class);

	@Autowired
	private SpaceDetectionService spaceDetectionService;

	@Autowired
	private MobileNumberSectionService mobileNumberSectionService;

	@Autowired
	private MobileTestLogService mobileTestLogService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private JedisPool jedisPool;

	@Autowired
	private ThreadExecutorService threadExecutorService;

	@Override
	public BackResult<List<MobileInfoDomain>> findByMobileNumbers(String mobileNumbers, String userId) {

		DistributedLock lock = new DistributedLock(jedisPool);
		String lockName = RedisKeys.getInstance().getzhlcfunKey(mobileNumbers);
		// 加锁
		String identifier = lock.lockWithTimeout(lockName, 10L, 3 * 1000);
		try {

			// 处理加锁业务
			if (null != identifier) {

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
					MobileNumberSection section = mobileNumberSectionService
							.findByNumberSection(mobile.substring(0, 7));

					if (null == section) {
						domain.setLastTime(DateUtils.getCurrentDateTime());
						domain.setChargesStatus("1");
						domain.setStatus("0"); // 空号
						list.add(domain);
						continue;
					}

					// 2 查询库 最近3个月
					BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(mobile, startTime,
							endTime);

					if (null != detail) {

						domain.setArea(section.getProvince() + "-" + section.getCity());
						domain.setNumberType(section.getMobilePhoneType());
						domain.setLastTime(detail.getReportTime());
						domain.setChargesStatus("1");

						if ("real".equals(this.isSpaceMobile(detail.getDelivrd()))) {
							domain.setStatus("1"); // 实号
						} else if ("kong".equals(this.isSpaceMobile(detail.getDelivrd()))) {
							domain.setStatus("0"); // 空号
						} else {
							domain.setStatus("2"); // 停机
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

						if ("real".equals(this.isSpaceMobile(detail.getDelivrd()))) {
							domain.setStatus("1"); // 实号
						} else if ("kong".equals(this.isSpaceMobile(detail.getDelivrd()))) {
							domain.setStatus("0"); // 空号
						} else {
							domain.setStatus("2"); // 停机
						}

						list.add(domain);
					}

				}

				Runnable run = new Runnable() {
					@Override
					public void run() {
						try {
							if (!CommonUtils.isNotEmpty(list)) {

								int changeCount = 0;

								List<MobileTestLog> listMobile = new ArrayList<MobileTestLog>();

								for (MobileInfoDomain mobileInfoDomain : list) {
									MobileTestLog mobileTestLog = new MobileTestLog();
									mobileTestLog.setOrderNo(DateUtils.getCurrentTimeMillis().substring(0, 4)
											+ System.currentTimeMillis());
									mobileTestLog.setArea(mobileInfoDomain.getArea());
									mobileTestLog.setChargesStatus(mobileInfoDomain.getChargesStatus());
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
				};

				// 加入线程池开始执行
				threadExecutorService.execute(run);
				result.setResultObj(list);
				lock.releaseLock(lockName, identifier);
				return result;

			} else {
				return new BackResult<List<MobileInfoDomain>>(ResultCode.RESULT_API_NOTCONCURRENT, "正在计算中");
			}

		} catch (Exception e) {
			e.printStackTrace();
			lock.releaseLock(lockName, identifier);
			logger.error("账号二次清洗出现系统异常：" + e.getMessage());
		}

		return new BackResult<List<MobileInfoDomain>>(ResultCode.RESULT_FAILED, "系统异常");
	}

	/**
	 * 状态0 ：未知 1：实号 2：空号
	 * 
	 * @param delivrd
	 * @return
	 */
	public String isSpaceMobile(String delivrd) {
		String realdelivrd = "-1012,-99,004,010,011,015,017,020,022,029,054,055,151,174,188,602,612,613,614,615,618,619,620,625,627,634,636,650,706,711,713,714,726,760,762,812,814,815,827,870,899,901,999,BLACK,BLKFAIL,BwList,CB:0255,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL:105,CL:106,CL:116,CL:125,DB:0008,DB:0119,DB:0140,DB:0141,DB:0142,DB:0144,DB:0160,DB:0309,DB:0318,DB00141,DELIVRD,DISTURB,E:401,E:BLACK,E:ODDL,E:ODSL,E:RPTSS,EM:101,GG:0024,HD:0001,HD:19,HD:31,HD:32,IA:0051,IA:0054,IA:0059,IA:0073,IB:0008,IB:0194,IC:0001,IC:0015,IC:0055,ID:0004,ID:0070,JL:0025,JL:0026,JL:0031,JT:105,KEYWORD,LIMIT,LT:0005,MA:0022,MA:0051,MA:0054,MB:0008,MB:1026,MB:1042,MB:1077,MB:1279,MBBLACK,MC:0055,MC:0151,MH:17,MI:0008,MI:0009,MI:0015,MI:0017,MI:0020,MI:0022,MI:0024,MI:0041,MI:0043,MI:0044,MI:0045,MI:0048,MI:0051,MI:0053,MI:0054,MI:0057,MI:0059,MI:0064,MI:0080,MI:0081,MI:0098,MI:0099,MI:0999,MK:0002,MK:0003,MK:0006,MK:0008,MK:0009,MK:0010,MK:0015,MK:0017,MK:0019,MK:0020,MK:0022,MK:0023,MK:0024,MK:0041,MK:0043,MK:0044,MK:0045,MK:0053,MK:0055";
		realdelivrd += "MK:0057,MK:0098,MK:0099,MN:0000,MN:0009,MN:0011,MN:0012,MN:0019,MN:0020,MN:0022,MN:0029,MN:0041,MN:0043,MN:0044,MN:0045,MN:0050,MN:0053,MN:0055,MN:0098,MN:0174,MT:101,NOPASS,NOROUTE,REFUSED,REJECT,REJECTD,REJECTE,RP:103,RP:106,RP:108,RP:11,RP:115,RP:117,RP:15,RP:17,RP:18,RP:19,RP:2,RP:20,RP:213,RP:22,RP:239,RP:254,RP:255,RP:27,RP:29,RP:36,RP:44,RP:45,RP:48,RP:50,RP:52,RP:55,RP:57,RP:59,RP:61,RP:67,RP:70,RP:77,RP:79,RP:8,RP:86,RP:90,RP:92,RP:98,SGIP:-1,SGIP:10,SGIP:106,SGIP:11,SGIP:117,SGIP:118,SGIP:121,SGIP:14,SGIP:15,SGIP:16,SGIP:17,SGIP:19,SGIP:2,SGIP:20,SGIP:22,SGIP:23,SGIP:-25,SGIP:27,SGIP:-3,SGIP:31,SGIP:43,SGIP:44,SGIP:45,SGIP:48,SGIP:57,SGIP:61,SGIP:64,SGIP:67,SGIP:79,SGIP:86,SGIP:89,SGIP:90,SGIP:92,SGIP:93,SGIP:98,SGIP:99,SME1,SME-1,SME19,SME20,SME210,SME-22,SME-26,SME28,SME3,SME6,SME-70,SME-74,SME8,SME92,SME-93,SYS:005,SYS:008,TIMEOUT,UNDELIV,UNKNOWN,VALVE:M,W-BLACK,YX:1006,YX:7000,YX:8019,YX:9006";
		realdelivrd += "YY:0206,-181,023,036,043,044,706,712,718,721,730,763,779,879,CB:0013,CL:104,GATEBLA,IB:0011,ID:0199,JL:0028,LT:0022,MI:0021,MK:0068,RP:16,RP:65,RP:88,SGIP:-13,SGIP:63,SGIP:70,622,660,MI:0006,MK:0051,RP:121";
		String pausedelivrd = "000,001,005,008,084,617,702,716,801,809,802,817,869,731,EXPIRED,IC:0151,LT:0010,LT:0011,LT:0024,LT:0059,LT:0093,LT:0-37,MC:0001,MI:0000,MI:0001,MI:0002,MI:0004,MI:0005,MI:0010,MI:0011,MI:0012,MI:0013,MI:0023,MI:0029,MI:0030,MI:0036,MI:0038,MI:0050,MI:0055,MI:0056,MI:0063,MI:0068,MI:0083,MI:0084,MI:0089,MK:0011,MK:0013,MK:0029,MK:0036,MN:0013,MN:0017,MN:0036,MN:0051,MN:0054,MN:0059,RP:10,RP:104,RP:105,RP:118,RP:124,RP:13,RP:14,RP:182,RP:219,RP:231,RP:24,RP:253,RP:31,RP:4,RP:5,RP:51,RP:53,RP:54,RP:64,RP:75,RP:9,RP:93,SGIP:13,SGIP:-17,SGIP:18,SGIP:-2,SGIP:24,SGIP:29,SGIP:-37,SGIP:4,SGIP:-43,SGIP:5,SGIP:50,SGIP:51,SGIP:52,SGIP:53,SGIP:55,SGIP:58,SGIP:59,SGIP:-74,SGIP:77,SGIP:8,041,059,642,680,813,IB:0072,ID:0013,JL:0028,MI:0078,MK:0050,MK:0115,MK:0150,RP:175,RP:32,SGIP:6,SGIP:63,051,081,112,605,RP:121,SGIP:84,608,705";

		String[] realdelivrds = realdelivrd.split(",");
		for (String string : realdelivrds) {
			if (string.equals(delivrd)) {
				return "real";
			}
		}

		String[] pausedelivrds = pausedelivrd.split(",");
		for (String string : pausedelivrds) {
			if (string.equals(delivrd)) {
				return "pause";
			}
		}

		return "kong";

	}

	@Override
	public BackResult<PageDomain<MobileTestLogDomain>> getPageByUserId(int pageNo, int pageSize, String userId) {
		BackResult<PageDomain<MobileTestLogDomain>> result = new BackResult<PageDomain<MobileTestLogDomain>>();

		PageDomain<MobileTestLogDomain> pageDomain = new PageDomain<MobileTestLogDomain>();

		try {

			Page<MobileTestLog> page = mobileTestLogService.getPageByUserId(pageNo, pageSize, userId);

			if (null != page) {

				pageDomain.setTotalPages(page.getTotalPages());
				pageDomain.setNumPerPage(pageSize);
				pageDomain.setCurrentPage(pageNo);

				if (!CommonUtils.isNotEmpty(page.getContent())) {

					List<MobileTestLogDomain> listDomian = new ArrayList<MobileTestLogDomain>();
					for (MobileTestLog mobileTestLog : page.getContent()) {
						MobileTestLogDomain domain = new MobileTestLogDomain();
						BeanUtils.copyProperties(mobileTestLog, domain);
						listDomian.add(domain);
					}

					pageDomain.setTlist(listDomian);
				}

			}

			result.setResultObj(pageDomain);
			result.setResultMsg("获取成功");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取账户二次清洗检测结果列表出现系统异常：" + e.getMessage());
			result.setResultMsg("系统异常");
			result.setResultCode(ResultCode.RESULT_FAILED);
		}
		return result;
	}

	@Override
	public BackResult<MobileInfoDomain> findByMobile(String mobile, String userId) {

		DistributedLock lock = new DistributedLock(jedisPool);
		String lockName = RedisKeys.getInstance().getkhApifunKey(mobile);
		// 加锁
		String identifier = lock.lockWithTimeout(lockName, 10L, 3 * 1000);

		try {

			// 处理加锁业务
			if (null != identifier) {

				BackResult<MobileInfoDomain> result = new BackResult<MobileInfoDomain>();

				Date startTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -180);
				Date endTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), 1);
				// 创建对象设置初始手机号码
				MobileInfoDomain domain = new MobileInfoDomain();
				domain.setMobile(mobile);
				domain.setChargesStatus("1");
				// 1 查询号段
				MobileNumberSection section = mobileNumberSectionService.findByNumberSection(mobile.substring(0, 7));

				if (null == section) {
					domain.setLastTime(DateUtils.getCurrentDateTime());
					domain.setStatus("0"); // 空号
				} else {
					domain.setArea(section.getProvince() + "-" + section.getCity());
					domain.setNumberType(section.getMobilePhoneType());
				}

				// 2 查询库 最近6个月
				BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(mobile, startTime, endTime);

				if (null != detail) {
					domain.setLastTime(detail.getReportTime());
					if ("real".equals(this.isSpaceMobile(detail.getDelivrd()))) {
						domain.setStatus("1"); // 实号
					} else if ("kong".equals(this.isSpaceMobile(detail.getDelivrd()))) {
						domain.setStatus("0"); // 空号
					} else if ("pause".equals(this.isSpaceMobile(detail.getDelivrd()))) {
						domain.setStatus("2"); // 停机
					}
				} else {
					
					if (this.random()) {
						domain.setStatus("3"); // 库无 不计费
						domain.setChargesStatus("0");
					} else {
						domain.setStatus("4"); // 沉默号
						domain.setChargesStatus("1");
					}
					
				}

				// 记录日志入库
				Runnable run = new Runnable() {
					@Override
					public void run() {
						try {
							MobileTestLog mobileTestLog = new MobileTestLog();
							mobileTestLog.setOrderNo(
									DateUtils.getCurrentTimeMillis().substring(0, 4) + System.currentTimeMillis());
							mobileTestLog.setArea(domain.getArea());
							mobileTestLog.setChargesStatus(domain.getChargesStatus());
							mobileTestLog.setCreateTime(new Date());
							mobileTestLog.setLastTime(domain.getLastTime());
							mobileTestLog.setMobile(domain.getMobile());
							mobileTestLog.setNumberType(domain.getNumberType());
							mobileTestLog.setStatus(domain.getStatus());
							mobileTestLog.setUserId(userId);

							// 检测结果日志入库
							mongoTemplate.insert(mobileTestLog);

							// 记录流水记录
							WaterConsumption waterConsumption = new WaterConsumption();
							waterConsumption.setUserId(userId);
							waterConsumption.setId(UUIDTool.getInstance().getUUID());
							waterConsumption.setConsumptionNum("ECJC_" + System.currentTimeMillis());
							waterConsumption.setMenu("客户API接口账户二次清洗");
							waterConsumption.setStatus("1");
							waterConsumption.setType("2"); // 账户二次检测
							waterConsumption.setCreateTime(new Date());
							waterConsumption.setCount(String.valueOf(1)); // 条数
							waterConsumption.setUpdateTime(new Date());
							mongoTemplate.save(waterConsumption);
						} catch (Exception e) {
							logger.error("账号二次清洗日志信息入库系统异常：" + e.getMessage());
						}
					}
				};

				// 加入线程池开始执行
				threadExecutorService.execute(run);

				result.setResultObj(domain);
				lock.releaseLock(lockName, identifier);
				return result;
			} else {
				return new BackResult<MobileInfoDomain>(ResultCode.RESULT_API_NOTCONCURRENT, "正在计算中");
			}

		} catch (Exception e) {
			e.printStackTrace();
			lock.releaseLock(lockName, identifier);
			logger.error("账号二次清洗出现系统异常：" + e.getMessage());
		}

		return new BackResult<MobileInfoDomain>(ResultCode.RESULT_FAILED, "系统异常");
	}

	/**
	 * 库无号几率 3%
	 * @return
	 */
	public Boolean random() {
		Random r = new Random();
		int n = r.nextInt(100);
		return n <= 3 ? Boolean.TRUE : Boolean.FALSE;
	}
	
	public static void main(String[] args) {
		Random r = new Random();
		int n = r.nextInt(100);
		
		
		boolean fag = n <= 3 ? Boolean.TRUE : Boolean.FALSE;
		System.out.println(fag);
		if (fag) {
			System.out.println(111);
		} else {
			System.out.println(222);
		}
		
		
	}
}
