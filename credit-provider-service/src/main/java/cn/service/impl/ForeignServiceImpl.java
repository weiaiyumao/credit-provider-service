package cn.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import cn.entity.CvsFilePath;
import cn.entity.base.BaseMobileDetail;
import cn.redis.RedisClient;
//import cn.redis.RedisClient;
import cn.redis.RedisLock;
import cn.service.ForeignService;
import cn.service.SpaceDetectionService;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import cn.utils.FileUtils;
import main.java.cn.common.BackResult;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.RunTestDomian;

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

	private HashMap<String, Object> map = new HashMap<String, Object>();

	@Autowired
	private RedisClient redisClient;

	@Override
	public BackResult<RunTestDomian> runTheTest(String fileUrl, String userId) {

		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();

		String key = "runTheTest_" + userId;

		RedisLock lock = new RedisLock(redisTemplate, key, 0, 30 * 60 * 1000);

		BufferedReader br = null;

		try {

			// 处理加锁业务
			if (lock.lock()) {

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

						// 检测 3个月内
						BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(lineTxt,
								thereStartTime, DateUtils.getCurrentDateTime());

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

							// 检测6个月内
							detail = spaceDetectionService.findByMobileAndReportTime(lineTxt, sixStartTime,
									thereStartTime);

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
								unKonwRowList.add("未知");
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
					// redisClient.remove(key);
					// 清空
					redisClient.remove(key);
					map.remove("testCount_" + userId);
					lock.unlock(); // 注销锁
					return result;
				}

				// 生成报表
				String filePath = loadfilePath + userId + "/" + DateUtils.getDate() + "/";
				Object[] head = { "手机号码", "状态", };
				if (!CommonUtils.isNotEmpty(thereDataList)) {
					logger.info("3月实号包总条数：" + thereDataList.size());
					FileUtils.createCvsFile("3月实号包.csv", filePath, thereDataList, head);
				}

				if (!CommonUtils.isNotEmpty(sixDataList)) {
					logger.info("6月实号包总条数：" + sixDataList.size());
					FileUtils.createCvsFile("6月实号包.csv", filePath, sixDataList, head);
				}

				if (!CommonUtils.isNotEmpty(unKonwDataList)) {
					logger.info("未知号码包总条数：" + unKonwDataList.size());
					FileUtils.createCvsFile("未知号码包.csv", filePath, unKonwDataList, head);
				}

				List<File> list = new ArrayList<File>();

				if (new File(filePath + "3月实号包.csv").isFile() && new File(filePath + "3月实号包.csv").exists()) {
					list.add(new File(filePath + "3月实号包.csv"));
				}

				if (new File(filePath + "6月实号包.csv").isFile() && new File(filePath + "6月实号包.csv").exists()) {
					list.add(new File(filePath + "6月实号包.csv"));
				}

				if (new File(filePath + "未知号码包.csv").isFile() && new File(filePath + "未知号码包.csv").exists()) {
					list.add(new File(filePath + "未知号码包.csv"));
				}

				String zipName = "TestResultPackage.zip";
				// 报表文件打包
				if (null != list && list.size() > 0) {
					zipName = "TestResultPackage.zip";
					FileUtils.createZip(list, filePath + zipName);
				}

				// 文件地址入库
				CvsFilePath cvsFilePath = new CvsFilePath();
				cvsFilePath.setUserId(userId);
				cvsFilePath.setSixFilePath(userId + "/" + DateUtils.getDate() + "/3月实号包.csv");
				cvsFilePath.setThereFilePath(userId + "/" + DateUtils.getDate() + "/6月实号包.csv");
				cvsFilePath.setUnknownFilePath(userId + "/" + DateUtils.getDate() + "/未知号码包.csv");
				cvsFilePath.setZipPath((userId + "/" + DateUtils.getDate() + "/TestResultPackage.zip"));
				cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
				cvsFilePath.setCreateTime(new Date());
				mongoTemplate.save(cvsFilePath);

				result.setResultMsg("成功");

				logger.info("用户编号：[" + userId + "]文件地址：[" + fileUrl + "]结束空号检索事件 事件结束时间："
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			} else {
				result.setResultMsg("任务执行中");
				if (map.size() > 0) {
					runTestDomian.setRunCount(Integer.valueOf(map.get("testCount_" + userId).toString()));
				} else {
					runTestDomian.setRunCount(0);
				}
				runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				result.setResultObj(runTestDomian);
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			// redisClient.remove(key);
			lock.unlock(); // 注销锁
			// 清空
			redisClient.remove(key);
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
		// redisClient.remove(key);
		lock.unlock(); // 注销锁
		// 清空
		redisClient.remove(key);
		result.setResultObj(runTestDomian);
		return result;
	}

}
