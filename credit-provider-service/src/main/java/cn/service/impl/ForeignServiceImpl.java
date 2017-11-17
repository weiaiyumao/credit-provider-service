package cn.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import cn.entity.CvsFilePath;
import cn.entity.MobileNumberSection;
import cn.entity.WaterConsumption;
import cn.entity.base.BaseMobileDetail;
import cn.redis.DistributedLock;
//import cn.redis.RedisClient;
import cn.redis.RedisLock;
import cn.service.CvsFilePathService;
import cn.service.ForeignService;
import cn.service.MobileNumberSectionService;
import cn.service.SpaceDetectionService;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import cn.utils.FileUtils;
import cn.utils.UUIDTool;
import main.java.cn.common.BackResult;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.CvsFilePathDomain;
import main.java.cn.domain.RunTestDomian;
import main.java.cn.domain.page.PageDomain;
import main.java.cn.hhtp.util.HttpUtil;
import main.java.cn.sms.util.ChuangLanSmsUtil;
import net.sf.json.JSONObject;
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

	// 注入RedisTemplate对象
	@Resource(name = "redisTemplate")
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private JedisPool jedisPool;

	private HashMap<String, Object> map = new HashMap<String, Object>();
	
	@Autowired
	private CvsFilePathService cvsFilePathService;

	@Autowired
	private MobileNumberSectionService mobileNumberSectionService;

	@Value("${consumeAccountUrl}")
	private String consumeAccountUrl;

	@Value("${findAccountUrl}")
	private String findAccountUrl;

	public BackResult<RunTestDomian> runTheTest11111(String fileUrl, String userId, String timestamp, String mobile) {

		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();

		// String key = "runTheTest_" + userId;

		RedisLock lock = new RedisLock(redisTemplate, "testFile_" + timestamp + "_" + userId, 0, 30 * 60 * 1000);

		BufferedReader br = null;

		try {

			// 处理加锁业务
			if (lock.lock()) {

				logger.info("key[" + "testFile_" + timestamp + "_" + userId + "]");

				map.remove("testCount_" + userId); // 清空条数

				int testCount = 0;
				map.put("testCount_" + userId, testCount);
				logger.info("用户编号：[" + userId + "]文件地址：[" + fileUrl + "]开始执行空号检索事件 事件开始时间："
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "post:" + port);

				List<List<Object>> thereDataList = new ArrayList<List<Object>>();
				List<Object> thereRowList = null;
				List<List<Object>> sixDataList = new ArrayList<List<Object>>();
				List<Object> sixRowList = null;
				List<List<Object>> unKonwDataList = new ArrayList<List<Object>>();
				List<Object> unKonwRowList = null;

				// 3个月前的时间
				Date thereStartTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -90);
				// 6个月前的时间
				Date sixStartTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -180);

				File file = new File(fileUrl);
				if (file.isFile() && file.exists()) {

					InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
					br = new BufferedReader(isr);
					String lineTxt = null;

					while ((lineTxt = br.readLine()) != null) {

						if (CommonUtils.isNotString(lineTxt)) {
							continue;
						}

						logger.info("执行查询3月数据每条开始时间" + System.currentTimeMillis());
						// 检测 3个月内
						BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(lineTxt,
								thereStartTime, DateUtils.getCurrentDateTime());

						logger.info("执行查询3月数据每条结束" + System.currentTimeMillis());
						// 数据存在 并且状态 为成功
						if (null != detail) {
							if (detail.getDelivrd().equals("DELIVRD")) {
								thereRowList = new ArrayList<Object>();
								thereRowList.add(detail.getMobile());
								thereRowList.add("实号");
								thereDataList.add(thereRowList);
							} else {
								unKonwRowList = new ArrayList<Object>();
								unKonwRowList.add(detail.getMobile());
								unKonwRowList.add(detail.getDelivrd());
								unKonwDataList.add(unKonwRowList);
							}
						} else {

							logger.info("执行查询6月数据每条开始时间" + System.currentTimeMillis());
							// 检测6个月内
							detail = spaceDetectionService.findByMobileAndReportTime(lineTxt, sixStartTime,
									thereStartTime);

							logger.info("执行查询6月数据每条开始时间" + System.currentTimeMillis());

							// 6个月内数据存在 并且状态 为成功
							if (null != detail) {
								if (detail.getDelivrd().equals("DELIVRD")) {
									sixRowList = new ArrayList<Object>();
									sixRowList.add(detail.getMobile());
									sixRowList.add("实号");
									sixDataList.add(sixRowList);
								} else {
									unKonwRowList = new ArrayList<Object>();
									unKonwRowList.add(detail.getMobile());
									unKonwRowList.add(detail.getDelivrd());
									unKonwDataList.add(unKonwRowList);
								}
							} else {
								unKonwRowList = new ArrayList<Object>();
								unKonwRowList.add(lineTxt);
								unKonwRowList.add("沉默号");
								unKonwDataList.add(unKonwRowList);
							}

						}

						testCount = testCount + 1;
						map.put("testCount_" + userId, testCount);
					}

				} else {
					logger.error("客户ID：[" + userId + "]执行号码检测发现文件地址不存在");
					result.setResultCode(ResultCode.RESULT_BUSINESS_EXCEPTIONS);
					result.setResultMsg("客户ID：[" + userId + "]执行号码检测发现文件地址不存在");
					// 清空
					map.remove("testCount_" + userId);
					lock.unlock(); // 注销锁
					return result;
				}

				// 文件地址入库
				CvsFilePath cvsFilePath = new CvsFilePath();
				cvsFilePath.setUserId(userId);

				// 生成报表
				String filePath = loadfilePath + userId + "/" + DateUtils.getDate() + "/";
				Object[] head = { "手机号码", "状态", };
				if (!CommonUtils.isNotEmpty(thereDataList)) {
					logger.info("实号总条数：" + thereDataList.size());
					FileUtils.createCvsFile("实号.csv", filePath, thereDataList, head);
					cvsFilePath.setThereCount(String.valueOf(thereDataList.size()));
				}

				if (!CommonUtils.isNotEmpty(sixDataList)) {
					logger.info("空号总条数：" + sixDataList.size());
					FileUtils.createCvsFile("空号.csv", filePath, sixDataList, head);
					cvsFilePath.setSixCount(String.valueOf(sixDataList.size()));
				}

				if (!CommonUtils.isNotEmpty(unKonwDataList)) {
					logger.info("沉默号总条数：" + unKonwDataList.size());
					FileUtils.createCvsFile("沉默号.csv", filePath, unKonwDataList, head);
					cvsFilePath.setUnknownSize(String.valueOf(unKonwDataList.size()));
				}

				List<File> list = new ArrayList<File>();

				if (!CommonUtils.isNotEmpty(thereDataList)) {
					list.add(new File(filePath + "实号.csv"));
					cvsFilePath.setThereFilePath(userId + "/" + DateUtils.getDate() + "/实号.csv");
					cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "实号.csv"));
				}

				if (!CommonUtils.isNotEmpty(sixDataList)) {
					list.add(new File(filePath + "空号.csv"));
					cvsFilePath.setSixFilePath(userId + "/" + DateUtils.getDate() + "/空号.csv");
					cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "空号.csv"));
				}

				if (!CommonUtils.isNotEmpty(unKonwDataList)) {
					list.add(new File(filePath + "沉默号.csv"));
					cvsFilePath.setUnknownFilePath(userId + "/" + DateUtils.getDate() + "/沉默号.csv");
					cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "沉默号.csv"));
				}

				String zipName = "测试结果包.zip";
				// 报表文件打包
				if (null != list && list.size() > 0) {
					zipName = "测试结果包.zip";
					FileUtils.createZip(list, filePath + zipName);
					cvsFilePath.setZipName(zipName);
					cvsFilePath.setZipPath((userId + "/" + DateUtils.getDate() + "/测试结果包.zip"));
					cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
				}

				cvsFilePath.setCreateTime(new Date());
				mongoTemplate.save(cvsFilePath);

				// 记账
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("creUserId", userId);
				jsonObject.put("count", testCount);
				logger.info("用户ID发送请求支付消费条数,请求参数:" + jsonObject);
				String responseStr = HttpUtil.createHttpPost(consumeAccountUrl, jsonObject);
				logger.info("用户ID发送请求支付消费条数,请求结果:" + responseStr);
				JSONObject json = JSONObject.fromObject(responseStr);

				if (json.get("resultCode").equals("000000") && json.get("resultObj").equals(Boolean.TRUE)) {
					logger.info("用户ID[" + userId + "]本次成功消费条数：" + testCount);
					// 记录流水记录
					WaterConsumption waterConsumption = new WaterConsumption();
					waterConsumption.setUserId(userId);
					waterConsumption.setId(UUIDTool.getInstance().getUUID());
					waterConsumption.setConsumptionNum("SHJC_" + timestamp);
					waterConsumption.setMenu("客户上传文件实号检测");
					waterConsumption.setStatus("1");
					waterConsumption.setType("1"); // 实号检测
					waterConsumption.setCreateTime(new Date());
					waterConsumption.setCount(String.valueOf(testCount)); // 条数
					waterConsumption.setUpdateTime(new Date());
					mongoTemplate.save(waterConsumption);
				} else {
					result.setResultCode(ResultCode.RESULT_BUSINESS_EXCEPTIONS);
					result.setResultMsg("用户记账失败！");
					return result;
				}

				result.setResultMsg("成功");

				logger.info("用户编号：[" + userId + "]文件地址：[" + fileUrl + "]结束空号检索事件 事件结束时间："
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

			} else {

				LineNumberReader rf = null;
				int lines = 0;
				File test = new File(fileUrl);
				File file = new File(fileUrl);
				if (file.isFile() && file.exists()) {
					long fileLength = test.length();
					rf = new LineNumberReader(new FileReader(test));

					if (rf != null) {
						rf.skip(fileLength);
						lines = rf.getLineNumber();
						rf.close();
					}
				}

				if (map.size() > 0) {
					runTestDomian.setRunCount(Integer.valueOf(map.get("testCount_" + userId).toString()));
				} else {
					runTestDomian.setRunCount(0);
				}

				if (lines == Integer.valueOf(map.get("testCount_" + userId).toString())) {
					result.setResultMsg("任务执行结束");
					runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
				} else {
					result.setResultMsg("任务执行中");
					runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				}

				result.setResultObj(runTestDomian);
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			lock.unlock(); // 注销锁
			// 清空
			map.remove("testCount_" + userId);
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			return result;
		} finally {
			try {

				if (null != br) {
					br.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
				result.setResultCode(ResultCode.RESULT_FAILED);
				result.setResultMsg("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
				return result;
			}
		}

		runTestDomian.setRunCount(Integer.valueOf(map.get("testCount_" + userId).toString()));
		runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
		lock.unlock(); // 注销锁
		// 清空
		result.setResultObj(runTestDomian);
		return result;
	}

	@Override
	public BackResult<RunTestDomian> runTheTest(String fileUrl, String userId, String timestamp, String mobile) {

		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();

		RedisLock lock = new RedisLock(redisTemplate, "testFile_" + timestamp + "_" + userId, 0, 2 * 60 * 60 * 1000);

		BufferedReader br = null;

		try {

			// 处理加锁业务
			if (lock.lock()) {

				LineNumberReader rf = null;
				int lines = 0;
				File test = new File(fileUrl);
				File file1 = new File(fileUrl);
				if (file1.isFile() && file1.exists()) {
					long fileLength = test.length();
					rf = new LineNumberReader(new FileReader(test));

					if (rf != null) {
						rf.skip(fileLength);
						lines = rf.getLineNumber();
						rf.close();
					}
				}

				// 验证账户余额
				JSONObject jsonAccount = new JSONObject();
				jsonAccount.put("mobile", mobile);
				logger.info("用户发送请求查询账户余额条数,请求参数:" + jsonAccount);
				String responseStr1 = HttpUtil.createHttpPost(findAccountUrl, jsonAccount);
				logger.info("用户发送请求查询账户余额条数,请求结果:" + responseStr1);
				JSONObject responseJson = JSONObject.fromObject(responseStr1);

				if (!responseJson.get("resultCode").equals("000000")) {
					runTestDomian.setStatus("3"); // 1执行中 2执行结束 3执行异常4账户余额不足
					lock.unlock(); // 注销锁
					// 清空
					result.setResultObj(runTestDomian);
					result.setResultMsg("查询账户余额失败");
					return result;
				}

				JSONObject accountJson = JSONObject.fromObject(responseJson.get("resultObj"));

				if (Integer.valueOf(accountJson.get("account").toString()) < lines) {
					runTestDomian.setStatus("4"); // 1执行中 2执行结束 3执行异常4账户余额不足
					lock.unlock(); // 注销锁
					// 清空
					result.setResultObj(runTestDomian);
					result.setResultMsg("账户余额不足");
					return result;
				}

				logger.info("key[" + "testFile_" + timestamp + "_" + userId + "]");

				map.remove("testCount_" + userId); // 清空需要记账的总条数

				map.remove("count_" + userId); // 清空实际检测的总条数

				int testCount = 0; // 需要记账的总条数
				int count = 0; // 实际检测的总条数
				map.put("testCount_" + userId, testCount);
				logger.info("用户编号：[" + userId + "]文件地址：[" + fileUrl + "]开始执行空号检索事件 事件开始时间："
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "post:" + port);

				List<List<Object>> thereDataList = new ArrayList<List<Object>>();
				List<Object> thereRowList = null;
				List<Map<String, Object>> sixDataList = new ArrayList<Map<String, Object>>();
				List<List<Object>> unKonwDataList = new ArrayList<List<Object>>();
				List<Object> unKonwRowList = null;

				// 3个月前的时间
				// Date thereStartTime =
				// DateUtils.addDay(DateUtils.getCurrentDateTime(), -90);
				// 6个月前的时间
				Date sixStartTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -270);

				File file = new File(fileUrl);
				if (file.isFile() && file.exists()) {

					InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
					br = new BufferedReader(isr);
					String lineTxt = null;

					while ((lineTxt = br.readLine()) != null) {

						count = count + 1;
						map.put("count_" + userId, count);
						if (CommonUtils.isNotString(lineTxt)) {
							continue;
						}

						// 验证是否为正常的１１位有效数字
						if (!CommonUtils.isNumeric(lineTxt)) {
							continue;
						}

						// 检测 3个月内
						BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(lineTxt, sixStartTime,
								DateUtils.getCurrentDateTime());

						if (null != detail) {
							if (detail.getDelivrd().equals("DELIVRD") || detail.getDelivrd().equals("MC:0055")
									|| detail.getDelivrd().equals("CJ:0007") || detail.getDelivrd().equals("CJ:0008")
									|| detail.getDelivrd().equals("DB:0141") || detail.getDelivrd().equals("DISTURB")
									|| detail.getDelivrd().equals("HD:0001") || detail.getDelivrd().equals("IC:0151")
									|| detail.getDelivrd().equals("ID:0004") || detail.getDelivrd().equals("MBBLACK")
									|| detail.getDelivrd().equals("MC:0055") || detail.getDelivrd().equals("MK:0008")
									|| detail.getDelivrd().equals("MK:0010") || detail.getDelivrd().equals("MK:0022")
									|| detail.getDelivrd().equals("MK:0024") || detail.getDelivrd().equals("MK:0029")
									|| detail.getDelivrd().equals("MN:0017") || detail.getDelivrd().equals("MN:0044")
									|| detail.getDelivrd().equals("MN:0051") || detail.getDelivrd().equals("MN:0054")
									|| detail.getDelivrd().equals("REJECT") || detail.getDelivrd().equals("SME19")
									|| detail.getDelivrd().equals("TIMEOUT") || detail.getDelivrd().equals("UNDELIV")
									|| detail.getDelivrd().equals("GG:0024") || detail.getDelivrd().equals("DB:0309")
									|| detail.getDelivrd().equals("SME92") || detail.getDelivrd().equals("DB:0114")
									|| detail.getDelivrd().equals("MN:0174") || detail.getDelivrd().equals("YX:7000")
									|| detail.getDelivrd().equals("MK:0004") || detail.getDelivrd().equals("NOROUTE")
									|| detail.getDelivrd().equals("CJ:0005") || detail.getDelivrd().equals("IC:0055")
									|| detail.getDelivrd().equals("REJECTE") || detail.getDelivrd().equals("MN:0053")
									|| detail.getDelivrd().equals("MB:1026")) {
								thereRowList = new ArrayList<Object>();
								thereRowList.add(detail.getMobile());
								// thereRowList.add("");
								// thereRowList.add("");
								thereDataList.add(thereRowList);
							} else if (detail.getDelivrd().equals("101") || detail.getDelivrd().equals("-1")
									|| detail.getDelivrd().equals("SGIP:2：12") || detail.getDelivrd().equals("ERRNUM")
									|| detail.getDelivrd().equals("RP:1") || detail.getDelivrd().equals("MN:0001")
									|| detail.getDelivrd().equals("SPMSERR:136")
									|| detail.getDelivrd().equals("MK:0000") || detail.getDelivrd().equals("MK:0001")
									|| detail.getDelivrd().equals("SGIP:1") || detail.getDelivrd().equals("SGIP:33")
									|| detail.getDelivrd().equals("SGIP:67") || detail.getDelivrd().equals("LT:0001")
									|| detail.getDelivrd().equals("3") || detail.getDelivrd().equals("Deliver")
									|| detail.getDelivrd().equals("CB:0001") || detail.getDelivrd().equals("CB:0053")
									|| detail.getDelivrd().equals("DB:0101") || detail.getDelivrd().equals("12")
									|| detail.getDelivrd().equals("12") || detail.getDelivrd().equals("601")
									|| detail.getDelivrd().equals("MK:0012")) {
								Map<String, Object> sixRowList = new HashMap<>();
								sixRowList.put("mobile", detail.getMobile());
								sixRowList.put("delivd", 1);// 空号状态
								sixRowList.put("reportTime", detail.getReportTime().getTime());
								sixDataList.add(sixRowList);
							} else if (detail.getDelivrd().equals("HD:31") || detail.getDelivrd().equals("IC:0001")
									|| detail.getDelivrd().equals("MI:0011") || detail.getDelivrd().equals("MI:0013")
									|| detail.getDelivrd().equals("MI:0029") || detail.getDelivrd().equals("MK:0005")

									|| detail.getDelivrd().equals("UNKNOWN") || detail.getDelivrd().equals("MI:0024")
									|| detail.getDelivrd().equals("MI:0054") || detail.getDelivrd().equals("MN:0059")
									|| detail.getDelivrd().equals("MI:0059") || detail.getDelivrd().equals("MI:0055")
									|| detail.getDelivrd().equals("MI:0004") || detail.getDelivrd().equals("MI:0005")) {
								Map<String, Object> sixRowList = new HashMap<>();
								sixRowList.put("mobile", detail.getMobile());
								sixRowList.put("delivd", 2);// 停机状态
								sixRowList.put("reportTime", detail.getReportTime().getTime());
								sixDataList.add(sixRowList);
							} else {
								unKonwRowList = new ArrayList<Object>();
								unKonwRowList.add(lineTxt);
								// unKonwRowList.add("沉默号");
								// unKonwRowList.add(detail.getDelivrd());
								unKonwDataList.add(unKonwRowList);

								// thereRowList = new ArrayList<Object>();
								// thereRowList.add(lineTxt);
								//// thereRowList.add("");
								//// thereRowList.add("");
								// thereDataList.add(thereRowList);

							}
						} else {

							// 二次清洗根据号段
							MobileNumberSection section = mobileNumberSectionService
									.findByNumberSection(lineTxt.substring(0, 7));

							if (null != section) {
								// thereRowList = new ArrayList<Object>();
								// thereRowList.add(lineTxt);
								// thereDataList.add(thereRowList);

								unKonwRowList = new ArrayList<Object>();
								unKonwRowList.add(lineTxt);
								unKonwDataList.add(unKonwRowList);
							} else {
								// 放空号
								Map<String, Object> sixRowList = new HashMap<>();
								sixRowList.put("mobile", lineTxt);
								sixRowList.put("delivd", 1);// 空号状态
								sixRowList.put("reportTime",
										DateUtils.converYYYYMMddHHmmssStrToDate("1900-01-01 00:00:00").getTime());
								// sixRowList.add("不存在的号段");
								sixDataList.add(sixRowList);

								// unKonwRowList = new ArrayList<Object>();
								// unKonwRowList.add(lineTxt);
								//// unKonwRowList.add("沉默号");
								//// unKonwRowList.add("");
								// unKonwDataList.add(unKonwRowList);
							}

						}

						testCount = testCount + 1;
						map.put("testCount_" + userId, testCount);
					}

				} else {
					logger.error("客户ID：[" + userId + "]执行号码检测发现文件地址不存在");
					result.setResultCode(ResultCode.RESULT_BUSINESS_EXCEPTIONS);
					result.setResultMsg("客户ID：[" + userId + "]执行号码检测发现文件地址不存在");
					// 清空
					map.remove("testCount_" + userId);
					map.remove("count_" + userId); // 清空实际检测的总条数
					lock.unlock(); // 注销锁
					return result;
				}

				// 文件地址入库
				CvsFilePath cvsFilePath = new CvsFilePath();
				cvsFilePath.setUserId(userId);

				// 生成报表
				String timeTemp = String.valueOf(System.currentTimeMillis());
				String filePath = loadfilePath + userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/";
				// Object[] head = { "手机号码", "状态", "代码状态" };
				if (!CommonUtils.isNotEmpty(thereDataList)) {
					logger.info("实号总条数：" + thereDataList.size());
					Object[] shhead = { "手机号码" };
					FileUtils.createCvsFile("实号.csv", filePath, thereDataList, shhead);
					cvsFilePath.setThereCount(String.valueOf(thereDataList.size()));
				}

				if (!CommonUtils.isNotEmpty(sixDataList)) {
					logger.info("空号总条数：" + sixDataList.size());
					Object[] head = { "手机号码" };
					try {
						Collections.sort(sixDataList, new Comparator<Map<String, Object>>() {
							@Override
							public int compare(Map<String, Object> arg0, Map<String, Object> arg1) {
								try {
									Long reportTime0 = Long.parseLong(
											arg0.get("delivd").toString() + arg0.get("reportTime").toString());
									Long reportTime1 = Long.parseLong(
											arg1.get("delivd").toString() + arg1.get("reportTime").toString());
									return reportTime0.compareTo(reportTime1);
								} catch (NumberFormatException e) {
									logger.error("手机号码arg0" + arg0.get("mobile") + "arg1" + arg1.get("mobile")
											+ "空号排序异常，reportTime格式转换异常" + e);
									return 0;
								}
							}
						});
					} catch (Exception e) {
						logger.error("排序系统异常：" + e.getMessage());
					}
					FileUtils.createCvsFileByMap("空号.csv", filePath, sixDataList, head);
					cvsFilePath.setSixCount(String.valueOf(sixDataList.size()));
				}

				if (!CommonUtils.isNotEmpty(unKonwDataList)) {
					logger.info("沉默号总条数：" + unKonwDataList.size());
					Object[] wzhead = { "手机号码" };
					FileUtils.createCvsFile("沉默号.csv", filePath, unKonwDataList, wzhead);
					cvsFilePath.setUnknownSize(String.valueOf(unKonwDataList.size()));
				}

				List<File> list = new ArrayList<File>();

				if (!CommonUtils.isNotEmpty(thereDataList)) {
					list.add(new File(filePath + "实号.csv"));
					cvsFilePath.setThereFilePath(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/实号.csv");
					cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "实号.csv"));
				}

				if (!CommonUtils.isNotEmpty(sixDataList)) {
					list.add(new File(filePath + "空号.csv"));
					cvsFilePath.setSixFilePath(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/空号.csv");
					cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "空号.csv"));
				}

				if (!CommonUtils.isNotEmpty(unKonwDataList)) {
					list.add(new File(filePath + "沉默号.csv"));
					cvsFilePath.setUnknownFilePath(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/沉默号.csv");
					cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "沉默号.csv"));
				}

				String zipName = "测试结果包.zip";
				// 报表文件打包
				if (null != list && list.size() > 0) {
					zipName = "测试结果包.zip";
					FileUtils.createZip(list, filePath + zipName);
					cvsFilePath.setZipName(zipName);
					cvsFilePath.setZipPath((userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/测试结果包.zip"));
					cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
				}

				cvsFilePath.setCreateTime(new Date());
				mongoTemplate.save(cvsFilePath);

				// 记账
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("creUserId", userId);
				jsonObject.put("count", testCount);
				logger.info("用户ID发送请求支付消费条数,请求参数:" + jsonObject);
				String responseStr = HttpUtil.createHttpPost(consumeAccountUrl, jsonObject);
				logger.info("用户ID发送请求支付消费条数,请求结果:" + responseStr);
				JSONObject json = JSONObject.fromObject(responseStr);

				if (json.get("resultCode").equals("000000") && json.get("resultObj").equals(Boolean.TRUE)) {
					logger.info("用户ID[" + userId + "]本次成功消费条数：" + testCount);
					// 记录流水记录
					WaterConsumption waterConsumption = new WaterConsumption();
					waterConsumption.setUserId(userId);
					waterConsumption.setId(UUIDTool.getInstance().getUUID());
					waterConsumption.setConsumptionNum("SHJC_" + timestamp);
					waterConsumption.setMenu("客户上传文件实号检测");
					waterConsumption.setStatus("1");
					waterConsumption.setType("1"); // 实号检测
					waterConsumption.setCreateTime(new Date());
					waterConsumption.setCount(String.valueOf(testCount)); // 条数
					waterConsumption.setUpdateTime(new Date());
					mongoTemplate.save(waterConsumption);
				} else {
					result.setResultCode(ResultCode.RESULT_BUSINESS_EXCEPTIONS);
					result.setResultMsg("用户记账失败！");
					return result;
				}

				// 发送短信
				ChuangLanSmsUtil.getInstance().sendSmsByMobileForTest(mobile);

				result.setResultMsg("成功");

				logger.info("用户编号：[" + userId + "]文件地址：[" + fileUrl + "]结束空号检索事件 事件结束时间："
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

			} else {

				LineNumberReader rf = null;
				int lines = 0;
				File test = new File(fileUrl);
				File file = new File(fileUrl);
				if (file.isFile() && file.exists()) {
					long fileLength = test.length();
					rf = new LineNumberReader(new FileReader(test));

					if (rf != null) {
						rf.skip(fileLength);
						lines = rf.getLineNumber();
						rf.close();
					}
				}

				if (map.size() > 0) {
					runTestDomian.setRunCount(Integer.valueOf(map.get("count_" + userId).toString()));
				} else {
					runTestDomian.setRunCount(0);
				}

				logger.info("lines: " + lines + "count_:" + map.get("count_" + userId).toString());

				if (lines <= Integer.valueOf(map.get("count_" + userId).toString())) {
					result.setResultMsg("任务执行结束");
					runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
					lock.unlock(); // 注销锁

					logger.info("手机号码：" + mobile + "结束运行检测任务");

				} else {
					result.setResultMsg("任务执行中");
					runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				}

				result.setResultObj(runTestDomian);
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			lock.unlock(); // 注销锁
			// 清空

			// 异常发送短信
			ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);

			map.remove("testCount_" + userId);
			map.remove("count_" + userId); // 清空实际检测的总条数
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			return result;
		} finally {
			try {

				if (null != br) {
					br.close();
				}

			} catch (IOException e) {
				e.printStackTrace();

				// 异常发送短信
				ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
				result.setResultCode(ResultCode.RESULT_FAILED);
				result.setResultMsg("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
				return result;
			}
		}

		runTestDomian.setRunCount(Integer.valueOf(map.get("testCount_" + userId).toString()));
		runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
		lock.unlock(); // 注销锁
		// 清空
		result.setResultObj(runTestDomian);
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

	@Override
	public BackResult<RunTestDomian> theTest(String fileUrl, String userId, String mobile, String source,String startLine,String type) {
		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();
		DistributedLock lock = new DistributedLock(jedisPool);
		String lockName = "theTestkey1111_" + mobile;
		try {
			// 执行检测
			if (type.equals("1")) {
				// 加锁
				String identifier = lock.lockWithTimeout(lockName, 800L, 2 * 60 * 60 * 1000);
				// 处理加锁业务
				if (null != identifier) {
					
					LineNumberReader rf = null;
					int lines = 0;
					File test = new File(fileUrl);
					File file1 = new File(fileUrl);
					if (file1.isFile() && file1.exists()) {
						long fileLength = test.length();
						rf = new LineNumberReader(new FileReader(test));

						if (rf != null) {
							rf.skip(fileLength);
							lines = rf.getLineNumber();
							rf.close();
						}
					}

					// 验证账户余额
					JSONObject jsonAccount = new JSONObject();
					jsonAccount.put("mobile", mobile);
					logger.info("用户发送请求查询账户余额条数,请求参数:" + jsonAccount);
					String responseStr1 = HttpUtil.createHttpPost(findAccountUrl, jsonAccount);
					logger.info("用户发送请求查询账户余额条数,请求结果:" + responseStr1);
					JSONObject responseJson = JSONObject.fromObject(responseStr1);

					if (!responseJson.get("resultCode").equals("000000")) {
						runTestDomian.setStatus("3"); // 1执行中 2执行结束 3执行异常4账户余额不足
						lock.releaseLock(lockName, identifier); // 注销锁
						// 清空
						result.setResultObj(runTestDomian);
						result.setResultMsg("查询账户余额失败");
						return result;
					}

					JSONObject accountJson = JSONObject.fromObject(responseJson.get("resultObj"));

					if (Integer.valueOf(accountJson.get("account").toString()) < lines) {
						runTestDomian.setStatus("4"); // 1执行中 2执行结束 3执行异常4账户余额不足
						lock.releaseLock(lockName, identifier); // 注销锁
						// 清空
						result.setResultObj(runTestDomian);
						result.setResultMsg("账户余额不足");
						return result;
					}
					
					File file = new File(fileUrl);
					if (!file.isFile() || !file.exists()) {
						logger.error("客户ID：[" + userId + "]执行号码检测发现文件地址不存在");
						result.setResultCode(ResultCode.RESULT_BUSINESS_EXCEPTIONS);
						result.setResultMsg("客户ID：[" + userId + "]执行号码检测发现文件地址不存在");
						// 清空
						map.remove("testCount_" + userId);
						map.remove("count_" + userId); // 清空实际检测的总条数
						lock.releaseLock(lockName, identifier); // 注销锁
						return result;
					}
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							BufferedReader br = null;
							try {
								map.remove("testCount_" + userId); // 清空需要记账的总条数

								map.remove("count_" + userId); // 清空实际检测的总条数

								int testCount = 0; // 需要记账的总条数
								int count = 0; // 实际检测的总条数
								map.put("testCount_" + userId, testCount);
								logger.info("用户编号：[" + userId + "]文件地址：[" + fileUrl + "]开始执行空号检索事件 事件开始时间："
										+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "post:" + port);

								List<List<Object>> thereDataList = new ArrayList<List<Object>>();
								List<Object> thereRowList = null;
								List<Map<String, Object>> sixDataList = new ArrayList<Map<String, Object>>();
								List<List<Object>> unKonwDataList = new ArrayList<List<Object>>();
								List<Object> unKonwRowList = null;

								Date sixStartTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -270);

								File file = new File(fileUrl);
								if (file.isFile() && file.exists()) {

									InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
									br = new BufferedReader(isr);
									String lineTxt = null;

									while ((lineTxt = br.readLine()) != null) {

										count = count + 1;
										map.put("count_" + userId, count);
										if (CommonUtils.isNotString(lineTxt)) {
											continue;
										}

										// 验证是否为正常的１１位有效数字
										if (!CommonUtils.isNumeric(lineTxt)) {
											continue;
										}

										// 检测 3个月内
										BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(lineTxt, sixStartTime,
												DateUtils.getCurrentDateTime());

										if (null != detail) {
											if (detail.getDelivrd().equals("DELIVRD") || detail.getDelivrd().equals("MC:0055")
													|| detail.getDelivrd().equals("CJ:0007") || detail.getDelivrd().equals("CJ:0008")
													|| detail.getDelivrd().equals("DB:0141") || detail.getDelivrd().equals("DISTURB")
													|| detail.getDelivrd().equals("HD:0001") || detail.getDelivrd().equals("IC:0151")
													|| detail.getDelivrd().equals("ID:0004") || detail.getDelivrd().equals("MBBLACK")
													|| detail.getDelivrd().equals("MC:0055") || detail.getDelivrd().equals("MK:0008")
													|| detail.getDelivrd().equals("MK:0010") || detail.getDelivrd().equals("MK:0022")
													|| detail.getDelivrd().equals("MK:0024") || detail.getDelivrd().equals("MK:0029")
													|| detail.getDelivrd().equals("MN:0017") || detail.getDelivrd().equals("MN:0044")
													|| detail.getDelivrd().equals("MN:0051") || detail.getDelivrd().equals("MN:0054")
													|| detail.getDelivrd().equals("REJECT") || detail.getDelivrd().equals("SME19")
													|| detail.getDelivrd().equals("TIMEOUT") || detail.getDelivrd().equals("UNDELIV")
													|| detail.getDelivrd().equals("GG:0024") || detail.getDelivrd().equals("DB:0309")
													|| detail.getDelivrd().equals("SME92") || detail.getDelivrd().equals("DB:0114")
													|| detail.getDelivrd().equals("MN:0174") || detail.getDelivrd().equals("YX:7000")
													|| detail.getDelivrd().equals("MK:0004") || detail.getDelivrd().equals("NOROUTE")
													|| detail.getDelivrd().equals("CJ:0005") || detail.getDelivrd().equals("IC:0055")
													|| detail.getDelivrd().equals("REJECTE") || detail.getDelivrd().equals("MN:0053")
													|| detail.getDelivrd().equals("MB:1026")) {
												thereRowList = new ArrayList<Object>();
												thereRowList.add(detail.getMobile());
												thereDataList.add(thereRowList);
											} else if (detail.getDelivrd().equals("101") || detail.getDelivrd().equals("-1")
													|| detail.getDelivrd().equals("SGIP:2：12") || detail.getDelivrd().equals("ERRNUM")
													|| detail.getDelivrd().equals("RP:1") || detail.getDelivrd().equals("MN:0001")
													|| detail.getDelivrd().equals("SPMSERR:136")
													|| detail.getDelivrd().equals("MK:0000") || detail.getDelivrd().equals("MK:0001")
													|| detail.getDelivrd().equals("SGIP:1") || detail.getDelivrd().equals("SGIP:33")
													|| detail.getDelivrd().equals("SGIP:67") || detail.getDelivrd().equals("LT:0001")
													|| detail.getDelivrd().equals("3") || detail.getDelivrd().equals("Deliver")
													|| detail.getDelivrd().equals("CB:0001") || detail.getDelivrd().equals("CB:0053")
													|| detail.getDelivrd().equals("DB:0101") || detail.getDelivrd().equals("12")
													|| detail.getDelivrd().equals("12") || detail.getDelivrd().equals("601")
													|| detail.getDelivrd().equals("MK:0012")) {
												Map<String, Object> sixRowList = new HashMap<>();
												sixRowList.put("mobile", detail.getMobile());
												sixRowList.put("delivd", 1);// 空号状态
												sixRowList.put("reportTime", detail.getReportTime().getTime());
												sixDataList.add(sixRowList);
											} else if (detail.getDelivrd().equals("HD:31") || detail.getDelivrd().equals("IC:0001")
													|| detail.getDelivrd().equals("MI:0011") || detail.getDelivrd().equals("MI:0013")
													|| detail.getDelivrd().equals("MI:0029") || detail.getDelivrd().equals("MK:0005")

													|| detail.getDelivrd().equals("UNKNOWN") || detail.getDelivrd().equals("MI:0024")
													|| detail.getDelivrd().equals("MI:0054") || detail.getDelivrd().equals("MN:0059")
													|| detail.getDelivrd().equals("MI:0059") || detail.getDelivrd().equals("MI:0055")
													|| detail.getDelivrd().equals("MI:0004") || detail.getDelivrd().equals("MI:0005")) {
												Map<String, Object> sixRowList = new HashMap<>();
												sixRowList.put("mobile", detail.getMobile());
												sixRowList.put("delivd", 2);// 停机状态
												sixRowList.put("reportTime", detail.getReportTime().getTime());
												sixDataList.add(sixRowList);
											} else {
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
												sixRowList.put("reportTime",
														DateUtils.converYYYYMMddHHmmssStrToDate("1900-01-01 00:00:00").getTime());
												sixDataList.add(sixRowList);

											}

										}

										testCount = testCount + 1;
										map.put("testCount_" + userId, testCount);
									}

								} else {
									
								}

								// 文件地址入库
								CvsFilePath cvsFilePath = new CvsFilePath();
								cvsFilePath.setUserId(userId);

								// 生成报表
								String timeTemp = String.valueOf(System.currentTimeMillis());
								String filePath = loadfilePath + userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/";
								if (!CommonUtils.isNotEmpty(thereDataList)) {
									logger.info("实号总条数：" + thereDataList.size());
									Object[] shhead = { "手机号码" };
									FileUtils.createCvsFile("实号.csv", filePath, thereDataList, shhead);
									cvsFilePath.setThereCount(String.valueOf(thereDataList.size()));
								}

								if (!CommonUtils.isNotEmpty(sixDataList)) {
									logger.info("空号总条数：" + sixDataList.size());
									Object[] head = { "手机号码" };
									try {
										Collections.sort(sixDataList, new Comparator<Map<String, Object>>() {
											@Override
											public int compare(Map<String, Object> arg0, Map<String, Object> arg1) {
												try {
													Long reportTime0 = Long.parseLong(
															arg0.get("delivd").toString() + arg0.get("reportTime").toString());
													Long reportTime1 = Long.parseLong(
															arg1.get("delivd").toString() + arg1.get("reportTime").toString());
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
									logger.info("沉默号总条数：" + unKonwDataList.size());
									Object[] wzhead = { "手机号码" };
									FileUtils.createCvsFile("沉默号.csv", filePath, unKonwDataList, wzhead);
									cvsFilePath.setUnknownSize(String.valueOf(unKonwDataList.size()));
								}

								List<File> list = new ArrayList<File>();

								if (!CommonUtils.isNotEmpty(thereDataList)) {
									list.add(new File(filePath + "实号.csv"));
									cvsFilePath.setThereFilePath(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/实号.csv");
									cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "实号.csv"));
								}

								if (!CommonUtils.isNotEmpty(sixDataList)) {
									list.add(new File(filePath + "空号.csv"));
									cvsFilePath.setSixFilePath(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/空号.csv");
									cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "空号.csv"));
								}

								if (!CommonUtils.isNotEmpty(unKonwDataList)) {
									list.add(new File(filePath + "沉默号.csv"));
									cvsFilePath.setUnknownFilePath(userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/沉默号.csv");
									cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "沉默号.csv"));
								}

								String zipName = "测试结果包.zip";
								// 报表文件打包
								if (null != list && list.size() > 0) {
									zipName = "测试结果包.zip";
									FileUtils.createZip(list, filePath + zipName);
									cvsFilePath.setZipName(zipName);
									cvsFilePath.setZipPath((userId + "/" + DateUtils.getDate() + "/" + timeTemp + "/测试结果包.zip"));
									cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
								}

								cvsFilePath.setCreateTime(new Date());
								mongoTemplate.save(cvsFilePath);

								// 记账
								JSONObject jsonObject = new JSONObject();
								jsonObject.put("creUserId", userId);
								jsonObject.put("count", testCount);
								logger.info("用户ID发送请求支付消费条数,请求参数:" + jsonObject);
								String responseStr = HttpUtil.createHttpPost(consumeAccountUrl, jsonObject);
								logger.info("用户ID发送请求支付消费条数,请求结果:" + responseStr);
								JSONObject json = JSONObject.fromObject(responseStr);

								if (json.get("resultCode").equals("000000") && json.get("resultObj").equals(Boolean.TRUE)) {
									logger.info("用户ID[" + userId + "]本次成功消费条数：" + testCount);
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
									waterConsumption.setSource(source);
									mongoTemplate.save(waterConsumption);
								} else {
									logger.error("用户ID[" + userId + "]本次成功消费条数：" + testCount + "出现系统异常记账失败！");
								}

								// 发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTest(mobile);

								result.setResultMsg("成功");

								logger.info("用户编号：[" + userId + "]文件地址：[" + fileUrl + "]结束空号检索事件 事件结束时间："
										+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
								
								runTestDomian.setRunCount(Integer.valueOf(map.get("testCount_" + userId).toString()));
								runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
								// 清空
								result.setResultObj(runTestDomian);
								logger.info("手机号码：" + mobile + "结束运行检测任务");
								lock.releaseLock(lockName, identifier); // 注销锁
							
							} catch (Exception e) {
								e.printStackTrace();
								logger.error("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
								lock.releaseLock(lockName, identifier); // 注销锁
								// 清空
								// 异常发送短信
								ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
								map.remove("testCount_" + userId);
								map.remove("count_" + userId); // 清空实际检测的总条数
							} finally {
								if (null != br) {
									try {
										br.close();
									} catch (IOException e) {
										e.printStackTrace();
										lock.releaseLock(lockName, identifier); // 注销锁
										logger.error("文件流关闭异常：" + e.getMessage());
									}
								}
							}
							
						}
					}, "开启线程用户手机号" + mobile + "执行实号检测").start();
					
					result.setResultMsg("任务执行中");
					runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
					runTestDomian.setRunCount(0); // 设置运行的总条数
					return result;
					
				}
			} else if(type.equals("2")) {
				int lines = 0;
				File test = new File(fileUrl);
				File file = new File(fileUrl);
				if (file.isFile() && file.exists()) {
					long fileLength = test.length();
					LineNumberReader rf = new LineNumberReader(new FileReader(test));
					if (rf != null) {
						rf.skip(fileLength);
						lines = rf.getLineNumber();
						rf.close();
					}
				}

				if (map.size() > 0) {
					runTestDomian.setRunCount(Integer.valueOf(map.get("count_" + userId).toString())); // 设置运行的总条数
					runTestDomian.setMobiles(FileUtils.getFileMenu(fileUrl, Integer.parseInt(startLine), runTestDomian.getRunCount())); // 设置已经检测了的手机号码
					
					logger.info("需要检测的总条数: 【" + lines + "】，已经检测完成的条数:" + map.get("count_" + userId).toString());

					if (lines <= Integer.valueOf(map.get("count_" + userId).toString())) {
						result.setResultMsg("任务执行结束");
						runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
					} else {
						result.setResultMsg("任务执行中");
						runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
					}
				} else {
					runTestDomian.setRunCount(0);
				}

				result.setResultObj(runTestDomian);
				return result;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			// 异常发送短信
			ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			return result;
		}

		return result;
	}

	public static void main(String[] args) throws IOException {  
        System.out.println(FileUtils.getFileMenu("D:\\test\\mk000164.txt", 1000, 10000));
    }  
}
