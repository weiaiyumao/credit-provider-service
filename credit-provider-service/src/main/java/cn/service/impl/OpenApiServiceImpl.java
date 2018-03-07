package cn.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import cn.service.OpenApiService;
import cn.utils.HttpClient;

@Service
public class OpenApiServiceImpl implements OpenApiService{

	@Value("${amiUrl}")
	private String amiUrl;
	
	@Override
	public String getCheckMobileStatue(JSONObject jsono) {
		return HttpClient.postJSON(amiUrl, jsono.toJSONString());
	}
	

}
