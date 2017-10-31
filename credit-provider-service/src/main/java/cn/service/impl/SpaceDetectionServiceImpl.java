package cn.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import cn.entity.SpaceMobile;
import cn.entity.base.BaseMobileDetail;
import cn.entity.cm.CM134;
import cn.entity.cm.CM135;
import cn.entity.cm.CM136;
import cn.entity.cm.CM137;
import cn.entity.cm.CM138;
import cn.entity.cm.CM139;
import cn.entity.cm.CM147;
import cn.entity.cm.CM150;
import cn.entity.cm.CM151;
import cn.entity.cm.CM152;
import cn.entity.cm.CM157;
import cn.entity.cm.CM158;
import cn.entity.cm.CM159;
import cn.entity.cm.CM1705;
import cn.entity.cm.CM178;
import cn.entity.cm.CM182;
import cn.entity.cm.CM183;
import cn.entity.cm.CM184;
import cn.entity.cm.CM187;
import cn.entity.cm.CM188;
import cn.entity.ct.CT133;
import cn.entity.ct.CT153;
import cn.entity.ct.CT1700;
import cn.entity.ct.CT177;
import cn.entity.ct.CT180;
import cn.entity.ct.CT181;
import cn.entity.ct.CT189;
import cn.entity.cu.CU130;
import cn.entity.cu.CU131;
import cn.entity.cu.CU132;
import cn.entity.cu.CU145;
import cn.entity.cu.CU155;
import cn.entity.cu.CU156;
import cn.entity.cu.CU1709;
import cn.entity.cu.CU176;
import cn.entity.cu.CU185;
import cn.entity.cu.CU186;
import cn.entity.unknown.UnknownMobileDetail;
import cn.service.SpaceDetectionService;
import cn.service.SpaceMobileService;
import cn.service.cm.CM134Service;
import cn.service.cm.CM135Service;
import cn.service.cm.CM136Service;
import cn.service.cm.CM137Service;
import cn.service.cm.CM138Service;
import cn.service.cm.CM139Service;
import cn.service.cm.CM147Service;
import cn.service.cm.CM150Service;
import cn.service.cm.CM151Service;
import cn.service.cm.CM152Service;
import cn.service.cm.CM157Service;
import cn.service.cm.CM158Service;
import cn.service.cm.CM159Service;
import cn.service.cm.CM1705Service;
import cn.service.cm.CM178Service;
import cn.service.cm.CM182Service;
import cn.service.cm.CM183Service;
import cn.service.cm.CM184Service;
import cn.service.cm.CM187Service;
import cn.service.cm.CM188Service;
import cn.service.ct.CT133Service;
import cn.service.ct.CT153Service;
import cn.service.ct.CT1700Service;
import cn.service.ct.CT177Service;
import cn.service.ct.CT180Service;
import cn.service.ct.CT181Service;
import cn.service.ct.CT189Service;
import cn.service.cu.CU130Service;
import cn.service.cu.CU131Service;
import cn.service.cu.CU132Service;
import cn.service.cu.CU145Service;
import cn.service.cu.CU155Service;
import cn.service.cu.CU156Service;
import cn.service.cu.CU1709Service;
import cn.service.cu.CU176Service;
import cn.service.cu.CU185Service;
import cn.service.cu.CU186Service;
import cn.service.unknown.UnknownMobileDetailService;
import cn.task.helper.MobileDetailHelper;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import main.java.cn.common.BackResult;
import main.java.cn.domain.SpacePhoneDomain;

@Service
public class SpaceDetectionServiceImpl implements SpaceDetectionService {

	@Autowired
	private CT1700Service cT1700Service;

	@Autowired
	private CM1705Service cM1705Service;

	@Autowired
	private CU1709Service cU1709Service;

	@Autowired
	private UnknownMobileDetailService unknownMobileDetailService;

	@Autowired
	private CM134Service cM134Service;

	@Autowired
	private CM135Service cM135Service;

	@Autowired
	private CM136Service cM136Service;

	@Autowired
	private CM137Service cM137Service;

	@Autowired
	private CM138Service cM138Service;

	@Autowired
	private CM139Service cM139Service;

	@Autowired
	private CM147Service cM147Service;

	@Autowired
	private CM150Service cM150Service;

	@Autowired
	private CM151Service cM151Service;

	@Autowired
	private CM152Service cM152Service;

	@Autowired
	private CM157Service cM157Service;

	@Autowired
	private CM158Service cM158Service;

	@Autowired
	private CM159Service cM159Service;

