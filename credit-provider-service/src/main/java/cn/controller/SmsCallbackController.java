package cn.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.service.SpaceDetectionService;

@RestController
@RequestMapping("/smsCallback")
public class SmsCallbackController {

	@Autowired
	private SpaceDetectionService spaceDetectionService;
	
	@RequestMapping("/removeOrSave")
	public void removeOrSave(HttpServletRequest request, HttpServletResponse response,String mobile,String status,String notifyTime){
		spaceDetectionService.smSCallBack(mobile,status,notifyTime);
	}
	
}
