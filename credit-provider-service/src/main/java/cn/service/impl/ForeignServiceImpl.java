package cn.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import cn.entity.CvsFilePath;
import cn.entity.MobileNumberSection;
import cn.entity.WaterConsumption;
import cn.entity.base.BaseMobileDetail;
import cn.redis.DistributedLock;
import cn.redis.RedisClient;
import cn.service.CvsFilePathService;
import cn.service.ForeignService;
import cn.service.MobileNumberSectionService;
import cn.service.SpaceDetectionService;
import cn.task.helper.MobileDetailHelper;
import cn.thread.ThreadExecutorService;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import cn.utils.FileUtils;
import cn.utils.UUIDTool;
import main.java.cn.common.BackResult;
import main.java.cn.common.RedisKeys;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.CvsFilePathDomain;
import main.java.cn.domain.RunTestDomian;
import main.java.cn.domain.page.PageDomain;
import main.java.cn.sms.util.ChuangLanSmsUtil;
import redis.clients.jedis.JedisPool;

@Service
public class ForeignServiceImpl implements ForeignService {

	private final static Logger logger = LoggerFactory.getLogger(ForeignServiceImpl.class);

	@Autowired
	private SpaceDetectionService spaceDetectionService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Value("${server.port}")
	private String port;

	@Value("${loadfilePath}")
	private String loadfilePath;

	@Autowired
	private JedisPool jedisPool;

	@Autowired
	private RedisClient redisClient;

	@Autowired
	private ThreadExecutorService threadExecutorService;

	@Autowired
	private CvsFilePathService cvsFilePathService;

	@Autowired
	private MobileNumberSectionService mobileNumberSectionService;

	@Deprecated
	@Override
	public BackResult<RunTestDomian> runTheTest(String fileUrl, String userId, String timestamp, String mobile) {
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();
		return result;
	}

