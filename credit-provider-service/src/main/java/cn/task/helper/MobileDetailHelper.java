package cn.task.helper;

import org.springframework.util.StringUtils;

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

public class MobileDetailHelper {

	private static MobileDetailHelper mobileDetailHelper;

	public static MobileDetailHelper getInstance() {
		if (mobileDetailHelper == null) {
			synchronized (MobileDetailHelper.class) {
				if (mobileDetailHelper == null) {
					mobileDetailHelper = new MobileDetailHelper();
				}
			}
		}
		return mobileDetailHelper;
	}
	
	/**
	 * 根据手机号码段 生成对应的对象
	 * @param mobile
	 * @return
	 */
	public BaseMobileDetail getBaseMobileDetail(String mobile) {

		if (StringUtils.isEmpty(mobile)) {
			return null;
		}
		
		BaseMobileDetail detail = null;

		String mob = mobile.substring(0, 3);

		switch (mob) {
		case "170":
			//  分3个 1700 1705 1709
			String mobi = mobile.substring(0, 4);
			if (mobi.equals("1700")) {
				detail = new CT1700();
			} else  if (mobi.equals("1701")) {
				detail = new CT1701();
			} else  if (mobi.equals("1702")) {
				detail = new CT1702();
			} else  if (mobi.equals("1703")) {
				detail = new CM1703();
			} else  if (mobi.equals("1704")) {
				detail = new CU1704();
			} else  if (mobi.equals("1705")) {
				detail = new CM1705();
			} else  if (mobi.equals("1706")) {
				detail = new CM1706();
			} else  if (mobi.equals("1707")) {
				detail = new CU1707();
			} else  if (mobi.equals("1708")) {
				detail = new CU1708();
			} else  if (mobi.equals("1709")) {
				detail = new CU1709();
			} else {
				detail = new UnknownMobileDetail();
			}
			break;
		case "134":
			detail = new CM134();
			break;
		case "135":
			detail = new CM135();
			break;
		case "136":
			detail = new CM136();
			break;
		case "137":
			detail = new CM137();
			break;
		case "138":
			detail = new CM138();
			break;
		case "139":
			detail = new CM139();
			break;
		case "147":
			detail = new CM147();
			break;
		case "150":
			detail = new CM150();
			break;
		case "151":
			detail = new CM151();
			break;
		case "152":
			detail = new CM152();
			break;
		case "157":
			detail = new CM157();
			break;
		case "158":
			detail = new CM158();
			break;
		case "159":
			detail = new CM159();
			break;
		case "178":
			detail = new CM178();
			break;
		case "182":
			detail = new CM182();
			break;
		case "183":
			detail = new CM183();
			break;
		case "184":
			detail = new CM184();
			break;
		case "187":
			detail = new CM187();
			break;
		case "188":
			detail = new CM188();
			break;
		case "133":
			detail = new CT133();
			break;
		case "153":
			detail = new CT153();
			break;
		case "173":
			detail = new CT173();
			break;
		case "177":
			detail = new CT177();
			break;
		case "180":
			detail = new CT180();
			break;
		case "181":
			detail = new CT181();
			break;
		case "189":
			detail = new CT189();
			break;
		case "130":
			detail = new CU130();
			break;
		case "131":
			detail = new CU131();
			break;
		case "132":
			detail = new CU132();
			break;
		case "145":
			detail = new CU145();
			break;
		case "155":
			detail = new CU155();
			break;
		case "156":
			detail = new CU156();
			break;
		case "171":
			detail = new CU171();
			break;
		case "175":
			detail = new CU175();
			break;
		case "176":
			detail = new CU176();
			break;
		case "185":
			detail = new CU185();
			break;
		case "186":
			detail = new CU186();
			break;
		default:
			detail = new UnknownMobileDetail();
			break;
		}
			
			return detail;
	
	}

