package cn.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import cn.service.MobileTestLogService;
import cn.service.SpaceDetectionService;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import cn.utils.UUIDTool;
import main.java.cn.common.BackResult;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.MobileInfoDomain;
import main.java.cn.domain.MobileTestLogDomain;
import main.java.cn.domain.page.PageDomain;
import main.java.cn.sms.util.ChuangLanSmsUtil;

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

	// 注入RedisTemplate对象
	@Resource(name = "redisTemplate")
	private RedisTemplate<String, String> redisTemplate;

	@Override
	public BackResult<List<MobileInfoDomain>> findByMobileNumbers(String mobileNumbers, String userId) {

		RedisLock lock = new RedisLock(redisTemplate, "sh_" + mobileNumbers + "_" + userId, 0, 3 * 1000);

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

				// this.logInfoSaveDB(list, userId);
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							if (!CommonUtils.isNotEmpty(list)) {

								int changeCount = 0;

								List<MobileTestLog> listMobile = new ArrayList<MobileTestLog>();

								for (MobileInfoDomain mobileInfoDomain : list) {
									MobileTestLog mobileTestLog = new MobileTestLog();
									mobileTestLog.setOrderNo(
											DateUtils.getCurrentTimeMillis().substring(0, 4) + System.currentTimeMillis());
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
				}, "日志信息入库").start();
				
				lock.unlock(); // 注销锁
				result.setResultObj(list);
				return result;
			} else {
				return new BackResult<List<MobileInfoDomain>>(ResultCode.RESULT_API_NOTCONCURRENT, "正在计算中");
			}

		} catch (Exception e) {
			e.printStackTrace();
			lock.unlock(); // 注销锁
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
	public Boolean isSpaceMobile(String delivrd) {
		if (delivrd.equals("101") || delivrd.equals("-1") || delivrd.equals("SGIP:2：12") || delivrd.equals("ERRNUM")
				|| delivrd.equals("RP:1") || delivrd.equals("MN:0001") || delivrd.equals("SPMSERR:136")
				|| delivrd.equals("MK:0000") || delivrd.equals("MK:0001") || delivrd.equals("SGIP:1")
				|| delivrd.equals("SGIP:33") || delivrd.equals("SGIP:67") || delivrd.equals("LT:0001")
				|| delivrd.equals("3") || delivrd.equals("Deliver") || delivrd.equals("CB:0001")
				|| delivrd.equals("CB:0053") || delivrd.equals("DB:0101") || delivrd.equals("12")
				|| delivrd.equals("12") || delivrd.equals("601") || delivrd.equals("MK:0012") || delivrd.equals("HD:31")
				|| delivrd.equals("IC:0001") || delivrd.equals("MI:0011") || delivrd.equals("MI:0013")
				|| delivrd.equals("MI:0029") || delivrd.equals("MK:0005")
				|| delivrd.equals("UNKNOWN") || delivrd.equals("MI:0024") || delivrd.equals("MI:0054")
				|| delivrd.equals("MN:0059") || delivrd.equals("MI:0059") || delivrd.equals("MI:0055")
				|| delivrd.equals("MI:0004") || delivrd.equals("MI:0005")) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	/**
	 * 日志数据入库
	 * 
	 * @param list
	 * @param userId
	 */
	public void logInfoSaveDB(List<MobileInfoDomain> list, String userId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (!CommonUtils.isNotEmpty(list)) {

						int changeCount = 0;

						List<MobileTestLog> listMobile = new ArrayList<MobileTestLog>();

						for (MobileInfoDomain mobileInfoDomain : list) {
							MobileTestLog mobileTestLog = new MobileTestLog();
							mobileTestLog.setOrderNo(
									DateUtils.getCurrentTimeMillis().substring(0, 4) + System.currentTimeMillis());
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
		}, "日志信息入库").start();
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
	public BackResult<Boolean> findByMobile(String mobile,String userId) {
		
		RedisLock lock = new RedisLock(redisTemplate, "sh_dg_" + mobile + "_" + userId, 0, 3 * 1000);

		try {

			// 处理加锁业务
			if (lock.lock()) {

				BackResult<Boolean> result = new BackResult<Boolean>();

				List<MobileInfoDomain> list = new ArrayList<MobileInfoDomain>();

				Date startTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -90);
				Date endTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), 1);
				// 创建对象设置初始手机号码
				MobileInfoDomain domain = new MobileInfoDomain();
				domain.setMobile(mobile);

				// 1 查询号段
				MobileNumberSection section = mobileNumberSectionService
						.findByNumberSection(mobile.substring(0, 7));

				if (null == section) {
					domain.setLastTime(DateUtils.getCurrentDateTime());
					domain.setChargesStatus("1");
					domain.setStatus("0"); // 空号
					list.add(domain);
					return result;
				}

				// 2 查询库 最近3个月
				BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(mobile, startTime,
						endTime);

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
					
					return result;
				}

				// 发送短信
				if (!ChuangLanSmsUtil.getInstance().sendYxByMobile(mobile)) {
					logger.info("----手机号码：" + mobile + "营销短信发送失败");
				}
				
				lock.unlock(); // 注销锁
				result.setResultObj(null);
				return result;
			} else {
				return new BackResult<Boolean>(ResultCode.RESULT_API_NOTCONCURRENT, "正在计算中");
			}

		} catch (Exception e) {
			e.printStackTrace();
			lock.unlock(); // 注销锁
			logger.error("账号二次清洗出现系统异常：" + e.getMessage());
		}

		return new BackResult<Boolean>(ResultCode.RESULT_FAILED, "系统异常");
	}

	
	public static void main(String[] args) {
		System.out.println(Runtime.getRuntime().availableProcessors());
	}
}
