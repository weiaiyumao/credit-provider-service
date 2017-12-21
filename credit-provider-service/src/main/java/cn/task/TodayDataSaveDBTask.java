package cn.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.entity.MobileNumberSection;
import cn.entity.base.BaseMobileDetail;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.MobileNumberSectionService;
import cn.service.SpaceDetectionService;
import cn.task.handler.ClDateSaveDBHandler;
import cn.task.helper.MobileDetailHelper;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import cn.utils.UUIDTool;

/**
 * 每日定时任务入库
 * 
 * @author ChuangLan
 *
 */
@Component
@Configuration
@EnableScheduling
public class TodayDataSaveDBTask {

	@Value("${spring.data.elasticsearch.cluster-name}")
	private String clusterName;

	@Value("${spring.data.elasticsearch.cluster-nodes}")
	private String clusterNodes;
	@Value("${spring.data.elasticsearch.cluster-port}")
	private int clusterPort;

	@Autowired
	private ClDateSaveDBHandler clDateSaveDBHandler;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private SpaceDetectionService spaceDetectionService;
	
	@Autowired
	private MobileNumberSectionService mobileNumberSectionService;

	private final static Logger logger = LoggerFactory.getLogger(TodayDataSaveDBTask.class);

	// 该任务执行一次 时间 秒 分 时 天 月 年
	@Scheduled(cron = "0 34 15 23 11 ?")
	public void ClDateSaveDbTask() throws IOException {
		String timestr = "2017-11-06,2017-11-05,2017-11-04,2017-11-03,2017-11-02,2017-11-01";
		String[] timeList = timestr.split(",");
		for(String time : timeList){
			this.insertMongodbInfo(time);
		}		
//		this.insertMongodbInfo("2017-10-01");
	}
	
