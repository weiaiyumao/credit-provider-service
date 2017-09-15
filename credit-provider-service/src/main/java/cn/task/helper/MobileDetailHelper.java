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
import cn.utils.UUIDTool;

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

		String id = UUIDTool.getInstance().getUUID();
		
		BaseMobileDetail detail = null;

		String mob = mobile.substring(0, 3);

		switch (mob) {
		case "170":
			//  分3个 1700 1705 1709
			String mobi = mobile.substring(0, 4);
			if (mobi.equals("1700")) {
				detail = new CT1700(id);
			} else  if (mobi.equals("1705")) {
				detail = new CM1705(id);
			} else if (mobi.equals("1709")) {
				detail = new CU1709(id);
			} else {
				detail = new UnknownMobileDetail(id);
			}
			break;
		case "134":
			detail = new CM134(id);
			break;
		case "135":
			detail = new CM135(id);
			break;
		case "136":
			detail = new CM136(id);
			break;
		case "137":
			detail = new CM137(id);
			break;
		case "138":
			detail = new CM138(id);
			break;
		case "139":
			detail = new CM139(id);
			break;
		case "147":
			detail = new CM147(id);
			break;
		case "150":
			detail = new CM150(id);
			break;
		case "151":
			detail = new CM151(id);
			break;
		case "152":
			detail = new CM152(id);
			break;
		case "157":
			detail = new CM157(id);
			break;
		case "158":
			detail = new CM158(id);
			break;
		case "159":
			detail = new CM159(id);
			break;
		case "178":
			detail = new CM178(id);
			break;
		case "182":
			detail = new CM182(id);
			break;
		case "183":
			detail = new CM183(id);
			break;
		case "184":
			detail = new CM184(id);
			break;
		case "187":
			detail = new CM187(id);
			break;
		case "188":
			detail = new CM188(id);
			break;
		case "133":
			detail = new CT133(id);
			break;
		case "153":
			detail = new CT153(id);
			break;
		case "177":
			detail = new CT177(id);
			break;
		case "180":
			detail = new CT180(id);
			break;
		case "181":
			detail = new CT181(id);
			break;
		case "189":
			detail = new CT189(id);
			break;
		case "130":
			detail = new CU130(id);
			break;
		case "131":
			detail = new CU131(id);
			break;
		case "132":
			detail = new CU132(id);
			break;
		case "145":
			detail = new CU145(id);
			break;
		case "155":
			detail = new CU155(id);
			break;
		case "156":
			detail = new CU156(id);
			break;
		case "176":
			detail = new CU176(id);
			break;
		case "185":
			detail = new CU185(id);
			break;
		case "186":
			detail = new CU186(id);
			break;
		default:
			detail = new UnknownMobileDetail(id);
			break;
		}
		
		return detail;
	}

}
