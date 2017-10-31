package cn.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.service.ApiMobileTestService;
import main.java.cn.common.BackResult;
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
	public BackResult<PageDomain<MobileTestLogDomain>> getPageByUserId(int pageNo, int pageSize, String userId){
		return apiMobileTestService.getPageByUserId(pageNo, pageSize, userId);
	}
}
