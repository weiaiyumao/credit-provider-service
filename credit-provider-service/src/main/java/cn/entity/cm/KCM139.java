package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.Mobile;

@Document(collection="KCM139")
public class KCM139 extends Mobile implements Serializable{

	private static final long serialVersionUID = 4768266340108927593L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}