	/**
	 * 根据手机号码段 生成对应的对象
	 * @param mobile
	 * @return
	 */
	public BaseMobileDetail getBaseMobileDetail(String mobile,Boolean status) {

		if (StringUtils.isEmpty(mobile)) {
			return null;
		}

//		String id = UUIDTool.getInstance().getUUID();
		
		BaseMobileDetail detail = null;

		String mob = mobile.substring(0, 3);

		if (status == Boolean.TRUE){//实号库
			switch (mob) {
			case "170":
				//  分3个 1700 1705 1709
				String mobi = mobile.substring(0, 4);
				if (mobi.equals("1700")) {
					detail = new CT1700();
				} else  if (mobi.equals("1701")) {
					detail = new CT1701();
				} else  if (mobi.equals("1702")) {
					detail = new CT1702();
				} else  if (mobi.equals("1703")) {
					detail = new CM1703();
				} else  if (mobi.equals("1704")) {
					detail = new CU1704();
				} else  if (mobi.equals("1705")) {
					detail = new CM1705();
				} else  if (mobi.equals("1706")) {
					detail = new CM1706();
				} else  if (mobi.equals("1707")) {
					detail = new CU1707();
				} else  if (mobi.equals("1708")) {
					detail = new CU1708();
				} else  if (mobi.equals("1709")) {
					detail = new CU1709();
				} else {
					detail = new UnknownMobileDetail();
				}
				break;
			case "134":
				detail = new CM134();
				break;
			case "135":
				detail = new CM135();
				break;
			case "136":
				detail = new CM136();
				break;
			case "137":
				detail = new CM137();
				break;
			case "138":
				detail = new CM138();
				break;
			case "139":
				detail = new CM139();
				break;
			case "147":
				detail = new CM147();
				break;
			case "150":
				detail = new CM150();
				break;
			case "151":
				detail = new CM151();
				break;
			case "152":
				detail = new CM152();
				break;
			case "157":
				detail = new CM157();
				break;
			case "158":
				detail = new CM158();
				break;
			case "159":
				detail = new CM159();
				break;
			case "178":
				detail = new CM178();
				break;
			case "182":
				detail = new CM182();
				break;
			case "183":
				detail = new CM183();
				break;
			case "184":
				detail = new CM184();
				break;
			case "187":
				detail = new CM187();
				break;
			case "188":
				detail = new CM188();
				break;
			case "133":
				detail = new CT133();
				break;
			case "153":
				detail = new CT153();
				break;
			case "173":
				detail = new CT173();
				break;
			case "177":
				detail = new CT177();
				break;
			case "180":
				detail = new CT180();
				break;
			case "181":
				detail = new CT181();
				break;
			case "189":
				detail = new CT189();
				break;
			case "130":
				detail = new CU130();
				break;
			case "131":
				detail = new CU131();
				break;
			case "132":
				detail = new CU132();
				break;
			case "145":
				detail = new CU145();
				break;
			case "155":
				detail = new CU155();
				break;
			case "156":
				detail = new CU156();
				break;
			case "171":
				detail = new CU171();
				break;
			case "175":
				detail = new CU175();
				break;
			case "176":
				detail = new CU176();
				break;
			case "185":
				detail = new CU185();
				break;
			case "186":
				detail = new CU186();
				break;
			default:
				detail = new UnknownMobileDetail();
				break;
			}
			
			return detail;
	}else{//空号库
		switch (mob) {
		case "170":
			//  分3个 1700 1705 1709
			String mobi = mobile.substring(0, 4);
			if (mobi.equals("1700")) {
				detail = new KCT1700();
			} else  if (mobi.equals("1701")) {
				detail = new KCT1701();
			} else  if (mobi.equals("1702")) {
				detail = new KCT1702();
			} else  if (mobi.equals("1703")) {
				detail = new KCM1703();
			} else  if (mobi.equals("1704")) {
				detail = new KCU1704();
			} else  if (mobi.equals("1705")) {
				detail = new KCM1705();
			} else  if (mobi.equals("1706")) {
				detail = new KCM1706();
			} else  if (mobi.equals("1707")) {
				detail = new KCU1707();
			} else  if (mobi.equals("1708")) {
				detail = new KCU1708();
			} else  if (mobi.equals("1709")) {
				detail = new KCU1709();
			} else {
				detail = new KUnknownMobileDetail();
			}
			break;
		case "134":
			detail = new KCM134();
			break;
		case "135":
			detail = new KCM135();
			break;
		case "136":
			detail = new KCM136();
			break;
		case "137":
			detail = new KCM137();
			break;
		case "138":
			detail = new KCM138();
			break;
		case "139":
			detail = new KCM139();
			break;
		case "147":
			detail = new KCM147();
			break;
		case "150":
			detail = new KCM150();
			break;
		case "151":
			detail = new KCM151();
			break;
		case "152":
			detail = new KCM152();
			break;
		case "157":
			detail = new KCM157();
			break;
		case "158":
			detail = new KCM158();
			break;
		case "159":
			detail = new KCM159();
			break;
		case "178":
			detail = new KCM178();
			break;
		case "182":
			detail = new KCM182();
			break;
		case "183":
			detail = new KCM183();
			break;
		case "184":
			detail = new KCM184();
			break;
		case "187":
			detail = new KCM187();
			break;
		case "188":
			detail = new KCM188();
			break;
		case "133":
			detail = new KCT133();
			break;
		case "153":
			detail = new KCT153();
			break;
		case "173":
			detail = new KCT173();
			break;
		case "177":
			detail = new KCT177();
			break;
		case "180":
			detail = new KCT180();
			break;
		case "181":
			detail = new KCT181();
			break;
		case "189":
			detail = new KCT189();
			break;
		case "130":
			detail = new KCU130();
			break;
		case "131":
			detail = new KCU131();
			break;
		case "132":
			detail = new KCU132();
			break;
		case "145":
			detail = new KCU145();
			break;
		case "155":
			detail = new KCU155();
			break;
		case "156":
			detail = new KCU156();
			break;
		case "171":
			detail = new KCU171();
			break;
		case "175":
			detail = new KCU175();
			break;
		case "176":
			detail = new KCU176();
			break;
		case "185":
			detail = new KCU185();
			break;
		case "186":
			detail = new KCU186();
			break;
		default:
			detail = new KUnknownMobileDetail();
			break;
		}
		
		return detail;
	}
	}
	
}


