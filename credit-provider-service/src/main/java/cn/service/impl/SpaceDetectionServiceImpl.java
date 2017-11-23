package cn.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import cn.entity.cm.CM1703;
import cn.entity.cm.CM1705;
import cn.entity.cm.CM1706;
import cn.entity.cm.CM178;
import cn.entity.cm.CM182;
import cn.entity.cm.CM183;
import cn.entity.cm.CM184;
import cn.entity.cm.CM187;
import cn.entity.cm.CM188;
import cn.entity.cm.KCM134;
import cn.entity.cm.KCM135;
import cn.entity.cm.KCM136;
import cn.entity.cm.KCM137;
import cn.entity.cm.KCM138;
import cn.entity.cm.KCM139;
import cn.entity.cm.KCM147;
import cn.entity.cm.KCM150;
import cn.entity.cm.KCM151;
import cn.entity.cm.KCM152;
import cn.entity.cm.KCM157;
import cn.entity.cm.KCM158;
import cn.entity.cm.KCM159;
import cn.entity.cm.KCM1703;
import cn.entity.cm.KCM1705;
import cn.entity.cm.KCM1706;
import cn.entity.cm.KCM178;
import cn.entity.cm.KCM182;
import cn.entity.cm.KCM183;
import cn.entity.cm.KCM184;
import cn.entity.cm.KCM187;
import cn.entity.cm.KCM188;
import cn.entity.ct.CT133;
import cn.entity.ct.CT153;
import cn.entity.ct.CT1700;
import cn.entity.ct.CT1701;
import cn.entity.ct.CT1702;
import cn.entity.ct.CT173;
import cn.entity.ct.CT177;
import cn.entity.ct.CT180;
import cn.entity.ct.CT181;
import cn.entity.ct.CT189;
import cn.entity.ct.KCT133;
import cn.entity.ct.KCT153;
import cn.entity.ct.KCT1700;
import cn.entity.ct.KCT1701;
import cn.entity.ct.KCT1702;
import cn.entity.ct.KCT173;
import cn.entity.ct.KCT177;
import cn.entity.ct.KCT180;
import cn.entity.ct.KCT181;
import cn.entity.ct.KCT189;
import cn.entity.cu.CU130;
import cn.entity.cu.CU131;
import cn.entity.cu.CU132;
import cn.entity.cu.CU145;
import cn.entity.cu.CU155;
import cn.entity.cu.CU156;
import cn.entity.cu.CU1704;
import cn.entity.cu.CU1707;
import cn.entity.cu.CU1708;
import cn.entity.cu.CU1709;
import cn.entity.cu.CU171;
import cn.entity.cu.CU175;
import cn.entity.cu.CU176;
import cn.entity.cu.CU185;
import cn.entity.cu.CU186;
import cn.entity.cu.KCU130;
import cn.entity.cu.KCU131;
import cn.entity.cu.KCU132;
import cn.entity.cu.KCU145;
import cn.entity.cu.KCU155;
import cn.entity.cu.KCU156;
import cn.entity.cu.KCU1704;
import cn.entity.cu.KCU1707;
import cn.entity.cu.KCU1708;
import cn.entity.cu.KCU1709;
import cn.entity.cu.KCU171;
import cn.entity.cu.KCU175;
import cn.entity.cu.KCU176;
import cn.entity.cu.KCU185;
import cn.entity.cu.KCU186;
import cn.entity.unknown.KUnknownMobileDetail;
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
import cn.service.cm.CM1703Service;
import cn.service.cm.CM1705Service;
import cn.service.cm.CM1706Service;
import cn.service.cm.CM178Service;
import cn.service.cm.CM182Service;
import cn.service.cm.CM183Service;
import cn.service.cm.CM184Service;
import cn.service.cm.CM187Service;
import cn.service.cm.CM188Service;
import cn.service.cm.KCM134Service;
import cn.service.cm.KCM135Service;
import cn.service.cm.KCM136Service;
import cn.service.cm.KCM137Service;
import cn.service.cm.KCM138Service;
import cn.service.cm.KCM139Service;
import cn.service.cm.KCM147Service;
import cn.service.cm.KCM150Service;
import cn.service.cm.KCM151Service;
import cn.service.cm.KCM152Service;
import cn.service.cm.KCM157Service;
import cn.service.cm.KCM158Service;
import cn.service.cm.KCM159Service;
import cn.service.cm.KCM1703Service;
import cn.service.cm.KCM1705Service;
import cn.service.cm.KCM1706Service;
import cn.service.cm.KCM178Service;
import cn.service.cm.KCM182Service;
import cn.service.cm.KCM183Service;
import cn.service.cm.KCM184Service;
import cn.service.cm.KCM187Service;
import cn.service.cm.KCM188Service;
import cn.service.ct.CT133Service;
import cn.service.ct.CT153Service;
import cn.service.ct.CT1700Service;
import cn.service.ct.CT1701Service;
import cn.service.ct.CT1702Service;
import cn.service.ct.CT173Service;
import cn.service.ct.CT177Service;
import cn.service.ct.CT180Service;
import cn.service.ct.CT181Service;
import cn.service.ct.CT189Service;
import cn.service.ct.KCT133Service;
import cn.service.ct.KCT153Service;
import cn.service.ct.KCT1700Service;
import cn.service.ct.KCT1701Service;
import cn.service.ct.KCT1702Service;
import cn.service.ct.KCT173Service;
import cn.service.ct.KCT177Service;
import cn.service.ct.KCT180Service;
import cn.service.ct.KCT181Service;
import cn.service.ct.KCT189Service;
import cn.service.cu.CU130Service;
import cn.service.cu.CU131Service;
import cn.service.cu.CU132Service;
import cn.service.cu.CU145Service;
import cn.service.cu.CU155Service;
import cn.service.cu.CU156Service;
import cn.service.cu.CU1704Service;
import cn.service.cu.CU1707Service;
import cn.service.cu.CU1708Service;
import cn.service.cu.CU1709Service;
import cn.service.cu.CU171Service;
import cn.service.cu.CU175Service;
import cn.service.cu.CU176Service;
import cn.service.cu.CU185Service;
import cn.service.cu.CU186Service;
import cn.service.cu.KCU130Service;
import cn.service.cu.KCU131Service;
import cn.service.cu.KCU132Service;
import cn.service.cu.KCU145Service;
import cn.service.cu.KCU155Service;
import cn.service.cu.KCU156Service;
import cn.service.cu.KCU1704Service;
import cn.service.cu.KCU1707Service;
import cn.service.cu.KCU1708Service;
import cn.service.cu.KCU1709Service;
import cn.service.cu.KCU171Service;
import cn.service.cu.KCU175Service;
import cn.service.cu.KCU176Service;
import cn.service.cu.KCU185Service;
import cn.service.cu.KCU186Service;
import cn.service.unknown.KUnknownMobileDetailService;
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
	private KCT1700Service kCT1700Service;

	@Autowired
	private CT1701Service cT1701Service;
	
	@Autowired
	private CT1702Service cT1702Service;
	
	@Autowired
	private CM1703Service cM1703Service;
	
	@Autowired
	private CU1704Service cU1704Service;
	
	@Autowired
	private CM1705Service cM1705Service;

	@Autowired
	private CM1706Service cM1706Service;
	
	@Autowired
	private CU1707Service cU1707Service;
	
	@Autowired
	private CU1708Service cU1708Service;
	
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
	private CT173Service cT173Service;

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
	private CU171Service cU171Service;

	@Autowired
	private CU175Service cU175Service;
	
	@Autowired
	private CU176Service cU176Service;

	@Autowired
	private CU185Service cU185Service;

	@Autowired
	private CU186Service cU186Service;
	
	@Autowired
	private KCT1701Service kCT1701Service;
	
	@Autowired
	private KCT1702Service kCT1702Service;
	
	@Autowired
	private KCM1703Service kCM1703Service;
	
	@Autowired
	private KCU1704Service kCU1704Service;
	
	@Autowired
	private KCM1705Service kCM1705Service;

	@Autowired
	private KCM1706Service kCM1706Service;
	
	@Autowired
	private KCU1707Service kCU1707Service;
	
	@Autowired
	private KCU1708Service kCU1708Service;
	
	@Autowired
	private KCU1709Service kCU1709Service;

	@Autowired
	private KUnknownMobileDetailService kUnknownMobileDetailService;

	@Autowired
	private KCM134Service kCM134Service;

	@Autowired
	private KCM135Service kCM135Service;

	@Autowired
	private KCM136Service kCM136Service;

	@Autowired
	private KCM137Service kCM137Service;

	@Autowired
	private KCM138Service kCM138Service;

	@Autowired
	private KCM139Service kCM139Service;

	@Autowired
	private KCM147Service kCM147Service;

	@Autowired
	private KCM150Service kCM150Service;

	@Autowired
	private KCM151Service kCM151Service;

	@Autowired
	private KCM152Service kCM152Service;

	@Autowired
	private KCM157Service kCM157Service;

	@Autowired
	private KCM158Service kCM158Service;

	@Autowired
	private KCM159Service kCM159Service;

	@Autowired
	private KCM178Service kCM178Service;

	@Autowired
	private KCM182Service kCM182Service;

	@Autowired
	private KCM183Service kCM183Service;

	@Autowired
	private KCM184Service kCM184Service;

	@Autowired
	private KCM187Service kCM187Service;

	@Autowired
	private KCM188Service kCM188Service;

	@Autowired
	private KCT133Service kCT133Service;

	@Autowired
	private KCT153Service kCT153Service;
	
	@Autowired
	private KCT173Service kCT173Service;

	@Autowired
	private KCT177Service kCT177Service;

	@Autowired
	private KCT180Service kCT180Service;

	@Autowired
	private KCT181Service kCT181Service;

	@Autowired
	private KCT189Service kCT189Service;

	@Autowired
	private KCU130Service kCU130Service;

	@Autowired
	private KCU131Service kCU131Service;

	@Autowired
	private KCU132Service kCU132Service;

	@Autowired
	private KCU145Service kCU145Service;

	@Autowired
	private KCU155Service kCU155Service;

	@Autowired
	private KCU156Service kCU156Service;

	@Autowired
	private KCU171Service kCU171Service;

	@Autowired
	private KCU175Service kCU175Service;
	
	@Autowired
	private KCU176Service kCU176Service;

	@Autowired
	private KCU185Service kCU185Service;

	@Autowired
	private KCU186Service kCU186Service;
	
	@Autowired
	private SpaceMobileService spaceMobileService;
	
	@Autowired
    private MongoTemplate mongoTemplate;
	
	private final static Logger logger = LoggerFactory.getLogger(SpaceDetectionService.class);
	
	@Override
	public BaseMobileDetail findByMobile(String mobile) {
		return this.getByMobile(mobile);
	}
	
	@Override
	public BaseMobileDetail findByMobileFromNull(String mobile) {
		return this.getByMobileFromNull(mobile);
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
			} else if (mobi.equals("1701")) {

				List<CT1701> list = cT1701Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1702")) {

				List<CT1702> list = cT1702Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1703")) {

				List<CM1703> list = cM1703Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1704")) {

				List<CU1704> list = cU1704Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
				
			} else if (mobi.equals("1705")) {

				List<CM1705> list = cM1705Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1706")) {

				List<CM1706> list = cM1706Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1707")) {

				List<CU1707> list = cU1707Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1708")) {

				List<CU1708> list = cU1708Service.findByMobile(mobile);
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
		case "173":

			List<CT173> listCT173 = cT173Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCT173)) {
				detail = listCT173.get(0);
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
		case "171":

			List<CU171> listCU171 = cU171Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCU171)) {
				detail = listCU171.get(0);
			}

			break;
		case "175":

			List<CU175> listCU175 = cU175Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listCU175)) {
				detail = listCU175.get(0);
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
	
	public BaseMobileDetail getByMobileFromNull(String mobile) {

		String mob = mobile.substring(0, 3);
		BaseMobileDetail detail = null;

		switch (mob) {
		case "170":
			// 分3个 1700 1705 1709
			String mobi = mobile.substring(0, 4);
			if (mobi.equals("1700")) {

				List<KCT1700> list = kCT1700Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1701")) {

				List<KCT1701> list = kCT1701Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1702")) {

				List<KCT1702> list = kCT1702Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1703")) {

				List<KCM1703> list = kCM1703Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1704")) {

				List<KCU1704> list = kCU1704Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
				
			} else if (mobi.equals("1705")) {

				List<KCM1705> list = kCM1705Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1706")) {

				List<KCM1706> list = kCM1706Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1707")) {

				List<KCU1707> list = kCU1707Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1708")) {

				List<KCU1708> list = kCU1708Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
					
			} else if (mobi.equals("1709")) {

				List<KCU1709> list = kCU1709Service.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}

			} else {

				List<KUnknownMobileDetail> list = kUnknownMobileDetailService.findByMobile(mobile);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			}
			break;
		case "134":

			List<KCM134> listKCM134 = kCM134Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM134)) {
				detail = listKCM134.get(0);
			}

			break;
		case "135":

			List<KCM135> listKCM135 = kCM135Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM135)) {
				detail = listKCM135.get(0);
			}

			break;
		case "136":

			List<KCM136> listKCM136 = kCM136Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM136)) {
				detail = listKCM136.get(0);
			}

			break;
		case "137":

			List<KCM137> listKCM137 = kCM137Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM137)) {
				detail = listKCM137.get(0);
			}

			break;
		case "138":

			List<KCM138> listKCM138 = kCM138Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM138)) {
				detail = listKCM138.get(0);
			}

			break;
		case "139":

			List<KCM139> listKCM139 = kCM139Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM139)) {
				detail = listKCM139.get(0);
			}

			break;
		case "147":

			List<KCM147> listKCM147 = kCM147Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM147)) {
				detail = listKCM147.get(0);
			}

			break;
		case "150":

			List<KCM150> listKCM150 = kCM150Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM150)) {
				detail = listKCM150.get(0);
			}

			break;
		case "151":

			List<KCM151> listKCM151 = kCM151Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM151)) {
				detail = listKCM151.get(0);
			}

			break;
		case "152":

			List<KCM152> listKCM152 = kCM152Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM152)) {
				detail = listKCM152.get(0);
			}

			break;
		case "157":

			List<KCM157> listKCM157 = kCM157Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM157)) {
				detail = listKCM157.get(0);
			}

			break;
		case "158":

			List<KCM158> listKCM158 = kCM158Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM158)) {
				detail = listKCM158.get(0);
			}

			break;
		case "159":

			List<KCM159> listKCM159 = kCM159Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM159)) {
				detail = listKCM159.get(0);
			}

			break;
		case "178":

			List<KCM178> listKCM178 = kCM178Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM178)) {
				detail = listKCM178.get(0);
			}

			break;
		case "182":

			List<KCM182> listKCM182 = kCM182Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM182)) {
				detail = listKCM182.get(0);
			}

			break;
		case "183":

			List<KCM183> listKCM183 = kCM183Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM183)) {
				detail = listKCM183.get(0);
			}

			break;
		case "184":

			List<KCM184> listKCM184 = kCM184Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM184)) {
				detail = listKCM184.get(0);
			}

			break;
		case "187":

			List<KCM187> listKCM187 = kCM187Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM187)) {
				detail = listKCM187.get(0);
			}

			break;
		case "188":

			List<KCM188> listKCM188 = kCM188Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCM188)) {
				detail = listKCM188.get(0);
			}

			break;
		case "133":

			List<KCT133> listKCT133 = kCT133Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCT133)) {
				detail = listKCT133.get(0);
			}

			break;
		case "153":

			List<KCT153> listKCT153 = kCT153Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCT153)) {
				detail = listKCT153.get(0);
			}

			break;
		case "173":

			List<KCT173> listKCT173 = kCT173Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCT173)) {
				detail = listKCT173.get(0);
			}

			break;
		case "177":

			List<KCT177> listKCT177 = kCT177Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCT177)) {
				detail = listKCT177.get(0);
			}

			break;
		case "180":

			List<KCT180> listKCT180 = kCT180Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCT180)) {
				detail = listKCT180.get(0);
			}

			break;
		case "181":

			List<KCT181> listKCT181 = kCT181Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCT181)) {
				detail = listKCT181.get(0);
			}

			break;
		case "189":

			List<KCT189> listKCT189 = kCT189Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCT189)) {
				detail = listKCT189.get(0);
			}

			break;
		case "130":

			List<KCU130> listKCU130 = kCU130Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCU130)) {
				detail = listKCU130.get(0);
			}

			break;
		case "131":

			List<KCU131> listKCU131 = kCU131Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCU131)) {
				detail = listKCU131.get(0);
			}

			break;
		case "132":

			List<KCU132> listKCU132 = kCU132Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCU132)) {
				detail = listKCU132.get(0);
			}

			break;
		case "145":

			List<KCU145> listKCU145 = kCU145Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCU145)) {
				detail = listKCU145.get(0);
			}

			break;
		case "155":

			List<KCU155> listKCU155 = kCU155Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCU155)) {
				detail = listKCU155.get(0);
			}

			break;
		case "156":

			List<KCU156> listKCU156 = kCU156Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCU156)) {
				detail = listKCU156.get(0);
			}

			break;
		case "171":

			List<KCU171> listKCU171 = kCU171Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCU171)) {
				detail = listKCU171.get(0);
			}

			break;
		case "175":

			List<KCU175> listKCU175 = kCU175Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCU175)) {
				detail = listKCU175.get(0);
			}

			break;
		case "176":

			List<KCU176> listKCU176 = kCU176Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCU176)) {
				detail = listKCU176.get(0);
			}

			break;
		case "185":

			List<KCU185> listKCU185 = kCU185Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCU185)) {
				detail = listKCU185.get(0);
			}

			break;
		case "186":

			List<KCU186> listKCU186 = kCU186Service.findByMobile(mobile);
			if (!CommonUtils.isNotEmpty(listKCU186)) {
				detail = listKCU186.get(0);
			}

			break;
		default:

			List<KUnknownMobileDetail> listUnknownMobileDetail = kUnknownMobileDetailService.findByMobile(mobile);
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
			} else if (mobi.equals("1701")) {

				List<CT1701> list = cT1701Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1702")) {

				List<CT1702> list = cT1702Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1703")) {

				List<CM1703> list = cM1703Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1704")) {

				List<CU1704> list = cU1704Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
				
			} else if (mobi.equals("1705")) {

				List<CM1705> list = cM1705Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1706")) {

				List<CM1706> list = cM1706Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1707")) {

				List<CU1707> list = cU1707Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1708")) {

				List<CU1708> list = cU1708Service.findByMobileAndReportTime(mobile, startTime, endTime);
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
		case "173":

			List<CT173> listCT173 = cT173Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCT173)) {
				detail = listCT173.get(0);
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
		case "171":

			List<CU171> listCU171 = cU171Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCU171)) {
				detail = listCU171.get(0);
			}

			break;
		case "175":

			List<CU175> listCU175 = cU175Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listCU175)) {
				detail = listCU175.get(0);
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

	@Override
	public BaseMobileDetail findByMobileAndReportTimeFromNull(String mobile, Date startTime, Date endTime) {
		String mob = mobile.substring(0, 3);
		BaseMobileDetail detail = null;

		switch (mob) {
		case "170":
			// 分3个 1700 1705 1709
			String mobi = mobile.substring(0, 4);
			if (mobi.equals("1700")) {

				List<KCT1700> list = kCT1700Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1701")) {

				List<KCT1701> list = kCT1701Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1702")) {

				List<KCT1702> list = kCT1702Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1703")) {

				List<KCM1703> list = kCM1703Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1704")) {

				List<KCU1704> list = kCU1704Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
				
			} else if (mobi.equals("1705")) {

				List<KCM1705> list = kCM1705Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1706")) {

				List<KCM1706> list = kCM1706Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1707")) {

				List<KCU1707> list = kCU1707Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			} else if (mobi.equals("1708")) {

				List<KCU1708> list = kCU1708Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
					
			} else if (mobi.equals("1709")) {

				List<KCU1709> list = kCU1709Service.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}

			} else {

				List<KUnknownMobileDetail> list = kUnknownMobileDetailService.findByMobileAndReportTime(mobile, startTime, endTime);
				if (!CommonUtils.isNotEmpty(list)) {
					detail = list.get(0);
				}
			}
			break;
		case "134":

			List<KCM134> listKCM134 = kCM134Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM134)) {
				detail = listKCM134.get(0);
			}

			break;
		case "135":

			List<KCM135> listKCM135 = kCM135Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM135)) {
				detail = listKCM135.get(0);
			}

			break;
		case "136":

			List<KCM136> listKCM136 = kCM136Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM136)) {
				detail = listKCM136.get(0);
			}

			break;
		case "137":

			List<KCM137> listKCM137 = kCM137Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM137)) {
				detail = listKCM137.get(0);
			}

			break;
		case "138":

			List<KCM138> listKCM138 = kCM138Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM138)) {
				detail = listKCM138.get(0);
			}

			break;
		case "139":

			List<KCM139> listKCM139 = kCM139Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM139)) {
				detail = listKCM139.get(0);
			}

			break;
		case "147":

			List<KCM147> listKCM147 = kCM147Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM147)) {
				detail = listKCM147.get(0);
			}

			break;
		case "150":

			List<KCM150> listKCM150 = kCM150Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM150)) {
				detail = listKCM150.get(0);
			}

			break;
		case "151":

			List<KCM151> listKCM151 = kCM151Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM151)) {
				detail = listKCM151.get(0);
			}

			break;
		case "152":

			List<KCM152> listKCM152 = kCM152Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM152)) {
				detail = listKCM152.get(0);
			}

			break;
		case "157":

			List<KCM157> listKCM157 = kCM157Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM157)) {
				detail = listKCM157.get(0);
			}

			break;
		case "158":

			List<KCM158> listKCM158 = kCM158Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM158)) {
				detail = listKCM158.get(0);
			}

			break;
		case "159":

			List<KCM159> listKCM159 = kCM159Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM159)) {
				detail = listKCM159.get(0);
			}

			break;
		case "178":

			List<KCM178> listKCM178 = kCM178Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM178)) {
				detail = listKCM178.get(0);
			}

			break;
		case "182":

			List<KCM182> listKCM182 = kCM182Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM182)) {
				detail = listKCM182.get(0);
			}

			break;
		case "183":

			List<KCM183> listKCM183 = kCM183Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM183)) {
				detail = listKCM183.get(0);
			}

			break;
		case "184":

			List<KCM184> listKCM184 = kCM184Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM184)) {
				detail = listKCM184.get(0);
			}

			break;
		case "187":

			List<KCM187> listKCM187 = kCM187Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM187)) {
				detail = listKCM187.get(0);
			}

			break;
		case "188":

			List<KCM188> listKCM188 = kCM188Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCM188)) {
				detail = listKCM188.get(0);
			}

			break;
		case "133":

			List<KCT133> listKCT133 = kCT133Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCT133)) {
				detail = listKCT133.get(0);
			}

			break;
		case "153":

			List<KCT153> listKCT153 = kCT153Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCT153)) {
				detail = listKCT153.get(0);
			}

			break;
		case "173":

			List<KCT173> listKCT173 = kCT173Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCT173)) {
				detail = listKCT173.get(0);
			}

			break;
		case "177":

			List<KCT177> listKCT177 = kCT177Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCT177)) {
				detail = listKCT177.get(0);
			}

			break;
		case "180":

			List<KCT180> listKCT180 = kCT180Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCT180)) {
				detail = listKCT180.get(0);
			}

			break;
		case "181":

			List<KCT181> listKCT181 = kCT181Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCT181)) {
				detail = listKCT181.get(0);
			}

			break;
		case "189":

			List<KCT189> listKCT189 = kCT189Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCT189)) {
				detail = listKCT189.get(0);
			}

			break;
		case "130":

			List<KCU130> listKCU130 = kCU130Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCU130)) {
				detail = listKCU130.get(0);
			}

			break;
		case "131":

			List<KCU131> listKCU131 = kCU131Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCU131)) {
				detail = listKCU131.get(0);
			}

			break;
		case "132":

			List<KCU132> listKCU132 = kCU132Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCU132)) {
				detail = listKCU132.get(0);
			}

			break;
		case "145":

			List<KCU145> listKCU145 = kCU145Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCU145)) {
				detail = listKCU145.get(0);
			}

			break;
		case "155":

			List<KCU155> listKCU155 = kCU155Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCU155)) {
				detail = listKCU155.get(0);
			}

			break;
		case "156":

			List<KCU156> listKCU156 = kCU156Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCU156)) {
				detail = listKCU156.get(0);
			}

			break;
		case "171":

			List<KCU171> listKCU171 = kCU171Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCU171)) {
				detail = listKCU171.get(0);
			}

			break;
		case "175":

			List<KCU175> listKCU175 = kCU175Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCU175)) {
				detail = listKCU175.get(0);
			}

			break;
		case "176":

			List<KCU176> listKCU176 = kCU176Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCU176)) {
				detail = listKCU176.get(0);
			}

			break;
		case "185":

			List<KCU185> listKCU185 = kCU185Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCU185)) {
				detail = listKCU185.get(0);
			}

			break;
		case "186":

			List<KCU186> listKCU186 = kCU186Service.findByMobileAndReportTime(mobile, startTime, endTime);
			if (!CommonUtils.isNotEmpty(listKCU186)) {
				detail = listKCU186.get(0);
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

	@Transactional
	public void deleteMobileFromNull(BaseMobileDetail mobileDetail,String mobile) {

		String mob = mobile.substring(0, 3);

		switch (mob) {
		case "170":
			// 分3个 1700 1705 1709
			String mobi = mobile.substring(0, 4);
			if (mobi.equals("1700")) {
				kCT1700Service.deleteByMobile(mobileDetail,mobile);				
			} else if (mobi.equals("1701")) {
				kCT1701Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1702")) {
				kCT1702Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1703")) {
				kCM1703Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1704")) {
				kCU1704Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1705")) {
				kCM1705Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1706")) {
				kCM1706Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1707")) {
				kCU1707Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1708")) {
				kCU1708Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1709")) {
				kCU1709Service.deleteByMobile(mobileDetail,mobile);
			} else {
				kUnknownMobileDetailService.deleteByMobile(mobileDetail,mobile);
			}
			break;
		case "134":
			kCM134Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "135":
			kCM135Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "136":
			kCM136Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "137":
			kCM137Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "138":
			kCM138Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "139":
			kCM139Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "147":
			kCM147Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "150":
			kCM150Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "151":
			kCM151Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "152":
			kCM152Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "157":
			kCM157Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "158":
			kCM158Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "159":
			kCM159Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "178":
			kCM178Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "182":
			kCM182Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "183":
			kCM183Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "184":
			kCM184Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "187":
			kCM187Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "188":
			kCM188Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "133":
			kCT133Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "153":
			kCT153Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "173":
			kCT173Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "177":
			kCT177Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "180":
			kCT180Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "181":
			kCT181Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "189":
			kCT189Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "130":
			kCU130Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "131":
			kCU131Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "132":
			kCU132Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "145":
			kCU145Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "155":
			kCU155Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "156":
			kCU156Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "171":
			kCU171Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "175":
			kCU175Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "176":
			kCU176Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "185":
			kCU185Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "186":
			kCU186Service.deleteByMobile(mobileDetail,mobile);
			break;
		default:
			kUnknownMobileDetailService.deleteByMobile(mobileDetail,mobile);
			break;
		}
	}
	
	@Transactional
	public void deleteMobile(BaseMobileDetail mobileDetail,String mobile) {

		String mob = mobile.substring(0, 3);

		switch (mob) {
		case "170":
			// 分3个 1700 1705 1709
			String mobi = mobile.substring(0, 4);
			if (mobi.equals("1700")) {
				cT1700Service.deleteByMobile(mobileDetail,mobile);				
			} else if (mobi.equals("1701")) {
				cT1701Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1702")) {
				cT1702Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1703")) {
				cM1703Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1704")) {
				cU1704Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1705")) {
				cM1705Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1706")) {
				cM1706Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1707")) {
				cU1707Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1708")) {
				cU1708Service.deleteByMobile(mobileDetail,mobile);
			} else if (mobi.equals("1709")) {
				cU1709Service.deleteByMobile(mobileDetail,mobile);
			} else {
				kUnknownMobileDetailService.deleteByMobile(mobileDetail,mobile);
			}
			break;
		case "134":
			cM134Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "135":
			cM135Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "136":
			cM136Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "137":
			cM137Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "138":
			cM138Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "139":
			cM139Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "147":
			cM147Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "150":
			cM150Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "151":
			cM151Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "152":
			cM152Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "157":
			cM157Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "158":
			cM158Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "159":
			cM159Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "178":
			cM178Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "182":
			cM182Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "183":
			cM183Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "184":
			cM184Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "187":
			cM187Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "188":
			cM188Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "133":
			cT133Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "153":
			cT153Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "173":
			cT173Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "177":
			cT177Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "180":
			cT180Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "181":
			cT181Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "189":
			cT189Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "130":
			cU130Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "131":
			cU131Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "132":
			cU132Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "145":
			cU145Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "155":
			cU155Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "156":
			cU156Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "171":
			cU171Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "175":
			cU175Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "176":
			cU176Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "185":
			cU185Service.deleteByMobile(mobileDetail,mobile);
			break;
		case "186":
			cU186Service.deleteByMobile(mobileDetail,mobile);
			break;
		default:
			kUnknownMobileDetailService.deleteByMobile(mobileDetail,mobile);
			break;
		}
	}

	@Override
	public void deleteByID(BaseMobileDetail mobileDetail,String mobile, Boolean status) {
		if(status){
			this.deleteMobile(mobileDetail, mobile);
		}else{
			this.deleteMobileFromNull( mobileDetail, mobile);
		}
		
	}

	@Override
	public BaseMobileDetail findByMobileToImport(String mobile, Boolean status) {
		if(status){
			return this.findByMobile(mobile);
		}else{
			return this.findByMobileFromNull(mobile);
		}
	}

}
