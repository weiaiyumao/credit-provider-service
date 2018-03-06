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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.entity.ApiLog;
import cn.entity.MobileNumberSection;
import cn.entity.MobileTestLog;
import cn.entity.WaterConsumption;
import cn.entity.base.BaseMobileDetail;
import cn.enums.JjrtIdTypeEnum;
import cn.enums.JjrtResultCodeEnum;
import cn.redis.DistributedLock;
import cn.redis.RedisClient;
import cn.service.ApiMobileTestService;
import cn.service.MobileNumberSectionService;
import cn.service.MobileTestLogService;
import cn.service.OpenApiService;
import cn.service.SpaceDetectionService;
import cn.task.helper.MobileDetailHelper;
//import cn.thread.ThreadExecutorService;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import cn.utils.UUIDTool;
import main.java.cn.common.BackResult;
import main.java.cn.common.RedisKeys;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.ApiLogPageDomain;
import main.java.cn.domain.MobileInfoDomain;
import main.java.cn.domain.MobileTestLogDomain;
import main.java.cn.domain.page.PageDomain;
import main.java.cn.hhtp.util.HttpUtil;
import main.java.cn.sms.util.ChuangLanSmsUtil;
import main.java.cn.untils.KeyUtil;
import redis.clients.jedis.JedisPool;

@Service
public class ApiMobileTestServiceImpl implements ApiMobileTestService {

	private final static Logger logger = LoggerFactory.getLogger(ApiMobileTestServiceImpl.class);

	@Autowired
	private SpaceDetectionService spaceDetectionService;
	
