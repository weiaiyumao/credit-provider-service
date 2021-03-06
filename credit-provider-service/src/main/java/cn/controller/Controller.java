package cn.controller;

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

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import cn.entity.MobileNumberSection;
import cn.entity.base.BaseMobileDetail;
import cn.entity.ct.CT133;
import cn.service.ForeignService;
import cn.service.MobileNumberSectionService;
import cn.service.SpaceDetectionService;
import cn.service.cm.CM136Service;
import cn.task.TodayDataSaveDBTask;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import cn.utils.UUIDTool;
import main.java.cn.common.BackResult;
import main.java.cn.domain.RunTestDomian;

/**
 * Created by WunHwanTseng on 2016/11/12. 111111
 */
@RestController
public class Controller {
	
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private SpaceDetectionService spaceDetectionService;
    
    @Autowired
    private TodayDataSaveDBTask todayDataSaveDBTask;
    
    @Autowired
    private ForeignService foreignService;
    
    @Autowired
    private CM136Service cM136Service;
    
	@Autowired
	private MobileNumberSectionService mobileNumberSectionService;
    
    
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
    
    @GetMapping("/test")
    public void test(){
    	
		try {
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster").put("client.transport.sniff", true)
					.put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));
			
			SearchResponse scrollResp = client.prepareSearch("201701","201701")
					.addSort("_doc", SortOrder.ASC).setScroll(new TimeValue(60000))

					.setSize(100).get(); // max of 100 hits will be returned for
											// each scroll
			int i = 0;
			Map<String, String> map = new HashMap<String, String>();
			do {
				for (SearchHit hit : scrollResp.getHits().getHits()) {
					String json = hit.getSourceAsString();

					System.out.println("i=" + i + ":" + hit.getId() + "," + hit.getSourceAsString());
					 JSONObject backjson = (JSONObject) JSONObject.parse(json);
					
					 String account = backjson.getString("account");
					 map.put(account, account);

				}

				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
						.actionGet();
			} while (scrollResp.getHits().getHits().length != 0); // Zero hits mark
																	// the end of
																	// the scroll
																	// and the while
																	// loop.
			for (int k = 0; k < 100000; k++) {
				String kk = map.get(String.valueOf(k));
				if (kk == null) {
					System.out.println(k);
				}
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
    }
    
    @GetMapping("/findname")
    public BaseMobileDetail findname() {
    	BaseMobileDetail detail  = null;
    try {
    	System.out.println(new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
    	Date startTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -90);
    	detail = spaceDetectionService.findByMobileAndReportTime("18611764785", startTime,
				DateUtils.getCurrentDateTime());
//    	List<CM136> detail = cM136Service.findByMobile("13663343685");
    	System.out.println(detail.getMobile());
    	System.out.println(new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
	} catch (Exception e) {
		// TODO: handle exception
	}
        return detail;
    }
    
    @GetMapping("/task")
    public void task() throws IOException {
    	todayDataSaveDBTask.ClDateSaveDbTask();
    }
    
    @GetMapping("/runTheTest")
    public BackResult<RunTestDomian> runTheTest(String type) {
//    	System.out.println(new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
//    	BackResult<RunTestDomian> result = foreignService.runTheTest("D:/test/6f072674b763400b89f00b412445fabb_18717717701.txt", "17671",String.valueOf(System.currentTimeMillis()),"13817367247");
    	BackResult<RunTestDomian> result = foreignService.theTest2("C:/Users/ChuangLan/Documents/QQEIM Files/2881526637/FileRecv/2(1).txt", "176721", "138173672147", "bendi", "0", type);
//    	System.out.println(new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() ));
    	return result;
    }
    
    public 
    
    
    @Value("${server.port}")
    String port;
    
    @RequestMapping("/hi")
    public String hi(String name){
    	
    	
    	MobileNumberSection section = mobileNumberSectionService.findByNumberSection(name.substring(0, 7));
    	
    	return "hi "+section.getNumberSection()+",i am from port:" +port;
    }
    
    public static void main1111(String[] args) {
    	
    	  //  183.194.70.206:59200  172.16.20.20:9300
    	try {
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster").put("client.transport.sniff", true)
					.put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("183.194.70.206"), 59300));
			
			SearchResponse scrollResp = client.prepareSearch("201701","201707")
					.addSort("_doc", SortOrder.ASC).setScroll(new TimeValue(60000))

					.setSize(100).get(); // max of 100 hits will be returned for
											// each scroll
			int i = 0;
			Map<String, String> map = new HashMap<String, String>();
			do {
				for (SearchHit hit : scrollResp.getHits().getHits()) {
					String json = hit.getSourceAsString();

					System.out.println("i=" + i + ":" + hit.getId() + "," + hit.getSourceAsString());
					 JSONObject backjson = (JSONObject) JSONObject.parse(json);
					
					 String account = backjson.getString("account");
					 map.put(account, account);

				}

				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
						.actionGet();
			} while (scrollResp.getHits().getHits().length != 0); // Zero hits mark
																	// the end of
																	// the scroll
																	// and the while
																	// loop.
			for (int k = 0; k < 100000; k++) {
				String kk = map.get(String.valueOf(k));
				if (kk == null) {
					System.out.println(k);
				}
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
    
    public static void main22222222222(String[] args) {
		BufferedReader br = null;
		try {
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
					
					List<MobileNumberSection> list = new ArrayList<MobileNumberSection>();
					
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
					section.setMobilePhoneType(str[8]);
					
					list.add(section);
					
					if (list.size() == 10000) {
						
					}
					
//					mongoTemplate
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
    
    public static void main(String[] args) {
    	try {
			
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster")
					.put("client.transport.sniff", true).put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));
			
					
			QueryBuilder qb = QueryBuilders.termsQuery("mobile", "13817367247");
			String monthstrs = "201710,201709,201708,201707,201706,201705,201704,201703,201702,201701";
			String[] monthList = monthstrs.split(",");
			for(String month: monthList){
				SearchResponse scrollResp = client.prepareSearch(month).setQuery(qb).setScroll(new TimeValue(60000)).setSize(1000).get();
				int num = scrollResp.getHits().getHits().length;
				
				
				int i = 0;
				Map<String, String> map = new HashMap<String, String>();
				do {
					for (SearchHit hit : scrollResp.getHits().getHits()) {
						String json = hit.getSourceAsString();

						System.out.println("i=" + i + ":" + hit.getId() + "," + hit.getSourceAsString());
						 JSONObject backjson = (JSONObject) JSONObject.parse(json);
						
						 String account = backjson.getString("account");
						 map.put(account, account);

					}

					scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
							.actionGet();
				} while (scrollResp.getHits().getHits().length != 0); 
				
				
				System.out.println("13817367247" + "号码, 在" + month + "月, 出现的次数为： " + num);
			}				
			
				
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("=====执行查询出现异常：" + e.getMessage());
		} 
	}
    
    
}
