package cn.entity.unknown;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.BaseMobileDetail;

@Document(collection="UnknownMobileDetail")
public class UnknownMobileDetail extends BaseMobileDetail implements Serializable{

	private static final long serialVersionUID = 119828393867864778L;
	@Id
	private String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
