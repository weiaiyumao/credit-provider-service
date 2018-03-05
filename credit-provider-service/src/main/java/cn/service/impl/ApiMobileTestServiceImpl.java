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
import cn.redis.RedisClient;
import cn.service.ApiMobileTestService;
import cn.service.MobileNumberSectionService;
import cn.service.MobileTestLogService;
import cn.service.SpaceDetectionService;
import cn.task.helper.MobileDetailHelper;
//import cn.thread.ThreadExecutorService;
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
	private RedisClient redisClient;

//	@Autowired
//	private ThreadExecutorService threadExecutorService;

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

						// 存在数据 (实号：real，空号：kong，沉默号：silence，停机：downtime)
						String status = MobileDetailHelper.getInstance().getMobileStatusForAPI(mobile, detail.getDelivrd());
						
						if (status.equals("real")) {
							domain.setStatus("1"); // 实号
						} else if (status.equals("kong")){
							// 空号
							domain.setStatus("0"); // 空号
						} else if (status.equals("silence")){
							// 沉默号
							domain.setStatus("4"); // 沉默号
						}else if (status.equals("downtime")){
							// 停机
							domain.setStatus("2"); // 停机
						} else {
							// 沉默号
							domain.setStatus("4"); // 沉默号
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

						
						// 存在数据 (实号：real，空号：kong，沉默号：silence)
						String status = MobileDetailHelper.getInstance().getMobileStatusForAPI(mobile, detail.getDelivrd());
						
						if (status.equals("real")) {
							domain.setStatus("1"); // 实号
						} else if (status.equals("kong")){
							// 空号
							domain.setStatus("0"); // 空号
						} else if (status.equals("silence")){
							// 沉默号
							domain.setStatus("4"); // 沉默号
						}else {
							// 沉默号
							domain.setStatus("4"); // 沉默号
						}
						
						list.add(domain);
					}

				}

				
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
						mobileTestLog.setType("2"); // 账户二次检测类型
						listMobile.add(mobileTestLog);

						if (mobileInfoDomain.getChargesStatus().equals("1")) {
							changeCount = changeCount + 1;
						}

					}

					// 记录消费条数进入redis
					String RQAPIcountKey = RedisKeys.getInstance().getRQAPIcountKey(userId);
					Integer RQAPIcount = Integer.parseInt(redisClient.get(RQAPIcountKey).toString());
					RQAPIcount = RQAPIcount - 1;
					redisClient.set(RQAPIcountKey, String.valueOf(RQAPIcount));
					
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

	@Override
	public BackResult<PageDomain<MobileTestLogDomain>> getPageByUserId(int pageNo, int pageSize, String userId, String type) {
		BackResult<PageDomain<MobileTestLogDomain>> result = new BackResult<PageDomain<MobileTestLogDomain>>();

		PageDomain<MobileTestLogDomain> pageDomain = new PageDomain<MobileTestLogDomain>();

		try {

			Page<MobileTestLog> page = mobileTestLogService.getPageByUserId(pageNo, pageSize, userId,type);

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
					
					// 存在数据 (实号：real，空号：kong，沉默号：silence，停机：downtime)
					String status = MobileDetailHelper.getInstance().getMobileStatusForAPI(mobile, detail.getDelivrd());
					
					if (status.equals("real")) {
						domain.setStatus("1"); // 实号
					} else if (status.equals("kong")){
						// 空号
						domain.setStatus("0"); // 空号
					} else if (status.equals("silence")){
						// 沉默号
						domain.setStatus("2");
					}else if (status.equals("downtime")){
						// 停机
						domain.setStatus("4"); // 停机
					} else {
						// 沉默号
						domain.setStatus("2");
					}

					// 给回执时间定制 返回值
					if (DateUtils.getIntervalDaysByDate(DateUtils.getCurrentDateTime(), detail.getReportTime()) >= 28) {
						domain.setLastTime(DateUtils.addDay(DateUtils.getCurrentDateTime(), -15)); // 系统当前时间减掉15天 当做回执时间
					} else {
						domain.setLastTime(detail.getReportTime());
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
				mobileTestLog.setType("3"); // 空号API检测类型
				// 检测结果日志入库
				mongoTemplate.insert(mobileTestLog);
				
				// 记录消费条数进入redis
				String KHAPIcountKey = RedisKeys.getInstance().getKHAPIcountKey(userId);
				Integer KhTestCount = Integer.parseInt(redisClient.get(KHAPIcountKey).toString());
				KhTestCount = KhTestCount - 1;
				redisClient.set(KHAPIcountKey, String.valueOf(KhTestCount));

				// 记录流水记录
				WaterConsumption waterConsumption = new WaterConsumption();
				waterConsumption.setUserId(userId);
				waterConsumption.setId(UUIDTool.getInstance().getUUID());
				waterConsumption.setConsumptionNum("ECJC_" + System.currentTimeMillis());
				waterConsumption.setMenu("空号API接口检测");
				waterConsumption.setStatus("1");
				waterConsumption.setType("3"); // 空号API检测类型
				waterConsumption.setCreateTime(new Date());
				waterConsumption.setCount(String.valueOf(1)); // 条数
				waterConsumption.setUpdateTime(new Date());
				mongoTemplate.save(waterConsumption);
				
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