	@Autowired
	private OpenApiService openApiService;
	
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
	public BackResult<PageDomain<ApiLogPageDomain>> getPageByCustomerId(int pageNo, int pageSize, String customerId, String method) {
		BackResult<PageDomain<ApiLogPageDomain>> result = new BackResult<PageDomain<ApiLogPageDomain>>();

		PageDomain<ApiLogPageDomain> pageDomain = new PageDomain<ApiLogPageDomain>();

		try {

			Page<ApiLog> page = mobileTestLogService.getPageByCustomerId(pageNo, pageSize, customerId,method);

			if (null != page) {

				pageDomain.setTotalPages(page.getTotalPages());
				pageDomain.setNumPerPage(pageSize);
				pageDomain.setCurrentPage(pageNo);

				if (!CommonUtils.isNotEmpty(page.getContent())) {

					List<ApiLogPageDomain> listDomian = new ArrayList<ApiLogPageDomain>();
					for (ApiLog apiLog : page.getContent()) {
						ApiLogPageDomain domain = new ApiLogPageDomain();
						domain.setId(apiLog.getId());
						domain.setCustomerId(customerId);
						domain.setMethod(method);
						domain.setCreateTime(apiLog.getCreatetime());
						//接口参数串解析成json对象
						JSONObject paramJson = JSONObject.parseObject(apiLog.getParams());
						domain.setName(paramJson.getString("name"));
						domain.setIdtype(JjrtIdTypeEnum.getName(paramJson.getString("idtype")));
						domain.setIdnum(paramJson.getString("idnum"));
						if("normal_checkBankInfo".equals(method)){
							domain.setCardno(paramJson.getString("cardno"));
							domain.setMobile(paramJson.getString("mobile"));
						}
						//接口返回结果串解析成json对象
						JSONObject resultJson = JSONObject.parseObject(apiLog.getResultJson());
						domain.setResult("0000000".equals(resultJson.getString("result"))?"成功":"失败");
						domain.setResultDesc(JjrtResultCodeEnum.getName(resultJson.getString("result")));
						listDomian.add(domain);
					}

					pageDomain.setTlist(listDomian);
				}

			}

			result.setResultObj(pageDomain);
			result.setResultMsg("操作成功");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取接口检测结果列表出现系统异常：" + e.getMessage());
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
	
	@Override
	public BackResult<MobileInfoDomain> findByMobileToAmi(String mobile, String userId,String method) {

		DistributedLock lock = new DistributedLock(jedisPool);
		String lockName = RedisKeys.getInstance().getkhApifunKey(mobile);
		// 加锁
		String identifier = lock.lockWithTimeout(lockName, 10L, 3 * 1000);
		String delivrdStates = "DELIVRD";//实号状态
		String pauseStates = "PAUSE:AMI,SGIP:13,SGIP:-74,MN:0013,MK:0013,SGIP:5,MI:0013,MK:0005,";//停机状态
		String onliNoStates = "ONLINE:NO,SGIP:24,SGIP:54,EXPIRED,MI:0024,SGIP:59,SGIP:29,";//在网但不可用状态
		String nullStates = "NULL:AMI,SME169,MK:0012,MN:0001,MK:0001,SGIP:1,SGIP:23,MK:0000,SGIP:12,MK:0010,";//空号状态
		String ynullStates = "YNULL:AMI";//预销号状态
		String unStates = "UNUSUAL:AMI";//异常状态
		String dev_log = "";
		
		try {

			// 处理加锁业务
			if (null != identifier) {

				BackResult<MobileInfoDomain> result = new BackResult<MobileInfoDomain>();

				Date startTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -30);
				Date endTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), 1);
				// 创建对象设置初始手机号码
				MobileInfoDomain domain = new MobileInfoDomain();
				domain.setMobile(mobile);
				domain.setChargesStatus("1");
				domain.setLastTime(DateUtils.getCurrentDateTime());
				// 1 查询号段
				MobileNumberSection section = mobileNumberSectionService.findByNumberSection(mobile.substring(0, 7));

				if (null == section) {
					domain.setLastTime(DateUtils.getCurrentDateTime());
					domain.setStatus("4"); // 销号
				} else {
					domain.setArea(section.getProvince() + "-" + section.getCity());
					domain.setNumberType(section.getMobilePhoneType());
				}

				// 2 查询库 最近1个月
				BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(mobile, startTime, endTime);
				if (null != detail) {					
					String delivrd = detail.getDelivrd();					
					if (delivrdStates.equals(delivrd)) {
						domain.setStatus("1"); // 实号
					} else if (pauseStates.contains(delivrd+",")){
						domain.setStatus("2"); // 停机
					} else if (onliNoStates.contains(delivrd+",")){
						domain.setStatus("3"); // 在网但不可用
					}else  if (nullStates.contains(delivrd+",")){
						domain.setStatus("4"); // 销号
					}else  if (ynullStates.equals(delivrd)){
						domain.setStatus("5"); // 预销号
					}else  if (unStates.equals(delivrd)){
						domain.setStatus("6"); // 异常
					}else{
						String paramString = "mobile=" + mobile;
						//获取参数json串
						JSONObject paramJson = KeyUtil.getParamJson(userId, method, paramString);
						String resultJson = openApiService.getCheckMobileStatue(paramJson);
						JSONObject resultObj = JSONObject.parseObject(resultJson);
						String amiStr = resultObj.getString("resultObj");
						JSONObject amiObj = JSONObject.parseObject(amiStr);
						if(!"0".equals(resultObj.getString("resultCode"))){
							return new BackResult<MobileInfoDomain>(resultObj.getString("resultCode"), resultObj.getString("resultMsg"));
						}
						if(!"0000000".equals(amiObj.getString("value"))){
							return new BackResult<MobileInfoDomain>(amiObj.getString("value"), amiObj.getString("name"));
						}
						String status = amiObj.getString("desc").substring(1, amiObj.getString("desc").length());
						if("1".equals(status)){
							dev_log = "DELIVRD";
							domain.setStatus("1"); // 实号
						}else if("2".equals(status)){
							dev_log = "PAUSE:AMI";
							domain.setStatus("2"); // 停机
						}else if("3".equals(status)){
							dev_log = "ONLINE:NO";
							domain.setStatus("3"); // 在网但不可用
						}else if("4".equals(status)){
							dev_log = "NULL:AMI";
							domain.setStatus("4"); // 销号
						}else if("5".equals(status)){
							dev_log = "YNULL:AMI";
							domain.setStatus("5"); // 预销号
						}else if("6".equals(status)){
							dev_log = "UNUSUAL:AMI";
							domain.setStatus("6"); // 异常
						}else{
							domain.setStatus("100"); //查无记录
						}
						
						BaseMobileDetail mobileDetail = MobileDetailHelper.getInstance().getBaseMobileDetail(mobile);
						mobileDetail.setMobile(mobile);
						mobileDetail.setMobile(dev_log);
						mobileDetail.setReportTime(DateUtils.getNowDate());
						mongoTemplate.insert(mobileDetail);
					}					
				} else {
					String paramString = "mobile=" + mobile;
					//获取参数json串
					JSONObject paramJson = KeyUtil.getParamJson(userId, method, paramString);
					String resultJson = openApiService.getCheckMobileStatue(paramJson);
					JSONObject resultObj = JSONObject.parseObject(resultJson);
					String amiStr = resultObj.getString("resultObj");					;
					JSONObject amiObj = JSONObject.parseObject(JSONArray.parseArray(amiStr).get(0).toString());
					if(!"0".equals(resultObj.getString("resultCode"))){
						return new BackResult<MobileInfoDomain>(resultObj.getString("resultCode"), resultObj.getString("resultMsg"));
					}
					if(!"0000".equals(amiObj.getString("value"))){
						return new BackResult<MobileInfoDomain>(amiObj.getString("value"), amiObj.getString("name"));
					}
					String status = amiObj.getString("desc").substring(1, amiObj.getString("desc").length());
					if("1".equals(status)){
						dev_log = "DELIVRD";
						domain.setStatus("1"); // 实号
					}else if("2".equals(status)){
						dev_log = "PAUSE:AMI";
						domain.setStatus("2"); // 停机
					}else if("3".equals(status)){
						dev_log = "ONLINE:NO";
						domain.setStatus("3"); // 在网但不可用
					}else if("4".equals(status)){
						dev_log = "NULL:AMI";
						domain.setStatus("4"); // 销号
					}else if("5".equals(status)){
						dev_log = "YNULL:AMI";
						domain.setStatus("5"); // 预销号
					}else if("6".equals(status)){
						dev_log = "UNUSUAL:AMI";
						domain.setStatus("6"); // 异常
					}else{
						domain.setStatus("100"); //查无记录
					}
					
					BaseMobileDetail mobileDetail = MobileDetailHelper.getInstance().getBaseMobileDetail(mobile);
					mobileDetail.setMobile(mobile);
					mobileDetail.setMobile(dev_log);
					mobileDetail.setReportTime(DateUtils.getNowDate());
					mongoTemplate.insert(mobileDetail);
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
				mobileTestLog.setType("4"); // 空号API检测类型
				// 检测结果日志入库
				mongoTemplate.insert(mobileTestLog);
				
				// 记录消费条数进入redis
				String msAPIcountKey = RedisKeys.getInstance().getMsAPIcountKey(userId);
				Integer msTestCount = Integer.parseInt(redisClient.get(msAPIcountKey).toString());
				msTestCount = msTestCount - 1;
				redisClient.set(msAPIcountKey, String.valueOf(msTestCount));

				// 记录流水记录
				WaterConsumption waterConsumption = new WaterConsumption();
				waterConsumption.setUserId(userId);
				waterConsumption.setId(UUIDTool.getInstance().getUUID());
				waterConsumption.setConsumptionNum("ECJC_" + System.currentTimeMillis());
				waterConsumption.setMenu("号码实时检测接口");
				waterConsumption.setStatus("1");
				waterConsumption.setType("4"); // 号码实时检测类型
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
			logger.error("号码实时检测接口出现系统异常：" + e.getMessage());
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