	@Autowired
	private CM178Service cM178Service;

	@Autowired
	private CM182Service cM182Service;

	@Autowired
	private CM183Service cM183Service;

	@Autowired
	private CM184Service cM184Service;

	@Autowired
	private CM187Service cM187Service;

	@Autowired
	private CM188Service cM188Service;

	@Autowired
	private CT133Service cT133Service;

	@Autowired
	private CT153Service cT153Service;

	@Autowired
	private CT177Service cT177Service;

	@Autowired
	private CT180Service cT180Service;

	@Autowired
	private CT181Service cT181Service;

	@Autowired
	private CT189Service cT189Service;

	@Autowired
	private CU130Service cU130Service;

	@Autowired
	private CU131Service cU131Service;

	@Autowired
	private CU132Service cU132Service;

	@Autowired
	private CU145Service cU145Service;

	@Autowired
	private CU155Service cU155Service;

	@Autowired
	private CU156Service cU156Service;

	@Autowired
	private CU176Service cU176Service;

	@Autowired
	private CU185Service cU185Service;

	@Autowired
	private CU186Service cU186Service;
	
	@Autowired
	private SpaceMobileService spaceMobileService;
	
	@Autowired
    private MongoTemplate mongoTemplate;
	
	private final static Logger logger = LoggerFactory.getLogger(SpaceDetectionService.class);
	
	@Override
	public BaseMobileDetail findByMobile(String mobile) {
		return this.getByMobile(mobile);
	}