	// 该任务执行一次 时间 秒 分 时 天 月 年
		public void insertMongodbInfo(String strdate) {

			try {
				logger.info("------------------开始定时任务------------------");
				Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster")
						.put("client.transport.sniff", true).put("client.transport.ping_timeout", "3600s").build();

				@SuppressWarnings("resource")
				TransportClient client = new PreBuiltTransportClient(settings)
						.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						QueryBuilder qb = QueryBuilders.termsQuery("reportTime", strdate);

						Long a = System.currentTimeMillis();
						SearchResponse scrollResp = client.prepareSearch("201711").setQuery(qb)
								.setScroll(new TimeValue(60000000)).setSize(1000).get();
						long startTime = System.currentTimeMillis();
						long endTime;
			            int i = 1;
			            int j=1;
						do {
							for (SearchHit hit : scrollResp.getHits().getHits()) 
							{
								
								Boolean status = false;
								String json = hit.getSourceAsString();
								JSONObject backjson = (JSONObject) JSONObject.parse(json);
								String realdelivrd = "-1012,-99,004,010,011,015,017,020,022,029,054,055,151,174,188,602,612,613,614,615,618,619,620,625,627,634,636,650,706,711,713,714,726,760,762,812,814,815,827,870,899,901,999,BLACK,BLKFAIL,BwList,CB:0255,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL:105,CL:106,CL:116,CL:125,DB:0008,DB:0119,DB:0140,DB:0141,DB:0142,DB:0144,DB:0160,DB:0309,DB:0318,DB00141,DELIVRD,DISTURB,E:401,E:BLACK,E:ODDL,E:ODSL,E:RPTSS,EM:101,GG:0024,HD:0001,HD:19,HD:31,HD:32,IA:0051,IA:0054,IA:0059,IA:0073,IB:0008,IB:0194,IC:0001,IC:0015,IC:0055,ID:0004,ID:0070,JL:0025,JL:0026,JL:0031,JT:105,KEYWORD,LIMIT,LT:0005,MA:0022,MA:0051,MA:0054,MB:0008,MB:1026,MB:1042,MB:1077,MB:1279,MBBLACK,MC:0055,MC:0151,MH:17,MI:0008,MI:0009,MI:0015,MI:0017,MI:0020,MI:0022,MI:0024,MI:0041,MI:0043,MI:0044,MI:0045,MI:0048,MI:0051,MI:0053,MI:0054,MI:0057,MI:0059,MI:0064,MI:0080,MI:0081,MI:0098,MI:0099,MI:0999,MK:0002,MK:0003,MK:0006,MK:0008,MK:0009,MK:0010,MK:0015,MK:0017,MK:0019,MK:0020,MK:0022,MK:0023,MK:0024,MK:0041,MK:0043,MK:0044,MK:0045,MK:0053,MK:0055";
								realdelivrd += "MK:0057,MK:0098,MK:0099,MN:0000,MN:0009,MN:0011,MN:0012,MN:0019,MN:0020,MN:0022,MN:0029,MN:0041,MN:0043,MN:0044,MN:0045,MN:0050,MN:0053,MN:0055,MN:0098,MN:0174,MT:101,NOPASS,NOROUTE,REFUSED,REJECT,REJECTD,REJECTE,RP:103,RP:106,RP:108,RP:11,RP:115,RP:117,RP:15,RP:17,RP:18,RP:19,RP:2,RP:20,RP:213,RP:22,RP:239,RP:254,RP:255,RP:27,RP:29,RP:36,RP:44,RP:45,RP:48,RP:50,RP:52,RP:55,RP:57,RP:59,RP:61,RP:67,RP:70,RP:77,RP:79,RP:8,RP:86,RP:90,RP:92,RP:98,SGIP:-1,SGIP:10,SGIP:106,SGIP:11,SGIP:117,SGIP:118,SGIP:121,SGIP:14,SGIP:15,SGIP:16,SGIP:17,SGIP:19,SGIP:2,SGIP:20,SGIP:22,SGIP:23,SGIP:-25,SGIP:27,SGIP:-3,SGIP:31,SGIP:43,SGIP:44,SGIP:45,SGIP:48,SGIP:57,SGIP:61,SGIP:64,SGIP:67,SGIP:79,SGIP:86,SGIP:89,SGIP:90,SGIP:92,SGIP:93,SGIP:98,SGIP:99,SME1,SME-1,SME19,SME20,SME210,SME-22,SME-26,SME28,SME3,SME6,SME-70,SME-74,SME8,SME92,SME-93,SYS:005,SYS:008,TIMEOUT,UNDELIV,UNKNOWN,VALVE:M,W-BLACK,YX:1006,YX:7000,YX:8019,YX:9006";
								realdelivrd += "YY:0206,-181,023,036,043,044,706,712,718,721,730,763,779,879,CB:0013,CL:104,GATEBLA,IB:0011,ID:0199,JL:0028,LT:0022,MI:0021,MK:0068,RP:16,RP:65,RP:88,SGIP:-13,SGIP:63,SGIP:70,622,660,MI:0006,MK:0051,RP:121";
								String mobile = backjson.getString("mobile");
								String delivrd = backjson.getString("delivrd");
								Date REPOR_TIME = DateUtils.parseDate(backjson.getString("reportTime"), "yyyy-MM-dd hh:mm:ss");
								if (realdelivrd.contains(delivrd)) {
									status = true;
								}
								if(mobile.length()==11){
									BaseMobileDetail mobileDetail = MobileDetailHelper.getInstance().getBaseMobileDetail(mobile,status);
									mobileDetail.setMobile(mobile);
									mobileDetail.setDelivrd(delivrd);
									mobileDetail.setReportTime(REPOR_TIME);									
									//去重号码数据处理
									spaceDetectionService.deleteByID( mobileDetail,mobile, status);									
									endTime = System.currentTimeMillis();
									System.out.println("已运行"+(endTime - startTime)/1000+"秒，已处理: " + i + " 行, " + "clientMsgId的值为: " + backjson.getString("clientMsgId"));																							
								}
								i++;
													
							}
							
							scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
									.execute().actionGet();
						} while (scrollResp.getHits().getHits().length != 0);
					}
				}, strdate+"线程开始执行定时任务入库").start();	
				logger.info("------------------"+strdate+"数据导入已完成------------------");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("=====执行创蓝数据入库出现异常：" + e.getMessage());
			} 

		}
	
	public void hbaseTest(String time) throws IOException {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// Table table = null;
				Table vacant = null;
				HBaseConfiguration hbaseConfig = null;

				try {
					System.setProperty("hadoop.home.dir", "/usr/local/hadoop-2.6.0/");
					org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
					conf.set("hbase.zookeeper.quorum",
							"172.16.20.30,172.16.20.31,172.16.20.32,172.16.20.33,172.16.20.34");
					conf.set("hbase.zookeeper.property.clientPort", "2181");
					conf.set("mapreduce.task.timeout", "1200000");
					conf.set("hbase.client.scanner.timeout.period", "600000");
					conf.set("hbase.rpc.timeout", "600000");
					Connection connection = ConnectionFactory.createConnection(conf);
					vacant = connection.getTable(TableName.valueOf("VACANT_NUMBER"));
				} catch (Exception e) {
					logger.error("请检查tomcat服务器或端口是否开启!{}", e);
					e.printStackTrace();
				}

				Scan scan = new Scan();
				FilterList filterList = new FilterList();
				String stime = time + " 00:00:00";
				String etime = DateUtils.formatDate(DateUtils.addDay(DateUtils.parseDate(time), 4)) + " 23:59:59";
				Filter stimeFilter = new SingleColumnValueFilter(Bytes.toBytes("cf1"), Bytes.toBytes("REPOR_TIME"),
						CompareOp.GREATER_OR_EQUAL, Bytes.toBytes(stime));
				filterList.addFilter(stimeFilter);
				Filter etimeFilter = new SingleColumnValueFilter(Bytes.toBytes("cf1"), Bytes.toBytes("REPOR_TIME"),
						CompareOp.LESS_OR_EQUAL, Bytes.toBytes(etime));
				filterList.addFilter(etimeFilter);
				scan.setFilter(filterList);
				scan.setCaching(1000);

				int i = 50001;
				int j = 1;
				List<BaseMobileDetail> resultList = new ArrayList<BaseMobileDetail>();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					ResultScanner ResultScannerFilterList = vacant.getScanner(scan);
					for (Result rr = ResultScannerFilterList.next(); rr != null; rr = ResultScannerFilterList.next()) {
						String mobile = "";
						String delivrd = "";
						Boolean status = false;
						// for(KeyValue kv:rr.list()){
						// mobile = new String(kv.getRow());
						// System.out.println("moblie : "+new StringBuffer(new
						// String( kv.getRow())).reverse());
						// System.out.println("column : "+new String(
						// kv.getValue()));
						// delivrd = new String( kv.getValue());
						//
						// }
						Map<String, Object> results = new HashMap<>();
						for (Cell cell : rr.listCells()) {
							results.put(Bytes.toString(CellUtil.cloneQualifier(cell)),
									Bytes.toString(CellUtil.cloneValue(cell)));
						}

						if(results.get("REPOR_TIME") == null || StringUtils.isBlank(results.get("REPOR_TIME").toString()) || "null".equals(results.get("REPOR_TIME").toString())  || "None".equals(results.get("REPOR_TIME").toString())){
							continue;
						}
						String realdelivrd = "-1012,-99,004,010,011,015,017,020,022,029,054,055,151,174,188,602,612,613,614,615,618,619,620,625,627,634,636,650,706,711,713,714,726,760,762,812,814,815,827,870,899,901,999,BLACK,BLKFAIL,BwList,CB:0255,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL:105,CL:106,CL:116,CL:125,DB:0008,DB:0119,DB:0140,DB:0141,DB:0142,DB:0144,DB:0160,DB:0309,DB:0318,DB00141,DELIVRD,DISTURB,E:401,E:BLACK,E:ODDL,E:ODSL,E:RPTSS,EM:101,GG:0024,HD:0001,HD:19,HD:31,HD:32,IA:0051,IA:0054,IA:0059,IA:0073,IB:0008,IB:0194,IC:0001,IC:0015,IC:0055,ID:0004,ID:0070,JL:0025,JL:0026,JL:0031,JT:105,KEYWORD,LIMIT,LT:0005,MA:0022,MA:0051,MA:0054,MB:0008,MB:1026,MB:1042,MB:1077,MB:1279,MBBLACK,MC:0055,MC:0151,MH:17,MI:0008,MI:0009,MI:0015,MI:0017,MI:0020,MI:0022,MI:0024,MI:0041,MI:0043,MI:0044,MI:0045,MI:0048,MI:0051,MI:0053,MI:0054,MI:0057,MI:0059,MI:0064,MI:0080,MI:0081,MI:0098,MI:0099,MI:0999,MK:0002,MK:0003,MK:0006,MK:0008,MK:0009,MK:0010,MK:0015,MK:0017,MK:0019,MK:0020,MK:0022,MK:0023,MK:0024,MK:0041,MK:0043,MK:0044,MK:0045,MK:0053,MK:0055";
						realdelivrd += "MK:0057,MK:0098,MK:0099,MN:0000,MN:0009,MN:0011,MN:0012,MN:0019,MN:0020,MN:0022,MN:0029,MN:0041,MN:0043,MN:0044,MN:0045,MN:0050,MN:0053,MN:0055,MN:0098,MN:0174,MT:101,NOPASS,NOROUTE,REFUSED,REJECT,REJECTD,REJECTE,RP:103,RP:106,RP:108,RP:11,RP:115,RP:117,RP:15,RP:17,RP:18,RP:19,RP:2,RP:20,RP:213,RP:22,RP:239,RP:254,RP:255,RP:27,RP:29,RP:36,RP:44,RP:45,RP:48,RP:50,RP:52,RP:55,RP:57,RP:59,RP:61,RP:67,RP:70,RP:77,RP:79,RP:8,RP:86,RP:90,RP:92,RP:98,SGIP:-1,SGIP:10,SGIP:106,SGIP:11,SGIP:117,SGIP:118,SGIP:121,SGIP:14,SGIP:15,SGIP:16,SGIP:17,SGIP:19,SGIP:2,SGIP:20,SGIP:22,SGIP:23,SGIP:-25,SGIP:27,SGIP:-3,SGIP:31,SGIP:43,SGIP:44,SGIP:45,SGIP:48,SGIP:57,SGIP:61,SGIP:64,SGIP:67,SGIP:79,SGIP:86,SGIP:89,SGIP:90,SGIP:92,SGIP:93,SGIP:98,SGIP:99,SME1,SME-1,SME19,SME20,SME210,SME-22,SME-26,SME28,SME3,SME6,SME-70,SME-74,SME8,SME92,SME-93,SYS:005,SYS:008,TIMEOUT,UNDELIV,UNKNOWN,VALVE:M,W-BLACK,YX:1006,YX:7000,YX:8019,YX:9006";
						realdelivrd += "YY:0206,-181,023,036,043,044,706,712,718,721,730,763,779,879,CB:0013,CL:104,GATEBLA,IB:0011,ID:0199,JL:0028,LT:0022,MI:0021,MK:0068,RP:16,RP:65,RP:88,SGIP:-13,SGIP:63,SGIP:70,622,660,MI:0006,MK:0051,RP:121";
						mobile = new StringBuffer(Bytes.toString(rr.getRow())).reverse().toString();
						delivrd = (String) results.get("DELIVRD");
						if (realdelivrd.contains(delivrd)) {
							status = true;
						}
						BaseMobileDetail mobileDetail = MobileDetailHelper.getInstance().getBaseMobileDetail(mobile,
								status);
						mobileDetail.setMobile(mobile);
						mobileDetail.setDelivrd(delivrd);						
						mobileDetail.setReportTime(sdf.parse((String) results.get("REPOR_TIME")));
						resultList.add(mobileDetail);
						if (i % 50000 == 0) {
							logger.info("=====开始执行第" + j + "批创蓝数据入库操作，任务开始时间:" + DateUtils.getNowTime() + "=====");
							mongoTemplate.insertAll(resultList);
							logger.info("=====开始执行第" + j + "批创蓝数据入库操作，任务结束时间:" + DateUtils.getNowTime() + "=====");
							resultList.clear();
							j++;
						}
						System.out.println(i-50000);
						i++;
					}

					mongoTemplate.insertAll(resultList);
					logger.info("=====数据导入完成=====");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, time + "线程开始执行定时任务入库").start();
		//
		// try {
		// Settings settings = Settings.builder().put("cluster.name",
		// clusterName).put("client.transport.sniff", true)
		// .put("client.transport.ping_timeout", "25s").build();
		//
		// @SuppressWarnings("resource")
		// TransportClient client = new
		// PreBuiltTransportClient(settings).addTransportAddress(
		// new InetSocketTransportAddress(InetAddress.getByName(clusterNodes),
		// clusterPort));
		//
		// SearchResponse scrollResp = client.prepareSearch("201701",
		// "201701").addSort("_doc", SortOrder.ASC)
		// .setScroll(new TimeValue(60000)).setSize(100).get();
		//
		// do {
		// for (SearchHit hit : scrollResp.getHits().getHits()) {
		//
		// String json = hit.getSourceAsString();
		//
		// JSONObject backjson = (JSONObject) JSONObject.parse(json);
		//
		// String mobile = backjson.getString("mobile");
		//
		// BaseMobileDetail detail =
		// MobileDetailHelper.getInstance().getBaseMobileDetail(mobile);
		// detail.setDelivrd(backjson.getString("delivrd"));
		// detail.setMobile(backjson.getString("mobile"));
		// detail.setReportTime(DateUtils.parseDate(backjson.getString("reportTime"),
		// "yyyy-MM-dd hh:mm:ss"));
		// clDateSaveDBHandler.execution(detail);
		// }
		//
		// scrollResp =
		// client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new
		// TimeValue(60000))
		// .execute().actionGet();
		// } while (scrollResp.getHits().getHits().length != 0);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// logger.error("=====执行创蓝数据入库出现异常：" + e.getMessage());
		// }
		//
		// logger.info("=====开始执行创蓝数据入库操作，任务结束时间:" + DateUtils.getNowTime() +
		// "=====");
	}
	
	public void hbaseTestAll() throws IOException {
				Table vacant = null;
				HBaseConfiguration hbaseConfig = null;

				try {
					System.setProperty("hadoop.home.dir", "/usr/local/hadoop-2.6.0/");//  
					org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
					conf.set("hbase.zookeeper.quorum",
							"172.16.20.30,172.16.20.31,172.16.20.32,172.16.20.33,172.16.20.34");
					conf.set("hbase.zookeeper.property.clientPort", "2181");
					conf.set("mapreduce.task.timeout", "1200000");
					conf.set("hbase.client.scanner.timeout.period", "600000");
					conf.set("hbase.rpc.timeout", "600000");
					Connection connection = ConnectionFactory.createConnection(conf);
					vacant = connection.getTable(TableName.valueOf("VACANT_NUMBER"));
				} catch (Exception e) {
					logger.error("请检查tomcat服务器或端口是否开启!{}", e);
					e.printStackTrace();
				}

				Scan scan = new Scan();
				scan.setCaching(10000);

				int i = 50001;
				int j = 1;
				List<BaseMobileDetail> resultList = new ArrayList<BaseMobileDetail>();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				logger.info("=====数据开始导入=====");
				try {
					ResultScanner ResultScannerFilterList = vacant.getScanner(scan);
					for (Result rr = ResultScannerFilterList.next(); rr != null; rr = ResultScannerFilterList.next()) {
						String mobile = "";
						String delivrd = "";
//						Boolean status = false;
						Map<String, Object> results = new HashMap<>();
						for (Cell cell : rr.listCells()) {
							results.put(Bytes.toString(CellUtil.cloneQualifier(cell)),
									Bytes.toString(CellUtil.cloneValue(cell)));
						}
						if(results.get("REPOR_TIME") == null || StringUtils.isBlank(results.get("REPOR_TIME").toString()) || "null".equals(results.get("REPOR_TIME").toString())  || "None".equals(results.get("REPOR_TIME").toString())){
							continue;
						}
//						String realdelivrd = "-1012,-99,004,010,011,015,017,020,022,029,054,055,151,174,188,602,612,613,614,615,618,619,620,625,627,634,636,650,706,711,713,714,726,760,762,812,814,815,827,870,899,901,999,BLACK,BLKFAIL,BwList,CB:0255,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL:105,CL:106,CL:116,CL:125,DB:0008,DB:0119,DB:0140,DB:0141,DB:0142,DB:0144,DB:0160,DB:0309,DB:0318,DB00141,DELIVRD,DISTURB,E:401,E:BLACK,E:ODDL,E:ODSL,E:RPTSS,EM:101,GG:0024,HD:0001,HD:19,HD:31,HD:32,IA:0051,IA:0054,IA:0059,IA:0073,IB:0008,IB:0194,IC:0001,IC:0015,IC:0055,ID:0004,ID:0070,JL:0025,JL:0026,JL:0031,JT:105,KEYWORD,LIMIT,LT:0005,MA:0022,MA:0051,MA:0054,MB:0008,MB:1026,MB:1042,MB:1077,MB:1279,MBBLACK,MC:0055,MC:0151,MH:17,MI:0008,MI:0009,MI:0015,MI:0017,MI:0020,MI:0022,MI:0024,MI:0041,MI:0043,MI:0044,MI:0045,MI:0048,MI:0051,MI:0053,MI:0054,MI:0057,MI:0059,MI:0064,MI:0080,MI:0081,MI:0098,MI:0099,MI:0999,MK:0002,MK:0003,MK:0006,MK:0008,MK:0009,MK:0010,MK:0015,MK:0017,MK:0019,MK:0020,MK:0022,MK:0023,MK:0024,MK:0041,MK:0043,MK:0044,MK:0045,MK:0053,MK:0055";
//						realdelivrd += "MK:0057,MK:0098,MK:0099,MN:0000,MN:0009,MN:0011,MN:0012,MN:0019,MN:0020,MN:0022,MN:0029,MN:0041,MN:0043,MN:0044,MN:0045,MN:0050,MN:0053,MN:0055,MN:0098,MN:0174,MT:101,NOPASS,NOROUTE,REFUSED,REJECT,REJECTD,REJECTE,RP:103,RP:106,RP:108,RP:11,RP:115,RP:117,RP:15,RP:17,RP:18,RP:19,RP:2,RP:20,RP:213,RP:22,RP:239,RP:254,RP:255,RP:27,RP:29,RP:36,RP:44,RP:45,RP:48,RP:50,RP:52,RP:55,RP:57,RP:59,RP:61,RP:67,RP:70,RP:77,RP:79,RP:8,RP:86,RP:90,RP:92,RP:98,SGIP:-1,SGIP:10,SGIP:106,SGIP:11,SGIP:117,SGIP:118,SGIP:121,SGIP:14,SGIP:15,SGIP:16,SGIP:17,SGIP:19,SGIP:2,SGIP:20,SGIP:22,SGIP:23,SGIP:-25,SGIP:27,SGIP:-3,SGIP:31,SGIP:43,SGIP:44,SGIP:45,SGIP:48,SGIP:57,SGIP:61,SGIP:64,SGIP:67,SGIP:79,SGIP:86,SGIP:89,SGIP:90,SGIP:92,SGIP:93,SGIP:98,SGIP:99,SME1,SME-1,SME19,SME20,SME210,SME-22,SME-26,SME28,SME3,SME6,SME-70,SME-74,SME8,SME92,SME-93,SYS:005,SYS:008,TIMEOUT,UNDELIV,UNKNOWN,VALVE:M,W-BLACK,YX:1006,YX:7000,YX:8019,YX:9006";
//						realdelivrd += "YY:0206,-181,023,036,043,044,706,712,718,721,730,763,779,879,CB:0013,CL:104,GATEBLA,IB:0011,ID:0199,JL:0028,LT:0022,MI:0021,MK:0068,RP:16,RP:65,RP:88,SGIP:-13,SGIP:63,SGIP:70,622,660,MI:0006,MK:0051,RP:121";
						mobile = new StringBuffer(Bytes.toString(rr.getRow())).reverse().toString().replace("\r", "");
						if(this.isTelephone(mobile)){							
							delivrd = (String) results.get("DELIVRD");
//							if (realdelivrd.contains(delivrd)) {
//								status = true;
//							}
							BaseMobileDetail mobileDetail = MobileDetailHelper.getInstance().getBaseMobileDetail(mobile);
							mobileDetail.setMobile(mobile);
							mobileDetail.setDelivrd(delivrd);
							mobileDetail.setReportTime(sdf.parse((String) results.get("REPOR_TIME")));
							resultList.add(mobileDetail);
							if (i % 50000 == 0) {
								logger.info("=====开始执行第" + j + "批创蓝数据入库操作，任务开始时间:" + DateUtils.getNowTime() + "=====");
								mongoTemplate.insertAll(resultList);
								logger.info("=====开始执行第" + j + "批创蓝数据入库操作，任务结束时间:" + DateUtils.getNowTime() + "=====");
								resultList.clear();
								j++;
							}
							i++;
							System.out.println(i);
						}
						
					}

					mongoTemplate.insertAll(resultList);
					logger.info("=====数据导入完成=====");

				} catch (Exception e) {
					e.printStackTrace();
				}
	}
	
	// 该任务执行一次 时间 秒 分 时 天 月 年
		@Scheduled(cron = "0 43 14 21 12 ?")
		public void hbaseTestDayTask() throws IOException {
			this.hbaseTestAll();
		}
		
	public void hbaseTestDay() throws IOException {
		Table vacant = null;
		HBaseConfiguration hbaseConfig = null;

		try {
			System.setProperty("hadoop.home.dir", "D:/hadoop-2.6.0/");
			org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
			conf.set("hbase.zookeeper.quorum",
					"172.16.20.30,172.16.20.31,172.16.20.32,172.16.20.33,172.16.20.34");
			conf.set("hbase.zookeeper.property.clientPort", "2181");
			conf.set("mapreduce.task.timeout", "1200000");
			conf.set("hbase.client.scanner.timeout.period", "600000");
			conf.set("hbase.rpc.timeout", "600000");
			Connection connection = ConnectionFactory.createConnection(conf);
			vacant = connection.getTable(TableName.valueOf("VACANT_NUMBER_DAY"));
		} catch (Exception e) {
			logger.error("请检查tomcat服务器或端口是否开启!{}", e);
			e.printStackTrace();
		}

		Scan scan = new Scan();
		scan.setCaching(10000);

		int i = 50001;
		int j = 1;
		List<BaseMobileDetail> resultList = new ArrayList<BaseMobileDetail>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			ResultScanner ResultScannerFilterList = vacant.getScanner(scan);
			for (Result rr = ResultScannerFilterList.next(); rr != null; rr = ResultScannerFilterList.next()) {
				String mobile = "";
				String delivrd = "";
				Boolean status = false;
				Map<String, Object> results = new HashMap<>();
				for (Cell cell : rr.listCells()) {
					results.put(Bytes.toString(CellUtil.cloneQualifier(cell)),
							Bytes.toString(CellUtil.cloneValue(cell)));
				}
				if(results.get("REPOR_TIME") == null || StringUtils.isBlank(results.get("REPOR_TIME").toString()) || "null".equals(results.get("REPOR_TIME").toString())  || "None".equals(results.get("REPOR_TIME").toString())){
					continue;
				}
				String realdelivrd = "-1012,-99,004,010,011,015,017,020,022,029,054,055,151,174,188,602,612,613,614,615,618,619,620,625,627,634,636,650,706,711,713,714,726,760,762,812,814,815,827,870,899,901,999,BLACK,BLKFAIL,BwList,CB:0255,CJ:0005,CJ:0006,CJ:0007,CJ:0008,CL:105,CL:106,CL:116,CL:125,DB:0008,DB:0119,DB:0140,DB:0141,DB:0142,DB:0144,DB:0160,DB:0309,DB:0318,DB00141,DELIVRD,DISTURB,E:401,E:BLACK,E:ODDL,E:ODSL,E:RPTSS,EM:101,GG:0024,HD:0001,HD:19,HD:31,HD:32,IA:0051,IA:0054,IA:0059,IA:0073,IB:0008,IB:0194,IC:0001,IC:0015,IC:0055,ID:0004,ID:0070,JL:0025,JL:0026,JL:0031,JT:105,KEYWORD,LIMIT,LT:0005,MA:0022,MA:0051,MA:0054,MB:0008,MB:1026,MB:1042,MB:1077,MB:1279,MBBLACK,MC:0055,MC:0151,MH:17,MI:0008,MI:0009,MI:0015,MI:0017,MI:0020,MI:0022,MI:0024,MI:0041,MI:0043,MI:0044,MI:0045,MI:0048,MI:0051,MI:0053,MI:0054,MI:0057,MI:0059,MI:0064,MI:0080,MI:0081,MI:0098,MI:0099,MI:0999,MK:0002,MK:0003,MK:0006,MK:0008,MK:0009,MK:0010,MK:0015,MK:0017,MK:0019,MK:0020,MK:0022,MK:0023,MK:0024,MK:0041,MK:0043,MK:0044,MK:0045,MK:0053,MK:0055";
				realdelivrd += "MK:0057,MK:0098,MK:0099,MN:0000,MN:0009,MN:0011,MN:0012,MN:0019,MN:0020,MN:0022,MN:0029,MN:0041,MN:0043,MN:0044,MN:0045,MN:0050,MN:0053,MN:0055,MN:0098,MN:0174,MT:101,NOPASS,NOROUTE,REFUSED,REJECT,REJECTD,REJECTE,RP:103,RP:106,RP:108,RP:11,RP:115,RP:117,RP:15,RP:17,RP:18,RP:19,RP:2,RP:20,RP:213,RP:22,RP:239,RP:254,RP:255,RP:27,RP:29,RP:36,RP:44,RP:45,RP:48,RP:50,RP:52,RP:55,RP:57,RP:59,RP:61,RP:67,RP:70,RP:77,RP:79,RP:8,RP:86,RP:90,RP:92,RP:98,SGIP:-1,SGIP:10,SGIP:106,SGIP:11,SGIP:117,SGIP:118,SGIP:121,SGIP:14,SGIP:15,SGIP:16,SGIP:17,SGIP:19,SGIP:2,SGIP:20,SGIP:22,SGIP:23,SGIP:-25,SGIP:27,SGIP:-3,SGIP:31,SGIP:43,SGIP:44,SGIP:45,SGIP:48,SGIP:57,SGIP:61,SGIP:64,SGIP:67,SGIP:79,SGIP:86,SGIP:89,SGIP:90,SGIP:92,SGIP:93,SGIP:98,SGIP:99,SME1,SME-1,SME19,SME20,SME210,SME-22,SME-26,SME28,SME3,SME6,SME-70,SME-74,SME8,SME92,SME-93,SYS:005,SYS:008,TIMEOUT,UNDELIV,UNKNOWN,VALVE:M,W-BLACK,YX:1006,YX:7000,YX:8019,YX:9006";
				realdelivrd += "YY:0206,-181,023,036,043,044,706,712,718,721,730,763,779,879,CB:0013,CL:104,GATEBLA,IB:0011,ID:0199,JL:0028,LT:0022,MI:0021,MK:0068,RP:16,RP:65,RP:88,SGIP:-13,SGIP:63,SGIP:70,622,660,MI:0006,MK:0051,RP:121";
				mobile = new StringBuffer(Bytes.toString(rr.getRow())).reverse().toString();
				if(this.isTelephone(mobile)){
					delivrd = (String) results.get("DELIVRD");
					if (realdelivrd.contains(delivrd)) {
						status = true;
					}
					BaseMobileDetail mobileDetail = MobileDetailHelper.getInstance().getBaseMobileDetail(mobile,status);
					mobileDetail.setMobile(mobile);
					mobileDetail.setDelivrd(delivrd);
					mobileDetail.setReportTime(sdf.parse((String) results.get("REPOR_TIME")));
					Query query = Query.query(Criteria.where("mobile").is(mobile));
					mongoTemplate.remove(query, mobileDetail.getClass());
					resultList.add(mobileDetail);
					if (i % 50000 == 0) {
						logger.info("=====开始执行第" + j + "批创蓝数据入库操作，任务开始时间:" + DateUtils.getNowTime() + "=====");
						mongoTemplate.insertAll(resultList);
						logger.info("=====开始执行第" + j + "批创蓝数据入库操作，任务结束时间:" + DateUtils.getNowTime() + "=====");
						resultList.clear();
						j++;
					}
					i++;
				}
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		mongoTemplate.insertAll(resultList);
		logger.info("=====数据导入完成=====");
}

	/**
	 * 开启7个线程同时执行定时任务
	 */
	@Scheduled(cron = "0 5 5 30 09 ?")
	public void taskSaveDB() {

		for (int i = 1; i <= 9; i++) {
			
			if (i == 3 || i == 8 || i == 9) {
				RunnableThreadTask rtt = new RunnableThreadTask("20170" + i, "20170" + i,clDateSaveDBHandler);
				new Thread(rtt, "线程" + i + "开始执行定时任务入库").start();
			}
			
		}

	}
	
//	public static void main(String[] args) {
//		
//		for (int i = 1; i <= 7; i++) {
//			RunnableThreadTask rtt = new RunnableThreadTask("20170" + i, "20170" + i);
//			new Thread(rtt, "线程" + i + "开始执行定时任务入库").start();
//		}
//		
//		
//	}
	
	public boolean isTelephone(String str){ 
		   if(str.length() != 11){
		       return false; 
		   } 
		   return true; 
	}
	
	@Scheduled(cron = "0 58 13 17 10 ?")
	public void taskSectionSaveDB(){
		BufferedReader br = null;
		try {
			logger.info("---------------开始执行任务---------------");
			File file = new File("D:/test/手机号段-20171001-368630-全新版.csv");
			if (file.isFile() && file.exists()) {

				InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "gbk");
				br = new BufferedReader(isr);
				String lineTxt = null;

				while ((lineTxt = br.readLine()) != null) {

					if (CommonUtils.isNotString(lineTxt)) {
						continue;
					}
					String[] str = lineTxt.split(",");
					
					if (mobileNumberSectionService.findByNumberSection(str[1]) == null) {

						MobileNumberSection section = new MobileNumberSection();
						section.setId(UUIDTool.getInstance().getUUID());
						section.setPrefix(str[0]);
						section.setNumberSection(str[1]);
						section.setProvince(str[2]);
						section.setCity(str[3]);
						section.setIsp(str[4]);
						section.setPostCode(str[5]);
						section.setCityCode(str[6]);
						section.setAreaCode(str[7]);
						if (str.length > 8) {
							section.setMobilePhoneType(str[8]);
						}
						
						mongoTemplate.save(section);
					}
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("执行任务系统异常： " + e.getMessage());
		}
		
		logger.info("---------------结束执行任务---------------");
	}

}
