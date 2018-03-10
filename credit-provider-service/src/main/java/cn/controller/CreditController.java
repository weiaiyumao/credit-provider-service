package cn.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.service.FileUploadService;
import cn.service.ForeignService;
import main.java.cn.common.BackResult;
import main.java.cn.domain.CvsFilePathDomain;
import main.java.cn.domain.FileUploadDomain;
import main.java.cn.domain.RunTestDomian;
import main.java.cn.domain.page.PageDomain;

@RestController
@RequestMapping("/credit")
public class CreditController {

	@Autowired
	private ForeignService foreignService;
	
	@Autowired
	private FileUploadService fileUploadService;
	
	@RequestMapping(value = "/runTheTest", method = RequestMethod.GET)
	public BackResult<RunTestDomian> runTheTest(HttpServletRequest request, HttpServletResponse response,String fileUrl,String userId, String timestamp,String mobile) {
		
		return foreignService.runTheTest(fileUrl, userId,timestamp,mobile);
	}
	
	@RequestMapping(value = "/theTest", method = RequestMethod.POST)
	public BackResult<RunTestDomian> theTest(String fileUrl,String userId, String source,String mobile,String startLine,String type) {
		return foreignService.theTest(fileUrl, userId, mobile, source, startLine,type);
	}
	
	@RequestMapping(value = "/findByUserId", method = RequestMethod.GET)
	public BackResult<List<CvsFilePathDomain>> findByUserId(HttpServletRequest request, HttpServletResponse response,String userId) {
		
		return foreignService.findByUserId(userId);
	}
	
	@RequestMapping(value = "/deleteCvsByIds", method = RequestMethod.GET)
	public BackResult<Boolean> deleteCvsByIds(HttpServletRequest request, HttpServletResponse response,String ids,String userId){
		return foreignService.deleteCvsByIds(ids, userId);
	}

	@RequestMapping(value = "/getCVSPageByUserId", method = RequestMethod.POST)
	public BackResult<PageDomain<CvsFilePathDomain>> getPageByUserId(int pageNo, int pageSize, String userId){
		return foreignService.getPageByUserId(pageNo, pageSize, userId);
	}
	
	@RequestMapping(value = "/saveFileUpload", method = RequestMethod.POST)
	public BackResult<FileUploadDomain> saveFileUpload(@RequestBody FileUploadDomain domain){
		return fileUploadService.save(domain);
	}
	
	@RequestMapping(value = "/findFileUploadById", method = RequestMethod.POST)
	public BackResult<FileUploadDomain> findFileUploadById(String id){
		return fileUploadService.findById(id);
	}
	
}