	public BaseMobileDetail getByMobile(String mobile) {

		String mob = mobile.substring(0, 3);
		BaseMobileDetail detail = null;

		switch (mob) {
		case "170":
			// 分3个 1700 1705 1709
			String mobi = mobile.substring(0, 4);
			if (mobi.equals("1700")) {

				List<CT1700> list = cT1700Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}

			} else if (mobi.equals("1705")) {

				List<CM1705> list = cM1705Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}

			} else if (mobi.equals("1709")) {

				List<CU1709> list = cU1709Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}

			} else {

				List<UnknownMobileDetail> list = unknownMobileDetailService.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			}
			break;
		case "134":

			List<CM134> listCM134 = cM134Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM134)) {
				detail = listCM134.get(0);
			}

			break;
		case "135":

			List<CM135> listCM135 = cM135Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM135)) {
				detail = listCM135.get(0);
			}

			break;
		case "136":

			List<CM136> listCM136 = cM136Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM136)) {
				detail = listCM136.get(0);
			}

			break;
		case "137":

			List<CM137> listCM137 = cM137Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM137)) {
				detail = listCM137.get(0);
			}

			break;
		case "138":

			List<CM138> listCM138 = cM138Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM138)) {
				detail = listCM138.get(0);
			}

			break;
		case "139":

			List<CM139> listCM139 = cM139Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM139)) {
				detail = listCM139.get(0);
			}

			break;
		case "147":

			List<CM147> listCM147 = cM147Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM147)) {
				detail = listCM147.get(0);
			}

			break;
		case "150":

			List<CM150> listCM150 = cM150Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM150)) {
				detail = listCM150.get(0);
			}

			break;
		case "151":

			List<CM151> listCM151 = cM151Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM151)) {
				detail = listCM151.get(0);
			}

			break;
		case "152":

			List<CM152> listCM152 = cM152Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM152)) {
				detail = listCM152.get(0);
			}

			break;
		case "157":

			List<CM157> listCM157 = cM157Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM157)) {
				detail = listCM157.get(0);
			}

			break;
		case "158":

			List<CM158> listCM158 = cM158Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM158)) {
				detail = listCM158.get(0);
			}

			break;
		case "159":

			List<CM159> listCM159 = cM159Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM159)) {
				detail = listCM159.get(0);
			}

			break;
		case "178":

			List<CM178> listCM178 = cM178Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM178)) {
				detail = listCM178.get(0);
			}

			break;
		case "182":

			List<CM182> listCM182 = cM182Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM182)) {
				detail = listCM182.get(0);
			}

			break;
		case "183":

			List<CM183> listCM183 = cM183Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM183)) {
				detail = listCM183.get(0);
			}

			break;
		case "184":

			List<CM184> listCM184 = cM184Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM184)) {
				detail = listCM184.get(0);
			}

			break;
		case "187":

			List<CM187> listCM187 = cM187Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM187)) {
				detail = listCM187.get(0);
			}

			break;
		case "188":

			List<CM188> listCM188 = cM188Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCM188)) {
				detail = listCM188.get(0);
			}

			break;
		case "133":

			List<CT133> listCT133 = cT133Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCT133)) {
				detail = listCT133.get(0);
			}

			break;
		case "153":

			List<CT153> listCT153 = cT153Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCT153)) {
				detail = listCT153.get(0);
			}

			break;
		case "177":

			List<CT177> listCT177 = cT177Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCT177)) {
				detail = listCT177.get(0);
			}

			break;
		case "180":

			List<CT180> listCT180 = cT180Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCT180)) {
				detail = listCT180.get(0);
			}

			break;
		case "181":

			List<CT181> listCT181 = cT181Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCT181)) {
				detail = listCT181.get(0);
			}

			break;
		case "189":

			List<CT189> listCT189 = cT189Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCT189)) {
				detail = listCT189.get(0);
			}

			break;
		case "130":

			List<CU130> listCU130 = cU130Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCU130)) {
				detail = listCU130.get(0);
			}

			break;
		case "131":

			List<CU131> listCU131 = cU131Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCU131)) {
				detail = listCU131.get(0);
			}

			break;
		case "132":

			List<CU132> listCU132 = cU132Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCU132)) {
				detail = listCU132.get(0);
			}

			break;
		case "145":

			List<CU145> listCU145 = cU145Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCU145)) {
				detail = listCU145.get(0);
			}

			break;
		case "155":

			List<CU155> listCU155 = cU155Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCU155)) {
				detail = listCU155.get(0);
			}

			break;
		case "156":

			List<CU156> listCU156 = cU156Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCU156)) {
				detail = listCU156.get(0);
			}

			break;
		case "176":

			List<CU176> listCU176 = cU176Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCU176)) {
				detail = listCU176.get(0);
			}

			break;
		case "185":

			List<CU185> listCU185 = cU185Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCU185)) {
				detail = listCU185.get(0);
			}

			break;
		case "186":

			List<CU186> listCU186 = cU186Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCU186)) {
				detail = listCU186.get(0);
			}

			break;
		default:

			List<UnknownMobileDetail> listUnknownMobileDetail = unknownMobileDetailService.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listUnknownMobileDetail)) {
				detail = listUnknownMobileDetail.get(0);
			}

			break;
		}

		return detail;

	}

	@Override
	public BaseMobileDetail findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		String mob = mobile.substring(0, 3);
		BaseMobileDetail detail = null;

		switch (mob) {
		case "170":
			// 分3个 1700 1705 1709
			String mobi = mobile.substring(0, 4);
			if (mobi.equals("1700")) {

				List<CT1700> list = cT1700Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}

			} else if (mobi.equals("1705")) {

				List<CM1705> list = cM1705Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}

			} else if (mobi.equals("1709")) {

				List<CU1709> list = cU1709Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}

			} else {

				List<UnknownMobileDetail> list = unknownMobileDetailService.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			}
			break;
		case "134":

			List<CM134> listCM134 = cM134Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM134)) {
				detail = listCM134.get(0);
			}

			break;
		case "135":

			List<CM135> listCM135 = cM135Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM135)) {
				detail = listCM135.get(0);
			}

			break;
		case "136":

			List<CM136> listCM136 = cM136Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM136)) {
				detail = listCM136.get(0);
			}

			break;
		case "137":

			List<CM137> listCM137 = cM137Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM137)) {
				detail = listCM137.get(0);
			}

			break;
		case "138":

			List<CM138> listCM138 = cM138Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM138)) {
				detail = listCM138.get(0);
			}

			break;
		case "139":

			List<CM139> listCM139 = cM139Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM139)) {
				detail = listCM139.get(0);
			}

			break;
		case "147":

			List<CM147> listCM147 = cM147Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM147)) {
				detail = listCM147.get(0);
			}

			break;
		case "150":

			List<CM150> listCM150 = cM150Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM150)) {
				detail = listCM150.get(0);
			}

			break;
		case "151":

			List<CM151> listCM151 = cM151Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM151)) {
				detail = listCM151.get(0);
			}

			break;
		case "152":

			List<CM152> listCM152 = cM152Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM152)) {
				detail = listCM152.get(0);
			}

			break;
		case "157":

			List<CM157> listCM157 = cM157Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM157)) {
				detail = listCM157.get(0);
			}

			break;
		case "158":

			List<CM158> listCM158 = cM158Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM158)) {
				detail = listCM158.get(0);
			}

			break;
		case "159":

			List<CM159> listCM159 = cM159Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM159)) {
				detail = listCM159.get(0);
			}

			break;
		case "178":

			List<CM178> listCM178 = cM178Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM178)) {
				detail = listCM178.get(0);
			}

			break;
		case "182":

			List<CM182> listCM182 = cM182Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM182)) {
				detail = listCM182.get(0);
			}

			break;
		case "183":

			List<CM183> listCM183 = cM183Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM183)) {
				detail = listCM183.get(0);
			}

			break;
		case "184":

			List<CM184> listCM184 = cM184Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM184)) {
				detail = listCM184.get(0);
			}

			break;
		case "187":

			List<CM187> listCM187 = cM187Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM187)) {
				detail = listCM187.get(0);
			}

			break;
		case "188":

			List<CM188> listCM188 = cM188Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCM188)) {
				detail = listCM188.get(0);
			}

			break;
		case "133":

			List<CT133> listCT133 = cT133Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCT133)) {
				detail = listCT133.get(0);
			}

			break;
		case "153":

			List<CT153> listCT153 = cT153Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCT153)) {
				detail = listCT153.get(0);
			}

			break;
		case "177":

			List<CT177> listCT177 = cT177Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCT177)) {
				detail = listCT177.get(0);
			}

			break;
		case "180":

			List<CT180> listCT180 = cT180Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCT180)) {
				detail = listCT180.get(0);
			}

			break;
		case "181":

			List<CT181> listCT181 = cT181Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCT181)) {
				detail = listCT181.get(0);
			}

			break;
		case "189":

			List<CT189> listCT189 = cT189Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCT189)) {
				detail = listCT189.get(0);
			}

			break;
		case "130":

			List<CU130> listCU130 = cU130Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCU130)) {
				detail = listCU130.get(0);
			}

			break;
		case "131":

			List<CU131> listCU131 = cU131Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCU131)) {
				detail = listCU131.get(0);
			}

			break;
		case "132":

			List<CU132> listCU132 = cU132Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCU132)) {
				detail = listCU132.get(0);
			}

			break;
		case "145":

			List<CU145> listCU145 = cU145Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCU145)) {
				detail = listCU145.get(0);
			}

			break;
		case "155":

			List<CU155> listCU155 = cU155Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCU155)) {
				detail = listCU155.get(0);
			}

			break;
		case "156":

			List<CU156> listCU156 = cU156Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCU156)) {
				detail = listCU156.get(0);
			}

			break;
		case "176":

			List<CU176> listCU176 = cU176Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCU176)) {
				detail = listCU176.get(0);
			}

			break;
		case "185":

			List<CU185> listCU185 = cU185Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCU185)) {
				detail = listCU185.get(0);
			}

			break;
		case "186":

			List<CU186> listCU186 = cU186Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCU186)) {
				detail = listCU186.get(0);
			}

			break;
		default:

			List<UnknownMobileDetail> listUnknownMobileDetail = unknownMobileDetailService.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listUnknownMobileDetail)) {
				detail = listUnknownMobileDetail.get(0);
			}

			break;
		}

		return detail;
	}

	@Override
	public BackResult<List<SpacePhoneDomain>> findSpacePhoneByMobile(String mobiles) {
		
		BackResult<List<SpacePhoneDomain>> result = new BackResult<List<SpacePhoneDomain>>();
		
		String[] str = mobiles.split(",");
		
		for (String mobile : str) {
			// 1、检测空号库 如果存在 直接返回结果
			SpaceMobile spaceMobile = spaceMobileService.findByMobile(mobile);
			
			if (null != spaceMobile) {
				
			}
			// 2、本库号码查询
			
			// 3、本库号码段查询
			
			// 4、发送短信验证空号
		}
		
		return result;
	}

	@Override
	public void smSCallBack(String mobile,String status,String notifyTime) {
		
		try {
			
			BaseMobileDetail detail = MobileDetailHelper.getInstance().getBaseMobileDetail(mobile);
			detail.setMobile(mobile);
			detail.setDelivrd(status);
			detail.setReportTime(DateUtils.StringToDate(DateUtils.getCurrentTimeMillis().substring(0,2)+notifyTime));
			
			// 检测 3个月内
			BaseMobileDetail baseMobileDetail = this.findByMobileAndReportTime(detail.getMobile(), DateUtils.addDay(DateUtils.getCurrentDateTime(), -270),
					DateUtils.getCurrentDateTime());
			
			// 更新数据库数据
			if (null != baseMobileDetail) {
				mongoTemplate.remove(baseMobileDetail);
				
			}
			mongoTemplate.save(detail);
			
		} catch (Exception e) {
			logger.error("更新数据出现系统异常：" + e.getMessage());
		}
	}



}