	@Override
	public BackResult<List<CvsFilePathDomain>> findByUserId(String userId) {

		BackResult<List<CvsFilePathDomain>> result = new BackResult<List<CvsFilePathDomain>>();

		List<CvsFilePathDomain> list = new ArrayList<CvsFilePathDomain>();

		try {
			List<CvsFilePath> listCvsFilePath = cvsFilePathService.findByUserId(userId);

			if (CommonUtils.isNotEmpty(listCvsFilePath)) {
				result.setResultMsg("改用户没有订单信息");
			}

			for (CvsFilePath cvsFilePath : listCvsFilePath) {
				CvsFilePathDomain domain = new CvsFilePathDomain();
				BeanUtils.copyProperties(cvsFilePath, domain);
				list.add(domain);
			}

			result.setResultObj(list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("客户ID：[" + userId + "]查询下载列表系统异常：" + e.getMessage());
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("客户ID：[" + userId + "]查询下载列表系统异常：" + e.getMessage());
		}

		return result;
	}

	@Override
	public BackResult<Boolean> deleteCvsByIds(String ids, String userId) {

		logger.info("用户ID：【" + userId + "执行删除下载记录");

		BackResult<Boolean> result = new BackResult<Boolean>();

		try {
			cvsFilePathService.deleteByIds(ids);
			result.setResultObj(Boolean.TRUE);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("客户ID：[" + userId + "]执行删除下载记录系统异常：" + e.getMessage());
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("客户ID：[" + userId + "]查询下载列表系统异常：" + e.getMessage());
			result.setResultObj(Boolean.FALSE);
		}
		return result;
	}

	@Override
	public BackResult<PageDomain<CvsFilePathDomain>> getPageByUserId(int pageNo, int pageSize, String userId) {
		BackResult<PageDomain<CvsFilePathDomain>> result = new BackResult<PageDomain<CvsFilePathDomain>>();

		PageDomain<CvsFilePathDomain> pageDomain = new PageDomain<CvsFilePathDomain>();

		try {

			Page<CvsFilePath> page = cvsFilePathService.getPageByUserId(pageNo, pageSize, userId);

			if (null != page) {
				pageDomain.setTotalNumber(Integer.valueOf(String.valueOf(page.getTotalElements())));
				pageDomain.setTotalPages(page.getTotalPages());
				pageDomain.setNumPerPage(pageSize);
				pageDomain.setCurrentPage(pageNo);

				if (!CommonUtils.isNotEmpty(page.getContent())) {

					List<CvsFilePathDomain> listDomian = new ArrayList<CvsFilePathDomain>();
					for (CvsFilePath mobileTestLog : page.getContent()) {
						CvsFilePathDomain domain = new CvsFilePathDomain();
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
			logger.error("获取实号检测下载列表异常：" + e.getMessage());
			result.setResultMsg("系统异常");
			result.setResultCode(ResultCode.RESULT_FAILED);
		}
		return result;
	}

	public BackResult<RunTestDomian> theTest555(String fileUrl, String userId, String mobile, String source,
			String startLine, String type) {
		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();
		DistributedLock lock = new DistributedLock(jedisPool);
		String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile);
		String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId);
		String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId);
		String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId);
		String succeedClearingCountkey = RedisKeys.getInstance().getkhSucceedClearingCountkey(userId);
		int expire = 2 * 60 * 60 * 1000;
		// 执行检测
		if (type.equals("1")) {
			// 加锁
			String identifier = lock.lockWithTimeout(lockName, 800L, expire);
			// 处理加锁业务
			if (null != identifier) {

				// 将标识存入redis
				redisClient.set(redisLockIdentifier, identifier, expire);

				// 创建一个线程
				Runnable run = new Runnable() {
					@Override
					public void run() {
						BufferedReader br = null;
						try {

							int testCount = 0; // 需要记账的总条数
							int count = 0; // 实际检测的总条数

							logger.info("----------用户编号：[" + userId + "]文件地址：[" + fileUrl + "]开始执行空号检索事件 事件开始时间："
									+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "post:" + port);

							List<List<Object>> thereDataList = new ArrayList<List<Object>>();
							List<Object> thereRowList = null;
							List<Map<String, Object>> sixDataList = new ArrayList<Map<String, Object>>();
							List<List<Object>> unKonwDataList = new ArrayList<List<Object>>();
							List<Object> unKonwRowList = null;

							Date sixStartTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -210);

							File file = new File(fileUrl);
							if (file.isFile() && file.exists()) {

								InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
								br = new BufferedReader(isr);
								String lineTxt = null;

								while ((lineTxt = br.readLine()) != null) {

									count = count + 1;
									redisClient.set(succeedTestCountkey, String.valueOf(count), expire);
									if (CommonUtils.isNotString(lineTxt)) {
										continue;
									}

									// 去掉字符串中的所有空格
									lineTxt = lineTxt.replace(" ", "");

									// 验证是否为正常的１１位有效数字
									if (!CommonUtils.isNumeric(lineTxt)) {
										continue;
									}

									// 检测 3个月内
									BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(lineTxt,
											sixStartTime, DateUtils.getCurrentDateTime());

									if (null != detail) {

										// 存在数据
										if ("real".equals(isSpaceMobile(detail.getDelivrd()))) {
											// 实号
											thereRowList = new ArrayList<Object>();
											thereRowList.add(detail.getMobile());
											thereDataList.add(thereRowList);
										} else if ("pause".equals(isSpaceMobile(detail.getDelivrd()))) {
											// 停机
											Map<String, Object> sixRowList = new HashMap<>();
											sixRowList.put("mobile", detail.getMobile());
											sixRowList.put("delivd", 2);// 停机状态
											sixRowList.put("reportTime", detail.getReportTime().getTime());
											sixDataList.add(sixRowList);
										} else if ("kong".equals(isSpaceMobile(detail.getDelivrd()))) {
											// 空号
											Map<String, Object> sixRowList = new HashMap<>();
											sixRowList.put("mobile", detail.getMobile());
											sixRowList.put("delivd", 1);// 空号状态
											sixRowList.put("reportTime", detail.getReportTime().getTime());
											sixDataList.add(sixRowList);
										} else {
											// 未知
											unKonwRowList = new ArrayList<Object>();
											unKonwRowList.add(lineTxt);
											unKonwDataList.add(unKonwRowList);
										}

									} else {

										// 二次清洗根据号段
										MobileNumberSection section = mobileNumberSectionService
												.findByNumberSection(lineTxt.substring(0, 7));

										if (null != section) {

											unKonwRowList = new ArrayList<Object>();
											unKonwRowList.add(lineTxt);
											unKonwDataList.add(unKonwRowList);
										} else {
											// 放空号
											Map<String, Object> sixRowList = new HashMap<>();
											sixRowList.put("mobile", lineTxt);
											sixRowList.put("delivd", 1);// 空号状态
											sixRowList.put("reportTime", DateUtils
													.converYYYYMMddHHmmssStrToDate("1900-01-01 00:00:00").getTime());
											sixDataList.add(sixRowList);

										}

									}

									testCount = testCount + 1;
								}

							}

							// 将需要结账的条数存入redis
							redisClient.set(succeedClearingCountkey, String.valueOf(testCount), expire * 2);

							// 文件地址入库
							CvsFilePath cvsFilePath = new CvsFilePath();
							cvsFilePath.setUserId(userId);

							// 生成报表
							String timeTemp = String.valueOf(System.currentTimeMillis());
							String filePath = loadfilePath + userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/";
							if (!CommonUtils.isNotEmpty(thereDataList)) {
								logger.info("----------实号总条数：" + thereDataList.size());
								Object[] shhead = { "手机号码" };
								FileUtils.createCvsFile("实号.csv", filePath, thereDataList, shhead);
								cvsFilePath.setThereCount(String.valueOf(thereDataList.size()));
							}

							if (!CommonUtils.isNotEmpty(sixDataList)) {
								logger.info("----------空号总条数：" + sixDataList.size());
								Object[] head = { "手机号码" };
								try {
									Collections.sort(sixDataList, new Comparator<Map<String, Object>>() {
										@Override
										public int compare(Map<String, Object> arg0, Map<String, Object> arg1) {
											try {
												Long reportTime0 = Long.parseLong(arg0.get("delivd").toString()
														+ arg0.get("reportTime").toString());
												Long reportTime1 = Long.parseLong(arg1.get("delivd").toString()
														+ arg1.get("reportTime").toString());
												return reportTime0.compareTo(reportTime1);
											} catch (NumberFormatException e) {
												return 0;
											}
										}
									});
								} catch (Exception e) {
								}
								FileUtils.createCvsFileByMap("空号.csv", filePath, sixDataList, head);
								cvsFilePath.setSixCount(String.valueOf(sixDataList.size()));
							}

							if (!CommonUtils.isNotEmpty(unKonwDataList)) {
								logger.info("----------沉默号总条数：" + unKonwDataList.size());
								Object[] wzhead = { "手机号码" };
								FileUtils.createCvsFile("沉默号.csv", filePath, unKonwDataList, wzhead);
								cvsFilePath.setUnknownSize(String.valueOf(unKonwDataList.size()));
							}

							List<File> list = new ArrayList<File>();

							if (!CommonUtils.isNotEmpty(thereDataList)) {
								list.add(new File(filePath + "实号.csv"));
								cvsFilePath.setThereFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/实号.csv");
								cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "实号.csv"));
							}

							if (!CommonUtils.isNotEmpty(sixDataList)) {
								list.add(new File(filePath + "空号.csv"));
								cvsFilePath.setSixFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/空号.csv");
								cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "空号.csv"));
							}

							if (!CommonUtils.isNotEmpty(unKonwDataList)) {
								list.add(new File(filePath + "沉默号.csv"));
								cvsFilePath.setUnknownFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/沉默号.csv");
								cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "沉默号.csv"));
							}

							String zipName = "测试结果包.zip";
							// 报表文件打包
							if (null != list && list.size() > 0) {
								zipName = "测试结果包.zip";
								FileUtils.createZip(list, filePath + zipName);
								cvsFilePath.setZipName(zipName);
								cvsFilePath.setZipPath(
										(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/测试结果包.zip"));
								cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
							}

							cvsFilePath.setCreateTime(new Date());
							
							if (CommonUtils.isNotString(cvsFilePath.getThereFilePath()) && CommonUtils.isNotString(cvsFilePath.getSixFilePath()) && CommonUtils.isNotString(cvsFilePath.getUnknownFilePath())){
								if ("pc1.0".equals(source)) {
									// 发送短信
									ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
								} else {
									// 异常发送短信
									ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestZZtEx(mobile);
								}
							} else {
								mongoTemplate.save(cvsFilePath);
							}
								
							// 记录流水记录
							WaterConsumption waterConsumption = new WaterConsumption();
							waterConsumption.setUserId(userId);
							waterConsumption.setId(UUIDTool.getInstance().getUUID());
							waterConsumption.setConsumptionNum("SHJC_" + System.currentTimeMillis());
							waterConsumption.setMenu("客户上传文件实号检测");
							waterConsumption.setStatus("1");
							waterConsumption.setType("1"); // 实号检测
							waterConsumption.setSource(source);
							waterConsumption.setCreateTime(new Date());
							waterConsumption.setCount(String.valueOf(testCount)); // 条数
							waterConsumption.setUpdateTime(new Date());
							mongoTemplate.save(waterConsumption);

							if ("pc1.0".equals(source)) {
								// 发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTest(mobile);
							} else {
								// 发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForZZTTest(mobile);
							}

							// 封装返回对象
							result.setResultMsg("成功");
							runTestDomian.setRunCount(count);
							runTestDomian.setStatus("2"); // 1执行中 2执行结束 // 3执行异常
							logger.info("----------用户编号：[" + userId + "]文件地址：[" + fileUrl + "]结束空号检索事件 事件结束时间："
									+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
						} catch (Exception e) {
							e.printStackTrace();
							logger.error("----------客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
							this.clearLockAndCountForRun(lock, userId, mobile);
							if ("pc1.0".equals(source)) {
								// 发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
							} else {
								// 异常发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestZZtEx(mobile);
							}
						} finally {
							if (null != br) {
								try {
									br.close();
								} catch (IOException e) {
									e.printStackTrace();
									this.clearLockAndCountForRun(lock, userId, mobile);
									logger.error("文件流关闭异常：" + e.getMessage());
								}
							}
						}

					}

					/**
					 * 清空条数注销锁
					 * 
					 * @param lock
					 * @param userId
					 * @param mobile
					 */
					private void clearLockAndCountForRun(DistributedLock lock, String userId, String mobile) {
						String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile);
						String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId);
						String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId);
						String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId);
						String identifier = redisClient.get(redisLockIdentifier);
						// 清空 记录到redis的条数
						redisClient.remove(KhTestCountKey);
						redisClient.remove(succeedTestCountkey);
						lock.releaseLock(lockName, identifier); // 注销锁

					}
				};

				// 加入线程池开始执行
				threadExecutorService.execute(run);
				result.setResultMsg("任务执行中");
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				runTestDomian.setRunCount(0); // 设置运行的总条数
			} else {
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				runTestDomian.setRunCount(0); // 设置运行的总条数
				result.setResultMsg("请修改API请求参数type=2查询实时的检测结果！");
			}
		} else if (type.equals("2")) {

			String KhTestCount = redisClient.get(KhTestCountKey);

			if (!CommonUtils.isNotString(KhTestCount)) {
				String succeedTestCount = redisClient.get(succeedTestCountkey);
				succeedTestCount = !CommonUtils.isNotString(succeedTestCount) ? succeedTestCount : "0";
				runTestDomian.setRunCount(Integer.valueOf(succeedTestCount.toString())); // 设置运行的总条数
				runTestDomian.setMobiles(FileUtils.getFileMenu(fileUrl, Integer.parseInt(startLine), 100)); // 设置已经检测了的手机号码
				logger.info("----------需要检测的总条数: 【" + KhTestCount + "】，已经检测完成的条数:" + succeedTestCount);
				if (Integer.parseInt(KhTestCount) <= Integer.valueOf(succeedTestCount)) {
					result.setResultMsg("任务执行结束");
					runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
					this.clearLockAndCountForRun(lock, userId, mobile);
				} else {
					result.setResultMsg("任务执行中");
					runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				}
			} else {
				result.setResultMsg("该账户没有正在检测的程序进程");
				runTestDomian.setRunCount(0);
				runTestDomian.setStatus("6"); // 没有在执行的检测
			}
		}
		
		result.setResultObj(runTestDomian);
		return result;
	}

	@Override
	public BackResult<RunTestDomian> theTest(String fileUrl, String userId, String mobile, String source,
											 String startLine, String type) {
		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();
		DistributedLock lock = new DistributedLock(jedisPool);
		String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile);
		String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId);
		String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId);
		String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId);
		String succeedClearingCountkey = RedisKeys.getInstance().getkhSucceedClearingCountkey(userId);
		int expire = 2 * 60 * 60 * 1000;
		// 执行检测
		if (type.equals("1")) {
			// 加锁
			String identifier = lock.lockWithTimeout(lockName, 800L, expire);
			// 处理加锁业务
			if (null != identifier) {

				// 将标识存入redis
				redisClient.set(redisLockIdentifier, identifier, expire);

				// 创建一个线程
				Runnable run = new Runnable() {
					@Override
					public void run() {
						BufferedReader br = null;
						try {

							int testCount = 0; // 需要记账的总条数
							int count = 0; // 实际检测的总条数

							logger.info("----------用户编号：[" + userId + "]文件地址：[" + fileUrl + "]开始执行空号检索事件 事件开始时间："
									+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "post:" + port);

							List<List<Object>> thereDataList = new ArrayList<List<Object>>();
							List<Object> thereRowList = null;
							List<Map<String, Object>> sixDataList = new ArrayList<Map<String, Object>>();
							List<List<Object>> unKonwDataList = new ArrayList<List<Object>>();
							List<Object> unKonwRowList = null;

							String thereListkey = "";
							String sixListkey = "";
							String unkownListkey = "";

							Date sixStartTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -180);

							File file = new File(fileUrl);
							if (file.isFile() && file.exists()) {

								InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
								br = new BufferedReader(isr);
								String lineTxt = null;

								while ((lineTxt = br.readLine()) != null) {

									count = count + 1;
									redisClient.set(succeedTestCountkey, String.valueOf(count), expire);
									if (CommonUtils.isNotString(lineTxt)) {
										continue;
									}

									// 去掉字符串中的所有空格
									lineTxt = lineTxt.replace(" ", "");

									// 验证是否为正常的１１位有效数字
									if (!CommonUtils.isNumeric(lineTxt)) {
										continue;
									}

									// 检测 3个月内
									BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(lineTxt,
											sixStartTime, DateUtils.getCurrentDateTime());

									if (null != detail) {

										// 存在数据 (实号：real，空号：kong，沉默号：silence)
										String status = MobileDetailHelper.getInstance().getMobileStatus(lineTxt, detail.getDelivrd());
										if (status.equals("real")) {
											// 实号
											thereRowList = new ArrayList<Object>();
											thereRowList.add(lineTxt);
											thereDataList.add(thereRowList);
										} else if (status.equals("kong")){
											// 空号
											Map<String, Object> sixRowList = new HashMap<>();
											sixRowList.put("mobile", lineTxt);
											sixRowList.put("delivd", 1);// 空号状态
											sixRowList.put("reportTime", detail.getReportTime().getTime());
											sixDataList.add(sixRowList);
										} else if (status.equals("silence")){
											// 沉默号
											unKonwRowList = new ArrayList<Object>();
											unKonwRowList.add(lineTxt);
											unKonwDataList.add(unKonwRowList);
										}else {
											// 沉默号
											unKonwRowList = new ArrayList<Object>();
											unKonwRowList.add(lineTxt);
											unKonwDataList.add(unKonwRowList);
										}

									} else {

										// 二次清洗根据号段
										MobileNumberSection section = mobileNumberSectionService
												.findByNumberSection(lineTxt.substring(0, 7));

										if (null != section) {
											// 沉默号
											unKonwRowList = new ArrayList<Object>();
											unKonwRowList.add(lineTxt);
											unKonwDataList.add(unKonwRowList);
										} else {
											// 空号
											Map<String, Object> sixRowList = new HashMap<>();
											sixRowList.put("mobile", lineTxt);
											sixRowList.put("delivd", 1);// 空号状态
											sixRowList.put("reportTime", DateUtils.getCurrentDateTime().getTime());
											sixDataList.add(sixRowList);

										}

									}

									testCount = testCount + 1;
								}

							} else {
								// 系统异常
							}

							// 将需要结账的条数存入redis
							redisClient.set(succeedClearingCountkey, String.valueOf(testCount), expire * 2);

							// 文件地址入库
							CvsFilePath cvsFilePath = new CvsFilePath();
							cvsFilePath.setUserId(userId);

							// 生成报表
							String timeTemp = String.valueOf(System.currentTimeMillis());
							String filePath = loadfilePath + userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/";
							if (!CommonUtils.isNotEmpty(thereDataList)) {
								logger.info("----------实号总条数：" + thereDataList.size());
								Object[] shhead = { "手机号码" };
								FileUtils.createCvsFile("实号.csv", filePath, thereDataList, shhead);
								cvsFilePath.setThereCount(String.valueOf(thereDataList.size()));
							}

							if (!CommonUtils.isNotEmpty(sixDataList)) {
								logger.info("----------空号总条数：" + sixDataList.size());
								Object[] head = { "手机号码" };
								try {
									Collections.sort(sixDataList, new Comparator<Map<String, Object>>() {
										@Override
										public int compare(Map<String, Object> arg0, Map<String, Object> arg1) {
											try {
												Long reportTime0 = Long.parseLong(arg0.get("delivd").toString()
														+ arg0.get("reportTime").toString());
												Long reportTime1 = Long.parseLong(arg1.get("delivd").toString()
														+ arg1.get("reportTime").toString());
												return reportTime0.compareTo(reportTime1);
											} catch (NumberFormatException e) {
												return 0;
											}
										}
									});
								} catch (Exception e) {
								}
								FileUtils.createCvsFileByMap("空号.csv", filePath, sixDataList, head);
								cvsFilePath.setSixCount(String.valueOf(sixDataList.size()));
							}

							if (!CommonUtils.isNotEmpty(unKonwDataList)) {
								logger.info("----------沉默号总条数：" + unKonwDataList.size());
								Object[] wzhead = { "手机号码" };
								FileUtils.createCvsFile("沉默号.csv", filePath, unKonwDataList, wzhead);
								cvsFilePath.setUnknownSize(String.valueOf(unKonwDataList.size()));
							}

							List<File> list = new ArrayList<File>();

							if (!CommonUtils.isNotEmpty(thereDataList)) {
								list.add(new File(filePath + "实号.csv"));
								cvsFilePath.setThereFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/实号.csv");
								cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "实号.csv"));
							}

							if (!CommonUtils.isNotEmpty(sixDataList)) {
								list.add(new File(filePath + "空号.csv"));
								cvsFilePath.setSixFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/空号.csv");
								cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "空号.csv"));
							}

							if (!CommonUtils.isNotEmpty(unKonwDataList)) {
								list.add(new File(filePath + "沉默号.csv"));
								cvsFilePath.setUnknownFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/沉默号.csv");
								cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "沉默号.csv"));
							}

							String zipName = "测试结果包.zip";
							// 报表文件打包
							if (null != list && list.size() > 0) {
								zipName = "测试结果包.zip";
								FileUtils.createZip(list, filePath + zipName);
								cvsFilePath.setZipName(zipName);
								cvsFilePath.setZipPath(
										(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/测试结果包.zip"));
								cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
							}

							cvsFilePath.setCreateTime(new Date());

							if (CommonUtils.isNotString(cvsFilePath.getThereFilePath()) && CommonUtils.isNotString(cvsFilePath.getSixFilePath()) && CommonUtils.isNotString(cvsFilePath.getUnknownFilePath())){
								if ("pc1.0".equals(source)) {
									// 发送短信
									ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
								} else {
									// 异常发送短信
									ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestZZtEx(mobile);
								}
								this.clearLockAndCountForRun(lock, userId, mobile);
								// 封装返回对象
								result.setResultMsg("执行异常");
								runTestDomian.setRunCount(0);
								runTestDomian.setStatus("3"); // 1执行中 2执行结束 // 3执行异常
							} else {
								mongoTemplate.save(cvsFilePath);
							}

							// 记录流水记录
							WaterConsumption waterConsumption = new WaterConsumption();
							waterConsumption.setUserId(userId);
							waterConsumption.setId(UUIDTool.getInstance().getUUID());
							waterConsumption.setConsumptionNum("SHJC_" + System.currentTimeMillis());
							waterConsumption.setMenu("客户上传文件实号检测");
							waterConsumption.setStatus("1");
							waterConsumption.setType("1"); // 实号检测
							waterConsumption.setSource(source);
							waterConsumption.setCreateTime(new Date());
							waterConsumption.setCount(String.valueOf(testCount)); // 条数
							waterConsumption.setUpdateTime(new Date());
							mongoTemplate.save(waterConsumption);

							if ("pc1.0".equals(source)) {
								// 发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTest(mobile);
							} else {
								// 发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForZZTTest(mobile);
							}

							// 封装返回对象
							result.setResultMsg("成功");
							runTestDomian.setRunCount(count);
							runTestDomian.setStatus("2"); // 1执行中 2执行结束 // 3执行异常
							logger.info("----------用户编号：[" + userId + "]文件地址：[" + fileUrl + "]结束空号检索事件 事件结束时间："
									+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
						} catch (Exception e) {
							e.printStackTrace();
							logger.error("----------客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
							this.clearLockAndCountForRun(lock, userId, mobile);
							if ("pc1.0".equals(source)) {
								// 发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
							} else {
								// 异常发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestZZtEx(mobile);
							}
						} finally {
							if (null != br) {
								try {
									br.close();
								} catch (IOException e) {
									e.printStackTrace();
									this.clearLockAndCountForRun(lock, userId, mobile);
									logger.error("文件流关闭异常：" + e.getMessage());
								}
							}
						}

					}

					/**
					 * 清空条数注销锁
					 *
					 * @param lock
					 * @param userId
					 * @param mobile
					 */
					private void clearLockAndCountForRun(DistributedLock lock, String userId, String mobile) {
						String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile);
						String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId);
						String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId);
						String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId);
						String identifier = redisClient.get(redisLockIdentifier);
						// 清空 记录到redis的条数
						redisClient.remove(KhTestCountKey);
						redisClient.remove(succeedTestCountkey);
						lock.releaseLock(lockName, identifier); // 注销锁

					}
				};

				// 加入线程池开始执行
				threadExecutorService.execute(run);
				result.setResultMsg("任务执行中");
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				runTestDomian.setRunCount(0); // 设置运行的总条数
			} else {
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				runTestDomian.setRunCount(0); // 设置运行的总条数
				result.setResultMsg("请修改API请求参数type=2查询实时的检测结果！");
			}
		} else if (type.equals("2")) {

			String KhTestCount = redisClient.get(KhTestCountKey);

			if (!CommonUtils.isNotString(KhTestCount)) {
				String succeedTestCount = redisClient.get(succeedTestCountkey);
				succeedTestCount = !CommonUtils.isNotString(succeedTestCount) ? succeedTestCount : "0";
				runTestDomian.setRunCount(Integer.valueOf(succeedTestCount.toString())); // 设置运行的总条数
				runTestDomian.setMobiles(FileUtils.getFileMenu(fileUrl, Integer.parseInt(startLine), 100)); // 设置已经检测了的手机号码
				logger.info("----------需要检测的总条数: 【" + KhTestCount + "】，已经检测完成的条数:" + succeedTestCount);
				if (Integer.parseInt(KhTestCount) <= Integer.valueOf(succeedTestCount)) {
					result.setResultMsg("任务执行结束");
					runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
					this.clearLockAndCountForRun(lock, userId, mobile);
				} else {
					result.setResultMsg("任务执行中");
					runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				}
			} else {
				result.setResultMsg("该账户没有正在检测的程序进程");
				runTestDomian.setRunCount(0);
				runTestDomian.setStatus("6"); // 没有在执行的检测
			}
		}

		result.setResultObj(runTestDomian);
		return result;
	}

	@Deprecated
	public BackResult<RunTestDomian> theTest1(String fileUrl, String userId, String mobile, String source,
			String startLine, String type) {
		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();
		DistributedLock lock = new DistributedLock(jedisPool);
		String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile);
		String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId);
		String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId);
		String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId);
		String succeedClearingCountkey = RedisKeys.getInstance().getkhSucceedClearingCountkey(userId);
		int expire = 2 * 60 * 60 * 1000;
		// 执行检测
		if (type.equals("1")) {
			// 加锁
			String identifier = lock.lockWithTimeout(lockName, 800L, expire);
			// 处理加锁业务
			if (null != identifier) {

				// 将标识存入redis
				redisClient.set(redisLockIdentifier, identifier, expire);

				// 创建一个线程
				Runnable run = new Runnable() {
					@Override
					public void run() {
						BufferedReader br = null;
						try {

							int testCount = 0; // 需要记账的总条数
							int count = 0; // 实际检测的总条数

							logger.info("----------用户编号：[" + userId + "]文件地址：[" + fileUrl + "]开始执行空号检索事件 事件开始时间："
									+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "post:" + port);

							List<List<Object>> thereDataList = new ArrayList<List<Object>>();
							List<Object> thereRowList = null;
							List<Map<String, Object>> sixDataList = new ArrayList<Map<String, Object>>();
							List<List<Object>> unKonwDataList = new ArrayList<List<Object>>();
							List<Object> unKonwRowList = null;

							Date sixStartTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -180);

							File file = new File(fileUrl);
							if (file.isFile() && file.exists()) {

								InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
								br = new BufferedReader(isr);
								String lineTxt = null;

								while ((lineTxt = br.readLine()) != null) {

									count = count + 1;
									redisClient.set(succeedTestCountkey, String.valueOf(count), expire);
									if (CommonUtils.isNotString(lineTxt)) {
										continue;
									}

									// 去掉字符串中的所有空格
									lineTxt = lineTxt.replace(" ", "");

									// 验证是否为正常的１１位有效数字
									if (!CommonUtils.isNumeric(lineTxt)) {
										continue;
									}

									// 检测 3个月内
									BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(lineTxt,
											sixStartTime, DateUtils.getCurrentDateTime());

									if (null != detail) {

										// 存在数据 (实号：real，空号：kong，沉默号：silence)
										String status = MobileDetailHelper.getInstance().getMobileStatus(lineTxt, detail.getDelivrd());
										if (status.equals("real")) {
											// 实号
											thereRowList = new ArrayList<Object>();
											thereRowList.add(lineTxt);
											thereDataList.add(thereRowList);
										} else if (status.equals("kong")){
											// 空号
											Map<String, Object> sixRowList = new HashMap<>();
											sixRowList.put("mobile", lineTxt);
											sixRowList.put("delivd", 1);// 空号状态
											sixRowList.put("reportTime", detail.getReportTime().getTime());
											sixDataList.add(sixRowList);
										} else if (status.equals("silence")){
											// 沉默号
											unKonwRowList = new ArrayList<Object>();
											unKonwRowList.add(lineTxt);
											unKonwDataList.add(unKonwRowList);
										}else {
											// 沉默号
											unKonwRowList = new ArrayList<Object>();
											unKonwRowList.add(lineTxt);
											unKonwDataList.add(unKonwRowList);
										}
										
									} else {

										// 二次清洗根据号段
										MobileNumberSection section = mobileNumberSectionService
												.findByNumberSection(lineTxt.substring(0, 7));

										if (null != section) {
											// 沉默号
											unKonwRowList = new ArrayList<Object>();
											unKonwRowList.add(lineTxt);
											unKonwDataList.add(unKonwRowList);
										} else {
											// 空号
											Map<String, Object> sixRowList = new HashMap<>();
											sixRowList.put("mobile", lineTxt);
											sixRowList.put("delivd", 1);// 空号状态
											sixRowList.put("reportTime", DateUtils.getCurrentDateTime().getTime());
											sixDataList.add(sixRowList);

										}

									}

									testCount = testCount + 1;
								}

							}

							// 将需要结账的条数存入redis
							redisClient.set(succeedClearingCountkey, String.valueOf(testCount), expire * 2);

							// 文件地址入库
							CvsFilePath cvsFilePath = new CvsFilePath();
							cvsFilePath.setUserId(userId);

							// 生成报表
							String timeTemp = String.valueOf(System.currentTimeMillis());
							String filePath = loadfilePath + userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/";
							if (!CommonUtils.isNotEmpty(thereDataList)) {
								logger.info("----------实号总条数：" + thereDataList.size());
								Object[] shhead = { "手机号码" };
								FileUtils.createCvsFile("实号.csv", filePath, thereDataList, shhead);
								cvsFilePath.setThereCount(String.valueOf(thereDataList.size()));
							}

							if (!CommonUtils.isNotEmpty(sixDataList)) {
								logger.info("----------空号总条数：" + sixDataList.size());
								Object[] head = { "手机号码" };
								try {
									Collections.sort(sixDataList, new Comparator<Map<String, Object>>() {
										@Override
										public int compare(Map<String, Object> arg0, Map<String, Object> arg1) {
											try {
												Long reportTime0 = Long.parseLong(arg0.get("delivd").toString()
														+ arg0.get("reportTime").toString());
												Long reportTime1 = Long.parseLong(arg1.get("delivd").toString()
														+ arg1.get("reportTime").toString());
												return reportTime0.compareTo(reportTime1);
											} catch (NumberFormatException e) {
												return 0;
											}
										}
									});
								} catch (Exception e) {
								}
								FileUtils.createCvsFileByMap("空号.csv", filePath, sixDataList, head);
								cvsFilePath.setSixCount(String.valueOf(sixDataList.size()));
							}

							if (!CommonUtils.isNotEmpty(unKonwDataList)) {
								logger.info("----------沉默号总条数：" + unKonwDataList.size());
								Object[] wzhead = { "手机号码" };
								FileUtils.createCvsFile("沉默号.csv", filePath, unKonwDataList, wzhead);
								cvsFilePath.setUnknownSize(String.valueOf(unKonwDataList.size()));
							}

							List<File> list = new ArrayList<File>();

							if (!CommonUtils.isNotEmpty(thereDataList)) {
								list.add(new File(filePath + "实号.csv"));
								cvsFilePath.setThereFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/实号.csv");
								cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "实号.csv"));
							}

							if (!CommonUtils.isNotEmpty(sixDataList)) {
								list.add(new File(filePath + "空号.csv"));
								cvsFilePath.setSixFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/空号.csv");
								cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "空号.csv"));
							}

							if (!CommonUtils.isNotEmpty(unKonwDataList)) {
								list.add(new File(filePath + "沉默号.csv"));
								cvsFilePath.setUnknownFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/沉默号.csv");
								cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "沉默号.csv"));
							}

							String zipName = "测试结果包.zip";
							// 报表文件打包
							if (null != list && list.size() > 0) {
								zipName = "测试结果包.zip";
								FileUtils.createZip(list, filePath + zipName);
								cvsFilePath.setZipName(zipName);
								cvsFilePath.setZipPath(
										(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/测试结果包.zip"));
								cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
							}

							cvsFilePath.setCreateTime(new Date());
							
							if (CommonUtils.isNotString(cvsFilePath.getThereFilePath()) && CommonUtils.isNotString(cvsFilePath.getSixFilePath()) && CommonUtils.isNotString(cvsFilePath.getUnknownFilePath())){
								if ("pc1.0".equals(source)) {
									// 发送短信
									ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
								} else {
									// 异常发送短信
									ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestZZtEx(mobile);
								}
								this.clearLockAndCountForRun(lock, userId, mobile);
								// 封装返回对象
								result.setResultMsg("执行异常");
								runTestDomian.setRunCount(0);
								runTestDomian.setStatus("3"); // 1执行中 2执行结束 // 3执行异常
							} else {
								mongoTemplate.save(cvsFilePath);
							}
								
							// 记录流水记录
							WaterConsumption waterConsumption = new WaterConsumption();
							waterConsumption.setUserId(userId);
							waterConsumption.setId(UUIDTool.getInstance().getUUID());
							waterConsumption.setConsumptionNum("SHJC_" + System.currentTimeMillis());
							waterConsumption.setMenu("客户上传文件实号检测");
							waterConsumption.setStatus("1");
							waterConsumption.setType("1"); // 实号检测
							waterConsumption.setSource(source);
							waterConsumption.setCreateTime(new Date());
							waterConsumption.setCount(String.valueOf(testCount)); // 条数
							waterConsumption.setUpdateTime(new Date());
							mongoTemplate.save(waterConsumption);

							if ("pc1.0".equals(source)) {
								// 发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTest(mobile);
							} else {
								// 发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForZZTTest(mobile);
							}

							// 封装返回对象
							result.setResultMsg("成功");
							runTestDomian.setRunCount(count);
							runTestDomian.setStatus("2"); // 1执行中 2执行结束 // 3执行异常
							logger.info("----------用户编号：[" + userId + "]文件地址：[" + fileUrl + "]结束空号检索事件 事件结束时间："
									+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
						} catch (Exception e) {
							e.printStackTrace();
							logger.error("----------客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
							this.clearLockAndCountForRun(lock, userId, mobile);
							if ("pc1.0".equals(source)) {
								// 发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
							} else {
								// 异常发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestZZtEx(mobile);
							}
						} finally {
							if (null != br) {
								try {
									br.close();
								} catch (IOException e) {
									e.printStackTrace();
									this.clearLockAndCountForRun(lock, userId, mobile);
									logger.error("文件流关闭异常：" + e.getMessage());
								}
							}
						}

					}

					/**
					 * 清空条数注销锁
					 * 
					 * @param lock
					 * @param userId
					 * @param mobile
					 */
					private void clearLockAndCountForRun(DistributedLock lock, String userId, String mobile) {
						String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile);
						String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId);
						String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId);
						String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId);
						String identifier = redisClient.get(redisLockIdentifier);
						// 清空 记录到redis的条数
						redisClient.remove(KhTestCountKey);
						redisClient.remove(succeedTestCountkey);
						lock.releaseLock(lockName, identifier); // 注销锁

					}
				};

				// 加入线程池开始执行
				threadExecutorService.execute(run);
				result.setResultMsg("任务执行中");
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				runTestDomian.setRunCount(0); // 设置运行的总条数
			} else {
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				runTestDomian.setRunCount(0); // 设置运行的总条数
				result.setResultMsg("请修改API请求参数type=2查询实时的检测结果！");
			}
		} else if (type.equals("2")) {

			String KhTestCount = redisClient.get(KhTestCountKey);

			if (!CommonUtils.isNotString(KhTestCount)) {
				String succeedTestCount = redisClient.get(succeedTestCountkey);
				succeedTestCount = !CommonUtils.isNotString(succeedTestCount) ? succeedTestCount : "0";
				runTestDomian.setRunCount(Integer.valueOf(succeedTestCount.toString())); // 设置运行的总条数
				runTestDomian.setMobiles(FileUtils.getFileMenu(fileUrl, Integer.parseInt(startLine), 100)); // 设置已经检测了的手机号码
				logger.info("----------需要检测的总条数: 【" + KhTestCount + "】，已经检测完成的条数:" + succeedTestCount);
				if (Integer.parseInt(KhTestCount) <= Integer.valueOf(succeedTestCount)) {
					result.setResultMsg("任务执行结束");
					runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
					this.clearLockAndCountForRun(lock, userId, mobile);
				} else {
					result.setResultMsg("任务执行中");
					runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				}
			} else {
				result.setResultMsg("该账户没有正在检测的程序进程");
				runTestDomian.setRunCount(0);
				runTestDomian.setStatus("6"); // 没有在执行的检测
			}
		}
		
		result.setResultObj(runTestDomian);
		return result;
	}
	
	@Override
	public BackResult<RunTestDomian> theTest2(String fileUrl, String userId, String mobile, String source,
			String startLine, String type) {
		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();
		DistributedLock lock = new DistributedLock(jedisPool);
		String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile);
		String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId);
		String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId);
		String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId);
		String succeedClearingCountkey = RedisKeys.getInstance().getkhSucceedClearingCountkey(userId);
		int expire = 2 * 60 * 60 * 1000;
		// 执行检测
		if (type.equals("1")) {
			// 加锁
			String identifier = lock.lockWithTimeout(lockName, 800L, expire);
			// 处理加锁业务
			if (null != identifier) {

				// 将标识存入redis
				redisClient.set(redisLockIdentifier, identifier, expire);

				// 创建一个线程
				Runnable run = new Runnable() {
					@Override
					public void run() {
						BufferedReader br = null;
						try {

							int testCount = 0; // 需要记账的总条数
							int count = 0; // 实际检测的总条数

							logger.info("----------用户编号：[" + userId + "]文件地址：[" + fileUrl + "]开始执行空号检索事件 事件开始时间："
									+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "post:" + port);

							List<List<Object>> thereDataList = new ArrayList<List<Object>>();
							List<Object> thereRowList = null;
							List<Map<String, Object>> sixDataList = new ArrayList<Map<String, Object>>();
							List<List<Object>> unKonwDataList = new ArrayList<List<Object>>();
							List<Object> unKonwRowList = null;

							Date sixStartTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -210);

							File file = new File(fileUrl);
							if (file.isFile() && file.exists()) {

								InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
								br = new BufferedReader(isr);
								String lineTxt = null;

								while ((lineTxt = br.readLine()) != null) {

									count = count + 1;
									redisClient.set(succeedTestCountkey, String.valueOf(count), expire);
									if (CommonUtils.isNotString(lineTxt)) {
										continue;
									}

									// 去掉字符串中的所有空格
									lineTxt = lineTxt.replace(" ", "");

									// 验证是否为正常的１１位有效数字
									if (!CommonUtils.isNumeric(lineTxt)) {
										continue;
									}

									// 检测 3个月内
									BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(lineTxt,
											sixStartTime, DateUtils.getCurrentDateTime());

									if (null != detail) {

										// 存在数据
										if ("real".equals(isSpaceMobile(detail.getDelivrd()))) {
											// 实号
											thereRowList = new ArrayList<Object>();
											thereRowList.add(detail.getMobile());
											thereRowList.add(detail.getDelivrd());
											thereRowList.add(DateUtils.formatDate(detail.getReportTime(), "yyyy-MM-dd HH:mm:ss"));
											thereDataList.add(thereRowList);
										} else if ("pause".equals(isSpaceMobile(detail.getDelivrd()))) {
											// 停机
											Map<String, Object> sixRowList = new HashMap<>();
											sixRowList.put("mobile", detail.getMobile());
											sixRowList.put("delivd", 2);// 停机状态
											sixRowList.put("reportTime", detail.getReportTime().getTime());
											sixDataList.add(sixRowList);
										} else if ("kong".equals(isSpaceMobile(detail.getDelivrd()))) {
											// 空号
											Map<String, Object> sixRowList = new HashMap<>();
											sixRowList.put("mobile", detail.getMobile());
											sixRowList.put("delivd", 1);// 空号状态
											sixRowList.put("reportTime", detail.getReportTime().getTime());
											sixDataList.add(sixRowList);
										} else {
											// 未知
											unKonwRowList = new ArrayList<Object>();
											unKonwRowList.add(lineTxt);
											unKonwRowList.add(detail.getDelivrd());
											thereRowList.add(DateUtils.formatDate(detail.getReportTime(), "yyyy-MM-dd HH:mm:ss"));
											unKonwDataList.add(unKonwRowList);
										}

									} else {

										// 二次清洗根据号段
										MobileNumberSection section = mobileNumberSectionService
												.findByNumberSection(lineTxt.substring(0, 7));

										if (null != section) {

											unKonwRowList = new ArrayList<Object>();
											unKonwRowList.add(lineTxt);
											unKonwRowList.add("没有");
											unKonwRowList.add("没有");
											unKonwDataList.add(unKonwRowList);
										} else {
											// 放空号
											Map<String, Object> sixRowList = new HashMap<>();
											sixRowList.put("mobile", lineTxt);
											sixRowList.put("delivd", 1);// 空号状态
											sixRowList.put("reportTime", DateUtils
													.converYYYYMMddHHmmssStrToDate("1900-01-01 00:00:00").getTime());
											sixDataList.add(sixRowList);

										}

									}

									testCount = testCount + 1;
								}

							}

							// 将需要结账的条数存入redis
							redisClient.set(succeedClearingCountkey, String.valueOf(testCount), expire * 2);

							// 文件地址入库
							CvsFilePath cvsFilePath = new CvsFilePath();
							cvsFilePath.setUserId(userId);

							// 生成报表
							String timeTemp = String.valueOf(System.currentTimeMillis());
							String filePath = loadfilePath + userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/";
							if (!CommonUtils.isNotEmpty(thereDataList)) {
								logger.info("----------实号总条数：" + thereDataList.size());
								Object[] shhead = { "手机号码","状态","时间" };
								FileUtils.createCvsFile("实号.csv", filePath, thereDataList, shhead);
								cvsFilePath.setThereCount(String.valueOf(thereDataList.size()));
							}

							if (!CommonUtils.isNotEmpty(sixDataList)) {
								logger.info("----------空号总条数：" + sixDataList.size());
								Object[] head = { "手机号码","状态","时间" };
								try {
									Collections.sort(sixDataList, new Comparator<Map<String, Object>>() {
										@Override
										public int compare(Map<String, Object> arg0, Map<String, Object> arg1) {
											try {
												Long reportTime0 = Long.parseLong(arg0.get("delivd").toString()
														+ arg0.get("reportTime").toString());
												Long reportTime1 = Long.parseLong(arg1.get("delivd").toString()
														+ arg1.get("reportTime").toString());
												return reportTime0.compareTo(reportTime1);
											} catch (NumberFormatException e) {
												return 0;
											}
										}
									});
								} catch (Exception e) {
								}
								FileUtils.createCvsFileByMap2("空号.csv", filePath, sixDataList, head);
								cvsFilePath.setSixCount(String.valueOf(sixDataList.size()));
							}

							if (!CommonUtils.isNotEmpty(unKonwDataList)) {
								logger.info("----------沉默号总条数：" + unKonwDataList.size());
								Object[] wzhead = { "手机号码","状态","时间" };
								FileUtils.createCvsFile("沉默号.csv", filePath, unKonwDataList, wzhead);
								cvsFilePath.setUnknownSize(String.valueOf(unKonwDataList.size()));
							}

							List<File> list = new ArrayList<File>();

							if (!CommonUtils.isNotEmpty(thereDataList)) {
								list.add(new File(filePath + "实号.csv"));
								cvsFilePath.setThereFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/实号.csv");
								cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "实号.csv"));
							}

							if (!CommonUtils.isNotEmpty(sixDataList)) {
								list.add(new File(filePath + "空号.csv"));
								cvsFilePath.setSixFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/空号.csv");
								cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "空号.csv"));
							}

							if (!CommonUtils.isNotEmpty(unKonwDataList)) {
								list.add(new File(filePath + "沉默号.csv"));
								cvsFilePath.setUnknownFilePath(
										userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/沉默号.csv");
								cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "沉默号.csv"));
							}

							String zipName = "测试结果包.zip";
							// 报表文件打包
							if (null != list && list.size() > 0) {
								zipName = "测试结果包.zip";
								FileUtils.createZip(list, filePath + zipName);
								cvsFilePath.setZipName(zipName);
								cvsFilePath.setZipPath(
										(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/测试结果包.zip"));
								cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
							}

							cvsFilePath.setCreateTime(new Date());
//							mongoTemplate.save(cvsFilePath);

//							// 记录流水记录
//							WaterConsumption waterConsumption = new WaterConsumption();
//							waterConsumption.setUserId(userId);
//							waterConsumption.setId(UUIDTool.getInstance().getUUID());
//							waterConsumption.setConsumptionNum("SHJC_" + System.currentTimeMillis());
//							waterConsumption.setMenu("客户上传文件实号检测");
//							waterConsumption.setStatus("1");
//							waterConsumption.setType("1"); // 实号检测
//							waterConsumption.setSource(source);
//							waterConsumption.setCreateTime(new Date());
//							waterConsumption.setCount(String.valueOf(testCount)); // 条数
//							waterConsumption.setUpdateTime(new Date());
//							mongoTemplate.save(waterConsumption);

//							if ("pc1.0".equals(source)) {
//								// 发送短信
//								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTest(mobile);
//							} else {
//								// 发送短信
//								ChuangLanSmsUtil.getInstance().sendSmsByMobileForZZTTest(mobile);
//							}

							// 封装返回对象
							result.setResultMsg("成功");
							runTestDomian.setRunCount(count);
							runTestDomian.setStatus("2"); // 1执行中 2执行结束 // 3执行异常
							logger.info("----------用户编号：[" + userId + "]文件地址：[" + fileUrl + "]结束空号检索事件 事件结束时间："
									+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
						} catch (Exception e) {
							e.printStackTrace();
							logger.error("----------客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
//							this.clearLockAndCountForRun(lock, userId, mobile);
//							// 异常发送短信
//							ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestZZtEx(mobile);
						} finally {
							if (null != br) {
								try {
									br.close();
								} catch (IOException e) {
									e.printStackTrace();
									this.clearLockAndCountForRun(lock, userId, mobile);
									logger.error("文件流关闭异常：" + e.getMessage());
								}
							}
						}

					}

					/**
					 * 清空条数注销锁
					 * 
					 * @param lock
					 * @param userId
					 * @param mobile
					 */
					private void clearLockAndCountForRun(DistributedLock lock, String userId, String mobile) {
						String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile);
						String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId);
						String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId);
						String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId);
						String identifier = redisClient.get(redisLockIdentifier);
						// 清空 记录到redis的条数
						redisClient.remove(KhTestCountKey);
						redisClient.remove(succeedTestCountkey);
						lock.releaseLock(lockName, identifier); // 注销锁

					}
				};

				// 加入线程池开始执行
				threadExecutorService.execute(run);
				result.setResultMsg("任务执行中");
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				runTestDomian.setRunCount(0); // 设置运行的总条数
			} else {
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				runTestDomian.setRunCount(0); // 设置运行的总条数
				result.setResultMsg("请修改API请求参数type=2查询实时的检测结果！");
			}
		} else if (type.equals("2")) {

			String KhTestCount = redisClient.get(KhTestCountKey);

			if (!CommonUtils.isNotString(KhTestCount)) {
				String succeedTestCount = redisClient.get(succeedTestCountkey);
				succeedTestCount = !CommonUtils.isNotString(succeedTestCount) ? succeedTestCount : "0";
				runTestDomian.setRunCount(Integer.valueOf(succeedTestCount.toString())); // 设置运行的总条数
				runTestDomian.setMobiles(FileUtils.getFileMenu(fileUrl, Integer.parseInt(startLine), 100)); // 设置已经检测了的手机号码
				logger.info("----------需要检测的总条数: 【" + KhTestCount + "】，已经检测完成的条数:" + succeedTestCount);
				if (Integer.parseInt(KhTestCount) <= Integer.valueOf(succeedTestCount)) {
					result.setResultMsg("任务执行结束");
					runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
					this.clearLockAndCountForRun(lock, userId, mobile);
				} else {
					result.setResultMsg("任务执行中");
					runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				}
			} else {
				result.setResultMsg("该账户没有正在检测的程序进程");
				runTestDomian.setRunCount(0);
				runTestDomian.setStatus("6"); // 没有在执行的检测
			}
		}
		
		result.setResultObj(runTestDomian);
		return result;
	}
	
	
	
	/**
	 * 清空条数注销锁
	 * 
	 * @param lock
	 * @param userId
	 * @param mobile
	 */
	private void clearLockAndCountForRun(DistributedLock lock, String userId, String mobile) {
		String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile);
		String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId);
		String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId);
		String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId);
		String identifier = redisClient.get(redisLockIdentifier);
		// 清空 记录到redis的条数
		redisClient.remove(KhTestCountKey);
		redisClient.remove(succeedTestCountkey);
		lock.releaseLock(lockName, identifier); // 注销锁

	}

	/**
	 * 返回状态
	 * 
	 * @param delivrd
	 * @return
	 */
	public String isSpaceMobile(String delivrd) {
		String realdelivrd = "-1012,-99,004,010,011,015,017,020,022,029,054,055,151,174,188,602,612,613,614,615,618,619,620,625,627,634,636,650,706,711,713,714,726,760,762,812,814,815,827,870,899,901,999,BLACK,BLKFAIL,BwList,CB:0255,CJ:0005,CJ:0006,CL:105,CL:106,CL:116,CL:125,DB:0008,DB:0119,DB:0140,DB:0141,DB:0142,DB:0144,DB:0160,DB:0309,DB:0318,DB00141,DELIVRD,DISTURB,E:401,E:BLACK,E:ODDL,E:ODSL,E:RPTSS,EM:101,GG:0024,HD:0001,HD:19,HD:31,HD:32,IA:0051,IA:0054,IA:0059,IA:0073,IB:0008,IB:0194,IC:0001,IC:0015,IC:0055,ID:0004,ID:0070,JL:0025,JL:0026,JL:0031,JT:105,KEYWORD,LIMIT,LT:0005,MA:0022,MA:0051,MA:0054,MB:0008,MB:1026,MB:1042,MB:1077,MB:1279,MBBLACK,MC:0055,MC:0151,MH:17,MI:0008,MI:0009,MI:0015,MI:0017,MI:0020,MI:0022,MI:0024,MI:0041,MI:0043,MI:0044,MI:0045,MI:0048,MI:0051,MI:0053,MI:0054,MI:0057,MI:0059,MI:0064,MI:0080,MI:0081,MI:0098,MI:0099,MI:0999,MK:0002,MK:0003,MK:0006,MK:0008,MK:0009,MK:0010,MK:0015,MK:0017,MK:0019,MK:0020,MK:0022,MK:0023,MK:0024,MK:0041,MK:0043,MK:0044,MK:0045,MK:0053,MK:0055";
		realdelivrd += "MK:0057,MK:0098,MK:0099,MN:0000,MN:0009,MN:0011,MN:0012,MN:0019,MN:0020,MN:0022,MN:0029,MN:0041,MN:0043,MN:0044,MN:0045,MN:0050,MN:0053,MN:0055,MN:0098,MN:0174,MT:101,NOPASS,NOROUTE,REFUSED,REJECT,REJECTD,REJECTE,RP:103,RP:106,RP:108,RP:11,RP:115,RP:117,RP:15,RP:17,RP:18,RP:19,RP:2,RP:20,RP:213,RP:22,RP:239,RP:254,RP:255,RP:27,RP:29,RP:36,RP:44,RP:45,RP:48,RP:50,RP:52,RP:55,RP:57,RP:59,RP:61,RP:67,RP:70,RP:77,RP:79,RP:8,RP:86,RP:90,RP:92,RP:98,SGIP:-1,SGIP:10,SGIP:106,SGIP:11,SGIP:117,SGIP:118,SGIP:121,SGIP:14,SGIP:15,SGIP:16,SGIP:17,SGIP:19,SGIP:2,SGIP:20,SGIP:22,SGIP:23,SGIP:-25,SGIP:27,SGIP:-3,SGIP:31,SGIP:43,SGIP:44,SGIP:45,SGIP:48,SGIP:57,SGIP:61,SGIP:64,SGIP:67,SGIP:79,SGIP:86,SGIP:89,SGIP:90,SGIP:92,SGIP:93,SGIP:98,SGIP:99,SME1,SME-1,SME19,SME20,SME210,SME-22,SME-26,SME28,SME3,SME6,SME-70,SME-74,SME8,SME92,SME-93,SYS:005,SYS:008,TIMEOUT,UNKNOWN,VALVE:M,W-BLACK,YX:1006,YX:7000,YX:8019,YX:9006";
		realdelivrd += "YY:0206,-181,023,036,043,044,706,712,718,721,730,763,779,879,CB:0013,CL:104,GATEBLA,IB:0011,ID:0199,JL:0028,LT:0022,MI:0021,MK:0068,RP:16,RP:65,RP:88,SGIP:-13,SGIP:63,SGIP:70,622,660,MI:0006,MK:0051,RP:121";
		String pausedelivrd = "000,001,005,008,084,617,702,716,801,809,802,817,869,731,EXPIRED,IC:0151,LT:0010,LT:0011,LT:0024,LT:0059,LT:0093,LT:0-37,MC:0001,MI:0000,MI:0001,MI:0002,MI:0004,MI:0005,MI:0010,MI:0011,MI:0012,MI:0013,MI:0023,MI:0029,MI:0030,MI:0036,MI:0038,MI:0050,MI:0055,MI:0056,MI:0063,MI:0068,MI:0083,MI:0084,MI:0089,MK:0011,MK:0013,MK:0029,MK:0036,MN:0013,MN:0017,MN:0036,MN:0051,MN:0054,MN:0059,RP:10,RP:104,RP:105,RP:118,RP:124,RP:13,RP:14,RP:182,RP:219,RP:231,RP:24,RP:253,RP:31,RP:4,RP:5,RP:51,RP:53,RP:54,RP:64,RP:75,RP:9,RP:93,SGIP:13,SGIP:-17,SGIP:18,SGIP:-2,SGIP:24,SGIP:29,SGIP:-37,SGIP:4,SGIP:-43,SGIP:5,SGIP:50,SGIP:51,SGIP:52,SGIP:53,SGIP:55,SGIP:58,SGIP:59,SGIP:-74,SGIP:77,SGIP:8,041,059,642,680,813,IB:0072,ID:0013,JL:0028,MI:0078,MK:0050,MK:0115,MK:0150,RP:175,RP:32,SGIP:6,SGIP:63,051,081,112,605,RP:121,SGIP:84,608,705";
		String nulldelivrd = "006,012,013,024,601,640,701,717,765,771,CB:0010,Err_Num,ID:0012,LT:0001,LT:0012,MI:0075,MI:0090,MK:0000,MK:0001,MK:0004,MK:0005,MK:0012,MK:0038,MK:0066,MK:0075,MK:0090,MN:0001,MN:0075,PHONERR,RP:1,RP:101,RP:102,RP:12,RP:23,RP:3,RP:56,RP:99,SGIP:1,SGIP:12,SGIP:3,SGIP:36,SGIP:54,SGIP:56,SGIP:75,SGIP:9,SME169,UTE,ERRNUM,IB:0169,LT:0009,LT:0086,LT:0-43,LT:0-74,MI:0210,RP:135,RP:63,CJ:0007,CJ:0008,UNDELIV";
		
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
		
		String[] nulldelivrds = nulldelivrd.split(",");
		
		for (String string : nulldelivrds) {
			if (string.equals(delivrd)) {
				return "kong";
			}
		}
		
		return "unkown";
	}
	
	/**
	 * 返回状态
	 * 
	 * @param delivrd
	 * @return
	 */
	public String isSpaceMobile2(String delivrd) {
		// 实号
		String realnum = "-1,-2,-43,0002,0003,0004,0006,0099,106,117,17,20,22,24,2:106,2:113,2:118,2:17,2:19,2:20,2:22,2:231,2:255,2:3,2:31,2:36,2:38,2:40,2:44,2:45,2:48,2:52,2:53,2:57,2:61,2:64,2:67,2:75,2:77,2:79,2:8,2:86,2:88,2:89,2:9,2:90,2:92,2:98,48,52,53,57,67,8,86,88,89,90,92,93,99,BEYONDN,CANCEL,CJ:0005,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:404,CL35RESP:11006,CL:105,CL:106,CL:116,CL:125,DELIVRD,DISTURB,GG:0024,HD:0001,HD:19,LT:00-1,LT:0003,LT:0009,LT:0017,LT:0020,LT:0022,LT:0040,LT:0048,LT:0054,LT:0057,LT:0059,LT:0061,LT:0064,LT:0067,LT:0079,LT:0086,LT:0089,LT:0090,LT:0092,LT:0098,LT:0117,MA:0006,MA:0027,MBBLACK,MD:9402,MD:9419,MG:0015,MG:0097,MO:0254,NOPASS,NOROUTE,REJECT,SGIP:-1,SGIP:-3,SGIP:-43,SGIP:0,SGIP:106,SGIP:108,SGIP:113,SGIP:118,SGIP:121,SGIP:16,SGIP:17,SGIP:18,SGIP:19,SGIP:20,SGIP:22,SGIP:24,SGIP:31,SGIP:36,SGIP:4,SGIP:40,SGIP:43,SGIP:44,SGIP:45,SGIP:48,SGIP:52,SGIP:53,SGIP:54,SGIP:57,SGIP:61,SGIP:63,SGIP:64,SGIP:67,SGIP:70,SGIP:77,SGIP:79,SGIP:8,SGIP:86,SGIP:88,SGIP:89,SGIP:9,SGIP:90,SGIP:92,SGIP:97,SGIP:98,SME102,SME103,SME29,SME50,SME77,SME8,SME88,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,YY:0206,failure,-1,-43,0002,0004,0099,106,16,17,20,22,27,2:106,2:113,2:118,2:16,2:17,2:20,2:22,2:23,2:24,2:253,2:254,2:255,2:3,2:36,2:40,2:45,2:48,2:55,2:57,2:59,2:61,2:64,2:67,2:70,2:77,2:79,2:8,2:86,2:88,2:89,2:9,2:90,2:92,2:98,40,53,57,6,67,77,79,8,86,89,90,92,98,99,BEYONDN,CANCEL,CJ:0005,CJ:0007,CJ:0008,CL30:480,CL35RESP:00001,CL35RESP:11006,CL:105,CL:106,CL:116,CL:125,CMPP20ERR:8,DELIVRD,DISTURB,GG:0024,HD:0001,HD:19,LT:0003,LT:0015,LT:0017,LT:0020,LT:0022,LT:0023,LT:0040,LT:0044,LT:0048,LT:0055,LT:0057,LT:0061,LT:0064,LT:0086,LT:0089,LT:0090,LT:0092,LT:0093,LT:0098,LT:0117,MA:0006,MA:0027,MBBLACK,MD:9402,MD:9403,MD:9419,MG:0015,MG:0097,MO:0254,NOROUTE,REJECT,SGIP:-1,SGIP:-11,SGIP:-15,SGIP:-25,SGIP:-3,SGIP:-43,SGIP:0,SGIP:113,SGIP:118,SGIP:121,SGIP:16,SGIP:17,SGIP:19,SGIP:20,SGIP:22,SGIP:24,SGIP:27,SGIP:32,SGIP:34,SGIP:36,SGIP:40,SGIP:43,SGIP:44,SGIP:45,SGIP:48,SGIP:50,SGIP:51,SGIP:52,SGIP:53,SGIP:57,SGIP:61,SGIP:63,SGIP:64,SGIP:70,SGIP:77,SGIP:79,SGIP:8,SGIP:83,SGIP:86,SGIP:88,SGIP:89,SGIP:90,SGIP:92,SGIP:97,SGIP:98,SGIP:99,SME102,SME103,SME15,SME29,SME50,SME69,SME77,SME8,SME88,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,YY:0206,failure,-1,-3,-37,-43,0002,0003,0004,0006,0007,0099,106,17,20,22,27,2:106,2:113,2:117,2:18,2:181,2:20,2:22,2:23,2:24,2:255,2:3,2:31,2:36,2:40,2:45,2:48,2:53,2:57,2:67,2:70,2:77,2:8,2:86,2:88,2:89,2:9,2:90,2:92,2:98,2:99,31,36,45,57,67,8,86,89,90,92,98,99,BEYONDN,CANCEL,CJ:0005,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35RESP:11006,CL:105,CL:116,CL:125,DELIVRD,DISTURB,ERRNUM,GG:0024,HD:0001,HD:19,LT:00-1,LT:0002,LT:0015,LT:0017,LT:0020,LT:0022,LT:0044,LT:0057,LT:0061,LT:0064,LT:0079,LT:0090,LT:0092,LT:0093,LT:0098,MA:0006,MA:0027,MBBLACK,MD:9402,MD:9403,MD:9419,MG:0015,MG:0097,MO:0254,NOPASS,NOROUTE,REJECT,SGIP:-1,SGIP:-2,SGIP:-3,SGIP:-43,SGIP:0,SGIP:100,SGIP:106,SGIP:113,SGIP:121,SGIP:14,SGIP:17,SGIP:19,SGIP:2,SGIP:20,SGIP:22,SGIP:27,SGIP:31,SGIP:36,SGIP:43,SGIP:44,SGIP:45,SGIP:50,SGIP:57,SGIP:58,SGIP:59,SGIP:6,SGIP:61,SGIP:63,SGIP:64,SGIP:67,SGIP:77,SGIP:8,SGIP:86,SGIP:88,SGIP:89,SGIP:9,SGIP:90,SGIP:92,SGIP:93,SGIP:97,SGIP:98,SME102,SME103,SME15,SME50,SME77,SME8,SME88,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,YY:0206,failure,0002,0003,0004,0099,614,615,617,636,660,714,812,815,BWLISTS,BwList ,CANCEL,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35RESP:11006,CL:105,CL:106,CL:116,CL:125,DELIVRD,DISTURB,ERRNUM,ErrArea,F0032,F0072,F0081,F0082,GG:0024,IC:0015,MA:018,MA:163,MA:174,MA:612,MA:613,MA:614,MA:615,MA:616,MA:618,MA:619,MA:620,MA:622,MA:625,MA:627,MA:634,MA:636,MA:650,MA:706,MA:711,MA:712,MA:713,MA:714,MA:717,MA:718,MA:721,MA:726,MA:731,MA:762,MA:763,MA:766,MA:812,MA:814,MA:815,MA:870,MBBLACK,MC:0055,MD:9015,MX:0008,NOROUTE,REJECT,REJECTD,SIGNERR,SME-3,SME134,SME15,SME17,SME50,SME9,SME92,SMGP612,SMGP614,SMGP619,SMGP622,SMGP634,SMGP650,SMGP713,SMGP714,SMGP717,SMGP815,SMGP870,SMGP999,TIMEOUT,TUIDING,W-BLACK,YY:0206,ZZ:9003,ZZ:9020,ZZ:9021,ZZ:9253,,      8,     31,    101,    102,   F007,  CHECK,  F0072,-1012,-9223,0002,0004,0099,101,8,9994,BEYONDN,BLACK,CANCEL,CB:0010,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,DB:0090,DB:0108,DB:0119,DB:0140,DB:0141,DB:0144,DB:0309,DELIVRD,DISTURB,E:408,E:ODDL,E:RPTSD,E:RPTSS,ERR:GJZ,EXPIRED,F002,F007,F0072,F0081,FAIL_BL,GG:0024,HD:0001,HD:0003,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0054,IA:0073,IB:0008,ID:0004,ID:0070,ID:0076,JL:0024,JT:105,KEYWORD,LIMIT,MA:0026,MA:0051,MB:1026,MB:1031,MB:1042,MB:1279,MBBLACK,MC:000,MC:0055,MC:0151,MF:9441,MH:0005,MH:19,MI:0000,MI:0001,MI:0002,MI:0008,MI:0012,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0024,MI:0030,MI:0043,MI:0044,MI:0045,MI:0050,MI:0051,MI:0053,MI:0057,MI:0064,MI:0080,MI:0081,MI:0088,MI:0089,MI:0098,MI:0099,MI:0660,MK:0003,MK:0006,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0020,MK:0021,MK:0022,MK:0024,MK:0036,MK:0041,MK:0044,MK:0045,MK:0048,MK:0055,MK:0057,MK:0063,MK:0118,MK:9402,MK:9403,MK:9415,MK:9441,MN:0009,MN:0011,MN:0012,MN:0017,MN:0019,MN:0022,MN:0036,MN:0045,MN:0050,MN:0053,MN:0055,MN:0098,MN:0139,MN:0174,MN:0235,MO:0254,MO:0255,MX:0002,MX:0011,MX:0013,MX:0024,NOROUTE,NOTITLE,PKE:027,REJECTD,REJECTE,REPEATD,SIGFAIL,SME10,SME19,SME20,SME8,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:7000,YX:8019,YX:9006,YY:0206,,      8,     31,    101,    102,  CHECK, E:ODSL,-1013,-14,-9223,0002,0004,0006,0099,1,101,27,660,8,BEYONDN,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:502,CL35ERROR,CL35RESP:11006,CL:105,CL:106,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,DB00141,DB:0090,DB:0107,DB:0108,DB:0117,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0309,DB:0318,DELIVRD,DISTURB,E:ODDL,E:RPTSD,E:RPTSS,EMSERR,ERR:GJZ,F002,F007,F0072,F0081,FAIL_BL,FAIL_RE,GG:0024,HD:0001,HD:0003,HD:0007,HD:0008,HD:19,HD:28,HD:29,HD:31,IA:0051,IA:0054,IA:0073,IB:0008,ID:0004,ID:0070,ID:0076,ID:0199,JL:0013,JL:0024,JL:0026,JT:105,KEYWORD,LIMIT,LT:0101,MA:0026,MA:0051,MA:0053,MA:0054,MA:0073,MB:0008,MB:0019,MB:0069,MB:1026,MB:1031,MB:1042,MB:1077,MB:1279,MBBLACK,MC:0151,MF:9441,MH:0004,MH:0005,MH:0008,MH:19,MH:5,MI:0008,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0030,MI:0043,MI:0044,MI:0045,MI:0048,MI:0050,MI:0051,MI:0053,MI:0057,MI:0064,MI:0075,MI:0080,MI:0098,MI:0099,MI:0999,MK:0003,MK:0006,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0019,MK:0020,MK:0021,MK:0022,MK:0024,MK:0036,MK:0038,MK:0041,MK:0043,MK:0044,MK:0045,MK:0053,MK:0055,MK:0063,MK:0075,MK:0090,MK:0115,MK:0118,MK:9402,MK:9403,MK:9415,MK:9441,MN:0000,MN:0009,MN:0011,MN:0012,MN:0017,MN:0019,MN:0020,MN:0022,MN:0036,MN:0041,MN:0044,MN:0045,MN:0050,MN:0051,MN:0053,MN:0054,MN:0055,MN:0098,MN:0099,MN:0139,MN:0174,MN:0235,MO:0254,MO:0255,MX:0002,MX:0004,MX:0008,MX:0011,MX:0013,MX:0024,NOROUTE,NOTITLE,PKE:027,REJECT,REJECTD,REJECTE,SIGFAIL,SIGNERR,SME1,SME15,SME19,SME20,SME8,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:1000,YX:7000,YX:8008,YX:8019,YX:9006,YY:0206,,      8,     31,    101,    102,  CHECK, REJECT,-1012,-14,-9223,0002,0003,0004,0007,0099,101,37     ,8,9994,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:502,CL35ERROR,CL35RESP:00001,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,DB:0008,DB:0010,DB:0090,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0309,DB:0318,DELIVRD,DISTURB,E:ODDL,E:RPTSD,E:RPTSS,ERR:GJZ,F007,F0072,F0081,FAIL_BL,GG:0024,HD:0001,HD:0003,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0008,IB:0011,IB:0182,IC:0001,IC:0055,ID:0004,ID:0070,ID:0076,ID:0199,JL:0024,JT:105,KEYWORD,LIMIT,MA:0006,MA:0026,MA:0051,MA:0053,MA:0073,MB:0008,MB:0019,MB:0069,MB:0255,MB:1026,MB:1031,MB:1042,MB:1077,MB:1279,MC:0055,MF:9441,MH:0004,MH:0005,MH:0008,MH:19,MI:0002,MI:0004,MI:0006,MI:0008,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0030,MI:0038,MI:0043,MI:0044,MI:0045,MI:0050,MI:0053,MI:0057,MI:0063,MI:0064,MI:0075,MI:0080,MI:0081,MI:0089,MI:0098,MI:0999,MK:0002,MK:0006,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0019,MK:0020,MK:0022,MK:0036,MK:0038,MK:0044,MK:0045,MK:0053,MK:0055,MK:0057,MK:0063,MK:0099,MK:0118,MK:9402,MK:9403,MK:9415,MK:9441,MN:0000,MN:0009,MN:0011,MN:0012,MN:0019,MN:0020,MN:0021,MN:0022,MN:0036,MN:0041,MN:0044,MN:0045,MN:0050,MN:0051,MN:0053,MN:0054,MN:0055,MN:0090,MN:0098,MN:0139,MN:0174,MN:0235,MO:0254,MO:0255,MX:0002,MX:0004,MX:0011,MX:0013,MX:0024,NOROUTE,NOTITLE,None,PKE:027,REJECT,REJECTD,REJECTE,SIGFAIL,SIGNERR,SME1,SME10,SME15,SME19,SME20,SME8,SME92,SUBFAIL,TIMEOUT,TUIDING,UNDELIV,VALVE:H,W-BLACK,WZ:9403,YX:7000,YX:8019,YX:9006,YY:0206,,      8,      9,    101,    102,  CHECK,-1013,-14,-9223,0002,0003,0004,0005,0099,37     ,8,9994,BLACK,CANCEL,CB:0010,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35RESP:00001,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,CMPP43,DB00141,DB:0005,DB:0010,DB:0090,DB:0107,DB:0108,DB:0114,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0309,DB:0318,DELIVRD,DISTURB,E:ODSL,E:RPTSD,E:RPTSS,EMSERR,ERR:GJZ,ERRNUM,F007,F0072,F0081,FAIL_BL,GG:0024,HD:0001,HD:0003,HD:0007,HD:0008,HD:19,HD:28,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0008,IB:0182,IC:0151,ID:0004,ID:0070,ID:0076,ID:0199,JL:0024,JT:105,KEYWORD,LIMIT,LT:9403,MA:0026,MA:0051,MA:0053,MA:0073,MB:0008,MB:0019,MB:0069,MB:1026,MB:1031,MB:1042,MB:1077,MBBLACK,MC:0151,MF:9441,MH:0004,MH:0005,MH:0008,MH:19,MI:0002,MI:0008,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0024,MI:0030,MI:0038,MI:0041,MI:0043,MI:0044,MI:0045,MI:0048,MI:0050,MI:0053,MI:0057,MI:0064,MI:0080,MI:0081,MI:0089,MI:0098,MI:0099,MK:0002,MK:0006,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0020,MK:0022,MK:0023,MK:0036,MK:0044,MK:0045,MK:0055,MK:0063,MK:0075,MK:0118,MK:9402,MK:9403,MK:9415,MK:9441,MN:0000,MN:0009,MN:0011,MN:0012,MN:0019,MN:0020,MN:0022,MN:0036,MN:0043,MN:0044,MN:0045,MN:0050,MN:0053,MN:0054,MN:0055,MN:0098,MN:0099,MN:0139,MN:0174,MO:0254,MO:0255,MX:0002,MX:0004,MX:0011,MX:0013,MX:0024,NOROUTE,NOTITLE,PKE:027,REJECT,REJECTE,SIGFAIL,SIGNERR,SME1,SME19,SME20,SME8,SME92,SUBFAIL,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:7000,YX:8008,YX:8019,YX:9006,YY:0206,,      8,     31,    101,    102,  BLACK,  CHECK,-1012,-1013,-14,-9223,0002,0003,0004,0005,0006,0099,101,660,8,9994,BEYONDN,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:502,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,DB00141,DB:0008,DB:0090,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0309,DB:0318,DELIVRD,DISTURB,E:ODSL,E:RPTSD,E:RPTSS,ERR:GJZ,ERRNUM,F002,F007,F0072,F0081,FAIL_BL,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0008,IB:0182,IC:0055,IC:0151,ID:0004,ID:0013,ID:0070,ID:0076,ID:0199,JL:0024,JT:105,KEYWORD,LIMIT,LT:9403,LT:9429,MA:0006,MA:0026,MA:0051,MA:0053,MB:0008,MB:0019,MB:0069,MB:1026,MB:1031,MB:1077,MBBLACK,MC:0151,MF:9441,MH:0004,MH:0005,MH:0008,MH:19,MH:5,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0043,MI:0044,MI:0045,MI:0048,MI:0050,MI:0051,MI:0054,MI:0057,MI:0064,MI:0075,MI:0080,MI:0081,MI:0089,MI:0090,MI:0098,MI:0099,MI:0660,MI:0999,MK:0002,MK:0003,MK:0006,MK:0008,MK:0009,MK:0010,MK:0015,MK:0016,MK:0017,MK:0019,MK:0020,MK:0022,MK:0023,MK:0024,MK:0036,MK:0038,MK:0043,MK:0044,MK:0045,MK:0048,MK:0053,MK:0055,MK:0057,MK:0063,MK:0075,MK:0090,MK:0099,MK:0115,MK:0118,MK:0150,MK:9402,MK:9403,MK:9415,MK:9441,MN:0000,MN:0009,MN:0011,MN:0012,MN:0017,MN:0019,MN:0020,MN:0022,MN:0029,MN:0036,MN:0043,MN:0044,MN:0045,MN:0050,MN:0053,MN:0054,MN:0055,MN:0056,MN:0098,MN:0139,MN:0174,MN:0235,MO:0254,MO:0255,MX:0002,MX:0004,MX:0011,MX:0013,MX:0024,NOTITLE,PKE:024,PKE:027,REJECT,REJECTD,REJECTE,SIGFAIL,SME1,SME19,SME20,SME8,SME92,SUBFAIL,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:1000,YX:7000,YX:8008,YX:8019,YX:9006,YY:0206,,      8,     17,     31,    101,    102,  CHECK, E:ODSL,-1013,-103,-9223,0002,0003,0004,0005,0099,101,15,704,8,9994,BLACK,CANCEL,CB:0255,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:404,CL35ERR:502,CL35RESP:00001,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,DB:0008,DB:0010,DB:0090,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0160,DB:0309,DB:0318,DELIVRD,DISTURB,E:ODDL,E:ODSL,E:RPTSD,E:RPTSS,ERR:GJZ,ERRNUM,F002,F007,F0072,FAIL_BL,FAIL_RE,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:28,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0008,IB:0182,IC:0055,ID:0004,ID:0070,ID:0076,ID:0199,JL:0024,JL:0028,JT:105,KEYWORD,LIMIT,LT:9403,MA:0022,MA:0026,MA:0051,MA:0073,MB:0008,MB:0019,MB:0069,MB:0255,MB:1026,MB:1031,MB:1041,MB:1042,MB:1077,MB:1279,MBBLACK,MC:0055,MC:0151,MC_T:55,MF:9441,MH:0005,MH:0008,MH:19,MI:0002,MI:0008,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0024,MI:0043,MI:0044,MI:0045,MI:0048,MI:0050,MI:0053,MI:0056,MI:0057,MI:0064,MI:0080,MI:0089,MI:0090,MI:0098,MI:0099,MK:0002,MK:0003,MK:0006,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0019,MK:0020,MK:0022,MK:0036,MK:0041,MK:0044,MK:0045,MK:0055,MK:0056,MK:0057,MK:0075,MK:0090,MK:0115,MK:0118,MK:9403,MK:9415,MK:9441,MN:0000,MN:0009,MN:0011,MN:0012,MN:0013,MN:0019,MN:0020,MN:0022,MN:0036,MN:0043,MN:0045,MN:0050,MN:0051,MN:0054,MN:0055,MN:0090,MN:0098,MN:0139,MN:0174,MO:0254,MO:0255,MX:0002,MX:0011,MX:0012,MX:0013,MX:0024,NOROUTE,NOTITLE,PKE:027,REJECT,REJECTD,REJECTE,SIGFAIL,SME10,SME19,SME20,SME8,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:1000,YX:7000,YX:8019,YX:9006,YY:0206,2:182,2:72,DELIVRD,SGIP:83,,    102,-9223,0004,0099,1,8,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL35ERR:502,CL35RESP:11006,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,CMPP30ERR:169,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0309,DB:0318,DELIVRD,DISTURB,E:RPTSS,F007,F0072,F0081,FAIL_RE,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0053,IA:0073,IC:0001,IC:0055,IC:0151,ID:0004,ID:0012,ID:0070,JL:0024,JT:105,KEYWORD,LIMIT,MA:0051,MA:0053,MB:0008,MB:1077,MBBLACK,MC:0001,MC:0151,MF:9441,MH:0005,MH:19,MI:0002,MI:0008,MI:0010,MI:0011,MI:0012,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0038,MI:0043,MI:0044,MI:0045,MI:0050,MI:0051,MI:0052,MI:0053,MI:0075,MI:0080,MI:0081,MI:0098,MK:0008,MK:0009,MK:0011,MK:0015,MK:0020,MK:0022,MK:0024,MK:0036,MK:0041,MK:0045,MK:0053,MK:0063,MK:0066,MK:0075,MK:0090,MK:9403,MK:9415,MN:0000,MN:0009,MN:0011,MN:0012,MN:0017,MN:0020,MN:0022,MN:0045,MN:0050,MN:0051,MN:0053,MN:0055,MN:0235,NOROUTE,PKE:027,REJECT,REJECTE,SIGFAIL,SME13,SME169,SME19,SME20,SME8,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,YX:7000,YX:9006,YY:0206,,      8,     31,    102,  -9234,  CHECK,-9223,0002,0003,0004,0007,0099,101,108,8,BEYONDN,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:404,CL35RESP:00001,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,DB00141,DB:0008,DB:0090,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0309,DB:0318,DELIVRD,DISTURB,E:408,E:ODDL,E:ODSL,E:RPTSD,E:RPTSS,ERR:GJZ,F007,F0072,F0081,FAIL_BL,FAIL_RE,GG:0024,HD:0001,HD:0003,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0008,IB:0011,IC:0001,IC:0055,ID:0004,ID:0070,ID:0076,ID:1241,JL:0024,JT:105,KEYWORD,LIMIT,LT:9429,MA:0026,MA:0051,MA:0053,MA:0054,MA:0073,MB:0008,MB:0019,MB:0069,MB:0255,MB:1026,MB:1031,MB:1042,MB:1077,MB:1279,MBBLACK,MC:0055,MC:0151,MF:9441,MH:0004,MH:0005,MH:0008,MH:19,MI:0002,MI:0008,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0043,MI:0044,MI:0045,MI:0048,MI:0050,MI:0051,MI:0057,MI:0063,MI:0064,MI:0075,MI:0080,MI:0081,MI:0089,MI:0098,MI:0099,MI:0999,MK:0002,MK:0003,MK:0006,MK:0008,MK:0011,MK:0015,MK:0016,MK:0017,MK:0019,MK:0020,MK:0021,MK:0022,MK:0024,MK:0036,MK:0038,MK:0041,MK:0044,MK:0045,MK:0048,MK:0055,MK:0056,MK:0063,MK:0075,MK:0098,MK:0099,MK:0118,MK:9403,MK:9441,MN:0000,MN:0009,MN:0011,MN:0012,MN:0017,MN:0019,MN:0022,MN:0029,MN:0036,MN:0043,MN:0044,MN:0045,MN:0050,MN:0051,MN:0053,MN:0055,MN:0098,MN:0139,MN:0174,MO:0254,MO:0255,MX:0002,MX:0008,MX:0011,MX:0013,MX:0024,NOROUTE,NOTITLE,PKE:027,REJECT,REJECTD,REJECTE,SIGFAIL,SME15,SME19,SME20,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:1006,YX:7000,YX:8019,YX:9006,YY:0206,,      8,     31,    101,    102,  CHECK, REJECT,-1012,-1013,-9223,0002,0004,0007,0099,101,37     ,9994,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:502,CL35RESP:00001,CL35RESP:11006,CL:105,CL:106,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,DB00141,DB:0090,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0309,DELIVRD,DISTURB,E:RPTSD,E:RPTSS,EMSERR,ERR:GJZ,F007,F0072,FAIL_BL,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0008,IB:0182,ID:0004,ID:0070,ID:0076,ID:0105,JT:105,KEYWORD,LIMIT,MA:0006,MA:0026,MA:0051,MA:0053,MA:0054,MB:0008,MB:0019,MB:0069,MB:1031,MB:1042,MB:1077,MBBLACK,MC:0001,MC:0151,MF:9441,MH:0004,MH:0005,MH:0008,MH:19,MI:0002,MI:0008,MI:0010,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0030,MI:0038,MI:0043,MI:0044,MI:0045,MI:0050,MI:0051,MI:0053,MI:0057,MI:0063,MI:0064,MI:0072,MI:0075,MI:0080,MI:0084,MI:0088,MI:0089,MI:0098,MI:0999,MK:0002,MK:0006,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0020,MK:0021,MK:0022,MK:0023,MK:0029,MK:0036,MK:0041,MK:0044,MK:0045,MK:0050,MK:0053,MK:0055,MK:0057,MK:0063,MK:0075,MK:0099,MK:0115,MK:9403,MK:9415,MK:9441,MN:0009,MN:0011,MN:0012,MN:0019,MN:0020,MN:0022,MN:0036,MN:0043,MN:0044,MN:0045,MN:0050,MN:0051,MN:0055,MN:0098,MN:0139,MN:0174,MN:0235,MO:0254,MO:0255,MX:0002,MX:0011,MX:0013,MX:0024,NOROUTE,NOTITLE,PKE:027,REJECT,REJECTD,REJECTE,REMOVED,SIGFAIL,SIGNERR,SME1,SME19,SME20,SME8,SME92,SUBFAIL,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:1000,YX:7000,YX:8019,YX:9006,YY:0206,,    102,  CHECK, E:ODSL,-1013,-106   ,-9223,0002,0004,0099,101,660,9994,BEYONDN,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:404,CL35ERR:502,CL35RESP:00001,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,CMPP30ERR:169,DB:0008,DB:0010,DB:0090,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0318,DELIVRD,DISTURB,E:ODSL,E:RPTSD,E:RPTSS,ERR:GJZ,ERRNUM,F007,F0072,F0081,FAIL_BL,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IB:0008,IB:0188,IC:0062,IC:0151,ID:0004,ID:0070,ID:0076,ID:1241,JL:0024,JT:105,KEYWORD,LIMIT,MA:0006,MA:0026,MA:0051,MA:0053,MA:0073,MB:0008,MB:0019,MB:0069,MB:0255,MB:1026,MB:1031,MB:1077,MB:1279,MBBLACK,MC:0151,MF:9441,MH:0004,MH:0005,MH:0008,MH:19,MI:0008,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0029,MI:0038,MI:0043,MI:0044,MI:0045,MI:0051,MI:0053,MI:0057,MI:0063,MI:0064,MI:0075,MI:0080,MI:0089,MI:0098,MI:0099,MI:0660,MI:0999,MK:0002,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0019,MK:0020,MK:0021,MK:0022,MK:0023,MK:0038,MK:0044,MK:0045,MK:0051,MK:0053,MK:0055,MK:0056,MK:0057,MK:0063,MK:0066,MK:0075,MK:0115,MK:0118,MK:9402,MK:9403,MK:9415,MN:0000,MN:0009,MN:0011,MN:0012,MN:0017,MN:0019,MN:0021,MN:0022,MN:0036,MN:0044,MN:0045,MN:0054,MN:0055,MN:0098,MN:0139,MO:0254,MO:0255,MX:0002,MX:0004,MX:0011,MX:0013,MX:0024,NOROUTE,NOTITLE,PKE:027,REJECT,REJECTD,REJECTE,SIGFAIL,SME1,SME10,SME13,SME15,SME19,SME20,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:1000,YX:7000,YX:8019,YX:9006,YY:0206,0002,0003,613,615,617,660,760,901,BWLISTS,BwList ,CANCEL,CL30:480,CL32RESP:11011,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:8,CMPP43,DELIVRD,DISTURB,ErrArea,F0032,F0072,F0081,F0082,GG:0024,IC:0015,MA:006,MA:163,MA:174,MA:610,MA:612,MA:613,MA:614,MA:615,MA:620,MA:622,MA:625,MA:627,MA:634,MA:636,MA:650,MA:711,MA:713,MA:714,MA:717,MA:726,MA:762,MA:814,MA:815,MA:827,MA:870,MA:998,MBBLACK,MC:0055,MD:9015,MX:0008,NOROUTE,REJECT,REJECTD,SME-3,SME134,SME17,SME50,SME92,SMGP001,SMGP612,SMGP614,SMGP615,SMGP713,SMGP717,SMGP812,SMGP815,SMGP870,TIMEOUT,TUIDING,UNKNOWN,W-BLACK,YY:0206,ZZ:9003,ZZ:9020,ZZ:9021,ZZ:9253,SIGNERR,-1,0002,0004,0005,0006,0099,106,118,15,17,2,20,22,24,2:11,2:113,2:15,2:19,2:20,2:219,2:22,2:23,2:255,2:3,2:36,2:40,2:44,2:45,2:53,2:56,2:57,2:6,2:61,2:67,2:8,2:86,2:88,2:89,2:9,2:90,2:92,2:98,3,45,53,57,8,88,89,90,98,99,BEYONDN,CANCEL,CJ:0005,CJ:0007,CJ:0008,CL30:480,CL35ERR:502,CL35RESP:00001,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:8,DELIVRD,DISTURB,GG:0024,HD:0001,HD:19,LT:0-43,LT:00-1,LT:0009,LT:0015,LT:0017,LT:0020,LT:0022,LT:0023,LT:0027,LT:0054,LT:0057,LT:0059,LT:0061,LT:0064,LT:0086,LT:0090,LT:0098,MA:0027,MBBLACK,MD:9403,MD:9419,MG:0015,MG:0097,MO:0254,NOPASS,NOROUTE,REJECT,SGIP:-1,SGIP:-11,SGIP:-4,SGIP:-43,SGIP:-7,SGIP:113,SGIP:14,SGIP:16,SGIP:17,SGIP:19,SGIP:20,SGIP:22,SGIP:29,SGIP:3,SGIP:30,SGIP:36,SGIP:44,SGIP:45,SGIP:48,SGIP:5,SGIP:50,SGIP:51,SGIP:53,SGIP:57,SGIP:61,SGIP:64,SGIP:67,SGIP:79,SGIP:8,SGIP:86,SGIP:88,SGIP:89,SGIP:9,SGIP:90,SGIP:92,SGIP:97,SGIP:98,SGIP:99,SME102,SME103,SME15,SME50,SME69,SME77,SME8,SME88,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,YY:0206,failure,-1,-43,0002,0004,0005,0007,0099,15,17,20,22,27,2:10,2:113,2:17,2:19,2:2,2:20,2:22,2:24,2:255,2:27,2:3,2:36,2:40,2:43,2:44,2:45,2:48,2:50,2:57,2:61,2:67,2:70,2:73,2:77,2:79,2:8,2:86,2:88,2:89,2:9,2:90,2:92,2:98,45,50,53,56,57,63,77,8,86,88,89,9,90,93,98,99,BEYONDN,CANCEL,CJ:0005,CJ:0007,CJ:0008,CL30:480,CL30ERROR,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:8,DELIVRD,DISTURB,ERRNUM,GG:0024,HD:0001,HD:19,LT:00-1,LT:0003,LT:0015,LT:0017,LT:0020,LT:0022,LT:0024,LT:0048,LT:0057,LT:0061,LT:0064,LT:0067,LT:0086,LT:0090,LT:0092,LT:0098,MA:0006,MA:0027,MBBLACK,MD:9402,MD:9403,MD:9419,MG:0015,MO:0254,NOPASS,NOROUTE,REJECT,SGIP:-1,SGIP:-18,SGIP:-26,SGIP:-3,SGIP:-43,SGIP:0,SGIP:106,SGIP:113,SGIP:117,SGIP:14,SGIP:17,SGIP:18,SGIP:19,SGIP:2,SGIP:20,SGIP:22,SGIP:24,SGIP:27,SGIP:36,SGIP:40,SGIP:43,SGIP:44,SGIP:45,SGIP:51,SGIP:57,SGIP:59,SGIP:61,SGIP:63,SGIP:64,SGIP:67,SGIP:73,SGIP:77,SGIP:79,SGIP:8,SGIP:86,SGIP:88,SGIP:89,SGIP:90,SGIP:92,SGIP:97,SGIP:98,SGIP:99,SME102,SME103,SME50,SME77,SME8,SME88,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,YY:0206,failure,,    102,  CHECK,-1013,-9223,0002,BLACK,CANCEL,CB:0010,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35RESP:00001,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,DB:0008,DB:0010,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0309,DELIVRD,DISTURB,E:ODDL,E:RPTSD,E:RPTSS,ERR:GJZ,F007,F0072,FAIL_BL,FAIL_RE,GG:0024,HD:0001,HD:0003,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0008,IB:0009,IB:0169,IB:0255,IC:0001,IC:0151,ID:0004,ID:0012,ID:0076,ID:1241,JT:105,KEYWORD,LIMIT,LT:0023,LT:0101,MA:0026,MA:0053,MB:0069,MB:1026,MB:1041,MB:1077,MBBLACK,MC:0001,MF:9441,MH:0004,MH:0005,MH:19,MI:0002,MI:0008,MI:0015,MI:0017,MI:0020,MI:0022,MI:0030,MI:0043,MI:0044,MI:0045,MI:0057,MI:0064,MI:0075,MI:0080,MI:0081,MI:0089,MI:0098,MI:0099,MI:0660,MK:0002,MK:0003,MK:0006,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0020,MK:0022,MK:0023,MK:0024,MK:0036,MK:0043,MK:0044,MK:0045,MK:0048,MK:0051,MK:0053,MK:0055,MK:0063,MK:0066,MK:0075,MK:9415,MK:9441,MN:0011,MN:0012,MN:0017,MN:0019,MN:0020,MN:0022,MN:0036,MN:0044,MN:0045,MN:0050,MN:0053,MN:0054,MN:0055,MN:0098,MN:0099,MN:0235,MO:0254,MO:0255,MX:0002,MX:0013,NOROUTE,PHONERR,PKE:027,REJECT,REJECTD,REJECTE,SIGFAIL,SME13,SME169,SME19,SME20,SME8,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:1006,YX:7000,YX:9006,YY:0206,,      8,     17,     31,    101,    102,  BLACK,  CHECK, E:ODSL,-1012,-1013,-9223,0002,0003,0004,0007,0099,101,108,15,9994,BEYONDN,BLACK,CANCEL,CB:0010,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35RESP:00001,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,DB00141,DB:0008,DB:0010,DB:0090,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0160,DB:0309,DB:0318,DELIVRD,DISTURB,E:ODDL,E:ODSL,E:RPTSD,E:RPTSS,ERR:GJZ,ERRNUM,F007,F0072,F0081,FAIL_BL,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0008,IB:0182,IB:0255,ID:0004,ID:0070,ID:0076,JL:0024,JT:105,KEYWORD,LIMIT,MA:0026,MA:0051,MA:0053,MA:0073,MB:0008,MB:0019,MB:0069,MB:0255,MB:1026,MB:1031,MB:1042,MB:1077,MB:1279,MBBLACK,MC:000,MC:0001,MF:9441,MH:0005,MH:0008,MH:19,MI:0002,MI:0008,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0029,MI:0036,MI:0041,MI:0043,MI:0044,MI:0045,MI:0050,MI:0051,MI:0054,MI:0057,MI:0063,MI:0064,MI:0081,MI:0084,MI:0088,MI:0089,MI:0098,MI:0099,MK:0002,MK:0003,MK:0006,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0020,MK:0022,MK:0023,MK:0029,MK:0036,MK:0043,MK:0044,MK:0045,MK:0055,MK:0056,MK:0063,MK:0075,MK:0090,MK:0098,MK:0115,MK:0118,MK:9403,MK:9415,MK:9441,MN:0000,MN:0009,MN:0011,MN:0012,MN:0017,MN:0019,MN:0020,MN:0022,MN:0036,MN:0041,MN:0043,MN:0044,MN:0045,MN:0051,MN:0055,MN:0075,MN:0098,MN:0099,MN:0174,MN:0235,MO:0254,MO:0255,MX:0002,MX:0011,MX:0012,MX:0013,MX:0024,NOROUTE,NOTITLE,PKE:027,REJECT,REJECTD,REJECTE,SIGFAIL,SME15,SME19,SME20,SME8,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:7000,YX:8008,YX:8019,YX:9006,YY:0206,,      8,     31,    101,    102,  -9219,  CHECK,-1013,-9223,0002,0003,0004,0099,9994,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:404,CL35ERR:502,CL35RESP:00001,CL35RESP:11006,CL:105,CL:106,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,DB00141,DB:0008,DB:0010,DB:0090,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0309,DB:0318,DELIVRD,DISTURB,E:408,E:ODDL,E:RPTSD,E:RPTSS,ERR:GJZ,ERRNUM,EXPIRED,F007,F0072,FAIL_BL,FAIL_RE,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:28,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0194,ID:0004,ID:0070,ID:0076,JL:0024,JT:105,KEYWORD,LIMIT,MA:0026,MA:0051,MA:0053,MB:0008,MB:0019,MB:0069,MB:0255,MB:1026,MB:1031,MB:1077,MB:1279,MBBLACK,MC:0151,MF:9441,MH:0005,MH:0008,MH:19,MI:0008,MI:0009,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0029,MI:0030,MI:0043,MI:0044,MI:0045,MI:0050,MI:0051,MI:0053,MI:0057,MI:0063,MI:0064,MI:0075,MI:0080,MI:0081,MI:0098,MI:0210,MI:0999,MK:0002,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0019,MK:0020,MK:0021,MK:0022,MK:0029,MK:0036,MK:0041,MK:0044,MK:0045,MK:0053,MK:0055,MK:0057,MK:0063,MK:0098,MK:0118,MK:9403,MK:9415,MK:9441,MN:0000,MN:0009,MN:0011,MN:0012,MN:0017,MN:0020,MN:0021,MN:0022,MN:0036,MN:0038,MN:0043,MN:0044,MN:0045,MN:0050,MN:0051,MN:0053,MN:0055,MN:0098,MN:0099,MN:0139,MN:0174,MN:0999,MO:0254,MO:0255,MX:0002,MX:0004,MX:0011,MX:0013,MX:0014,MX:0024,NOROUTE,NOTITLE,PKE:024,PKE:027,REJECT,REJECTD,REJECTE,SIGFAIL,SIGNERR,SME1,SME15,SME19,SME20,SME41,SME8,SME92,SUBFAIL,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:7000,YX:8019,YX:9006,YY:0206,2:1,2:40,2:45,CL30RESP:-40,DELIVRD,DISTURB,SGIP:-37,SGIP:90,001,899,BwList ,CL:125,DELIVRD,F0072,NOROUTE,SME50,SME92,TIMEOUT,YY:0206,DELIVRD,GG:0024,MA:711,REJECTD,SMGP801,TIMEOUT,YY:0206,ZZ:9003,DELIVRD,YY:0206,CMPP30ERR:169,DELIVRD,HD:0007,HD:31,MBBLACK,MC:000,MI:0010,MI:0017,MI:0022,MI:0081,MI:0098,MK:0020,MK:0022,MK:0023,MK:0055,MN:0013,SME13,SME169,SME19,SME41,2:24,2:40,CANCEL,CJ:0007,DELIVRD,LT:0013,SGIP:22,SGIP:53,    102,CJ:0005,CJ:0006,CL35RESP:11006,CL:125,DB:0108,DB:0119,DELIVRD,F0072,HD:0001,ID:0012,ID:0076,JT:105,MBBLACK,MH:0005,MH:19,MI:0017,MI:0023,MI:0030,MI:0075,MI:0081,MK:0017,MK:0022,MK:0066,MK:9415,MN:0012,MN:0022,MN:0050,MN:0075,MN:0174,SME19,SME20,SME92,TIMEOUT,CJ:0006,CL:116,CL:125,CMPP30ERR:169,DELIVRD,GG:0024,HD:0001,HD:0007,HD:29,ID:0013,MBBLACK,MH:19,MI:0010,MI:0017,MI:0022,MI:0081,MK:0015,MK:0022,MN:0017,MN:0020,MN:0174,REJECTD,SIGFAIL,SME13,SME169,YX:7000,-1,-43,0004,17,2,20,2:17,2:27,2:48,2:8,51,89,92,CJ:0007,CL:116,CL:125,DELIVRD,GG:0024,HD:0001,LT:0003,LT:0020,LT:0086,MBBLACK,REJECT,SGIP:-1,SGIP:-3,SGIP:113,SGIP:17,SGIP:18,SGIP:19,SGIP:23,SGIP:36,SGIP:54,SGIP:67,SGIP:72,SGIP:9,SGIP:92,SGIP:98,SME102,SME50,SME8,SME92,UNDELIV,0099,2,22,2:117,2:20,2:22,2:4,2:40,2:57,2:8,2:9,2:90,50,BEYONDN,DELIVRD,DISTURB,GG:0024,HD:0001,LT:0002,LT:0003,LT:0009,LT:0017,LT:0020,LT:0022,MBBLACK,SGIP:113,SGIP:17,SGIP:2,SGIP:22,SGIP:36,SGIP:4,SGIP:64,SGIP:72,SGIP:86,SGIP:88,SGIP:9,SGIP:92,SGIP:98,SME102,SME50,SME8,TUIDING,UNDELIV,-1,0004,2,2:11,2:17,2:20,2:22,2:36,2:4,2:40,2:45,2:8,2:89,2:9,4,89,BEYONDN,CANCEL,CJ:0007,CJ:0008,DELIVRD,GG:0024,HD:0001,LT:00-1,LT:0002,LT:0003,LT:0004,LT:0009,LT:0010,LT:0017,LT:0020,LT:0022,LT:0057,LT:0061,LT:0092,MBBLACK,REJECT,SGIP:-1,SGIP:113,SGIP:121,SGIP:17,SGIP:19,SGIP:22,SGIP:3,SGIP:36,SGIP:53,SGIP:67,SGIP:72,SGIP:82,SGIP:9,SGIP:99,SME102,SME50,SME92,TUIDING,YY:0206,2:113,2:14,2:2,2:20,2:22,2:36,2:8,2:9,2:92,50,53,89,BEYONDN,CL:105,CL:125,CMPP20ERR:8,DELIVRD,DISTURB,HD:0001,LT:0-43,LT:0003,LT:0017,LT:0020,LT:0022,LT:0057,MBBLACK,MD:9419,MG:0023,MO:0254,NOROUTE,SGIP:-1,SGIP:-3,SGIP:0,SGIP:113,SGIP:22,SGIP:3,SGIP:48,SGIP:58,SGIP:86,SGIP:9,SGIP:92,SME102,SME103,SME50,SME92,UNDELIV,failure,1,CL30:480,CMPP20ERR:13,CMPP20ERR:19,CMPP30ERR:169,DISTURB,ERRNUM,MH:18,MI:0010,NOROUTE,REJECT,UNDELIV,0002,0004,0099,660,BWLISTS,BwList ,CANCEL,CL30:480,CL32RESP:11011,CL:105,CL:106,CL:116,CL:125,CMPP20ERR:8,DELIVRD,DISTURB,ErrArea,F0032,GG:0024,MA:612,MA:613,MA:614,MA:615,MA:618,MA:620,MA:625,MA:636,MA:650,MA:680,MA:711,MA:713,MA:714,MA:716,MA:717,MA:718,MA:726,MA:762,MA:814,MA:870,MBBLACK,NOPASS,NOROUTE,REJECT,REJECTD,SME-3,SME134,SME17,SME50,SME92,SMGP815,SMGP870,TIMEOUT,UNKNOWN,ZZ:9003,ZZ:9253,-1,-43,17,20,2:113,2:2,2:20,2:255,2:36,2:40,2:44,2:45,2:50,2:53,2:57,2:8,2:86,2:89,2:90,35,50,51,53,89,BEYONDN,CANCEL,CJ:0005,CJ:0007,CL30:480,CL35RESP:11006,CL:105,CL:116,CL:125,DELIVRD,GG:0024,HD:0001,LT:0002,LT:0020,LT:0040,MBBLACK,MD:9402,MD:9403,MD:9419,MG:0015,MO:0254,NOROUTE,REJECT,SGIP:-1,SGIP:-4,SGIP:-43,SGIP:113,SGIP:17,SGIP:19,SGIP:2,SGIP:20,SGIP:22,SGIP:27,SGIP:31,SGIP:38,SGIP:40,SGIP:43,SGIP:44,SGIP:45,SGIP:57,SGIP:58,SGIP:64,SGIP:67,SGIP:79,SGIP:8,SGIP:86,SGIP:89,SGIP:9,SGIP:90,SGIP:98,SME102,SME50,SME77,SME88,SME92,TUIDING,UNDELIV,YY:0206,-1,-43,0002,0004,0006,0007,0099,17,2,20,22,2:113,2:118,2:15,2:2,2:20,2:255,2:36,2:40,2:43,2:44,2:45,2:53,2:57,2:79,2:8,2:86,2:88,2:89,2:90,2:92,2:98,48,53,57,67,8,86,89,BEYONDN,CANCEL,CJ:0005,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERROR,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:8,DELIVRD,DISTURB,GG:0024,HD:0001,HD:19,LT:0-43,LT:0015,LT:0017,LT:0020,LT:0022,LT:0040,LT:0044,LT:0048,LT:0057,LT:0061,LT:0086,MA:0006,MBBLACK,MD:9402,MD:9403,MD:9419,MG:0015,MO:0254,NOROUTE,REJECT,SGIP:-1,SGIP:-3,SGIP:-43,SGIP:0,SGIP:100,SGIP:108,SGIP:113,SGIP:118,SGIP:16,SGIP:19,SGIP:2,SGIP:20,SGIP:22,SGIP:31,SGIP:36,SGIP:44,SGIP:45,SGIP:53,SGIP:57,SGIP:58,SGIP:6,SGIP:61,SGIP:63,SGIP:67,SGIP:73,SGIP:79,SGIP:8,SGIP:86,SGIP:88,SGIP:89,SGIP:9,SGIP:90,SGIP:92,SGIP:97,SGIP:98,SGIP:99,SME102,SME103,SME50,SME52,SME69,SME77,SME8,SME88,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,YY:0206,0002,0003,0004,0099,615,714,716,779,BWLISTS,BwList ,CANCEL,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35RESP:11006,CL:105,CL:106,CL:116,CL:125,CMPP20ERR:8,DELIVRD,DISTURB,F0032,F0082,GG:0024,IC:0015,MA:174,MA:614,MA:615,MA:617,MA:619,MA:620,MA:634,MA:636,MA:650,MA:706,MA:711,MA:713,MA:714,MA:717,MA:718,MA:721,MA:726,MA:762,MA:768,MA:810,MA:812,MA:814,MA:815,MA:870,MA:875,MBBLACK,MC:0055,MD:9015,MX:0008,NOPASS,NOROUTE,REJECT,REJECTD,SME-3,SME134,SME15,SME17,SME50,SME92,SMGP614,SMGP618,SMGP650,SMGP702,SMGP711,SMGP713,SMGP870,TIMEOUT,TUIDING,UNKNOWN,YY:0206,ZZ:9003,ZZ:9020,ZZ:9021,,    102,-9223,0002,0004,0005,0099,BLACK,CANCEL,CB:0010,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:502,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP30ERR:169,DB00141,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0309,DELIVRD,DISTURB,E:RPTSS,ERR:GJZ,EXPIRED,F007,F0072,F0081,FAIL_BL,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0054,IB:0008,IC:0151,ID:0004,ID:0012,ID:0070,ID:0076,JL:0024,JT:105,KEYWORD,LIMIT,LT:0023,LT:0101,MA:0026,MA:0051,MB:0019,MB:1031,MBBLACK,MC:0151,MH:0005,MH:19,MI:0010,MI:0015,MI:0017,MI:0020,MI:0022,MI:0041,MI:0044,MI:0045,MI:0051,MI:0057,MI:0064,MI:0075,MI:0081,MI:0089,MK:0008,MK:0011,MK:0015,MK:0017,MK:0019,MK:0020,MK:0022,MK:0024,MK:0036,MK:0044,MK:0045,MK:0053,MK:0057,MK:0075,MK:9403,MK:9415,MK:9911,MN:0009,MN:0011,MN:0012,MN:0020,MN:0045,MN:0075,MN:0099,MN:0174,MX:0002,NOWAY00,PHONERR,PKE:027,REJECT,REJECTE,SIGFAIL,SME13,SME169,SME19,SME20,SME41,SME8,SME92,SWITCH0,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:7000,YX:9006,YY:0206,failure,0002,0003,0004,0007,0099,612,614,615,618,875,BWLISTS,BwList ,CANCEL,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:8,DELIVRD,DISTURB,ErrArea,F0032,F0072,F0081,F0082,GG:0024,IC:0015,MA:006,MA:163,MA:174,MA:605,MA:612,MA:613,MA:614,MA:615,MA:617,MA:618,MA:619,MA:620,MA:624,MA:625,MA:627,MA:634,MA:636,MA:706,MA:711,MA:713,MA:714,MA:726,MA:762,MA:768,MA:779,MA:812,MA:814,MA:815,MA:817,MA:827,MA:870,MA:879,MBBLACK,MC:0055,MD:9015,MX:0008,NOPASS,NOROUTE,REJECT,SME-3,SME15,SME17,SME50,SME9,SME92,SMGP612,SMGP614,SMGP615,SMGP620,SMGP711,SMGP713,SMGP717,SMGP815,SMGP870,TIMEOUT,TUIDING,UNKNOWN,W-BLACK,YY:0206,ZZ:9003,ZZ:9020,ZZ:9021,0002,0003,0006,0099,615,712,714,BWLISTS,BwList ,CANCEL,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:502,CL35RESP:11006,CL:105,CL:116,CL:125,DELIVRD,DISTURB,ErrArea,F0032,F0072,F0081,F0082,GG:0024,IC:0015,LT:0101,MA:006,MA:163,MA:174,MA:612,MA:613,MA:614,MA:615,MA:619,MA:620,MA:622,MA:627,MA:634,MA:636,MA:650,MA:706,MA:711,MA:713,MA:714,MA:717,MA:718,MA:726,MA:731,MA:762,MA:766,MA:812,MA:813,MA:814,MA:815,MA:817,MA:818,MA:870,MBBLACK,MC:0055,MD:9015,NOPASS,NOROUTE,REJECT,SME-3,SME15,SME17,SME50,SME92,SMGP612,SMGP614,SMGP615,SMGP706,SMGP717,SMGP814,SMGP870,SMGP999,TIMEOUT,TUIDING,UNKNOWN,ZZ:9003,ZZ:9020,ZZ:9021,ZZ:9253,,      8,    102,  CHECK, E:ODSL,-1013,-103,-9223,0002,0003,0004,0005,0099,101,43,9994,BLACK,CANCEL,CB:0010,CB:0255,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:502,CL35ERROR,CL35RESP:00001,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,CMPP30ERR:169,DB:0008,DB:0090,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0309,DB:0318,DELIVRD,DISTURB,E:RPTSD,E:RPTSS,ERR:GJZ,F007,F0072,FAIL_BL,FAIL_RE,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0008,IB:0182,IC:0151,ID:0004,ID:0070,ID:0076,ID:1241,JL:0024,JT:105,KEYWORD,KFC:FIL,LIMIT,MA:0026,MA:0051,MA:0053,MA:0054,MB:0008,MB:0019,MB:0069,MB:1026,MB:1031,MB:1077,MB:1279,MBBLACK,MC:0055,MC:0151,MF:9441,MH:0005,MH:0008,MH:19,MI:0002,MI:0008,MI:0010,MI:0011,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0030,MI:0043,MI:0044,MI:0045,MI:0050,MI:0051,MI:0053,MI:0055,MI:0057,MI:0063,MI:0064,MI:0075,MI:0081,MI:0098,MI:0999,MK:0002,MK:0006,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0020,MK:0022,MK:0023,MK:0029,MK:0036,MK:0038,MK:0041,MK:0043,MK:0044,MK:0045,MK:0055,MK:0057,MK:0063,MK:0075,MK:0090,MK:0118,MK:9403,MK:9415,MK:9441,MN:0000,MN:0009,MN:0011,MN:0012,MN:0017,MN:0019,MN:0020,MN:0022,MN:0036,MN:0043,MN:0044,MN:0045,MN:0050,MN:0051,MN:0053,MN:0054,MN:0055,MN:0098,MN:0099,MN:0139,MN:0174,MO:0254,MO:0255,MX:0002,MX:0004,MX:0013,MX:0024,NOROUTE,NOTITLE,PHONERR,PKE:027,REJECT,REJECTD,REJECTE,SIGFAIL,SIGNERR,SME1,SME13,SME15,SME169,SME19,SME20,SME41,SME8,SME92,SUBFAIL,TIMEOUT,TUIDING,UNDELIV,VALVE:M,W-BLACK,WZ:9403,YX:7000,YX:8019,YX:9006,YY:0206,,      8,     31,    102,  CHECK, E:ODSL,-1013,-9223,0002,0003,0004,0007,0099,43,660,9994,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL32RESP:11011,CL35ERR:502,CL35ERROR,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,CMPP30ERR:169,DB00141,DB:0005,DB:0008,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0309,DB:0318,DELIVRD,DISTURB,E:ODDL,E:RPTSD,E:RPTSS,EMSERR,ERR:GJZ,ERRNUM,EXPIRED,F002,F007,F0072,FAIL_BL,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0054,IA:0073,IB:0008,IB:0194,ID:0004,ID:0070,ID:0076,JL:0024,JT:105,KEYWORD,KFC:FIL,LIMIT,LT:0012,MA:0022,MA:0026,MA:0051,MA:0053,MA:0073,MB:0008,MB:0019,MB:0069,MB:1026,MB:1031,MB:1042,MB:1077,MB:1279,MBBLACK,MH:0005,MH:19,MI:0000,MI:0002,MI:0008,MI:0011,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0030,MI:0043,MI:0044,MI:0045,MI:0053,MI:0056,MI:0057,MI:0064,MI:0075,MI:0081,MI:0090,MI:0098,MI:0099,MI:0660,MI:0999,MK:0003,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0019,MK:0020,MK:0022,MK:0023,MK:0024,MK:0029,MK:0036,MK:0038,MK:0043,MK:0044,MK:0045,MK:0053,MK:0055,MK:0056,MK:0057,MK:0063,MK:0075,MK:0084,MK:0098,MK:9403,MK:9415,MN:0000,MN:0011,MN:0012,MN:0019,MN:0020,MN:0022,MN:0038,MN:0043,MN:0044,MN:0045,MN:0050,MN:0051,MN:0053,MN:0055,MN:0075,MN:0098,MN:0174,MO:0254,MX:0002,MX:0013,MX:0024,NOROUTE,NOTITLE,PHONERR,PKE:027,PKE:099,REJECT,REJECTD,REJECTE,SIGFAIL,SME1,SME13,SME15,SME169,SME19,SME20,SME41,SME8,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:1000,YX:7000,YX:9006,YY:0206,,      8,  CHECK,0002,0006,0099,43,9994,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP30ERR:169,DB:0008,DB:0090,DB:0108,DB:0119,DB:0140,DB:0141,DB:0309,DELIVRD,DISTURB,E:RPTSD,E:RPTSS,ERR:GJZ,F0072,FAIL_BL,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0008,ID:0004,ID:0076,JT:105,KEYWORD,MA:0053,MB:1026,MB:1042,MB:1077,MBBLACK,MC:0055,MC:0151,MF:9441,MH:0005,MH:19,MI:0008,MI:0010,MI:0011,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0029,MI:0043,MI:0044,MI:0045,MI:0051,MI:0052,MI:0053,MI:0057,MI:0063,MI:0064,MI:0075,MI:0098,MI:0099,MK:0010,MK:0011,MK:0015,MK:0017,MK:0020,MK:0022,MK:0029,MK:0036,MK:0044,MK:0066,MK:0075,MK:9403,MK:9415,MN:0011,MN:0012,MN:0020,MN:0022,MN:0029,MN:0036,MN:0044,MN:0045,MN:0051,MN:0053,MN:0055,MN:0075,MN:0098,MN:0174,MO:0255,NOROUTE,None,PHONERR,PKE:027,REJECT,REJECTD,REJECTE,SME1,SME13,SME169,SME19,SME41,SME8,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,YX:7000,YX:9006,YY:0206,-1,-43,0004,0099,121,15,17,19,2,20,22,2:113,2:15,2:16,2:17,2:19,2:20,2:219,2:22,2:255,2:31,2:34,2:36,2:40,2:44,2:45,2:48,2:55,2:57,2:61,2:67,2:70,2:8,2:86,2:89,2:9,2:90,2:92,2:99,3,44,48,56,67,8,86,89,90,92,BEYONDN,CANCEL,CJ:0005,CJ:0007,CJ:0008,CL30:480,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:8,DELIVRD,DISTURB,ERRNUM,GG:0024,HD:0001,HD:19,LT:0-43,LT:00-1,LT:0002,LT:0003,LT:0009,LT:0010,LT:0017,LT:0020,LT:0022,LT:0027,LT:0057,LT:0086,LT:0090,LT:0092,MA:0006,MA:0027,MBBLACK,MD:9403,MD:9419,MG:0015,MO:0254,NOROUTE,REJECT,SGIP:-1,SGIP:-13,SGIP:-14,SGIP:-15,SGIP:-3,SGIP:-43,SGIP:-7,SGIP:0,SGIP:100,SGIP:106,SGIP:108,SGIP:113,SGIP:117,SGIP:118,SGIP:14,SGIP:15,SGIP:16,SGIP:17,SGIP:19,SGIP:20,SGIP:22,SGIP:24,SGIP:27,SGIP:3,SGIP:31,SGIP:32,SGIP:35,SGIP:36,SGIP:40,SGIP:43,SGIP:44,SGIP:45,SGIP:48,SGIP:53,SGIP:57,SGIP:58,SGIP:61,SGIP:64,SGIP:67,SGIP:75,SGIP:79,SGIP:8,SGIP:86,SGIP:88,SGIP:89,SGIP:9,SGIP:90,SGIP:92,SGIP:97,SGIP:98,SME102,SME103,SME50,SME52,SME77,SME8,SME88,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,YY:0206,-1,-17,-3,-43,0002,0004,0099,10,117,15,16,17,20,22,2:106,2:113,2:15,2:16,2:17,2:19,2:2,2:20,2:22,2:231,2:24,2:253,2:255,2:27,2:3,2:31,2:36,2:40,2:44,2:45,2:48,2:57,2:6,2:61,2:67,2:73,2:79,2:8,2:86,2:88,2:89,2:9,2:90,2:92,2:98,3,40,45,48,50,53,56,57,64,67,73,8,86,88,89,9,90,92,98,99,BEYONDN,CANCEL,CJ:0005,CJ:0007,CJ:0008,CL30:480,CL32RESP:11011,CL35ERR:404,CL35ERR:502,CL35RESP:00001,CL35RESP:11006,CL:105,CL:106,CL:116,CL:125,CMPP20ERR:8,DELIVRD,DISTURB,GG:0024,HD:0001,HD:19,LT:00-1,LT:0002,LT:0003,LT:0017,LT:0020,LT:0022,LT:0023,LT:0024,LT:0027,LT:0044,LT:0048,LT:0057,LT:0061,LT:0079,LT:0086,LT:0090,LT:0092,LT:0098,MA:0006,MA:0027,MBBLACK,MD:9402,MD:9403,MD:9419,MG:0015,MG:0097,MO:0254,NOPASS,NOROUTE,REJECT,SGIP:-1,SGIP:-11,SGIP:-14,SGIP:-15,SGIP:-17,SGIP:-2,SGIP:-3,SGIP:-37,SGIP:-43,SGIP:-7,SGIP:0,SGIP:100,SGIP:106,SGIP:108,SGIP:113,SGIP:116,SGIP:117,SGIP:118,SGIP:121,SGIP:14,SGIP:15,SGIP:16,SGIP:17,SGIP:19,SGIP:2,SGIP:20,SGIP:22,SGIP:3,SGIP:31,SGIP:35,SGIP:36,SGIP:40,SGIP:43,SGIP:44,SGIP:45,SGIP:48,SGIP:50,SGIP:52,SGIP:57,SGIP:58,SGIP:6,SGIP:61,SGIP:63,SGIP:67,SGIP:70,SGIP:73,SGIP:75,SGIP:79,SGIP:8,SGIP:86,SGIP:88,SGIP:89,SGIP:9,SGIP:90,SGIP:92,SGIP:97,SGIP:98,SIGNERR,SME103,SME15,SME29,SME50,SME69,SME77,SME8,SME88,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,YY:0206,,      8,    102,  CHECK, E:ODSL,-1013,-9223,0002,0004,0007,0099,101,43,9994,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,CMPP30ERR:169,CMPP43,DB00141,DB:0008,DB:0010,DB:0090,DB:0107,DB:0108,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0309,DELIVRD,DISTURB,E:408,E:ODDL,E:RPTSD,E:RPTSS,ERR:GJZ,F007,F0072,F0081,FAIL_BL,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:28,HD:29,HD:31,IA:0051,IA:0054,IA:0073,IB:0008,IB:0182,ID:0004,ID:0012,ID:0070,ID:0076,ID:1241,JL:0024,JT:105,KEYWORD,LIMIT,MA:0022,MA:0051,MA:0053,MA:0073,MB:0008,MB:0019,MB:0069,MB:1026,MB:1031,MB:1077,MBBLACK,MC:0055,MF:9441,MH:0005,MH:0007,MH:0008,MH:19,MI:0002,MI:0008,MI:0009,MI:0011,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0029,MI:0036,MI:0044,MI:0045,MI:0050,MI:0051,MI:0053,MI:0057,MI:0063,MI:0064,MI:0075,MI:0080,MI:0089,MI:0098,MI:0099,MI:0999,MK:0006,MK:0008,MK:0009,MK:0011,MK:0015,MK:0017,MK:0019,MK:0020,MK:0021,MK:0022,MK:0029,MK:0036,MK:0043,MK:0044,MK:0045,MK:0053,MK:0055,MK:0056,MK:0063,MK:0099,MK:0115,MK:0118,MK:9402,MK:9403,MK:9415,MK:9441,MN:0000,MN:0011,MN:0012,MN:0019,MN:0020,MN:0022,MN:0036,MN:0040,MN:0043,MN:0044,MN:0045,MN:0050,MN:0051,MN:0053,MN:0056,MN:0090,MN:0098,MN:0174,MN:0999,MO:0254,MX:0002,MX:0008,MX:0011,MX:0013,MX:0024,NOROUTE,PHONERR,PKE:027,REJECT,REJECTD,REJECTE,SIGFAIL,SME1,SME13,SME169,SME19,SME20,SME41,SME8,SME92,TIMEOUT,TUIDING,UNDELIV,VALVE:M,W-BLACK,YX:1000,YX:7000,YX:8019,YX:9006,YY:0206,,      8,    102,  CHECK,  F0072,-9223,0002,0004,0005,9994,BLACK,CANCEL,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35ERR:404,CL35RESP:11006,CL:105,CL:116,CL:125,CMPP20ERR:19,CMPP20ERR:20,CMPP20ERR:8,CMPP30ERR:169,DB00141,DB:0008,DB:0010,DB:0090,DB:0108,DB:0114,DB:0119,DB:0140,DB:0141,DB:0143,DB:0144,DB:0160,DB:0309,DELIVRD,DISTURB,E:RPTSD,E:RPTSS,ERR:GJZ,F007,F0072,FAIL_BL,FAIL_RE,GG:0024,HD:0001,HD:0007,HD:0008,HD:19,HD:29,HD:31,IA:0051,IA:0053,IA:0054,IA:0073,IB:0008,IB:0182,IC:0062,IC:0151,ID:0004,ID:0070,ID:0076,JL:0024,JT:105,KEYWORD,LIMIT,LT:0023,LT:0101,MA:0026,MA:0051,MA:0053,MA:0073,MB:0008,MB:0019,MB:0069,MB:1026,MB:1031,MB:1042,MB:1077,MB:1279,MBBLACK,MC:0055,MC:0151,MF:9441,MH:0005,MH:0008,MH:19,MI:0008,MI:0010,MI:0015,MI:0017,MI:0020,MI:0022,MI:0023,MI:0029,MI:0036,MI:0043,MI:0044,MI:0045,MI:0048,MI:0050,MI:0052,MI:0057,MI:0063,MI:0064,MI:0075,MI:0078,MI:0080,MI:0081,MI:0088,MI:0098,MI:0660,MK:0003,MK:0006,MK:0008,MK:0011,MK:0015,MK:0017,MK:0019,MK:0020,MK:0022,MK:0023,MK:0024,MK:0029,MK:0036,MK:0043,MK:0044,MK:0045,MK:0048,MK:0053,MK:0055,MK:0057,MK:0066,MK:0075,MK:0099,MK:0115,MK:0118,MK:9403,MK:9441,MN:0000,MN:0011,MN:0012,MN:0020,MN:0022,MN:0044,MN:0045,MN:0050,MN:0053,MN:0055,MN:0098,MN:0099,MN:0174,MO:0254,MX:0002,MX:0011,MX:0013,MX:0024,NOROUTE,NOTITLE,PHONERR,PKE:027,REJECT,REJECTD,REJECTE,REPEATD,SIGFAIL,SME13,SME169,SME19,SME20,SME41,SME8,SME92,TIMEOUT,TUIDING,UNDELIV,W-BLACK,WZ:9403,YX:7000,YX:8019,YX:9006,YY:0206,0002,0003,0004,0005,0007,0099,010,602,612,615,617,618,712,713,714,875,BWLISTS,BwList ,CANCEL,CL30:480,CL30RESP:-42,CL32RESP:11011,CL35RESP:11006,CL:105,CL:106,CL:116,CL:125,CMPP43,DELETED,DELIVRD,DISTURB,ERRNUM,ErrArea,F0032,F0072,F0081,F0082,GG:0024,IC:0015,MA:163,MA:174,MA:602,MA:605,MA:612,MA:613,MA:614,MA:615,MA:618,MA:619,MA:620,MA:622,MA:634,MA:636,MA:706,MA:711,MA:713,MA:714,MA:717,MA:726,MA:730,MA:762,MA:812,MA:814,MA:815,MA:819,MA:827,MA:870,MA:879,MBBLACK,MC:0055,MD:9015,MX:0008,NOROUTE,REJECT,REJECTD,SME-3,SME134,SME15,SME17,SME50,SME9,SME92,SMGP612,SMGP614,SMGP615,SMGP619,SMGP620,SMGP627,SMGP636,SMGP650,SMGP706,SMGP711,SMGP713,SMGP717,SMGP812,SMGP814,SMGP870,TIMEOUT,TUIDING,UNKNOWN,W-BLACK,YY:0206,ZZ:9003,ZZ:9020,ZZ:9021,ERRNUM,NOROUTE,CMPP20ERR:13,CL35RESP:00053,CMPP20ERR:13,CMPP30ERR:13,CMPP30ERR:169,DELIVRD,GG:0024,ID:0013,MA:0021,MBBLACK,MH:18,MI:0010,NOROUTE,PHONERR,REJECT,SME13,SME169,SME20,UNDELIV,CL30:480,CL35RESP:00053,CL35RESP:11006,CL:125,DELIVRD,DISTURB,MA:702,MA:870,MBBLACK,NOROUTE,REJECT,REJECTD,SME13,SME17,SME50,ZZ:9003";
		// 沉默号
		String monum = "56,-37,2:100,2:219,2:40,LT:0-43,LT:0004,LT:0010,LT:0011,LT:0015,LT:0024,REJECT,SGIP:-37,SGIP:-68,SGIP:15,SGIP:57,SGIP:83,IC:0055,ID:0070,MI:0008,MI:0015,MI:0057,2:11,2:29,2:54,LT:0054,SGIP:11,SGIP:50,SGIP:55,SGIP:63,SGIP:89,SGIP:93,DB:0143,MI:0002,MI:0029,MI:0045,MI:0999,MN:0011,MN:0029,CL35RESP:11006,ID:0004,MB:1031,MI:0051,MI:0054,MI:0999,MN:0051,63,SGIP:118,SGIP:90,17,2:17,2:255,93,27,2:92,51,92,93,SGIP:58,SGIP:90,SGIP:92,2:4,4,SGIP:44,SGIP:88,MK:0004,2:56";
		// 空号
		String nullnum = "-37,-74,0005,1,10,11,12,13,15,2,23,27,29,2:1,2:10,2:11,2:117,2:12,2:121,2:13,2:14,2:15,2:18,2:182,2:2,2:213,2:219,2:23,2:24,2:253,2:254,2:27,2:29,2:4,2:41,2:5,2:50,2:51,2:54,2:55,2:59,2:93,2:99,3,36,4,44,5,50,51,54,55,59,63,75,79,9,CMPP20ERR:8,LT:0-37,LT:0-43,LT:0-74,LT:0001,LT:0002,LT:0004,LT:0005,LT:0010,LT:0011,LT:0012,LT:0013,LT:0015,LT:0023,LT:0024,LT:0027,LT:0055,LT:0069,LT:0093,MD:9403,SGIP:-18,SGIP:-2,SGIP:-25,SGIP:-37,SGIP:-4,SGIP:-68,SGIP:-74,SGIP:1,SGIP:10,SGIP:100,SGIP:11,SGIP:117,SGIP:12,SGIP:13,SGIP:14,SGIP:15,SGIP:2,SGIP:21,SGIP:23,SGIP:27,SGIP:28,SGIP:29,SGIP:3,SGIP:30,SGIP:33,SGIP:35,SGIP:38,SGIP:41,SGIP:5,SGIP:50,SGIP:51,SGIP:55,SGIP:56,SGIP:58,SGIP:59,SGIP:73,SGIP:75,SGIP:83,SGIP:84,SGIP:93,SGIP:99,SIGNERR,SME15,SME69,-3,-37,-74,0003,1,10,11,12,13,15,2,24,29,2:1,2:10,2:11,2:12,2:121,2:13,2:14,2:15,2:182,2:2,2:213,2:219,2:231,2:239,2:27,2:29,2:4,2:44,2:5,2:50,2:51,2:52,2:53,2:54,2:93,2:99,3,4,48,5,50,51,54,55,59,88,9,93,LT:0-37,LT:0-43,LT:0-74,LT:00-1,LT:0001,LT:0002,LT:0004,LT:0005,LT:0009,LT:0010,LT:0011,LT:0012,LT:0013,LT:0024,LT:0027,LT:0054,LT:0059,LT:0067,LT:0079,NOPASS,SGIP:-2,SGIP:-37,SGIP:-4,SGIP:-68,SGIP:-74,SGIP:1,SGIP:10,SGIP:106,SGIP:108,SGIP:11,SGIP:117,SGIP:12,SGIP:13,SGIP:14,SGIP:15,SGIP:18,SGIP:2,SGIP:21,SGIP:23,SGIP:28,SGIP:29,SGIP:3,SGIP:31,SGIP:33,SGIP:35,SGIP:37,SGIP:4,SGIP:5,SGIP:54,SGIP:55,SGIP:56,SGIP:58,SGIP:59,SGIP:67,SGIP:75,SGIP:84,SGIP:9,SGIP:93,-2,-74,1,10,11,118,12,13,15,2,23,24,29,2:1,2:10,2:11,2:12,2:121,2:13,2:14,2:15,2:17,2:182,2:2,2:213,2:219,2:254,2:27,2:29,2:4,2:44,2:5,2:50,2:51,2:52,2:54,2:55,2:59,2:61,2:64,2:79,2:93,3,4,48,5,50,51,53,54,55,56,59,63,9,93,CMPP20ERR:8,LT:0-37,LT:0-43,LT:0-74,LT:0001,LT:0003,LT:0004,LT:0005,LT:0009,LT:0010,LT:0011,LT:0012,LT:0013,LT:0023,LT:0024,LT:0027,LT:0040,LT:0048,LT:0054,LT:0055,LT:0059,LT:0067,LT:0086,LT:0117,SGIP:-11,SGIP:-17,SGIP:-25,SGIP:-37,SGIP:-4,SGIP:-68,SGIP:-74,SGIP:1,SGIP:10,SGIP:108,SGIP:11,SGIP:116,SGIP:117,SGIP:118,SGIP:12,SGIP:13,SGIP:15,SGIP:18,SGIP:21,SGIP:23,SGIP:24,SGIP:28,SGIP:29,SGIP:3,SGIP:33,SGIP:35,SGIP:37,SGIP:4,SGIP:40,SGIP:41,SGIP:48,SGIP:5,SGIP:51,SGIP:52,SGIP:53,SGIP:54,SGIP:55,SGIP:56,SGIP:75,SGIP:79,SGIP:83,SGIP:99,SME29,0005,0006,0007,001,006,601,602,640,650,701,702,705,712,760,765,771,801,802,869,899,CMPP20ERR:8,CMPP43,EXPIRED,Err_Num,LT:0023,LT:0061,LT:0101,MA:001,MA:006,MA:010,MA:601,MA:602,MA:617,MA:640,MA:660,MA:680,MA:701,MA:702,MA:705,MA:708,MA:716,MA:760,MA:765,MA:771,MA:779,MA:801,MA:802,MA:817,MA:827,MA:869,MA:875,MA:899,MA:999,NOPASS,SMGP601,SMGP602,SMGP615,SMGP627,SMGP640,SMGP701,SMGP705,SMGP711,SMGP765,SMGP801,SMGP802,SMGP812,SMGP869,UNDELIV,UNKNOWN,0005,0006,0007,1,27,CL35ERR:404,CMPP30ERR:169,DB00141,DB:0107,DB:0143,IB:0072,IC:0001,IC:0055,IC:0151,ID:0012,ID:0013,MC:0001,MH:0004,MI:0004,MI:0005,MI:0010,MI:0011,MI:0013,MI:0029,MI:0036,MI:0038,MI:0041,MI:0054,MI:0055,MI:0056,MI:0059,MI:0063,MI:0075,MI:0083,MI:0084,MI:0999,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0029,MK:0040,MK:0053,MK:0090,MK:0099,MN:0000,MN:0001,MN:0013,MN:0020,MN:0029,MN:0041,MN:0043,MN:0044,MN:0051,MN:0054,MN:0059,PKE:099,REJECT,SME13,SME169,0003,0005,0007,9994,CB:0010,ERRNUM,EXPIRED,IC:0001,IC:0055,IC:0151,ID:0103,MC:000,MC:0001,MC:0055,MI:0000,MI:0001,MI:0002,MI:0004,MI:0005,MI:0010,MI:0011,MI:0012,MI:0013,MI:0024,MI:0029,MI:0036,MI:0038,MI:0041,MI:0054,MI:0055,MI:0056,MI:0059,MI:0063,MI:0081,MI:0083,MI:0084,MI:0089,MI:0090,MI:0660,MK:0000,MK:0001,MK:0002,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0029,MK:0040,MK:0057,MN:0001,MN:0013,MN:0029,MN:0043,MN:0059, E:ODSL,0005,0006,1,CB:0010,EXPIRED,IC:0062,IC:0151,ID:0103,MBBLACK,MC:000,MC:0001,MC:0151,MI:0000,MI:0001,MI:0005,MI:0010,MI:0011,MI:0012,MI:0013,MI:0024,MI:0029,MI:0036,MI:0041,MI:0048,MI:0051,MI:0054,MI:0055,MI:0056,MI:0059,MI:0083,MI:0084,MI:0090,MI:0099,MI:0660,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0024,MK:0029,MK:0041,MN00001,MN:0001,MN:0013,MN:0017,MN:0029,MN:0059,MN:0099,     31, E:ODSL,0006,0007,1,CB:0255,CMPP30ERR:169,EXPIRED,IC:0001,IC:0055,ID:0012,MB:1279,MC:000,MC:0001,MC:0055,MI:0000,MI:0001,MI:0004,MI:0005,MI:0010,MI:0011,MI:0012,MI:0013,MI:0029,MI:0036,MI:0051,MI:0054,MI:0055,MI:0056,MI:0059,MI:0063,MI:0075,MI:0083,MI:0084,MI:0090,MI:0210,MI:0660,MI:0999,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0024,MK:0029,MK:0038,MK:0041,MK:0053,MK:0099,MK:0150,MN00001,MN:0001,MN:0013,MN:0017,MN:0029,MN:0041,MN:0051,MN:0059,MN:0235,REJECTD,SME13,SME169,0007,1,CB:0010,CJ:0007,CMPP30ERR:169,DB:0117,EXPIRED,HD:0003,IC:0001,IC:0062,ID:0012,MB:1042,MC:000,MC:0001,MC:0055,MI:0000,MI:0001,MI:0002,MI:0004,MI:0005,MI:0008,MI:0010,MI:0011,MI:0012,MI:0013,MI:0024,MI:0029,MI:0030,MI:0036,MI:0041,MI:0053,MI:0055,MI:0056,MI:0059,MI:0063,MI:0083,MI:0084,MK:0000,MK:0001,MK:0004,MK:0005,MK:0011,MK:0012,MK:0013,MK:0029,MK:0040,MK:0041,MN00001,MN:0001,MN:0013,MN:0041,MN:0051,MN:0059,MX:0008,NOROUTE,PKE:099,SME13,SME169,  -9234,0006,0007,1,CMPP30ERR:169,IC:0001,IC:0062,IC:0151,ID:0012,ID:1241,MC:000,MC:0001,MH:0004,MI:0000,MI:0001,MI:0004,MI:0005,MI:0010,MI:0011,MI:0012,MI:0013,MI:0029,MI:0036,MI:0041,MI:0051,MI:0054,MI:0055,MI:0059,MI:0063,MI:0075,MI:0081,MI:0083,MI:0084,MI:0210,MI:0660,MI:0999,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0023,MK:0024,MK:0029,MK:0038,MK:0053,MK:0063,MN00001,MN:0001,MN:0017,MN:0029,MN:0041,MN:0044,MN:0053,MN:0059,MN:0235,SME13,SME15,SME169,SWITCH0,CL30RESP:-40,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,NOROUTE,REJECT,SGIP:23,UNDELIV,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,NOROUTE,REJECT,SGIP:23,UNDELIV,CL35RESP:00053,CMPP20ERR:13,ERRNUM,NOROUTE,UNDELIV,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,NOROUTE,REJECT,SGIP:23,UNDELIV,CL35RESP:00053,CMPP20ERR:13,CMPP30ERR:169,DISTURB,ERRNUM,HD:18,NOROUTE,REJECT,SGIP:23,SIGNERR,UNDELIV,-43,-74,0004,0099,1,10,100,101,12,13,2,23,24,29,2:1,2:10,2:101,2:11,2:12,2:13,2:2,2:20,2:23,2:24,2:255,2:29,2:36,2:4,2:45,2:51,2:54,2:55,2:59,2:67,2:93,4,54,67,72,CJ:0007,CJ:0008,CL30:480,DISTURB,LT:0-37,LT:0-74,LT:0001,LT:0002,LT:0005,LT:0012,LT:0017,LT:0022,LT:0023,LT:0027,LT:0040,LT:0054,LT:0059,LT:0061,LT:0064,LT:0067,LT:0069,LT:0092,LT:0093,LT:0100,LT:0101,MBBLACK,MG:0029,NOROUTE,SGIP:-1,SGIP:-43,SGIP:-74,SGIP:1,SGIP:10,SGIP:100,SGIP:101,SGIP:11,SGIP:113,SGIP:12,SGIP:13,SGIP:2,SGIP:22,SGIP:23,SGIP:24,SGIP:27,SGIP:29,SGIP:4,SGIP:5,SGIP:50,SGIP:51,SGIP:53,SGIP:54,SGIP:55,SGIP:56,SGIP:59,SGIP:61,SGIP:67,SGIP:69,SGIP:72,SGIP:75,SGIP:90,SGIP:92,SGIP:93,SME102,SME15,SME50,SME8,SME88,TIMEOUT,UNDELIV,YY:0206,failure,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,REJECT,SGIP:23,UNDELIV,0002,CL32RESP:11011,CL:105,ERR:GJZ,IA:0054,IB:0008,ID:0076,LT:0023,MC:000,MC:0055,MI:0000,MI:0001,MI:0004,MI:0005,MI:0013,MI:0024,MI:0029,MI:0036,MI:0041,MI:0054,MI:0055,MI:0056,MI:0059,MI:0083,MI:0084,MI:0089,MI:0660,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0017,MK:0029,MK:0044,MK:0115,MN:0001,MN:0013,MN:0029,MN:0041,MN:0054,MN:0059,MN:0174,REJECTD,CL35RESP:00053,CMPP20ERR:13,CMPP20ERR:19,CMPP20ERR:20,CMPP30ERR:169,DB:0143,DISTURB,ERRNUM,MB:1078,MH:18,REJECT,UNDELIV,ZD1Resp:104,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,NOROUTE,REJECT,RESP:0004,UNDELIV,  -9223,0005,0006,1,9994,CB:0010,CB:0255,DB:0010,EXPIRED,IC:0151,MC:000,MC:0001,MI:0000,MI:0001,MI:0004,MI:0005,MI:0010,MI:0011,MI:0012,MI:0013,MI:0024,MI:0029,MI:0030,MI:0036,MI:0041,MI:0053,MI:0054,MI:0055,MI:0056,MI:0059,MI:0083,MI:0084,MI:0090,MI:0660,MK:0000,MK:0001,MK:0004,MK:0005,MK:0009,MK:0010,MK:0012,MK:0013,MK:0029,MK:0040,MK:0053,MK:0090,MK:9415,MN:0001,MN:0013,MN:0020,MN:0041,MN:0054,MN:0059,MN:0235,PKE:099,SME8,0005,0006,1,CB:0010,CB:0255,DB:0008,EXPIRED,FAIL_RE,IC:0001,IC:0055,IC:0151,JL:0024,MB:1026,MB:1279,MC:000,MC:0055,MI:0000,MI:0001,MI:0004,MI:0005,MI:0011,MI:0012,MI:0013,MI:0024,MI:0029,MI:0036,MI:0041,MI:0054,MI:0055,MI:0056,MI:0059,MI:0081,MI:0083,MI:0090,MI:0099,MI:0660,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0024,MK:0040,MK:0090,MN00001,MN:0000,MN:0001,MN:0013,MN:0017,MN:0029,MN:0041,MN:0053,MN:0054,MN:0059,      8,0003,0005,0006,0007,1,CB:0010,DB:0309,EXPIRED,FAIL_RE,IA:0073,IC:0001,IC:0055,ID:0103,MB:1042,MC:000,MC:0001,MC:0055,MI:0000,MI:0001,MI:0002,MI:0004,MI:0005,MI:0009,MI:0010,MI:0011,MI:0012,MI:0013,MI:0024,MI:0030,MI:0036,MI:0041,MI:0050,MI:0054,MI:0055,MI:0056,MI:0059,MI:0081,MI:0083,MI:0084,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0024,MK:0029,MK:0036,MK:0040,MK:0041,MK:0050,MK:9441,MN:0001,MN:0013,MN:0020,MN:0029,MN:0041,MN:0050,MN:0051,MN:0053,MN:0059,MN:0174,MN:0235,REPEATD,SME8,0004,0005,0006,001,006,0099,601,602,618,640,701,702,705,708,765,802,869,899,DELETED,ERR_NUM,EXPIRED,Err_Num,LT:0023,MA:001,MA:010,MA:018,MA:601,MA:602,MA:608,MA:617,MA:618,MA:640,MA:660,MA:680,MA:701,MA:702,MA:705,MA:706,MA:708,MA:716,MA:718,MA:760,MA:761,MA:765,MA:771,MA:779,MA:801,MA:802,MA:817,MA:869,MA:875,MA:899,MA:999,NOPASS,SIGNERR,SME15,SMGP601,SMGP602,SMGP640,SMGP660,SMGP680,SMGP701,SMGP702,SMGP705,SMGP765,SMGP801,SMGP802,SMGP869,UNDELIV,CL30RESP:-40,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,LT:0101,NOROUTE,REJECT,SGIP:23,UNDELIV,-2,-37,-43,-74,1,10,11,12,13,27,29,2:1,2:10,2:12,2:13,2:14,2:17,2:182,2:2,2:213,2:231,2:24,2:27,2:29,2:4,2:48,2:5,2:50,2:51,2:52,2:54,2:55,2:59,2:64,2:73,2:75,2:93,36,4,5,50,51,54,55,59,63,9,93,LT:0-37,LT:0-74,LT:0001,LT:0002,LT:0003,LT:0004,LT:0005,LT:0010,LT:0011,LT:0012,LT:0013,LT:0024,LT:0040,LT:0048,LT:0055,LT:0067,LT:0079,LT:0093,MA:0006,SGIP:-25,SGIP:-37,SGIP:-68,SGIP:-74,SGIP:0,SGIP:1,SGIP:10,SGIP:106,SGIP:108,SGIP:11,SGIP:12,SGIP:13,SGIP:15,SGIP:18,SGIP:2,SGIP:21,SGIP:23,SGIP:24,SGIP:27,SGIP:28,SGIP:33,SGIP:35,SGIP:37,SGIP:4,SGIP:40,SGIP:43,SGIP:52,SGIP:54,SGIP:55,SGIP:56,SGIP:58,SGIP:59,SGIP:63,SGIP:72,SGIP:73,SGIP:75,SGIP:93,-37,-74,0006,1,10,106,11,12,13,2,23,24,29,2:1,2:106,2:11,2:12,2:13,2:14,2:15,2:182,2:213,2:219,2:23,2:254,2:29,2:4,2:5,2:51,2:53,2:54,2:55,2:59,2:63,2:64,2:93,3,4,40,48,5,51,54,55,59,67,73,92,CL30RESP:-42,CL:106,LT:0-37,LT:0-43,LT:0-74,LT:0001,LT:0002,LT:0004,LT:0005,LT:0009,LT:0010,LT:0011,LT:0012,LT:0013,LT:0023,LT:0027,LT:0040,LT:0054,LT:0055,LT:0059,LT:0069,LT:0079,LT:0093,SGIP:-11,SGIP:-17,SGIP:-25,SGIP:-37,SGIP:-4,SGIP:-68,SGIP:-74,SGIP:1,SGIP:10,SGIP:108,SGIP:11,SGIP:118,SGIP:12,SGIP:13,SGIP:15,SGIP:21,SGIP:23,SGIP:28,SGIP:29,SGIP:3,SGIP:33,SGIP:35,SGIP:37,SGIP:38,SGIP:4,SGIP:41,SGIP:48,SGIP:5,SGIP:50,SGIP:52,SGIP:53,SGIP:54,SGIP:55,SGIP:56,SGIP:58,SGIP:75,SGIP:83,SGIP:9,SGIP:93,SME15,0004,0005,0006,0007,00112,0099,1,BEYONDN,CB:0255,CL35ERROR,CMPP30ERR:169,DB:0090,IC:0055,IC:0062,ID:0070,MA:0051,MB:1042,MC:000,MC:0055,MC:0151,MI:0000,MI:0001,MI:0004,MI:0005,MI:0010,MI:0011,MI:0012,MI:0013,MI:0023,MI:0024,MI:0029,MI:0036,MI:0038,MI:0041,MI:0050,MI:0051,MI:0053,MI:0054,MI:0055,MI:0056,MI:0059,MI:0063,MI:0083,MI:0084,MI:0999,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0029,MK:0041,MK:0090,MK:9403,MN00001,MN:0000,MN:0001,MN:0013,MN:0029,MN:0041,MN:0051,MN:0059,MN:0174,YX:8019,0005,0006,1,CL35ERR:404,EXPIRED,FAIL_RE,IC:0001,IC:0055,IC:0151,MC:0055,MC:0151,MI:0000,MI:0001,MI:0004,MI:0005,MI:0010,MI:0011,MI:0012,MI:0013,MI:0024,MI:0030,MI:0038,MI:0048,MI:0053,MI:0055,MI:0056,MI:0059,MI:0080,MI:0083,MI:0090,MI:0660,MI:0999,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0024,MK:0041,MK:0053,MN00001,MN:0001,MN:0013,MN:0029,MN:0038,MN:0050,MN:0053,MN:0054,MN:0059,PKE:099,0005,0006,0007,1,12,CB:0010,CB:0255,DB:0117,DB:0160,F0081,IB:0008,IC:0001,IC:0055,IC:0151,MB:1042,MC:000,MC:0001,MC:0055,MI:0000,MI:0001,MI:0002,MI:0004,MI:0005,MI:0010,MI:0011,MI:0012,MI:0013,MI:0024,MI:0036,MI:0038,MI:0041,MI:0054,MI:0055,MI:0056,MI:0059,MI:0083,MI:0084,MI:0089,MI:0090,MI:0099,MI:0660,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0023,MK:0024,MK:0043,MK:0090,MK:0099,MN00001,MN:0001,MN:0013,MN:0029,MN:0041,MN:0054,MN:0059,CL30RESP:-40,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,NOROUTE,SGIP:23,UNDELIV,CL30RESP:-40,CMPP20ERR:13,ERRNUM,NOROUTE,SGIP:23,UNDELIV,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,REJECT,SGIP:23,UNDELIV,CMPP20ERR:13,DISTURB,ERRNUM,MBBLACK,NOROUTE,REJECT,SGIP:23,UNDELIV,CL30RESP:-40,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,NOROUTE,SGIP:23,UNDELIV,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,NOROUTE,REJECT,SGIP:23,UNDELIV,1,12,23,2:23,69,CJ:0007,CJ:0008,CL35RESP:00053,LT:0-98,LT:0001,LT:0023,NOROUTE,REJECT,SGIP:-43,SGIP:-98,SGIP:1,SGIP:10,SGIP:101,SGIP:12,SGIP:23,SGIP:24,SGIP:27,SGIP:29,SGIP:54,SGIP:59,SGIP:69,SGIP:75,SGIP:93,SME123,SME29,UNDELIV,failure,CL35RESP:00053,CMPP20ERR:13,ERRNUM,NOROUTE,RESP:0004,SGIP:23,SIGNERR,UNDELIV,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,NOROUTE,REJECT,RESP:0004,SGIP:23,TIMEOUT,UNDELIV,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,NOROUTE,REJECT,RESP:0004,SGIP:23,UNDELIV,601,705,802,869,CANCEL,CMPP20ERR:8,DISTURB,EXPIRED,ErrArea,Err_Num,F0032,GG:0024,IC:0015,MA:601,MA:602,MA:640,MA:650,MA:660,MA:680,MA:701,MA:702,MA:705,MA:760,MA:765,MA:771,MA:801,MA:802,MA:869,MA:899,MBBLACK,MC:0055,REJECT,REJECTD,SME15,SME17,SMGP601,SMGP705,SMGP802,UNDELIV,ZZ:9003,601,640,802,CMPP20ERR:8,DISTURB,EXPIRED,ErrArea,Err_Num,F0072,IC:0015,MA:601,MA:602,MA:640,MA:680,MA:701,MA:702,MA:705,MA:760,MA:765,MA:801,MA:802,MA:869,MA:899,MBBLACK,NOROUTE,REJECT,SME15,SME17,SME50,SME92,SMGP601,SMGP640,SMGP705,UNDELIV,DISTURB,EXPIRED,Err_Num,IC:0015,MA:601,MA:602,MA:640,MA:660,MA:680,MA:701,MA:702,MA:705,MA:760,MA:765,MA:801,MA:802,MA:869,MA:899,NOROUTE,REJECT,REJECTD,SME17,SME50,SMGP601,SMGP602,SMGP640,UNDELIV,ZZ:9003,0005,0006,0099,CJ:0007,CJ:0008,CL30:480,CMPP20ERR:19,CMPP20ERR:20,DB:0108,DB:0119,DB:0143,DB:0309,DISTURB,E:RPTSS,GG:0024,HD:0001,HD:0008,IA:0051,IA:0054,IA:0073,IB:0008,IC:0001,IC:0151,ID:0013,JT:105,LIMIT,MA:0051,MB:1078,MC:0001,MC:0055,MC:0151,MI:0000,MI:0001,MI:0004,MI:0005,MI:0011,MI:0013,MI:0020,MI:0024,MI:0029,MI:0051,MI:0054,MI:0055,MI:0083,MI:0084,MI:0099,MK:0000,MK:0001,MK:0004,MK:0005,MK:0008,MK:0010,MK:0011,MK:0012,MK:0013,MK:0015,MK:0024,MK:0029,MK:0036,MK:0038,MK:0045,MK:0090,MK:9415,MN:0001,MN:0017,MN:0022,MN:0029,MN:0051,MN:0054,MN:0059,NOROUTE,REJECT,SIGFAIL,SME20,SME8,SME92,SWITCH0,TIMEOUT,UNDELIV,-1,1,10,11,12,17,24,29,2:1,2:10,2:12,2:13,2:17,2:255,2:36,2:4,2:45,2:5,2:51,2:55,4,5,51,54,DISTURB,LT:0001,LT:0005,LT:0010,LT:0011,LT:0012,LT:0024,LT:0061,LT:0064,MD:9020,NOROUTE,REJECT,SGIP:-1,SGIP:-43,SGIP:0,SGIP:1,SGIP:10,SGIP:113,SGIP:12,SGIP:13,SGIP:15,SGIP:17,SGIP:2,SGIP:20,SGIP:24,SGIP:29,SGIP:4,SGIP:45,SGIP:5,SGIP:51,SGIP:54,SGIP:57,SGIP:59,SGIP:61,SGIP:69,SGIP:8,SGIP:98,TIMEOUT,UNDELIV,failure,0005,0006,0007,1,CJ:0007,CJ:0008,CL30:480,CMPP20ERR:19,CMPP20ERR:20,DB:0309,DISTURB,GG:0024,HD:0007,HD:0008,HD:29,HD:31,IA:0051,IA:0054,IB:0008,IC:0001,IC:0055,IC:0151,ID:0004,LT:0101,MC:000,MC:0001,MC:0055,MC:0151,MI:0000,MI:0001,MI:0004,MI:0005,MI:0008,MI:0010,MI:0011,MI:0012,MI:0013,MI:0015,MI:0020,MI:0022,MI:0024,MI:0036,MI:0043,MI:0050,MI:0051,MI:0053,MI:0054,MI:0055,MI:0083,MI:0084,MK:0000,MK:0001,MK:0004,MK:0005,MK:0008,MK:0010,MK:0011,MK:0012,MK:0013,MK:0015,MK:0020,MK:0024,MK:0029,MK:0045,MK:0053,MK:9403,MN:0001,MN:0013,MN:0017,MN:0051,MN:0053,MN:0054,MN:0055,MN:0059,NOROUTE,REJECT,REJECTD,SIGFAIL,SME41,UNDELIV,YY:0206,    102,0002,1,CJ:0007,CJ:0008,CL30:480,CL32RESP:11011,CMPP20ERR:19,CMPP20ERR:20,DB:0119,DB:0143,DISTURB,F0072,HD:0008,HD:31,IA:0051,IB:0008,IC:0001,IC:0055,IC:0151,MC:000,MC:0055,MC:0151,MI:0000,MI:0002,MI:0004,MI:0005,MI:0008,MI:0011,MI:0012,MI:0013,MI:0015,MI:0020,MI:0024,MI:0029,MI:0045,MI:0050,MI:0055,MI:0083,MI:0084,MK:0000,MK:0001,MK:0004,MK:0005,MK:0008,MK:0010,MK:0011,MK:0012,MK:0013,MK:0024,MK:0029,MK:0053,MK:9415,MN:0001,MN:0011,MN:0012,MN:0013,MN:0022,MN:0029,MN:0036,MN:0050,MN:0054,MN:0059,NOROUTE,REJECT,SME19,SME20,SME41,SME8,SME92,TIMEOUT,UNDELIV,YY:0206,-37,-74,-75,0099,1,10,11,12,13,15,24,27,29,2:1,2:10,2:11,2:12,2:13,2:15,2:182,2:2,2:219,2:24,2:255,2:29,2:36,2:4,2:40,2:45,2:5,2:50,2:51,2:53,2:54,2:59,2:89,2:93,4,5,50,53,54,55,59,93,CANCEL,CJ:0008,CL30:480,CMPP20ERR:8,DISTURB,LT:0-37,LT:0-74,LT:00-1,LT:0001,LT:0002,LT:0004,LT:0005,LT:0010,LT:0011,LT:0012,LT:0013,LT:0015,LT:0017,LT:0024,LT:0027,LT:0040,LT:0054,LT:0055,LT:0059,LT:0061,LT:0064,LT:0093,MG:0015,NOROUTE,SGIP:-17,SGIP:-37,SGIP:-4,SGIP:-43,SGIP:-74,SGIP:-75,SGIP:0,SGIP:1,SGIP:10,SGIP:11,SGIP:117,SGIP:12,SGIP:13,SGIP:14,SGIP:15,SGIP:2,SGIP:20,SGIP:22,SGIP:24,SGIP:27,SGIP:29,SGIP:3,SGIP:4,SGIP:45,SGIP:5,SGIP:50,SGIP:51,SGIP:53,SGIP:55,SGIP:57,SGIP:59,SGIP:61,SGIP:63,SGIP:8,SGIP:82,SGIP:83,SGIP:86,SGIP:88,SGIP:89,SGIP:93,SME103,SME88,TIMEOUT,W-BLACK,YY:0206,-1,-37,-43,-74,-75,1,10,11,12,13,15,24,27,29,2:1,2:10,2:11,2:12,2:13,2:15,2:181,2:182,2:219,2:24,2:27,2:29,2:36,2:45,2:5,2:50,2:51,2:53,2:54,2:55,2:59,2:86,2:93,4,5,51,54,55,59,63,90,CJ:0007,CJ:0008,CL30:480,CL35RESP:11006,CL:125,CMPP20ERR:8,LT:0-37,LT:0-43,LT:0-74,LT:00-1,LT:0001,LT:0004,LT:0005,LT:0010,LT:0011,LT:0012,LT:0013,LT:0015,LT:0024,LT:0027,LT:0040,LT:0054,LT:0055,LT:0057,LT:0059,LT:0061,LT:0064,LT:0090,LT:0093,MD:9419,MG:0015,NOROUTE,REJECT,SGIP:-1,SGIP:-17,SGIP:-3,SGIP:-37,SGIP:-4,SGIP:-43,SGIP:-74,SGIP:-75,SGIP:1,SGIP:10,SGIP:100,SGIP:11,SGIP:118,SGIP:12,SGIP:13,SGIP:14,SGIP:15,SGIP:18,SGIP:19,SGIP:20,SGIP:23,SGIP:24,SGIP:27,SGIP:29,SGIP:31,SGIP:40,SGIP:45,SGIP:5,SGIP:50,SGIP:51,SGIP:53,SGIP:54,SGIP:55,SGIP:57,SGIP:58,SGIP:59,SGIP:61,SGIP:67,SGIP:8,SGIP:82,SGIP:89,SGIP:90,SGIP:93,SME103,SME92,TIMEOUT,YY:0206,-37,-74,0099,1,10,11,12,13,15,17,22,23,24,29,2:1,2:10,2:12,2:13,2:15,2:181,2:182,2:219,2:23,2:24,2:255,2:27,2:29,2:5,2:50,2:51,2:53,2:54,2:55,2:59,2:86,2:93,5,54,55,59,63,88,CL30:480,CL:125,CMPP20ERR:8,DISTURB,LT:0-37,LT:0-43,LT:0-74,LT:0001,LT:0005,LT:0011,LT:0012,LT:0013,LT:0015,LT:0024,LT:0027,LT:0040,LT:0054,LT:0055,LT:0059,LT:0064,LT:0093,MG:0015,NOROUTE,SGIP:-17,SGIP:-3,SGIP:-37,SGIP:-4,SGIP:-43,SGIP:-74,SGIP:-75,SGIP:1,SGIP:10,SGIP:11,SGIP:118,SGIP:12,SGIP:13,SGIP:14,SGIP:15,SGIP:18,SGIP:2,SGIP:20,SGIP:23,SGIP:24,SGIP:27,SGIP:29,SGIP:38,SGIP:4,SGIP:44,SGIP:45,SGIP:5,SGIP:50,SGIP:51,SGIP:54,SGIP:55,SGIP:57,SGIP:59,SGIP:61,SGIP:64,SGIP:75,SGIP:77,SGIP:8,SGIP:86,SGIP:89,SGIP:93,SGIP:98,SME103,SME15,SME8,TIMEOUT,UNDELIV,-1,-37,-43,-74,0004,0099,1,10,11,12,13,15,17,2,23,24,27,29,2:1,2:10,2:11,2:12,2:13,2:15,2:17,2:181,2:182,2:219,2:23,2:24,2:255,2:27,2:29,2:40,2:45,2:5,2:50,2:51,2:53,2:54,2:55,2:59,2:69,2:89,2:93,5,51,54,55,59,8,93,CANCEL,CJ:0007,CJ:0008,CL30:480,CL35ERR:502,CL35RESP:11006,CL:116,GG:0024,LT:0-37,LT:0-74,LT:00-1,LT:0001,LT:0002,LT:0004,LT:0005,LT:0010,LT:0011,LT:0012,LT:0013,LT:0015,LT:0024,LT:0027,LT:0040,LT:0054,LT:0055,LT:0059,LT:0061,LT:0064,LT:0093,REJECT,SGIP:-37,SGIP:-4,SGIP:-43,SGIP:-74,SGIP:-75,SGIP:1,SGIP:10,SGIP:11,SGIP:118,SGIP:12,SGIP:13,SGIP:14,SGIP:15,SGIP:17,SGIP:18,SGIP:19,SGIP:2,SGIP:20,SGIP:23,SGIP:24,SGIP:27,SGIP:29,SGIP:36,SGIP:4,SGIP:40,SGIP:45,SGIP:5,SGIP:50,SGIP:51,SGIP:52,SGIP:53,SGIP:54,SGIP:55,SGIP:56,SGIP:57,SGIP:59,SGIP:61,SGIP:63,SGIP:64,SGIP:69,SGIP:72,SGIP:75,SGIP:77,SGIP:8,SGIP:89,SGIP:90,SGIP:93,SGIP:98,SME15,SME8,SME88,TIMEOUT,TUIDING,YY:0206,MK:0012,0003,0005,0006,0007,001,006,601,602,640,701,702,705,760,765,802,899,CL35RESP:11006,CMPP43,EXPIRED,Err_Num,F0072,F0170,IC:0015,MA:001,MA:006,MA:010,MA:018,MA:174,MA:601,MA:602,MA:617,MA:627,MA:634,MA:640,MA:660,MA:701,MA:702,MA:705,MA:706,MA:721,MA:760,MA:761,MA:765,MA:771,MA:801,MA:802,MA:815,MA:869,MA:899,MA:999,MD:9015,SME15,SMGP601,SMGP614,SMGP622,SMGP640,SMGP701,SMGP702,SMGP705,SMGP765,SMGP771,SMGP801,SMGP802,SMGP812,SMGP869,UNDELIV,YY:0206,CMPP20ERR:13,DISTURB,ERRNUM,REJECT,SGIP:23,UNDELIV,-37,-74,0006,0099,1,10,11,12,13,15,2,23,24,27,29,2:1,2:10,2:11,2:12,2:13,2:15,2:17,2:182,2:219,2:23,2:24,2:27,2:29,2:4,2:5,2:51,2:54,2:55,2:59,2:61,2:69,2:93,4,5,54,55,57,59,8,93,CJ:0008,CMPP20ERR:8,DISTURB,LT:0-37,LT:0-74,LT:00-1,LT:0001,LT:0004,LT:0005,LT:0010,LT:0011,LT:0012,LT:0013,LT:0015,LT:0023,LT:0024,LT:0027,LT:0054,LT:0055,LT:0059,LT:0061,LT:0064,LT:0069,LT:0093,SGIP:-37,SGIP:-74,SGIP:1,SGIP:10,SGIP:100,SGIP:11,SGIP:12,SGIP:13,SGIP:15,SGIP:18,SGIP:23,SGIP:24,SGIP:28,SGIP:29,SGIP:36,SGIP:4,SGIP:5,SGIP:50,SGIP:51,SGIP:53,SGIP:54,SGIP:55,SGIP:56,SGIP:59,SGIP:61,SGIP:69,SGIP:75,SGIP:82,SGIP:92,SGIP:93,SME103,SME15,SME8,TIMEOUT,-37,-74,0003,1,10,11,12,13,15,23,24,27,29,2:1,2:10,2:11,2:12,2:13,2:14,2:17,2:18,2:182,2:19,2:21,2:213,2:219,2:22,2:23,2:24,2:27,2:29,2:4,2:5,2:50,2:51,2:54,2:55,2:59,2:61,2:69,2:72,2:83,2:93,4,5,51,54,55,56,59,69,90,93,LT:0-37,LT:0-74,LT:00-1,LT:0001,LT:0002,LT:0004,LT:0005,LT:0009,LT:0010,LT:0011,LT:0012,LT:0013,LT:0023,LT:0024,LT:0027,LT:0054,LT:0055,LT:0059,LT:0064,LT:0069,LT:0079,LT:0093,SGIP:-37,SGIP:-4,SGIP:-74,SGIP:1,SGIP:10,SGIP:11,SGIP:117,SGIP:12,SGIP:13,SGIP:14,SGIP:15,SGIP:17,SGIP:18,SGIP:21,SGIP:23,SGIP:24,SGIP:27,SGIP:28,SGIP:29,SGIP:3,SGIP:33,SGIP:37,SGIP:4,SGIP:40,SGIP:43,SGIP:48,SGIP:5,SGIP:50,SGIP:51,SGIP:52,SGIP:54,SGIP:55,SGIP:56,SGIP:59,SGIP:64,SGIP:69,SGIP:72,SGIP:75,SGIP:84,SGIP:93,SGIP:96,SME15,0005,0006,0007,001,006,601,602,608,617,640,660,701,702,705,760,765,802,869,899,CMPP43,EXPIRED,ErrArea,Err_Num,F0072,MA:001,MA:006,MA:010,MA:018,MA:163,MA:601,MA:602,MA:608,MA:612,MA:618,MA:627,MA:640,MA:660,MA:680,MA:701,MA:702,MA:705,MA:716,MA:731,MA:760,MA:765,MA:771,MA:801,MA:802,MA:817,MA:869,MA:879,MA:899,MA:999,SMGP601,SMGP602,SMGP613,SMGP615,SMGP620,SMGP627,SMGP640,SMGP680,SMGP701,SMGP705,SMGP726,SMGP765,SMGP771,SMGP801,SMGP802,SMGP869,UNDELIV,0006,1,DB:0008,DB:0090,F0170,FAIL_RE,IB:0009,IB:0072,IC:0001,IC:0055,MB:1042,MC:000,MC:0001,MC:0055,MF:9441,MI:0000,MI:0002,MI:0004,MI:0005,MI:0008,MI:0011,MI:0012,MI:0013,MI:0024,MI:0029,MI:0036,MI:0043,MI:0053,MI:0054,MI:0055,MI:0056,MI:0059,MI:0063,MI:0078,MI:0083,MI:0084,MI:0660,MI:0999,MK:0000,MK:0001,MK:0002,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0029,MK:0066,MK:9441,MN:0001,MN:0013,MN:0017,MN:0022,MN:0029,MN:0041,MN:0050,MN:0051,MN:0054,MN:0059,MN:0083,MX:0008,NOROUTE,REJECTD,SME15,CL30RESP:-40,CMPP20ERR:13,DISTURB,ERRNUM,NOROUTE,REJECT,RESP:0004,SGIP:23,UNDELIV,0005,0006,001,006,601,602,617,640,660,701,702,705,712,714,760,765,771,802,815,869,899,CL:106,CMPP43,EXPIRED,Err_Num,LT:0023,LT:0101,MA:001,MA:010,MA:018,MA:601,MA:602,MA:622,MA:640,MA:650,MA:660,MA:680,MA:701,MA:702,MA:705,MA:716,MA:717,MA:760,MA:765,MA:771,MA:801,MA:802,MA:818,MA:836,MA:869,MA:875,MA:899,MA:999,REJECTD,SME134,SMGP601,SMGP602,SMGP640,SMGP660,SMGP680,SMGP701,SMGP705,SMGP718,SMGP765,SMGP771,SMGP801,SMGP802,SMGP817,SMGP869,SMGP899,UNDELIV,0004,0005,0007,001,006,010,601,602,612,618,640,660,701,702,705,760,765,771,802,899,CL35ERR:404,CMPP20ERR:8,EXPIRED,Err_Num,MA:001,MA:010,MA:018,MA:601,MA:602,MA:617,MA:618,MA:640,MA:660,MA:680,MA:701,MA:702,MA:705,MA:716,MA:721,MA:760,MA:765,MA:771,MA:801,MA:802,MA:827,MA:836,MA:869,MA:875,MA:899,MA:999,REJECTD,SME134,SME9,SMGP001,SMGP601,SMGP602,SMGP627,SMGP640,SMGP650,SMGP660,SMGP680,SMGP701,SMGP705,SMGP713,SMGP765,SMGP771,SMGP801,SMGP802,SMGP869,UNDELIV,W-BLACK,YY:0206,0006,0007,1,CL35ERR:404,DB:0107,EXPIRED,F0081,IC:0001,IC:0055,MB:1042,MC:000,MC:0001,MI:0000,MI:0001,MI:0004,MI:0005,MI:0012,MI:0013,MI:0024,MI:0029,MI:0036,MI:0038,MI:0041,MI:0054,MI:0056,MI:0059,MI:0080,MI:0083,MI:0084,MI:0089,MI:0099,MI:0660,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0019,MK:0024,MK:0053,MN:0001,MN:0013,MN:0029,MN:0038,MN:0041,MN:0059,MN:0084,SME10,0005,0006,00112,1,CB:0010,CB:0255,CL30RESP:-42,CL35ERR:404,DB:0090,IB:0182,IC:0001,IC:0055,IC:0151,MC:000,MC:0001,MC:0055,MC:0151,MF:9441,MI:0001,MI:0004,MI:0005,MI:0010,MI:0012,MI:0013,MI:0024,MI:0029,MI:0036,MI:0038,MI:0041,MI:0050,MI:0051,MI:0054,MI:0055,MI:0059,MI:0080,MI:0083,MI:0084,MI:0089,MK:0000,MK:0001,MK:0002,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0041,MK:0090,MK:9441,MN00001,MN:0001,MN:0013,MN:0017,MN:0029,MN:0036,MN:0041,MN:0054,MN:0059,MN:0139,MO:0255,YX:1006,YX:8019,0007,1,CB:0010,DB:0143,F007,IC:0001,IC:0055,IC:0151,ID:0012,ID:0070,JL:0024,LT:0101,MA:0051,MC:000,MC:0001,MI:0000,MI:0001,MI:0002,MI:0004,MI:0005,MI:0012,MI:0013,MI:0024,MI:0030,MI:0036,MI:0041,MI:0050,MI:0054,MI:0055,MI:0056,MI:0059,MI:0080,MI:0081,MI:0083,MI:0084,MI:0089,MK:0000,MK:0001,MK:0004,MK:0005,MK:0008,MK:0012,MK:0013,MK:0024,MK:0034,MK:0041,MK:0045,MK:0055,MK:0057,MK:0063,MN:0000,MN:0001,MN:0013,MN:0017,MN:0041,MN:0050,MN:0054,MN:0059,MX:0002,NOWAY00,SIGFAIL,SME20,-2,-37,-74,0002,0003,0006,1,10,106,11,12,13,23,24,27,29,2:1,2:10,2:11,2:118,2:12,2:13,2:14,2:182,2:2,2:213,2:23,2:24,2:254,2:27,2:29,2:4,2:5,2:50,2:51,2:53,2:54,2:59,2:64,2:69,2:83,2:93,4,5,50,51,53,54,55,57,59,69,9,93,98,LT:0-37,LT:0-74,LT:0001,LT:0004,LT:0005,LT:0011,LT:0012,LT:0013,LT:0015,LT:0023,LT:0024,LT:0040,LT:0048,LT:0054,LT:0055,LT:0059,LT:0061,LT:0064,LT:0067,LT:0069,LT:0093,SGIP:-2,SGIP:-37,SGIP:-4,SGIP:-74,SGIP:1,SGIP:10,SGIP:11,SGIP:12,SGIP:121,SGIP:13,SGIP:18,SGIP:2,SGIP:23,SGIP:28,SGIP:29,SGIP:33,SGIP:37,SGIP:4,SGIP:41,SGIP:5,SGIP:50,SGIP:51,SGIP:52,SGIP:54,SGIP:55,SGIP:56,SGIP:59,SGIP:63,SGIP:69,SGIP:77,SGIP:83,SGIP:84,SGIP:93,SGIP:99,SME15,SME29,-25,-37,-74,0005,0006,0007,1,11,12,13,14,2,23,24,27,29,2:1,2:10,2:11,2:118,2:12,2:121,2:13,2:14,2:18,2:181,2:182,2:213,2:219,2:23,2:239,2:29,2:4,2:5,2:50,2:51,2:53,2:54,2:55,2:59,2:63,2:64,2:93,2:99,36,4,5,51,54,55,59,63,93,CL30RESP:-42,ERRNUM,LT:0-37,LT:0-43,LT:0-74,LT:0001,LT:0004,LT:0005,LT:0009,LT:0010,LT:0011,LT:0012,LT:0013,LT:0015,LT:0040,LT:0054,LT:0055,LT:0059,LT:0064,LT:0067,LT:0069,LT:0093,SGIP:-25,SGIP:-4,SGIP:-68,SGIP:-74,SGIP:-75,SGIP:1,SGIP:10,SGIP:11,SGIP:12,SGIP:13,SGIP:18,SGIP:21,SGIP:23,SGIP:24,SGIP:27,SGIP:28,SGIP:29,SGIP:33,SGIP:37,SGIP:4,SGIP:5,SGIP:51,SGIP:53,SGIP:54,SGIP:55,SGIP:56,SGIP:59,SGIP:64,SGIP:83,SGIP:93,SGIP:99,SME102,failure,0003,0005,0006,1,CB:0010,CB:0255,DB:0318,ERRNUM,EXPIRED,IA:0053,IC:0001,IC:0055,IC:0151,MB:1042,MC:000,MC:0001,MC:0151,MI:0000,MI:0001,MI:0004,MI:0005,MI:0010,MI:0012,MI:0013,MI:0024,MI:0030,MI:0038,MI:0041,MI:0043,MI:0048,MI:0054,MI:0055,MI:0056,MI:0059,MI:0081,MI:0083,MI:0084,MI:0660,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0024,MK:0040,MK:0041,MK:0057,MK:0090,MN00001,MN:0001,MN:0009,MN:0013,MN:0017,MN:0029,MN:0041,MN:0054,MN:0055,MN:0059,PKE:099,R:00601,WZ:9403,-1013,0003,0006,0007,1,CB:0010,CB:0255,DB:0107,DB:0318,EXPIRED,F0081,IC:0001,IC:0055,MC:000,MC:0001,MI:0000,MI:0001,MI:0002,MI:0004,MI:0005,MI:0011,MI:0012,MI:0013,MI:0024,MI:0030,MI:0041,MI:0051,MI:0053,MI:0054,MI:0055,MI:0056,MI:0059,MI:0083,MI:0084,MI:0089,MI:0099,MI:0999,MK:0000,MK:0001,MK:0004,MK:0005,MK:0010,MK:0012,MK:0013,MK:0041,MK:0063,MK:0090,MK:9415,MN00001,MN:0001,MN:0009,MN:0013,MN:0017,MN:0019,MN:0029,MN:0036,MN:0041,MN:0051,MN:0054,MN:0059,MN:0090,PKE:099,SIGNERR,0006,001,006,601,640,660,702,705,716,760,765,771,802,827,869,899,901,CMPP20ERR:8,EXPIRED,Err_Num,MA:001,MA:006,MA:010,MA:018,MA:601,MA:608,MA:617,MA:624,MA:627,MA:640,MA:650,MA:660,MA:680,MA:701,MA:702,MA:704,MA:705,MA:708,MA:712,MA:716,MA:760,MA:765,MA:771,MA:779,MA:801,MA:802,MA:817,MA:869,MA:875,MA:899,MA:999,NOPASS,SMGP601,SMGP602,SMGP640,SMGP660,SMGP680,SMGP701,SMGP702,SMGP705,SMGP765,SMGP801,SMGP802,SMGP813,SMGP869,UNDELIV,ZZ:9253,CL35RESP:00053,CMPP20ERR:13,DISTURB,UNDELIV,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,REJECT,UNDELIV,CMPP20ERR:13,DISTURB,ERRNUM,SGIP:23,UNDELIV,CL35RESP:00053,CMPP20ERR:13,ERRNUM,NOROUTE,REJECT,SGIP:23,UNDELIV,CMPP20ERR:13,ERRNUM,NOROUTE,CL30RESP:-40,CL35RESP:00053,CMPP20ERR:13,DISTURB,ERRNUM,MBBLACK,NOROUTE,REJECT,RESP:0004,SGIP:23,UNDELIV,CL35RESP:00053,DISTURB,ERRNUM,REJECT,SGIP:23,UNDELIV,CL35RESP:00053,CMPP20ERR:13,ERRNUM,NOROUTE,REJECT,UNDELIV,DISTURB,IB:0009,IB:0013,IC:0055,MB:0011,MB:1078,MC:0151,MI:0075,MK:0000,MK:0001,MK:0010,MK:0012,MK:0075,MN:0075,REJECTD,601,705,EXPIRED,Err_Num,IC:0015,MA:601,MA:602,MA:701,MA:705,MA:765,MA:802,MA:869,MA:899,SIGNERR,SME134,UNDELIV";
		
		String[] realdelivrds = realnum.split(",");
		for (String string : realdelivrds) {
			if (string.equals(delivrd)) {
				return "real";
			}
		}
		
		String[] pausedelivrds = monum.split(",");
		for (String string : pausedelivrds) {
			if (string.equals(delivrd)) {
				return "pause";
			}
		}
		
		String[] nulldelivrds = nullnum.split(",");
		
		for (String string : nulldelivrds) {
			if (string.equals(delivrd)) {
				return "kong";
			}
		}
		
		return "unkown";
	}
	
	public String isSpaceMobile3(String mobile) {
		
		
		
		
		
		return null;
	}
	
	
	public static void main(String[] args) throws IOException {
		System.out.println(FileUtils.getFileMenu("D:\\test\\mk000164.txt", 1000, 10000));
	}
}
