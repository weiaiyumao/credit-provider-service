package com.eureka.provider.credit_provider_service;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cn.service.ForeignService;
import main.java.cn.common.BackResult;
import main.java.cn.domain.RunTestDomian;

public class TestCase {
	
	@Autowired
    private ForeignService foreignService;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		System.out.println(1111);
    	BackResult<RunTestDomian> result = foreignService.runTheTest("C:\\Users\\ChuangLan\\Desktop\\电销号码.txt", "17671",String.valueOf(System.currentTimeMillis()),"13817367247");
	}

}
