package cn.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * elasticsearch通用工具累
 * @author zhangsh Tel:13817876783
 * @date 2017年8月3日 上午11:15:09
 * @Title: ElasticSearchUtil
 * @ClassName: ElasticSearchUtil
 * @Description:
 */
public class ElasticSearchUtil {
	public static final Logger logger = LoggerFactory.getLogger(ElasticSearchUtil.class);
	public static TransportClient client;
	private BulkProcessor bulkProcessor;

	@SuppressWarnings("resource")
	public void init(String ip, int threadCount) {
		try {
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster")
					.put("client.transport.sniff", true).put("client.transport.ping_timeout", "25s").build();

			client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), 9300));

			bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
				@Override
				public void beforeBulk(long executionId, BulkRequest request) {
					//System.gc();
					logger.info("------------beforeBulk------------------");
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
					logger.info("------------afterBulk------------------");
					System.gc();
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
					logger.error("happen fail = " + failure.getMessage() + " cause = " + failure.getCause());
				}
				// setBulkActions 多少请求提交 默认1000个请求提交
				// setBulkSize 多大数据提交 默认5M提交
				// setFlushInterval 间隔多久提交 默认不提交
				// setConcurrentRequests 多少个并发请求 默认0
			}).setBulkActions(500).setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
					.setBackoffPolicy(BackoffPolicy.exponentialBackoff())
					.setFlushInterval(TimeValue.timeValueSeconds(1)).setConcurrentRequests(threadCount).build();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 往es库中添加数据
	 * @author zhangsh Tel:13817876783
	 * @date 2017年8月3日 上午11:16:06
	 * @Title add
	 * @Description 
	 * @param year
	 * @param month
	 * @param body void 
	 * @throws
	 */
	public BulkProcessor add(String year, String month, String body) {
		return bulkProcessor.add(new IndexRequest(year, month).source(body, XContentType.JSON));
	}
	/**
	 * 初始化mapping
	 * @author zhangsh Tel:13817876783
	 * @date 2017年8月3日 下午3:07:49
	 * @Title adminPutMapping
	 * @Description 
	 * @param yearMonth
	 * @throws IOException void 
	 * @throws
	 */
	public void adminPutMapping(String yearMonth) throws IOException {
		CreateIndexRequestBuilder cib = client.admin().indices().prepareCreate(yearMonth);

		XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject("properties") // 设置之定义字段

				.startObject("account")// 字段id
				.field("type", "keyword")// 设置数据类型
				.field("index", "not_analyzed").endObject()

				.startObject("content").field("type", "text").field("index", "analyzed").field("analyzer", "ik_max_word").field("search_analyzer", "ik_max_word").endObject()

				.startObject("signature").field("type", "keyword").field("index", "not_analyzed").endObject()

				.startObject("msgSrcCode").field("type", "keyword").field("index", "not_analyzed").endObject()

				.startObject("mobile").field("type", "keyword").field("index", "not_analyzed").endObject()

				.startObject("sendTime").field("type", "date")
				.field("format", "strict_date_optional_time||epoch_millis||yyyy-MM-dd HH:mm:ss")
				.field("index", "not_analyzed").endObject()

				.startObject("reportTime").field("type", "date")
				.field("format", "strict_date_optional_time||epoch_millis||yyyy-MM-dd HH:mm:ss")
				.field("index", "not_analyzed").endObject().startObject("rsTime").field("type", "integer")
				.field("index", "not_analyzed").endObject()

				.startObject("monthSeq").field("type", "keyword").field("index", "not_analyzed").endObject()

				.startObject("spCode").field("type", "keyword").field("index", "not_analyzed").endObject()

				.startObject("productId").field("type", "keyword").field("index", "not_analyzed")

				.endObject()

				.startObject("clientMsgId").field("type", "keyword").field("index", "not_analyzed").endObject()

				.startObject("accountType").field("type", "keyword").field("index", "not_analyzed").endObject()
				.startObject("submitType").field("type", "keyword").field("index", "not_analyzed").endObject()
				.startObject("city").field("type", "keyword").field("index", "not_analyzed").endObject()
				.startObject("delivrd").field("type", "keyword").field("index", "not_analyzed").endObject()
				.startObject("province").field("type", "keyword").field("index", "not_analyzed").endObject()
				.startObject("year").field("type", "keyword").field("index", "not_analyzed").endObject()
				.startObject("month").field("type", "keyword").field("index", "not_analyzed").endObject().endObject()
				.endObject();
		cib.addMapping("yearMonth", mapping);

		CreateIndexResponse response = cib.execute().actionGet();
		System.out.println(response);

	}

	/**
	 * 关闭刷新es中的缓存数据
	 * @author zhangsh Tel:13817876783
	 * @date 2017年8月3日 上午11:17:04
	 * @Title close
	 * @Description  void 
	 * @throws
	 */
	public void close() {
		bulkProcessor.flush();
		bulkProcessor.close();
	}
	/**
	 * 判断当前索引是否已经mapping
	 * @author zhangsh Tel:13817876783
	 * @date 2017年8月4日 上午11:53:45
	 * @Title judgeIndexMapping
	 * @Description 
	 * @param indexMapping
	 * @return boolean 
	 * @throws
	 */
	public boolean judgeIndexMapping(String indexMapping) {
		boolean bl=false;
		ClusterStateResponse response = client.admin().cluster().prepareState().execute().actionGet();
		String[] indexs = response.getState().getMetaData().getConcreteAllIndices();// 获取所有的索引
		for (String index : indexs) {
			if(indexMapping.equals(index)){
				bl=true;
				break;
			}
			
		}
		return bl;
	}

}
