package cn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.service.ForeignService;
import main.java.cn.common.BackResult;
import main.java.cn.domain.RunTestDomian;

@RestController
@RequestMapping("/credit")
public class CreditController {

	@Autowired
	private ForeignService foreignService;

	@RequestMapping(value = "/runTheTest", method = RequestMethod.GET)
	public BackResult<RunTestDomian> runTheTest(String fileUrl,String userId) {
		return foreignService.runTheTest(fileUrl, userId);
	}

}
