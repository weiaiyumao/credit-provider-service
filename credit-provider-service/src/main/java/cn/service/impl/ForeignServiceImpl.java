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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import cn.entity.CvsFilePath;
import cn.entity.FileUpload;
import cn.entity.MobileNumberSection;
import cn.entity.WaterConsumption;
import cn.entity.base.BaseMobileDetail;
import cn.entity.base.MobileSort;
import cn.redis.DistributedLock;
import cn.redis.ObjectSer;
import cn.redis.RedisClient;
import cn.service.CvsFilePathService;
import cn.service.FileUploadService;
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

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

	@Autowired
	private FileUploadService fileUploadService;

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

	public BackResult<RunTestDomian> theTest4(String fileUrl, String userId, String mobile, String source,
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
										String status = MobileDetailHelper.getInstance().getMobileStatus(lineTxt,
												detail.getDelivrd());
										if (status.equals("real")) {
											// 实号
											thereRowList = new ArrayList<Object>();
											thereRowList.add(lineTxt);
											thereDataList.add(thereRowList);
										} else if (status.equals("kong")) {
											// 空号
											Map<String, Object> sixRowList = new HashMap<>();
											sixRowList.put("mobile", lineTxt);
											sixRowList.put("delivd", 1);// 空号状态
											sixRowList.put("reportTime", detail.getReportTime().getTime());
											sixDataList.add(sixRowList);
										} else if (status.equals("silence")) {
											// 沉默号
											unKonwRowList = new ArrayList<Object>();
											unKonwRowList.add(lineTxt);
											unKonwDataList.add(unKonwRowList);
										} else {
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

							if (CommonUtils.isNotString(cvsFilePath.getThereFilePath())
									&& CommonUtils.isNotString(cvsFilePath.getSixFilePath())
									&& CommonUtils.isNotString(cvsFilePath.getUnknownFilePath())) {
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
								runTestDomian.setStatus("3"); // 1执行中 2执行结束 //
																// 3执行异常
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
		return null;
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
	 * 清空条数注销锁
	 * 
	 * @param lock
	 * @param userId
	 * @param mobile
	 */
	private void clearLockAndCountForRun(String userId, String mobile) {
		String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile);
		String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId);
		String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId);
		String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId);
		String identifier = redisClient.get(redisLockIdentifier);
		String generateResultskey = RedisKeys.getInstance().getkhGenerateResultskey(userId); // 空号检测线程key
		String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId); // 线程执行全局异常key
		// 清空 记录到redis的条数
		redisClient.remove(KhTestCountKey);
		redisClient.remove(succeedTestCountkey);
		redisClient.remove(generateResultskey);
		redisClient.remove(exceptionkey);
		this.releaseLock(lockName, identifier); // 注销锁

	}
	
	/**
	 * 释放锁
	 * 
	 * @param lockName
	 *            锁的key
	 * @param identifier
	 *            释放锁的标识
	 * @return
	 */
	private boolean releaseLock(String lockName, String identifier) {
		Jedis conn = null;
		String lockKey = "lock:" + lockName;
		boolean retFlag = false;
		try {
			conn = jedisPool.getResource();
			while (true) {
				// 监视lock，准备开始事务
				conn.watch(lockKey);
				// 通过前面返回的value值判断是不是该锁，若是该锁，则删除，释放锁
				if (identifier.equals(conn.get(lockKey))) {
					Transaction transaction = conn.multi();
					transaction.del(lockKey);
					List<Object> results = transaction.exec();
					if (results == null) {
						continue;
					}
					retFlag = true;
				}
				conn.unwatch();
				break;
			}
		} catch (JedisException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return retFlag;
	}
	
	@Override
	public BackResult<RunTestDomian> theTest(String fileUrl, String userId, String mobile, String source,
			String startLine, String type) {

		// 基础验证
		FileUpload fileUpload = fileUploadService.findByOne(fileUrl);

		if (null == fileUpload) {
			return new BackResult<RunTestDomian>(ResultCode.RESULT_DATA_EXCEPTIONS, "文件检测异常，没有检测到可以检测的文件！");
		}
		
		if (type.equals("1")) {
			return theTest(fileUpload, userId, mobile, source);
		} else {
			return findTheTestRunStatus(userId, startLine, fileUpload.getFileUploadUrl(),source,mobile);
		}

	}

	/**
	 * 查询运行状态
	 * @param userId 用户id 
	 * @param startLine 开始行
	 * @param fileUrl 文件地址
	 * @return
	 */
	private BackResult<RunTestDomian> findTheTestRunStatus(String userId, String startLine, String fileUrl,String source,String mobile) {

		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();

		String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId); // 获取空号检测 需要检测的总条数key （根据文件获取的总条数）
		String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId); // 获取空号检测 已经成功检测的总条数（运行中，不考虑不计费的条数） 
		String KhTestCount = redisClient.get(KhTestCountKey);
		
		// 出现异常终止检测
		String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId); // 线程执行全局异常key
		String exceptions = redisClient.get(exceptionkey);
		if (exceptions.equals(ResultCode.RESULT_FAILED)) {
			this.clearLockAndCountForRun(userId, mobile);
			this.sendMessage(source, Boolean.FALSE, mobile, userId);
		}

		if (!CommonUtils.isNotString(KhTestCount)) {
			String succeedTestCount = redisClient.get(succeedTestCountkey);
			succeedTestCount = !CommonUtils.isNotString(succeedTestCount) ? succeedTestCount : "0";
			runTestDomian.setRunCount(Integer.valueOf(succeedTestCount.toString())); // 设置运行的总条数
			runTestDomian.setMobiles(FileUtils.getFileMenu(fileUrl, Integer.parseInt(startLine), 100)); // 设置已经检测了的手机号码
			logger.info("----------需要检测的总条数: 【" + KhTestCount + "】，已经检测完成的条数:" + succeedTestCount);
			if (Integer.parseInt(KhTestCount) <= Integer.valueOf(succeedTestCount)) {
				result.setResultMsg("任务执行结束");
				runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
			} else {
				result.setResultMsg("任务执行中");
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
			}
		} else {
			result.setResultMsg("该账户没有正在检测的程序进程");
			runTestDomian.setRunCount(0);
			runTestDomian.setStatus("6"); // 没有在执行的检测
		}

		return null;
	}

	/**
	 * web 上传文件空号检测 最新版本
	 * @param fileUpload 文件对象
	 * @param userId 用户id
	 * @param mobile 手机号码
	 * @param source 来源
	 * @return
	 */
	private BackResult<RunTestDomian> theTest(FileUpload fileUpload, String userId, String mobile,
			String source) {

		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();

		// 定义基础rediskey
		DistributedLock lock = new DistributedLock(jedisPool);
		String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile); // 锁名
		String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId); // 获取空号检测
																					// 需要检测的总条数key
																					// （根据文件获取的总条数）
		String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId); // 获取空号检测
																								// 已经成功检测的总条数（运行中，不考虑不计费的条数）
		String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId); // 获取空号检测
																								// redis锁的唯一标识
		String succeedClearingCountkey = RedisKeys.getInstance().getkhSucceedClearingCountkey(userId); // 获取空号检测
																										// 已经成功检测的总条数（运行结束需要记账的总条数）
		String generateResultskey = RedisKeys.getInstance().getkhGenerateResultskey(userId); // 空号检测线程key
																								// 多线程执行是
																								// 全部执行完毕生成文件使用
		String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId); // 线程执行全局异常key
		
		int expire = 60 * 60 * 1000; // 超时时间 （1小时）

		try {
			// 加锁
			String identifier = lock.lockWithTimeout(lockName, 800L, expire);
			// 处理加锁业务
			if (null != identifier) {

				// 将标识存入redis
				redisClient.set(redisLockIdentifier, identifier, expire);

				// 初始化条数 需要进行检测的条数 检测一条 条数 + 1 累加
				redisClient.set(succeedTestCountkey, String.valueOf(0), expire);

				// 初始化条数 需要进行需要检测的总条数
				redisClient.set(KhTestCountKey, fileUpload.getFileRows().toString(), expire);

				// 初始化条数 多线程检测的线程数
				redisClient.set(generateResultskey, String.valueOf(0).toString(), expire);
				
				// 初始化 线程执行全局异常key
				redisClient.set(exceptionkey, ResultCode.RESULT_SUCCEED, expire);

				logger.info("----------用户编号：[" + userId + "]开始执行空号检索事件 事件开始时间："
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

				BufferedReader br = null;
				// 将数据取出放入内存中
				List<String> mobileList = new ArrayList<String>();
				File file = new File(fileUpload.getFileUploadUrl());
				if (file.isFile() && file.exists()) {

					br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
					String lineTxt = null;

					while ((lineTxt = br.readLine()) != null) {

						if (CommonUtils.isNotString(lineTxt)) {
							continue;
						}

						// 去掉字符串中的所有空格
						lineTxt = lineTxt.replace(" ", "");

						// 验证是否为正常的１１位有效数字
						if (!CommonUtils.isNumeric(lineTxt)) {
							continue;
						}

						mobileList.add(lineTxt);
					}

				}

				// 需要计费的总条数存入redis
				redisClient.set(succeedClearingCountkey, String.valueOf(mobileList.size()), expire * 2);

				int threads = (mobileList.size() / 200000) + 1;

				if (threads == 1) {
					// 单线程执行检测
					mobileListTest(mobileList, userId, succeedTestCountkey, generateResultskey, expire, threads, source,
							mobile, lock, fileUpload.getFileName(), String.valueOf(mobileList.size()));
				} else {
					// 多线程执行检测
					for (int i = 0; i < threads; i++) {

						Integer rows = 200000;
						// 第一个和最后一个特殊处理
						if (i == 0) {
							List<String> list = mobileList.subList(i, rows);
							mobileListTest(list, userId, succeedTestCountkey, generateResultskey, expire, threads,
									source, mobile, lock, fileUpload.getFileName(), String.valueOf(mobileList.size()));
						} else if (i == threads - 1) {
							List<String> list = mobileList.subList(i * rows + 1, mobileList.size());
							mobileListTest(list, userId, succeedTestCountkey, generateResultskey, expire, threads,
									source, mobile, lock, fileUpload.getFileName(), String.valueOf(mobileList.size()));
						} else {
							List<String> list = mobileList.subList(i * rows + 1, (i + 1) * rows);
							mobileListTest(list, userId, succeedTestCountkey, generateResultskey, expire, threads,
									source, mobile, lock, fileUpload.getFileName(), String.valueOf(mobileList.size()));
						}

					}
				}

				result.setResultMsg("任务执行中");
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				runTestDomian.setRunCount(0); // 设置运行的总条数
			} else {
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				runTestDomian.setRunCount(0); // 设置运行的总条数
				result.setResultMsg("请修改API请求参数type=2查询实时的检测结果！");
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("----------客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			sendMessage(source, Boolean.FALSE, mobile,userId);
			redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
		}

		result.setResultObj(runTestDomian);
		return result;

	}

	/**
	 * 线程执行空号检测
	 * 
	 * @param mobileList
	 *            需要检测的手机号码集合
	 * @param userId
	 *            用户id
	 * @param succeedTestCountkey
	 *            正在进行检测的条数key 累加
	 * @param generateResultskey
	 *            线程的key 累加
	 * @param expire
	 *            超时时间
	 * @param cthreads
	 *            线程总数
	 * @param source
	 *            来源
	 * @param mobile
	 *            用户手机号码
	 * @param lock
	 *            锁
	 * @param fileName
	 *            文件名
	 * @param succeedClearingCount
	 *            需要结算的总条数
	 */
	private synchronized void mobileListTest(List<String> mobileList, String userId, String succeedTestCountkey,
			String generateResultskey, Integer expire, Integer cthreads, String source, String mobile,
			DistributedLock lock, String fileName, String succeedClearingCount) {

		if (!CommonUtils.isNotEmpty(mobileList)) {
			// 创建一个线程
			Runnable run = new Runnable() {
				@Override
				public void run() {
					
					String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId); // 线程执行全局异常key
					Jedis jedis = null;
					try {
						jedis = jedisPool.getResource();

						// 线程数累加
						Integer threads = Integer.parseInt(redisClient.get(generateResultskey));
						threads = threads + 1;
						redisClient.set(generateResultskey, String.valueOf(0), expire);

						Date sixStartTime = DateUtils.addDay(new Date(), -180);

						Integer count = 0;

						for (String mobile : mobileList) {
							
							// 出现异常终止检测
							String exceptions = jedis.get(exceptionkey);
							if (exceptions.equals(ResultCode.RESULT_FAILED)) {
								break;
							}

							// 检测 3个月内
							BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(mobile,
									sixStartTime, new Date());

							String realListkey = RedisKeys.getInstance().getkhRealListtkey(userId); // 实号列表
							String kongListtkey = RedisKeys.getInstance().getkhKongListtkey(userId); // 空号列表
							String silenceListtkey = RedisKeys.getInstance().getkhSilenceListtkey(userId); // 沉默号列表

							MobileSort sort = new MobileSort();
							sort.setMobile(mobile);

							if (null != detail) {

								sort.setReportTime(detail.getReportTime());
								sort.setDelivrd(detail.getDelivrd());

								// 存在数据 (实号：real，空号：kong，沉默号：silence)
								String status = MobileDetailHelper.getInstance().getMobileStatus(mobile,
										detail.getDelivrd());

								if (status.equals("real")) {
									// 实号
									if (detail.getDelivrd().equals("DELIVRD")) {
										sort.setSortRanking(realrandom());
									} else {
										sort.setSortRanking(2);
									}
									jedis.zadd(realListkey.getBytes(), sort.getSortRanking(),
											ObjectSer.ObjectToByte(sort));
								} else if (status.equals("kong")) {
									// 空号
									sort.setSortRanking(getMobileSortRanking(detail.getDelivrd()));
									jedis.zadd(kongListtkey.getBytes(), sort.getSortRanking(),
											ObjectSer.ObjectToByte(sort));
								} else {
									// 沉默号
									sort.setSortRanking(1);
									jedis.zadd(silenceListtkey.getBytes(), sort.getSortRanking(),
											ObjectSer.ObjectToByte(sort));
								}

							} else {

								// 二次清洗根据号段
								MobileNumberSection section = mobileNumberSectionService
										.findByNumberSection(mobile.substring(0, 7));

								if (null != section) {
									// 沉默号
									sort.setSortRanking(1);
									jedis.zadd(silenceListtkey.getBytes(), sort.getSortRanking(),
											ObjectSer.ObjectToByte(sort));
								} else {
									// 空号
									sort.setSortRanking(5);
									jedis.zadd(kongListtkey.getBytes(), sort.getSortRanking(),
											ObjectSer.ObjectToByte(sort));
								}

							}

							// 成功检测条数累加
							count = count + 1;
							redisClient.set(succeedTestCountkey, String.valueOf(count), expire);
						}

						// 检测完成统计 生成结果文件
						pushGenerateResults(userId, source, mobile, lock, fileName, succeedClearingCount, cthreads,expire);
					} catch (Exception e) {
						redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
						e.printStackTrace();
						logger.error("用户ID:"+userId+"线程执行空号检测异常：" + e.getMessage());
					} finally {
						// 返还到连接池
						jedis.close();
					}

				}
			};

			// 加入线程池开始执行
			threadExecutorService.execute(run);
		}

	}

	/**
	 * 空号排序规则
	 * 
	 * @param delivrd
	 * @return
	 */
	private Integer getMobileSortRanking(String delivrd) {

		String sortOne = "SME169,MK:0012,MN:0001,MK:0001,SGIP:1,SGIP:23,MK:0000,SGIP:12,MK:0010";

		String sortTwo = "SGIP:13,SGIP:-74,MN:0013,MK:0013,SGIP:5,MI:0013,MK:0005";

		String sortThree = "SGIP:24,SGIP:54,EXPIRED,MI:0024,SGIP:59,SGIP:29";

		// 第一梯队
		String[] sortOnes = sortOne.split(",");
		for (String string : sortOnes) {
			if (string.equals(delivrd)) {
				return 1;
			}
		}

		// 第二梯队
		String[] sortTwos = sortTwo.split(",");
		for (String string : sortTwos) {
			if (string.equals(delivrd)) {
				return 2;
			}
		}

		// 第三梯队
		String[] sortThrees = sortThree.split(",");
		for (String string : sortThrees) {
			if (string.equals(delivrd)) {
				return 3;
			}
		}

		return 4;

	}

	/**
	 * 实号排序规则 前:DELIVRD 70% 后：DELIVRD 30% 中间：其它
	 * 
	 * @return
	 */
	private Integer realrandom() {
		Random r = new Random();
		int n = r.nextInt(100);
		return n <= 70 ? 1 : 3;
	}

	/**
	 * 生成结果报表
	 * 
	 * @param userId
	 *            用户id
	 * @param source
	 *            来源
	 * @param mobile
	 *            手机号码
	 * @param lock
	 *            锁
	 * @param fileName
	 *            文件名
	 * @param succeedClearingCount
	 *            需要计费的条数
	 * @param cthreads
	 *            线程总数
	 */
	private synchronized void pushGenerateResults(String userId, String source, String mobile, DistributedLock lock,
			String fileName, String succeedClearingCount, Integer cthreads,Integer expire) {

		try {
			Integer threads = Integer.parseInt(redisClient.get(RedisKeys.getInstance().getkhGenerateResultskey(userId)));

			if (threads == cthreads) {
				String realListkey = RedisKeys.getInstance().getkhRealListtkey(userId); // 实号列表
				String kongListtkey = RedisKeys.getInstance().getkhKongListtkey(userId); // 空号列表
				String silenceListtkey = RedisKeys.getInstance().getkhSilenceListtkey(userId); // 沉默号列表

				// 文件地址入库
				CvsFilePath cvsFilePath = new CvsFilePath();
				cvsFilePath.setUserId(userId);

				String timeTemp = String.valueOf(System.currentTimeMillis());
				String filePath = loadfilePath + userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/";

				List<List<Object>> realList = getRedisSortListByKey(realListkey,expire,userId);
				// 生成实号报表
				if (!CommonUtils.isNotEmpty(realList)) {
					logger.info("----------实号总条数：" + realList.size());
					Object[] shhead = { "手机号码" };
					FileUtils.createCvsFile("实号.csv", filePath, realList, shhead);
					cvsFilePath.setThereCount(String.valueOf(realList.size()));
				}

				List<List<Object>> kongList = getRedisSortListByKey(kongListtkey,expire,userId);
				// 生成空号报表
				if (!CommonUtils.isNotEmpty(kongList)) {
					logger.info("----------空号总条数：" + kongList.size());
					Object[] head = { "手机号码" };
					FileUtils.createCvsFile("空号.csv", filePath, kongList, head);
					cvsFilePath.setSixCount(String.valueOf(kongList.size()));
				}

				List<List<Object>> silenceList = getRedisSortListByKey(silenceListtkey,expire,userId);
				// 生成沉默号报表
				if (!CommonUtils.isNotEmpty(silenceList)) {
					logger.info("----------沉默号总条数：" + silenceList.size());
					Object[] wzhead = { "手机号码" };
					FileUtils.createCvsFile("沉默号.csv", filePath, silenceList, wzhead);
					cvsFilePath.setUnknownSize(String.valueOf(silenceList.size()));
				}

				List<File> list = new ArrayList<File>();
				if (!CommonUtils.isNotEmpty(realList)) {
					list.add(new File(filePath + "实号.csv"));
					cvsFilePath.setThereFilePath(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/实号.csv");
					cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "实号.csv"));
				}

				if (!CommonUtils.isNotEmpty(kongList)) {
					list.add(new File(filePath + "空号.csv"));
					cvsFilePath.setSixFilePath(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/空号.csv");
					cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "空号.csv"));
				}

				if (!CommonUtils.isNotEmpty(silenceList)) {
					list.add(new File(filePath + "沉默号.csv"));
					cvsFilePath.setUnknownFilePath(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/沉默号.csv");
					cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "沉默号.csv"));
				}

				// 报表文件打包
				if (null != list && list.size() > 0) {
					String zipName = fileName + ".zip";
					FileUtils.createZip(list, filePath + zipName);
					cvsFilePath.setZipName(zipName);
					cvsFilePath.setZipPath((userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/测试结果包.zip"));
					cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
				}

				cvsFilePath.setCreateTime(new Date());

				if (CommonUtils.isNotString(cvsFilePath.getThereFilePath())
						&& CommonUtils.isNotString(cvsFilePath.getSixFilePath())
						&& CommonUtils.isNotString(cvsFilePath.getUnknownFilePath())) {
					sendMessage(source, Boolean.FALSE, mobile,userId);
					this.clearLockAndCountForRun(lock, userId, mobile);

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
				waterConsumption.setCount(succeedClearingCount); // 条数
				waterConsumption.setUpdateTime(new Date());
				mongoTemplate.save(waterConsumption);

				sendMessage(source, Boolean.TRUE, mobile,userId);
			}
		} catch (Exception e) {
			String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId); // 线程执行全局异常key
			redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
			e.printStackTrace();
			logger.error("用户ID:"+userId+"线程执行空号检测异常：" + e.getMessage());
		}
		
	}

	/**
	 * 根据key 获取排序后的列表
	 * 
	 * @param key
	 * @return
	 */
	public List<List<Object>> getRedisSortListByKey(String key,Integer expire,String userId) {

		Jedis jedis = null;

		List<List<Object>> list = new ArrayList<List<Object>>();
		try {
			jedis = jedisPool.getResource();

			// 从redis中获取实号列表
			Set<byte[]> set = jedis.zrevrange(key.getBytes(), 0, -1);
			Iterator<byte[]> iter = set.iterator();
			// 实号list
			List<Object> objList = null;
			while (iter.hasNext()) {
				MobileSort sort = (MobileSort) ObjectSer.ByteToObject(iter.next());
				objList = new ArrayList<Object>();
				objList.add(sort.getMobile());
				list.add(objList);
			}

		} catch (Exception e) {
			logger.error("用户ID:"+userId+"从redis中获取结果列表异常：" + e.getMessage());
			String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId); // 线程执行全局异常key
			redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
			e.printStackTrace();
		} finally {
			// 返还到连接池
			jedis.close();
		}
		return list;
	}

	/**
	 * 发送短信 通知
	 * 
	 * @param source
	 *            来源
	 * @param fag
	 *            true 发送成功检测完成短信 false 发送检测失败短信
	 */
	public void sendMessage(String source, Boolean fag, String mobile,String userId) {

		if (fag) {
			if ("pc1.0".equals(source)) {
				// 发送短信
				ChuangLanSmsUtil.getInstance().sendSmsByMobileForTest(mobile);
			} else {
				// 发送短信
				ChuangLanSmsUtil.getInstance().sendSmsByMobileForZZTTest(mobile);
			}
		} else {
			if ("pc1.0".equals(source)) {
				// 发送短信
				ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
			} else {
				// 异常发送短信
				ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestZZtEx(mobile);
			}
		}

	}

	// public Integer getThreadSize(Integer rows){
	// int s = rows / 200000;
	// if (s <= 0) {
	// return 1;
	// } else {
	//
	// }
	// }

	public static void main(String[] args) {

		// List<String> list = new ArrayList<String>();
		// for (int i = 0; i <= 1000001; i++) {
		// list.add(String.valueOf(i));
		//// System.out.println(i);
		// }
		//
		// int threads = (list.size() / 200000) + 1;
		//
		// System.out.println("threads"+threads);
		// // 多线程执行检测
		// for (int i = 0; i < threads; i++) {
		//
		// Integer rows = 200000;
		//
		// if (i == 0) {
		// List<String> list1 = list.subList(i, rows);
		// System.out.println("第一个"+i + "-" + rows);
		// } else if (i == threads - 1) {
		// List<String> list3 = list.subList(i * rows + 1, list.size());
		// System.out.println(("最后一个"+i * rows + 1) + "-" + list.size());
		// for (String string : list3) {
		// System.out.println(string);
		// }
		// } else {
		// List<String> list2 = list.subList(i * rows + 1, (i + 1) * rows);
		// System.out.println((i * rows + 1) + "-" + ((i + 1) * 200000));
		// }
		//
		//
		//
		// }
	}
}
