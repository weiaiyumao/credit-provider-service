package cn.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.service.ApiMobileTestService;
import main.java.cn.common.BackResult;
import main.java.cn.domain.ApiLogPageDomain;
import main.java.cn.domain.MobileInfoDomain;
import main.java.cn.domain.MobileTestLogDomain;
import main.java.cn.domain.page.PageDomain;

@RestController
@RequestMapping("/apiMobileTest")
public class ApiMobileTestController {

	@Autowired
	private ApiMobileTestService apiMobileTestService;
	
	@RequestMapping(value = "/findByMobileNumbers", method = RequestMethod.POST)
	public BackResult<List<MobileInfoDomain>> findByMobileNumbers(String mobileNumbers,String userId){
		return apiMobileTestService.findByMobileNumbers(mobileNumbers,userId);
	}
	
	@RequestMapping(value = "/getPageByUserId", method = RequestMethod.POST)
	public BackResult<PageDomain<MobileTestLogDomain>> getPageByUserId(int pageNo, int pageSize, String userId,String type){
		return apiMobileTestService.getPageByUserId(pageNo, pageSize, userId,type);
	}
	
	@RequestMapping(value = "/getPageByCustomerId", method = RequestMethod.POST)
	public BackResult<PageDomain<ApiLogPageDomain>> getPageByCustomerId(int pageNo, int pageSize, String customerId, String method){
		return apiMobileTestService.getPageByCustomerId(pageNo, pageSize, customerId,method);
	}
	
	@RequestMapping(value = "/findByMobile", method = RequestMethod.POST)
	public BackResult<MobileInfoDomain> findByMobile(String mobile,String userId){
		return apiMobileTestService.findByMobile(mobile,userId);
	}
	
	@RequestMapping(value = "/findByMobileToAmi", method = RequestMethod.POST)
	public BackResult<MobileInfoDomain> findByMobileToAmi(String mobile,String userId,String method){
		return apiMobileTestService.findByMobileToAmi(mobile,userId,method);
	}
}
